package io.mckenz.weathervoting.utils;

import io.mckenz.weathervoting.WeatherVoting;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.net.URISyntaxException;

/**
 * Utility class for checking for plugin updates.
 */
public class UpdateChecker implements Listener {

    private final WeatherVoting plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateAvailable = false;
    private final boolean notifyAdmins;

    /**
     * Create a new update checker
     * @param plugin The plugin instance
     * @param resourceId The SpigotMC resource ID
     * @param notifyAdmins Whether to notify admins when they join
     */
    public UpdateChecker(WeatherVoting plugin, int resourceId, boolean notifyAdmins) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.notifyAdmins = notifyAdmins;
        
        // Register the join event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Check for updates
     */
    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String currentVersion = plugin.getDescription().getVersion();
                latestVersion = fetchLatestVersion();
                
                if (latestVersion == null) {
                    plugin.getLogger().warning("Failed to check for updates.");
                    return;
                }
                
                // Compare versions (simple string comparison, could be improved)
                if (!currentVersion.equals(latestVersion)) {
                    updateAvailable = true;
                    plugin.getLogger().info("A new update is available: " + latestVersion + " (Current: " + currentVersion + ")");
                    plugin.getLogger().info("Download it at: https://www.spigotmc.org/resources/" + resourceId);
                } else {
                    plugin.getLogger().info("You are running the latest version: " + currentVersion);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Fetch the latest version from SpigotMC API
     * @return The latest version string or null if the check failed
     */
    private String fetchLatestVersion() {
        try {
            URI uri = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    return reader.readLine();
                }
            } else {
                plugin.getLogger().warning("Failed to check for updates: HTTP response code " + responseCode);
            }
        } catch (URISyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create URI for update check", e);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
        }
        return null;
    }

    /**
     * Check if an update is available
     * @return True if an update is available, false otherwise
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Get the latest version
     * @return The latest version string
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Notify admins when they join if an update is available
     * @param event The player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Only notify players with permission if notifications are enabled
        if (updateAvailable && notifyAdmins && player.hasPermission("weathervoting.update")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                String prefix = plugin.getVoteManager().getPrefix();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    prefix + "&eA new update is available: &f" + latestVersion + 
                    " &e(Current: &f" + plugin.getDescription().getVersion() + "&e)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    prefix + "&eDownload it at: &fhttps://www.spigotmc.org/resources/" + resourceId));
            }, 40L); // Delay for 2 seconds after join
        }
    }
} 