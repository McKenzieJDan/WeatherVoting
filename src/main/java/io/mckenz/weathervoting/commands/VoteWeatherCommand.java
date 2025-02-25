package io.mckenz.weathervoting.commands;

import io.mckenz.weathervoting.WeatherVoting;
import io.mckenz.weathervoting.managers.VoteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoteWeatherCommand implements CommandExecutor, TabCompleter {
    
    private final WeatherVoting plugin;
    
    public VoteWeatherCommand(WeatherVoting plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        VoteManager voteManager = plugin.getVoteManager();
        
        if (!(sender instanceof Player player)) {
            String message = voteManager.getMessage("players-only", "&cOnly players can vote for weather!");
            sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            return true;
        }
        
        if (!player.hasPermission("weathervoting.vote")) {
            String message = voteManager.getMessage("no-permission", "&cYou don't have permission to vote for weather!");
            player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            return true;
        }
        
        if (args.length < 1) {
            showVoteStatus(player);
            return true;
        }
        
        String weatherType = args[0].toLowerCase();
        String worldName = player.getWorld().getName();
        
        if (voteManager.vote(player, weatherType, worldName)) {
            String weatherName = voteManager.getWeatherName(weatherType);
            String message = voteManager.getMessage("vote-cast", "&aYou have voted for {weather} weather!")
                    .replace("{weather}", weatherName);
            player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            showVoteStatus(player);
        } else {
            String message = voteManager.getMessage("invalid-weather", "&cInvalid weather type! Use: sunny, rain, or thunder");
            player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
        }
        
        return true;
    }
    
    private void showVoteStatus(Player player) {
        VoteManager voteManager = plugin.getVoteManager();
        String worldName = player.getWorld().getName();
        
        int sunnyVotes = voteManager.getSunnyVotes(worldName);
        int rainVotes = voteManager.getRainVotes(worldName);
        int thunderVotes = voteManager.getThunderVotes(worldName);
        int requiredVotes = voteManager.getRequiredVotes();
        
        String header = voteManager.getMessage("vote-status-header", "&6=== Weather Vote Status ===");
        String format = voteManager.getMessage("vote-status-format", "&e{weather}: &f{votes}/{required}");
        String footer = voteManager.getMessage("vote-status-footer", "&6To vote: &f/voteweather <sunny|rain|thunder>");
        
        player.sendMessage(VoteManager.colorize(header));
        
        String sunnyFormat = format.replace("{weather}", voteManager.getWeatherName("sunny"))
                                  .replace("{votes}", String.valueOf(sunnyVotes))
                                  .replace("{required}", String.valueOf(requiredVotes));
        
        String rainFormat = format.replace("{weather}", voteManager.getWeatherName("rain"))
                                 .replace("{votes}", String.valueOf(rainVotes))
                                 .replace("{required}", String.valueOf(requiredVotes));
        
        String thunderFormat = format.replace("{weather}", voteManager.getWeatherName("thunder"))
                                    .replace("{votes}", String.valueOf(thunderVotes))
                                    .replace("{required}", String.valueOf(requiredVotes));
        
        player.sendMessage(VoteManager.colorize(sunnyFormat));
        player.sendMessage(VoteManager.colorize(rainFormat));
        player.sendMessage(VoteManager.colorize(thunderFormat));
        player.sendMessage(VoteManager.colorize(footer));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> weatherTypes = Arrays.asList("sunny", "rain", "thunder");
            
            for (String type : weatherTypes) {
                if (type.startsWith(args[0].toLowerCase())) {
                    completions.add(type);
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
} 