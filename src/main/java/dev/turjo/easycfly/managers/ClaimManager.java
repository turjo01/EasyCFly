package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
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
        plugin.getLogger().info("Checking flight permission for " + player.getName() + " at " + 
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        
        // Check if wilderness flight is allowed
        boolean allowWilderness = plugin.getConfigManager().getConfig()
            .getBoolean("claims.allow-wilderness", true);
        
        // Check bypass permission first
        if (player.hasPermission("easycfly.bypass.claim")) {
            plugin.getLogger().info("Player " + player.getName() + " has bypass permission");
            return true;
        }
        
        // Check if GriefPrevention is available
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            
            if (claim == null) {
                // No claim - check wilderness setting
                plugin.getLogger().info("No claim found at location - wilderness: " + allowWilderness);
                return allowWilderness;
            }
            
            // Check if player owns the claim
            if (claim.ownerID != null && claim.ownerID.equals(player.getUniqueId())) {
                plugin.getLogger().info("Player " + player.getName() + " owns this claim");
                return true;
            }
            
            // Check if player is trusted in this claim
            if (claim.allowBuild(player, location.getBlock().getType()) == null) {
                plugin.getLogger().info("Player " + player.getName() + " is trusted in this claim");
                return true;
            }
            
            // Check if player is trusted via our trust system
            if (claim.ownerID != null && plugin.getTrustManager().isTrusted(claim.ownerID, player.getUniqueId())) {
                plugin.getLogger().info("Player " + player.getName() + " is trusted via EasyCFly trust system");
                return true;
            }
            
            plugin.getLogger().info("Player " + player.getName() + " is not trusted in this claim");
            return false;
        }
        
        // No claim plugin found - allow if wilderness is enabled
        plugin.getLogger().info("No GriefPrevention found - using wilderness setting: " + allowWilderness);
        return allowWilderness;
    }
    
    /**
     * Get the owner of a claim at the given location
     */
    public UUID getClaimOwner(Location location) {
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            if (claim != null) {
                return claim.ownerID;
            }
        }
        return null;
    }
    
    /**
     * Check if location is in any claim
     */
    public boolean isInClaim(Location location) {
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            return claim != null;
        }
        return false;
    }
}