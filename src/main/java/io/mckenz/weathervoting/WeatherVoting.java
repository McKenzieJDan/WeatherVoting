package io.mckenz.weathervoting;

import io.mckenz.weathervoting.commands.ForecastCommand;
import io.mckenz.weathervoting.commands.VoteWeatherCommand;
import io.mckenz.weathervoting.managers.CooldownManager;
import io.mckenz.weathervoting.managers.VoteManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeatherVoting extends JavaPlugin {
    
    private VoteManager voteManager;
    private CooldownManager cooldownManager;
    private Map<String, Long> lastWeatherChange;
    
    @Override
    public void onEnable() {
        // Create config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize managers
        cooldownManager = new CooldownManager(this);
        voteManager = new VoteManager(this);
        lastWeatherChange = new HashMap<>();
        
        // Register commands
        getCommand("voteweather").setExecutor(new VoteWeatherCommand(this));
        
        // Register forecast command if enabled
        if (getConfig().getBoolean("forecast.enabled", true)) {
            getCommand("forecast").setExecutor(new ForecastCommand(this));
        }
        
        getLogger().info("WeatherVoting has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("WeatherVoting has been disabled!");
    }
    
    public VoteManager getVoteManager() {
        return voteManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public void setLastWeatherChange(String world) {
        lastWeatherChange.put(world, System.currentTimeMillis());
    }
    
    public long getLastWeatherChange(String world) {
        return lastWeatherChange.getOrDefault(world, 0L);
    }
} 