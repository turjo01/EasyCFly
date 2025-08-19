package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimManager {
    
    private final EasyCFly plugin;
    
    public ClaimManager(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if player is in their own claim or wilderness (if allowed)
     */
    public boolean canFlyAtLocation(Player player, Location location) {
        // Check if wilderness flight is allowed
        boolean allowWilderness = plugin.getConfigManager().getConfig()
            .getBoolean("claims.allow-wilderness", true);
        
        // For now, allow flight in wilderness or if player has bypass permission
        if (allowWilderness || player.hasPermission("easycfly.bypass.claim")) {
            return true;
        }
        
        // Check if player is trusted at this location
        return plugin.getTrustManager().isTrustedAtLocation(player.getUniqueId(), location);
    }
    
    /**
     * Get the owner of a claim at the given location
     */
    public UUID getClaimOwner(Location location) {
        // This would integrate with claim plugins
        // For now, return null (wilderness)
        return null;
    }
    
    /**
     * Check if location is in any claim
     */
    public boolean isInClaim(Location location) {
        // This would integrate with claim plugins
        // For now, return false (wilderness)
        return false;
    }
}