package dev.turjo.easycfly.commands;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CFlyTrustCommand implements CommandExecutor, TabCompleter {
    
    private final EasyCFly plugin;
    
    public CFlyTrustCommand(EasyCFly plugin) {
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
            // Show trusted players
            showTrustedPlayers(player);
            return true;
        }
        
        if (args.length == 1) {
            String targetName = args[0];
            
            if (targetName.equalsIgnoreCase("list")) {
                showTrustedPlayers(player);
                return true;
            }
            
            if (targetName.equalsIgnoreCase("clear")) {
                clearTrust(player);
                return true;
            }
            
            // Trust a player
            trustPlayer(player, targetName);
            return true;
        }
        
        plugin.getMessageUtil().sendMessage(player, "trust.usage");
        return true;
    }
    
    private void trustPlayer(Player owner, String targetName) {
        if (targetName.equalsIgnoreCase(owner.getName())) {
            plugin.getMessageUtil().sendMessage(owner, "trust.cannot-trust-self");
            return;
        }
        
        // Check max trust limit
        if (plugin.getTrustManager().hasMaxTrust(owner.getUniqueId()) && 
            !owner.hasPermission("easycfly.trust.unlimited")) {
            int maxTrust = plugin.getConfigManager().getConfig().getInt("trust.max-trusted-players", 10);
            plugin.getMessageUtil().sendMessage(owner, "trust.max-reached", 
                "%max%", String.valueOf(maxTrust));
            return;
        }
        
        // Find target player
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            plugin.getMessageUtil().sendMessage(owner, "trust.player-not-found", 
                "%player%", targetName);
            return;
        }
        
        // Check if already trusted
        if (plugin.getTrustManager().isTrusted(owner.getUniqueId(), target.getUniqueId())) {
            plugin.getMessageUtil().sendMessage(owner, "trust.already-trusted", 
                "%player%", target.getName());
            return;
        }
        
        // Trust the player
        plugin.getTrustManager().trustPlayer(owner.getUniqueId(), target.getUniqueId());
        
        plugin.getMessageUtil().sendMessage(owner, "trust.player-trusted", 
            "%player%", target.getName());
        
        // Notify trusted player if online
        if (target.isOnline()) {
            plugin.getMessageUtil().sendMessage((Player) target, "trust.you-were-trusted", 
                "%player%", owner.getName());
        }
    }
    
    private void showTrustedPlayers(Player player) {
        List<String> trustedNames = plugin.getTrustManager().getTrustedPlayerNames(player.getUniqueId());
        
        if (trustedNames.isEmpty()) {
            plugin.getMessageUtil().sendMessage(player, "trust.no-trusted-players");
            return;
        }
        
        plugin.getMessageUtil().sendMessage(player, "trust.list-header");
        
        for (String name : trustedNames) {
            plugin.getMessageUtil().sendMessage(player, "trust.list-entry", 
                "%player%", name);
        }
        
        int maxTrust = plugin.getConfigManager().getConfig().getInt("trust.max-trusted-players", 10);
        plugin.getMessageUtil().sendMessage(player, "trust.list-footer", 
            "%count%", String.valueOf(trustedNames.size()),
            "%max%", String.valueOf(maxTrust));
    }
    
    private void clearTrust(Player player) {
        int count = plugin.getTrustManager().getTrustCount(player.getUniqueId());
        
        if (count == 0) {
            plugin.getMessageUtil().sendMessage(player, "trust.no-trusted-players");
            return;
        }
        
        plugin.getTrustManager().clearTrust(player.getUniqueId());
        plugin.getMessageUtil().sendMessage(player, "trust.cleared", 
            "%count%", String.valueOf(count));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("list");
            completions.add("clear");
            
            // Add online player names
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(sender.getName()))
                .collect(Collectors.toList()));
            
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}