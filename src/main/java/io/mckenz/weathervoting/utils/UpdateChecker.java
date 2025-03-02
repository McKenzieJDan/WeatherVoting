package io.mckenz.weathervoting.utils;

import io.mckenz.weathervoting.WeatherVoting;
import io.mckenz.weathervoting.managers.VoteManager;
import org.bukkit.Bukkit;
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
    private final boolean notifyAdmins;
    private boolean updateAvailable = false;
    private String latestVersion = null;

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
        if (this.notifyAdmins) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
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
                
                // Debug log the raw versions
                plugin.debug("Raw current version: " + currentVersion);
                plugin.debug("Raw latest version: " + latestVersion);
                
                // Normalize versions for comparison
                String normalizedCurrent = normalizeVersion(currentVersion);
                String normalizedLatest = normalizeVersion(latestVersion);
                
                // Debug log the normalized versions
                plugin.debug("Normalized current version: " + normalizedCurrent);
                plugin.debug("Normalized latest version: " + normalizedLatest);
                
                // Compare versions using semantic versioning
                boolean isNewer = isNewerVersion(normalizedLatest, normalizedCurrent);
                
                if (isNewer) {
                    updateAvailable = true;
                    plugin.getLogger().info("A new update is available: v" + latestVersion);
                    plugin.getLogger().info("You are currently running: v" + currentVersion);
                    plugin.getLogger().info("Download the latest version from: https://www.spigotmc.org/resources/" + resourceId);
                } else {
                    plugin.getLogger().info("You are running the latest version: v" + currentVersion);
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
    private String fetchLatestVersion() throws IOException {
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
        }
        return null;
    }
    
    /**
     * Compare two version strings for equality
     * 
     * @param version1 The first version string
     * @param version2 The second version string
     * @return True if the versions are equal, false otherwise
     */
    private boolean versionsEqual(String version1, String version2) {
        // Simple string comparison after normalization
        return version1.equals(version2);
    }
    
    /**
     * Check if version1 is newer than version2
     * 
     * @param version1 The version to check
     * @param version2 The version to compare against
     * @return True if version1 is newer than version2
     */
    private boolean isNewerVersion(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");
        
        // Compare each part of the version
        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            
            if (v1Part > v2Part) {
                return true;
            } else if (v1Part < v2Part) {
                return false;
            }
        }
        
        // Versions are equal
        return false;
    }
    
    /**
     * Normalize a version string for comparison
     * @param version The version string to normalize
     * @return The normalized version string
     */
    private String normalizeVersion(String version) {
        // Remove all 'v' prefixes (handles cases like 'vv1.1.0')
        while (version.startsWith("v")) {
            version = version.substring(1);
        }
        
        // Remove any suffixes like -RELEASE, -SNAPSHOT, etc.
        int dashIndex = version.indexOf('-');
        if (dashIndex > 0) {
            version = version.substring(0, dashIndex);
        }
        
        // Trim any whitespace
        version = version.trim();
        
        // Ensure consistent format for comparison
        // For example, convert "1.1" to "1.1.0" if needed
        String[] parts = version.split("\\.");
        if (parts.length == 2) {
            version = version + ".0";
        }
        
        return version;
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
        
        // Only notify players with permission
        if (updateAvailable && player.hasPermission("weathervoting.update")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Get the prefix from VoteManager
                String prefix = plugin.getVoteManager().getPrefix();
                
                // Send update notification messages
                player.sendMessage(VoteManager.colorize(prefix + "&7A new update is available: &bv" + latestVersion));
                player.sendMessage(VoteManager.colorize(prefix + "&7You are currently running: &bv" + plugin.getDescription().getVersion()));
                player.sendMessage(VoteManager.colorize(prefix + "&7Download the latest version from: &bhttps://www.spigotmc.org/resources/" + resourceId));
            }, 40L); // Delay for 2 seconds after join
        }
    }
} 