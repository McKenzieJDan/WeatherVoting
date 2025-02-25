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
        // Check player cooldown
        CooldownManager cooldownManager = plugin.getCooldownManager();
        if (cooldownManager.isPlayerOnCooldown(player)) {
            int remainingTime = cooldownManager.getRemainingCooldown(player);
            String message = getMessage("cooldown-vote", "&cYou must wait {time} seconds before voting again!")
                    .replace("{time}", String.valueOf(remainingTime));
            player.sendMessage(colorize(getPrefix() + message));
            return false;
        }
        
        // Check world cooldown
        if (cooldownManager.isWorldOnCooldown(worldName)) {
            int remainingTime = cooldownManager.getWorldRemainingCooldown(worldName);
            String message = getMessage("cooldown-change", "&cWeather was recently changed. Wait {time} seconds before another change.")
                    .replace("{time}", String.valueOf(remainingTime));
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
        
        String voteType;
        
        // Add new vote
        switch (weatherType.toLowerCase()) {
            case "sunny":
            case "clear":
                sunnyVotes.get(worldKey).add(playerUUID);
                voteType = "sunny";
                break;
            case "rain":
            case "rainy":
                rainVotes.get(worldKey).add(playerUUID);
                voteType = "rain";
                break;
            case "thunder":
            case "storm":
                thunderVotes.get(worldKey).add(playerUUID);
                voteType = "thunder";
                break;
            default:
                return false;
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
        
        int requiredVotes = calculateRequiredVotes(Bukkit.getOnlinePlayers().size());
        
        String message = getMessage("vote-broadcast", "&e{player} &7has voted for &e{weather} &7weather! &7(&e{votes}/{required}&7)")
                .replace("{player}", player.getName())
                .replace("{weather}", weatherName)
                .replace("{votes}", String.valueOf(votes))
                .replace("{required}", String.valueOf(requiredVotes));
        
        Bukkit.broadcastMessage(colorize(getPrefix() + message));
    }
    
    private void checkVotes(World world) {
        String worldKey = world.getName();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = calculateRequiredVotes(onlinePlayers);
        
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
        String message = getMessage("weather-changed", "&aThe weather has been changed to {weather} due to voting!")
                .replace("{weather}", weatherName);
        Bukkit.broadcastMessage(colorize(getPrefix() + message));
    }
    
    private int calculateRequiredVotes(int onlinePlayers) {
        double percentage = plugin.getConfig().getDouble("voting.threshold-percentage", 50.0);
        return Math.max(1, (int) Math.ceil(onlinePlayers * (percentage / 100.0)));
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
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        return calculateRequiredVotes(onlinePlayers);
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