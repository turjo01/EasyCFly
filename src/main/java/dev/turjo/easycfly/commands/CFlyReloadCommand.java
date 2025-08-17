package dev.turjo.easycfly.commands;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CFlyReloadCommand implements CommandExecutor {
    
    private final EasyCFly plugin;
    
    public CFlyReloadCommand(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("easycfly.admin.reload")) {
            plugin.getMessageUtil().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        try {
            plugin.reload();
            plugin.getMessageUtil().sendMessage(sender, "admin.reload-success");
        } catch (Exception e) {
            plugin.getMessageUtil().sendMessage(sender, "admin.reload-error");
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
        }
        
        return true;
    }
}