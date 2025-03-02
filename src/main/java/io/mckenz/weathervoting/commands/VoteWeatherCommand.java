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
import java.util.UUID;

public class VoteWeatherCommand implements CommandExecutor, TabCompleter {
    
    private final WeatherVoting plugin;
    
    public VoteWeatherCommand(WeatherVoting plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        VoteManager voteManager = plugin.getVoteManager();
        
        if (!(sender instanceof Player player)) {
            String message = voteManager.getMessage("player-only", "&7This command can only be used by players.");
            sender.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            return true;
        }
        
        if (!player.hasPermission("weathervoting.vote")) {
            String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
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
            String message = voteManager.getMessage("vote-cast", "&7You voted for &e%weather%&7.")
                    .replace("%weather%", weatherName);
            player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            showVoteStatus(player);
        } else {
            String message = voteManager.getMessage("invalid-weather", "&7Invalid weather type. Use sunny, rain, or thunder.");
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
        
        String header = voteManager.getMessage("vote-status-header", "&6Weather Vote Status");
        String entryFormat = voteManager.getMessage("vote-status-entry", "&e%weather%&7: &f%votes%&7/&f%required% &7votes");
        
        player.sendMessage(VoteManager.colorize(header));
        
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
        
        player.sendMessage(VoteManager.colorize(sunnyFormat));
        player.sendMessage(VoteManager.colorize(rainFormat));
        player.sendMessage(VoteManager.colorize(thunderFormat));
        
        // Add your vote info
        String playerVote = getPlayerVote(player, worldName);
        if (playerVote != null) {
            String yourVoteMsg = voteManager.getMessage("vote-status-your-vote", "&7Your vote: &e%weather%")
                .replace("%weather%", voteManager.getWeatherName(playerVote));
            player.sendMessage(VoteManager.colorize(yourVoteMsg));
        } else {
            String noVoteMsg = voteManager.getMessage("vote-status-no-vote", "&7You haven't voted yet.");
            player.sendMessage(VoteManager.colorize(noVoteMsg));
        }
    }
    
    private String getPlayerVote(Player player, String worldName) {
        VoteManager voteManager = plugin.getVoteManager();
        UUID playerUUID = player.getUniqueId();
        
        return voteManager.getPlayerVote(playerUUID, worldName);
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