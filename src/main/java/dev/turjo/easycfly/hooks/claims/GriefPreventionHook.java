package dev.turjo.easycfly.hooks.claims;

import dev.turjo.easycfly.EasyCFly;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GriefPreventionHook implements ClaimHook {
    
    private final EasyCFly plugin;
    
    public GriefPreventionHook(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean isInOwnClaim(Player player, Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            
            if (claim == null) {
                return false;
            }
            
            return claim.ownerID.equals(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking GriefPrevention claim: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public UUID getClaimOwner(Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            
            if (claim == null) {
                return null;
            }
            
            return claim.ownerID;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting GriefPrevention claim owner: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isInClaim(Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            return claim != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking GriefPrevention claim: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getClaimName(Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            
            if (claim == null) {
                return null;
            }
            
            // GriefPrevention doesn't have claim names by default, use ID
            return "Claim #" + claim.getID();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting GriefPrevention claim name: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public double getClaimSize(Location location) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            
            if (claim == null) {
                return 0;
            }
            
            return claim.getArea();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting GriefPrevention claim size: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public String getPluginName() {
        return "GriefPrevention";
    }
}