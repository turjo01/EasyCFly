package dev.turjo.easycfly.listeners;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerListener implements Listener {
    
    private final EasyCFly plugin;
    
    public PlayerListener(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check for updates if player has permission
        if (player.hasPermission("easycfly.admin.update") && 
            plugin.getUpdateChecker() != null) {
            plugin.getUpdateChecker().notifyPlayer(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Disable flight when player leaves
        if (plugin.getFlightManager().isFlying(player)) {
            plugin.getFlightManager().disableFlight(player, false);
        }
        
        // Clear any cooldowns to prevent memory leaks
        plugin.getCooldownManager().removeCooldown(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Check if flight is disabled in the new world
        String worldName = player.getWorld().getName();
        if (plugin.getConfigManager().getConfig().getStringList("worlds.disabled-worlds").contains(worldName)) {
            if (plugin.getFlightManager().isFlying(player)) {
                plugin.getFlightManager().disableFlight(player);
                plugin.getMessageUtil().sendMessage(player, "error.world-disabled");
            }
        }
        
        // Check if player can still fly in new world
        if (plugin.getFlightManager().isFlying(player)) {
            if (!plugin.getFlightManager().canFly(player, player.getLocation())) {
                plugin.getFlightManager().disableFlight(player);
                plugin.getMessageUtil().sendMessage(player, "flight.cannot-fly");
            }
        }
    }
}