package dev.turjo.easycfly.utils;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    
    private final EasyCFly plugin;
    private final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public MessageUtil(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = getMessage(key, replacements);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }
    
    public String getMessage(String key, String... replacements) {
        String message = plugin.getConfigManager().getMessages().getString(key);
        
        if (message == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return "&cMissing message: " + key;
        }
        
        // Add prefix if not already present
        String prefix = plugin.getConfigManager().getMessages().getString("general.prefix", "");
        if (!message.startsWith(prefix) && !key.startsWith("actionbar") && !key.startsWith("bossbar")) {
            message = prefix + message;
        }
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return colorize(message);
    }
    
    public String colorize(String message) {
        if (message == null) return null;
        
        // Handle hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            message = message.replace("&#" + hexCode, replacement);
        }
        
        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void sendActionBar(Player player, String key, String... replacements) {
        String message = getMessage(key, replacements);
        if (message != null && !message.isEmpty()) {
            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message)
            );
        }
    }
    
    public void sendTitle(Player player, String titleKey, String subtitleKey, String... replacements) {
        String title = getMessage(titleKey, replacements);
        String subtitle = getMessage(subtitleKey, replacements);
        
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    public void reload() {
        // Reload messages configuration
        plugin.getConfigManager().reloadConfigs();
    }
}