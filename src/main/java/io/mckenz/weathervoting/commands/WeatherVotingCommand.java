package io.mckenz.weathervoting.commands;

import io.mckenz.weathervoting.WeatherVoting;
import io.mckenz.weathervoting.managers.VoteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin command handler for the WeatherVoting plugin
 */
public class WeatherVotingCommand implements CommandExecutor, TabCompleter {
    
    private final WeatherVoting plugin;
    
    public WeatherVotingCommand(WeatherVoting plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        VoteManager voteManager = plugin.getVoteManager();
        
        if (!sender.hasPermission("weathervoting.admin")) {
            String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
            sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                if (!sender.hasPermission("weathervoting.status")) {
                    String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
                    sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
                    return true;
                }
                showStatus(sender);
                break;
                
            case "toggle":
                if (!sender.hasPermission("weathervoting.toggle")) {
                    String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
                    sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
                    return true;
                }
                togglePlugin(sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("weathervoting.reload")) {
                    String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
                    sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
                    return true;
                }
                reloadConfig(sender);
                break;
                
            case "debug":
                if (!sender.hasPermission("weathervoting.debug")) {
                    String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
                    sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
                    return true;
                }
                toggleDebug(sender);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        VoteManager voteManager = plugin.getVoteManager();
        
        String header = voteManager.getMessage("help-header", "&6WeatherVoting Commands");
        String statusHelp = voteManager.getMessage("help-status", "&f/weathervoting status &7- Show plugin status");
        String toggleHelp = voteManager.getMessage("help-toggle", "&f/weathervoting toggle &7- Enable/disable the plugin");
        String reloadHelp = voteManager.getMessage("help-reload", "&f/weathervoting reload &7- Reload the configuration");
        String debugHelp = voteManager.getMessage("help-debug", "&f/weathervoting debug &7- Toggle debug mode");
        
        sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + header));
        
        if (sender.hasPermission("weathervoting.status")) {
            sender.sendMessage(VoteManager.colorize(statusHelp));
        }
        
        if (sender.hasPermission("weathervoting.toggle")) {
            sender.sendMessage(VoteManager.colorize(toggleHelp));
        }
        
        if (sender.hasPermission("weathervoting.reload")) {
            sender.sendMessage(VoteManager.colorize(reloadHelp));
        }
        
        if (sender.hasPermission("weathervoting.debug")) {
            sender.sendMessage(VoteManager.colorize(debugHelp));
        }
    }
    
    private void showStatus(CommandSender sender) {
        VoteManager voteManager = plugin.getVoteManager();
        
        String header = voteManager.getMessage("status-header", "&6WeatherVoting Status");
        String enabledStatus = voteManager.getMessage("status-enabled", "&7Plugin: &e%enabled%")
                .replace("%enabled%", plugin.isPluginEnabled() ? "enabled" : "disabled");
        String debugStatus = voteManager.getMessage("status-debug", "&7Debug mode: &e%debug%")
                .replace("%debug%", plugin.isDebugMode() ? "enabled" : "disabled");
        
        sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + header));
        sender.sendMessage(VoteManager.colorize(enabledStatus));
        sender.sendMessage(VoteManager.colorize(debugStatus));
        
        // Show vote counts for all worlds
        String votesHeader = voteManager.getMessage("status-votes-header", "&6Vote Counts");
        sender.sendMessage(VoteManager.colorize(votesHeader));
        
        // This is a simplified approach - you would need to iterate through all worlds
        // and show vote counts for each
        for (String worldName : plugin.getServer().getWorlds().stream().map(world -> world.getName()).toList()) {
            int sunnyVotes = voteManager.getSunnyVotes(worldName);
            int rainVotes = voteManager.getRainVotes(worldName);
            int thunderVotes = voteManager.getThunderVotes(worldName);
            int requiredVotes = voteManager.getRequiredVotes();
            
            sender.sendMessage(VoteManager.colorize("&7World: &f" + worldName));
            
            String entryFormat = voteManager.getMessage("status-votes-entry", "&e%weather%&7: &f%votes%&7/&f%required%");
            
            String sunnyFormat = entryFormat
                    .replace("%weather%", voteManager.getWeatherName("sunny"))
                    .replace("%votes%", String.valueOf(sunnyVotes))
                    .replace("%required%", String.valueOf(requiredVotes));
            
            String rainFormat = entryFormat
                    .replace("%weather%", voteManager.getWeatherName("rain"))
                    .replace("%votes%", String.valueOf(rainVotes))
                    .replace("%required%", String.valueOf(requiredVotes));
            
            String thunderFormat = entryFormat
                    .replace("%weather%", voteManager.getWeatherName("thunder"))
                    .replace("%votes%", String.valueOf(thunderVotes))
                    .replace("%required%", String.valueOf(requiredVotes));
            
            sender.sendMessage(VoteManager.colorize("  " + sunnyFormat));
            sender.sendMessage(VoteManager.colorize("  " + rainFormat));
            sender.sendMessage(VoteManager.colorize("  " + thunderFormat));
        }
    }
    
    private void togglePlugin(CommandSender sender) {
        VoteManager voteManager = plugin.getVoteManager();
        
        boolean newState = !plugin.isPluginEnabled();
        plugin.setPluginEnabled(newState);
        String state = newState ? "enabled" : "disabled";
        
        String message = voteManager.getMessage("toggle-success", "&7WeatherVoting has been &e%state%&7.")
                .replace("%state%", state);
        
        sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
        
        // Log the change
        plugin.getLogger().info("Plugin has been " + state + " by " + sender.getName());
    }
    
    private void reloadConfig(CommandSender sender) {
        VoteManager voteManager = plugin.getVoteManager();
        
        plugin.reloadConfig();
        
        String message = voteManager.getMessage("reload-success", "&7Configuration reloaded.");
        sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
    }
    
    private void toggleDebug(CommandSender sender) {
        VoteManager voteManager = plugin.getVoteManager();
        
        boolean newState = !plugin.isDebugMode();
        plugin.setDebugMode(newState);
        String state = newState ? "enabled" : "disabled";
        
        String message = voteManager.getMessage("debug-success", "&7Debug mode has been &e%state%&7.")
                .replace("%state%", state);
        
        sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
        
        // Log the change
        plugin.getLogger().info("Debug mode has been " + state + " by " + sender.getName());
        
        // Test debug logging
        if (newState) {
            plugin.debug("Debug mode enabled by " + sender.getName());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> subCommands = Arrays.asList("status", "toggle", "reload", "debug");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    // Only add commands the sender has permission to use
                    if (sender.hasPermission("weathervoting." + subCommand)) {
                        completions.add(subCommand);
                    }
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
} 