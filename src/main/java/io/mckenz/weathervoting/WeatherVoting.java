package io.mckenz.weathervoting;

import io.mckenz.weathervoting.commands.ForecastCommand;
import io.mckenz.weathervoting.commands.VoteWeatherCommand;
import io.mckenz.weathervoting.managers.CooldownManager;
import io.mckenz.weathervoting.managers.VoteManager;
import io.mckenz.weathervoting.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeatherVoting extends JavaPlugin {
    
    private VoteManager voteManager;
    private CooldownManager cooldownManager;
    private Map<String, Long> lastWeatherChange;
    private UpdateChecker updateChecker;
    
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
        
        // Initialize update checker if enabled
        if (getConfig().getBoolean("update-checker.enabled", true)) {
            int resourceId = getConfig().getInt("update-checker.resource-id", 122848);
            boolean notifyAdmins = getConfig().getBoolean("update-checker.notify-admins", true);
            updateChecker = new UpdateChecker(this, resourceId, notifyAdmins);
            updateChecker.checkForUpdates();
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
    
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
} 