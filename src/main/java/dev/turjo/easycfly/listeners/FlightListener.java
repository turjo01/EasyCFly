package dev.turjo.easycfly.listeners;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlightListener implements Listener {
    
    private final EasyCFly plugin;
    
    public FlightListener(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        // Only handle if player is trying to start flying
        if (!event.isFlying()) {
            return;
        }
        
        // If player is trying to fly but not through our plugin
        if (!plugin.getFlightManager().isFlying(player)) {
            // Check if they can fly at this location
            if (!plugin.getFlightManager().canFly(player, player.getLocation())) {
                event.setCancelled(true);
                plugin.getMessageUtil().sendMessage(player, "flight.cannot-fly");
                plugin.getLogger().info("Cancelled flight toggle for " + player.getName() + " - no permission");
            }
        }
    }
}