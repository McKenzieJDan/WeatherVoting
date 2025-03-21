package io.mckenz.weathervoting.commands;

import io.mckenz.weathervoting.WeatherVoting;
import io.mckenz.weathervoting.managers.VoteManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForecastCommand implements CommandExecutor {
    
    private final WeatherVoting plugin;
    
    public ForecastCommand(WeatherVoting plugin) {
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
        
        if (!player.hasPermission("weathervoting.forecast")) {
            String message = voteManager.getMessage("no-permission", "&7You don't have permission to use this command.");
            player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
            return true;
        }
        
        World world = player.getWorld();
        String worldName = world.getName();
        
        String currentWeather = voteManager.getCurrentWeather(world);
        String timeUntilChange = voteManager.getTimeUntilWeatherChange(world);
        
        String message = voteManager.getMessage("forecast", "&7Current weather: &e%current%&7. Next change in: &f%time%&7.")
                .replace("%current%", currentWeather)
                .replace("%time%", timeUntilChange);
        
        player.sendMessage(VoteManager.colorize(voteManager.getPrefix() + message));
        
        return true;
    }
} 