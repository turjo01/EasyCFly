package dev.turjo.easycfly.hooks.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WorldGuardHook implements ClaimHook {
    
    private final EasyCFly plugin;
    
    public WorldGuardHook(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean isInOwnClaim(Player player, Location location) {
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) {
                return false;
            }
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
            );
            
            for (ProtectedRegion region : regions) {
                if (region.getOwners().contains(player.getUniqueId()) ||
                    region.getOwners().contains(player.getName())) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public UUID getClaimOwner(Location location) {
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) {
                return null;
            }
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
            );
            
            for (ProtectedRegion region : regions) {
                if (!region.getOwners().getUniqueIds().isEmpty()) {
                    return region.getOwners().getUniqueIds().iterator().next();
                }
            }
            
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting WorldGuard region owner: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isInClaim(Location location) {
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) {
                return false;
            }
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
            );
            
            return regions.size() > 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getClaimName(Location location) {
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) {
                return null;
            }
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
            );
            
            for (ProtectedRegion region : regions) {
                return region.getId();
            }
            
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting WorldGuard region name: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public double getClaimSize(Location location) {
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) {
                return 0;
            }
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
            );
            
            for (ProtectedRegion region : regions) {
                return region.volume();
            }
            
            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting WorldGuard region size: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public String getPluginName() {
        return "WorldGuard";
    }
}