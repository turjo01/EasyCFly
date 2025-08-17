package dev.turjo.easycfly.listeners;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class FlightListener implements Listener {
    
    private final EasyCFly plugin;
    
    public FlightListener(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is flying and moved to a different location
        if (plugin.getFlightManager().isFlying(player)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                
                // Check if still in valid flight area
                if (!plugin.getFlightManager().canFly(player, event.getTo())) {
                    plugin.getFlightManager().disableFlight(player);
                    plugin.getMessageUtil().sendMessage(player, "flight.left-claim");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        // If player is trying to fly but doesn't have permission through our plugin
        if (event.isFlying() && !plugin.getFlightManager().isFlying(player)) {
            if (!plugin.getFlightManager().canFly(player, player.getLocation())) {
                event.setCancelled(true);
                plugin.getMessageUtil().sendMessage(player, "flight.cannot-fly");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Prevent fall damage if configured
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
            plugin.getConfigManager().getConfig().getBoolean("flight.prevent-fall-damage", true)) {
            
            // Check if player was recently flying
            if (plugin.getFlightManager().getFlightData(player) != null) {
                long timeSinceDisable = System.currentTimeMillis() - 
                    plugin.getFlightManager().getFlightData(player).getStartTime();
                
                int protectionTime = plugin.getConfigManager().getConfig()
                    .getInt("flight.fall-damage-protection-time", 5) * 1000;
                
                if (timeSinceDisable < protectionTime) {
                    event.setCancelled(true);
                    plugin.getMessageUtil().sendMessage(player, "flight.fall-damage-protected",
                        "%time%", String.valueOf(protectionTime / 1000));
                }
            }
        }
    }
}