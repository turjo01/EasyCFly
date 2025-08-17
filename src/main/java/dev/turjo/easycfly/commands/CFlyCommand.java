package dev.turjo.easycfly.commands;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CFlyCommand implements CommandExecutor, TabCompleter {
    
    private final EasyCFly plugin;
    
    public CFlyCommand(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Toggle flight
            plugin.getFlightManager().toggleFlight(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "toggle":
                plugin.getFlightManager().toggleFlight(player);
                break;
                
            case "on":
            case "enable":
                plugin.getFlightManager().enableFlight(player);
                break;
                
            case "off":
            case "disable":
                plugin.getFlightManager().disableFlight(player);
                break;
                
            case "status":
            case "info":
                showFlightInfo(player);
                break;
                
            case "time":
                showRemainingTime(player);
                break;
                
            case "help":
                showHelp(player);
                break;
                
            default:
                plugin.getMessageUtil().sendMessage(player, "general.unknown-command");
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showFlightInfo(Player player) {
        boolean isFlying = plugin.getFlightManager().isFlying(player);
        boolean canFly = plugin.getFlightManager().canFly(player, player.getLocation());
        long cooldown = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId());
        int remainingTime = plugin.getFlightManager().getRemainingFlightTime(player);
        
        plugin.getMessageUtil().sendMessage(player, "info.header");
        plugin.getMessageUtil().sendMessage(player, "info.status", 
            "%status%", isFlying ? "§aEnabled" : "§cDisabled");
        plugin.getMessageUtil().sendMessage(player, "info.can-fly", 
            "%can_fly%", canFly ? "§aYes" : "§cNo");
        
        if (cooldown > 0) {
            plugin.getMessageUtil().sendMessage(player, "info.cooldown", 
                "%time%", String.valueOf(cooldown));
        }
        
        if (remainingTime > 0) {
            plugin.getMessageUtil().sendMessage(player, "info.remaining-time", 
                "%time%", String.valueOf(remainingTime));
        }
        
        if (plugin.getEconomyManager().isEnabled()) {
            double cost = plugin.getEconomyManager().getFlightCost(player);
            double balance = plugin.getEconomyManager().getBalance(player);
            
            plugin.getMessageUtil().sendMessage(player, "info.cost", 
                "%cost%", plugin.getEconomyManager().formatMoney(cost));
            plugin.getMessageUtil().sendMessage(player, "info.balance", 
                "%balance%", plugin.getEconomyManager().formatMoney(balance));
        }
        
        plugin.getMessageUtil().sendMessage(player, "info.footer");
    }
    
    private void showRemainingTime(Player player) {
        if (!plugin.getFlightManager().isFlying(player)) {
            plugin.getMessageUtil().sendMessage(player, "flight.not-flying");
            return;
        }
        
        int remainingTime = plugin.getFlightManager().getRemainingFlightTime(player);
        
        if (remainingTime > 0) {
            plugin.getMessageUtil().sendMessage(player, "flight.remaining-time", 
                "%time%", String.valueOf(remainingTime));
        } else {
            plugin.getMessageUtil().sendMessage(player, "flight.unlimited-time");
        }
    }
    
    private void showHelp(Player player) {
        plugin.getMessageUtil().sendMessage(player, "help.header");
        plugin.getMessageUtil().sendMessage(player, "help.toggle");
        plugin.getMessageUtil().sendMessage(player, "help.on");
        plugin.getMessageUtil().sendMessage(player, "help.off");
        plugin.getMessageUtil().sendMessage(player, "help.status");
        plugin.getMessageUtil().sendMessage(player, "help.time");
        plugin.getMessageUtil().sendMessage(player, "help.trust");
        plugin.getMessageUtil().sendMessage(player, "help.untrust");
        plugin.getMessageUtil().sendMessage(player, "help.footer");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("toggle", "on", "off", "enable", "disable", "status", "info", "time", "help");
        }
        
        return new ArrayList<>();
    }
}