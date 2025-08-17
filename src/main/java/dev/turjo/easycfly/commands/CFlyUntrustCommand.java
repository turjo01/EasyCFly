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

public class CFlyUntrustCommand implements CommandExecutor, TabCompleter {
    
    private final EasyCFly plugin;
    
    public CFlyUntrustCommand(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            plugin.getMessageUtil().sendMessage(player, "trust.usage");
            return true;
        }
        
        String targetName = args[0];
        
        // Find target player
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            plugin.getMessageUtil().sendMessage(player, "trust.player-not-found", 
                "%player%", targetName);
            return true;
        }
        
        // Check if player is trusted
        if (!plugin.getTrustManager().isTrusted(player.getUniqueId(), target.getUniqueId())) {
            plugin.getMessageUtil().sendMessage(player, "trust.not-trusted", 
                "%player%", target.getName());
            return true;
        }
        
        // Untrust the player
        plugin.getTrustManager().untrustPlayer(player.getUniqueId(), target.getUniqueId());
        
        plugin.getMessageUtil().sendMessage(player, "trust.player-untrusted", 
            "%player%", target.getName());
        
        // Notify untrusted player if online
        if (target.isOnline()) {
            plugin.getMessageUtil().sendMessage((Player) target, "trust.you-were-untrusted", 
                "%player%", player.getName());
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            List<String> trustedNames = plugin.getTrustManager().getTrustedPlayerNames(player.getUniqueId());
            
            return trustedNames.stream()
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}