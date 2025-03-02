package io.mckenz.weathervoting.managers;

import io.mckenz.weathervoting.WeatherVoting;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VoteManager {
    
    private final WeatherVoting plugin;
    private final Map<String, Set<UUID>> sunnyVotes;
    private final Map<String, Set<UUID>> rainVotes;
    private final Map<String, Set<UUID>> thunderVotes;
    
    public VoteManager(WeatherVoting plugin) {
        this.plugin = plugin;
        this.sunnyVotes = new HashMap<>();
        this.rainVotes = new HashMap<>();
        this.thunderVotes = new HashMap<>();
    }
    
    public boolean vote(Player player, String weatherType, String worldName) {
        // Check if plugin is enabled
        if (!plugin.isPluginEnabled()) {
            String message = getMessage("plugin-disabled", "&7The WeatherVoting plugin is currently disabled.");
            player.sendMessage(colorize(getPrefix() + message));
            return false;
        }
        
        // Validate weather type first
        String voteType;
        switch (weatherType.toLowerCase()) {
            case "sunny":
            case "clear":
                voteType = "sunny";
                break;
            case "rain":
            case "rainy":
                voteType = "rain";
                break;
            case "thunder":
            case "storm":
                voteType = "thunder";
                break;
            default:
                String message = getMessage("invalid-weather", "&7Invalid weather type. Use sunny, rain, or thunder.");
                player.sendMessage(colorize(getPrefix() + message));
                return false;
        }

        // Check player cooldown
        CooldownManager cooldownManager = plugin.getCooldownManager();
        if (cooldownManager.isPlayerOnCooldown(player)) {
            int remainingTime = cooldownManager.getRemainingCooldown(player);
            String message = getMessage("vote-cooldown", "&7You must wait &f%seconds% &7seconds before voting again.")
                    .replace("%seconds%", String.valueOf(remainingTime));
            player.sendMessage(colorize(getPrefix() + message));
            return false;
        }
        
        // Check world cooldown
        if (cooldownManager.isWorldOnCooldown(worldName)) {
            int remainingTime = cooldownManager.getWorldRemainingCooldown(worldName);
            String message = getMessage("change-cooldown", "&7Weather was recently changed. Please wait &f%seconds% &7seconds.")
                    .replace("%seconds%", String.valueOf(remainingTime));
            player.sendMessage(colorize(getPrefix() + message));
            return false;
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        String worldKey = world.getName();
        
        // Initialize vote sets for this world if they don't exist
        sunnyVotes.putIfAbsent(worldKey, new HashSet<>());
        rainVotes.putIfAbsent(worldKey, new HashSet<>());
        thunderVotes.putIfAbsent(worldKey, new HashSet<>());
        
        // Remove previous votes by this player
        sunnyVotes.get(worldKey).remove(playerUUID);
        rainVotes.get(worldKey).remove(playerUUID);
        thunderVotes.get(worldKey).remove(playerUUID);
        
        // Add new vote
        switch (voteType) {
            case "sunny":
                sunnyVotes.get(worldKey).add(playerUUID);
                break;
            case "rain":
                rainVotes.get(worldKey).add(playerUUID);
                break;
            case "thunder":
                thunderVotes.get(worldKey).add(playerUUID);
                break;
        }
        
        // Set player cooldown
        cooldownManager.setPlayerCooldown(player);
        
        // Broadcast vote if enabled
        if (plugin.getConfig().getBoolean("announcements.broadcast-votes", true)) {
            broadcastVote(player, voteType, worldKey);
        }
        
        // Check if threshold is reached
        checkVotes(world);
        
        return true;
    }
    
    private void broadcastVote(Player player, String weatherType, String worldKey) {
        String weatherName = getWeatherName(weatherType);
        int votes = 0;
        
        switch (weatherType) {
            case "sunny":
                votes = sunnyVotes.getOrDefault(worldKey, new HashSet<>()).size();
                break;
            case "rain":
                votes = rainVotes.getOrDefault(worldKey, new HashSet<>()).size();
                break;
            case "thunder":
                votes = thunderVotes.getOrDefault(worldKey, new HashSet<>()).size();
                break;
        }
        
        int requiredVotes = calculateRequiredVotes(0);
        
        String message = getMessage("vote-announcement", "&e%player% &7voted for &e%weather%&7. (&f%votes%&7/&f%required%&7)")
                .replace("%player%", player.getName())
                .replace("%weather%", weatherName)
                .replace("%votes%", String.valueOf(votes))
                .replace("%required%", String.valueOf(requiredVotes));
        
        Bukkit.broadcastMessage(colorize(getPrefix() + message));
    }
    
    private void checkVotes(World world) {
        String worldKey = world.getName();
        int requiredVotes = calculateRequiredVotes(0);
        
        if (sunnyVotes.getOrDefault(worldKey, new HashSet<>()).size() >= requiredVotes) {
            // Set weather to sunny
            world.setStorm(false);
            world.setThundering(false);
            
            // Set weather duration
            int duration = plugin.getConfig().getInt("weather-duration.sunny", 6000);
            world.setWeatherDuration(duration);
            
            broadcastWeatherChange("sunny");
            resetVotes(worldKey);
            
            // Set last weather change time
            plugin.setLastWeatherChange(worldKey);
        } else if (rainVotes.getOrDefault(worldKey, new HashSet<>()).size() >= requiredVotes) {
            // Set weather to rainy
            world.setStorm(true);
            world.setThundering(false);
            
            // Set weather duration
            int duration = plugin.getConfig().getInt("weather-duration.rain", 3000);
            world.setWeatherDuration(duration);
            
            broadcastWeatherChange("rain");
            resetVotes(worldKey);
            
            // Set last weather change time
            plugin.setLastWeatherChange(worldKey);
        } else if (thunderVotes.getOrDefault(worldKey, new HashSet<>()).size() >= requiredVotes) {
            // Set weather to thunder
            world.setStorm(true);
            world.setThundering(true);
            
            // Set weather duration
            int duration = plugin.getConfig().getInt("weather-duration.thunder", 2000);
            world.setWeatherDuration(duration);
            world.setThunderDuration(duration);
            
            broadcastWeatherChange("thunder");
            resetVotes(worldKey);
            
            // Set last weather change time
            plugin.setLastWeatherChange(worldKey);
        }
    }
    
    private void broadcastWeatherChange(String weatherType) {
        String weatherName = getWeatherName(weatherType);
        String message = getMessage("weather-changed", "&7The weather has been changed to &e%weather%&7!")
                .replace("%weather%", weatherName);
        Bukkit.broadcastMessage(colorize(getPrefix() + message));
    }
    
    private int calculateRequiredVotes(int onlinePlayers) {
        // Get all players, including those from GeyserMC/Floodgate
        int totalPlayers = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                totalPlayers++;
            }
        }
        
        double percentage = plugin.getConfig().getDouble("voting.threshold-percentage", 50.0);
        return Math.max(1, (int) Math.ceil(totalPlayers * (percentage / 100.0)));
    }
    
    private void resetVotes(String worldKey) {
        sunnyVotes.getOrDefault(worldKey, new HashSet<>()).clear();
        rainVotes.getOrDefault(worldKey, new HashSet<>()).clear();
        thunderVotes.getOrDefault(worldKey, new HashSet<>()).clear();
    }
    
    public int getSunnyVotes(String worldName) {
        return sunnyVotes.getOrDefault(worldName, new HashSet<>()).size();
    }
    
    public int getRainVotes(String worldName) {
        return rainVotes.getOrDefault(worldName, new HashSet<>()).size();
    }
    
    public int getThunderVotes(String worldName) {
        return thunderVotes.getOrDefault(worldName, new HashSet<>()).size();
    }
    
    public int getRequiredVotes() {
        return calculateRequiredVotes(0);
    }
    
    public String getPrefix() {
        return plugin.getConfig().getString("messages.prefix", "&6[WeatherVoting] &r");
    }
    
    public String getMessage(String path, String defaultValue) {
        return plugin.getConfig().getString("messages." + path, defaultValue);
    }
    
    public String getWeatherName(String weatherType) {
        return plugin.getConfig().getString("messages.weather-types." + weatherType, weatherType);
    }
    
    /**
     * Checks if a player has voted for a specific weather type
     * @param playerUUID The player's UUID
     * @param weatherType The weather type to check
     * @param worldName The world name
     * @return true if the player has voted for this weather type
     */
    public boolean hasVotedFor(UUID playerUUID, String weatherType, String worldName) {
        switch (weatherType.toLowerCase()) {
            case "sunny":
                return sunnyVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID);
            case "rain":
                return rainVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID);
            case "thunder":
                return thunderVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID);
            default:
                return false;
        }
    }
    
    /**
     * Gets the weather type a player has voted for
     * @param playerUUID The player's UUID
     * @param worldName The world name
     * @return The weather type the player voted for, or null if they haven't voted
     */
    public String getPlayerVote(UUID playerUUID, String worldName) {
        if (sunnyVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID)) {
            return "sunny";
        } else if (rainVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID)) {
            return "rain";
        } else if (thunderVotes.getOrDefault(worldName, new HashSet<>()).contains(playerUUID)) {
            return "thunder";
        }
        return null;
    }
    
    public static String colorize(String message) {
        return message.replace("&", "§");
    }
    
    public String getCurrentWeather(World world) {
        if (world.isThundering()) {
            return getWeatherName("thunder");
        } else if (world.hasStorm()) {
            return getWeatherName("rain");
        } else {
            return getWeatherName("sunny");
        }
    }
    
    public String getTimeUntilWeatherChange(World world) {
        int ticksLeft = world.getWeatherDuration();
        int seconds = ticksLeft / 20;
        
        if (seconds > 3600) {
            return (seconds / 3600) + " hours, " + ((seconds % 3600) / 60) + " minutes";
        } else if (seconds > 60) {
            return (seconds / 60) + " minutes, " + (seconds % 60) + " seconds";
        } else {
            return seconds + " seconds";
        }
    }
} 