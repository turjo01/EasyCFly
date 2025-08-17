package dev.turjo.easycfly.commands;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CFlyInfoCommand implements CommandExecutor {
    
    private final EasyCFly plugin;
    
    public CFlyInfoCommand(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
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
        
        int trustedCount = plugin.getTrustManager().getTrustCount(player.getUniqueId());
        int maxTrust = plugin.getConfigManager().getConfig().getInt("trust.max-trusted-players", 10);
        
        plugin.getMessageUtil().sendMessage(player, "info.trusted-count", 
            "%count%", String.valueOf(trustedCount),
            "%max%", String.valueOf(maxTrust));
        
        plugin.getMessageUtil().sendMessage(player, "info.footer");
        
        return true;
    }
}