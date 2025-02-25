package io.mckenz.weathervoting.managers;

import io.mckenz.weathervoting.WeatherVoting;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final WeatherVoting plugin;
    private final Map<UUID, Long> playerCooldowns;
    
    public CooldownManager(WeatherVoting plugin) {
        this.plugin = plugin;
        this.playerCooldowns = new HashMap<>();
    }
    
    public boolean isPlayerOnCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!playerCooldowns.containsKey(playerUUID)) {
            return false;
        }
        
        long lastVote = playerCooldowns.get(playerUUID);
        long cooldownTime = plugin.getConfig().getLong("cooldowns.between-votes", 60) * 1000;
        
        return System.currentTimeMillis() - lastVote < cooldownTime;
    }
    
    public int getRemainingCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!playerCooldowns.containsKey(playerUUID)) {
            return 0;
        }
        
        long lastVote = playerCooldowns.get(playerUUID);
        long cooldownTime = plugin.getConfig().getLong("cooldowns.between-votes", 60) * 1000;
        long timeElapsed = System.currentTimeMillis() - lastVote;
        
        return Math.max(0, (int) ((cooldownTime - timeElapsed) / 1000));
    }
    
    public boolean isWorldOnCooldown(String worldName) {
        long lastChange = plugin.getLastWeatherChange(worldName);
        long cooldownTime = plugin.getConfig().getLong("cooldowns.between-changes", 300) * 1000;
        
        return System.currentTimeMillis() - lastChange < cooldownTime;
    }
    
    public int getWorldRemainingCooldown(String worldName) {
        long lastChange = plugin.getLastWeatherChange(worldName);
        long cooldownTime = plugin.getConfig().getLong("cooldowns.between-changes", 300) * 1000;
        long timeElapsed = System.currentTimeMillis() - lastChange;
        
        return Math.max(0, (int) ((cooldownTime - timeElapsed) / 1000));
    }
    
    public void setPlayerCooldown(Player player) {
        playerCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
} 