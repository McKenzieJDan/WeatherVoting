package io.mckenz.weathervoting;

import io.mckenz.weathervoting.commands.ForecastCommand;
import io.mckenz.weathervoting.commands.VoteWeatherCommand;
import io.mckenz.weathervoting.commands.WeatherVotingCommand;
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
    private boolean pluginEnabled = true;
    private boolean debugMode = false;
    
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
        getCommand("weathervoting").setExecutor(new WeatherVotingCommand(this));
        
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
    
    /**
     * Check if the plugin is currently enabled
     * @return true if the plugin is enabled
     */
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }
    
    /**
     * Set the plugin's enabled state
     * @param enabled true to enable the plugin, false to disable
     */
    public void setPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
    }
    
    /**
     * Check if debug mode is enabled
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Set the debug mode state
     * @param enabled true to enable debug mode, false to disable
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }
    
    /**
     * Log a debug message if debug mode is enabled
     * @param message The message to log
     */
    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }
} 