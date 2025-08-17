package dev.turjo.easycfly.hooks.claims;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface ClaimHook {
    
    /**
     * Check if the player is in their own claim at the given location
     */
    boolean isInOwnClaim(Player player, Location location);
    
    /**
     * Get the owner of the claim at the given location
     */
    UUID getClaimOwner(Location location);
    
    /**
     * Check if the location is in any claim
     */
    boolean isInClaim(Location location);
    
    /**
     * Get the name of the claim at the given location
     */
    String getClaimName(Location location);
    
    /**
     * Get the size of the claim at the given location
     */
    double getClaimSize(Location location);
    
    /**
     * Get the plugin name this hook is for
     */
    String getPluginName();
}