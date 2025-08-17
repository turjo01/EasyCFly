package dev.turjo.easycfly.hooks;

import dev.turjo.easycfly.EasyCFly;
import dev.turjo.easycfly.hooks.claims.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HookManager {
    
    private final EasyCFly plugin;
    private final List<ClaimHook> claimHooks;
    private boolean placeholderAPIEnabled = false;
    
    public HookManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.claimHooks = new ArrayList<>();
    }
    
    public void setupHooks() {
        setupClaimHooks();
        setupPlaceholderAPI();
        
        plugin.getLogger().info("Loaded " + claimHooks.size() + " claim plugin hooks");
    }
    
    private void setupClaimHooks() {
        List<String> priority = plugin.getConfigManager().getConfig().getStringList("claims.priority");
        
        for (String pluginName : priority) {
            Plugin claimPlugin = plugin.getServer().getPluginManager().getPlugin(pluginName);
            if (claimPlugin == null || !claimPlugin.isEnabled()) {
                continue;
            }
            
            ClaimHook hook = createClaimHook(pluginName);
            if (hook != null) {
                claimHooks.add(hook);
                plugin.getLogger().info("Hooked into " + pluginName + " for claim support");
            }
        }
        
        if (claimHooks.isEmpty()) {
            plugin.getLogger().warning("No supported claim plugins found!");
        }
    }
    
    private ClaimHook createClaimHook(String pluginName) {
        switch (pluginName.toLowerCase()) {
            case "worldguard":
                return new WorldGuardHook(plugin);
            case "griefprevention":
                return new GriefPreventionHook(plugin);
            case "lands":
                return new LandsHook(plugin);
            case "towny":
                return new TownyHook(plugin);
            case "residence":
                return new ResidenceHook(plugin);
            default:
                return null;
        }
    }
    
    private void setupPlaceholderAPI() {
        Plugin papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        placeholderAPIEnabled = papi != null && papi.isEnabled();
        
        if (placeholderAPIEnabled) {
            plugin.getLogger().info("PlaceholderAPI found and enabled");
        }
    }
    
    public boolean isInOwnClaim(Player player, Location location) {
        for (ClaimHook hook : claimHooks) {
            if (hook.isInOwnClaim(player, location)) {
                return true;
            }
        }
        
        // Check if wilderness is allowed
        return plugin.getConfigManager().getConfig().getBoolean("claims.allow-wilderness", false);
    }
    
    public UUID getClaimOwner(Location location) {
        for (ClaimHook hook : claimHooks) {
            UUID owner = hook.getClaimOwner(location);
            if (owner != null) {
                return owner;
            }
        }
        return null;
    }
    
    public boolean isInClaim(Location location) {
        for (ClaimHook hook : claimHooks) {
            if (hook.isInClaim(location)) {
                return true;
            }
        }
        return false;
    }
    
    public String getClaimName(Location location) {
        for (ClaimHook hook : claimHooks) {
            String name = hook.getClaimName(location);
            if (name != null) {
                return name;
            }
        }
        return "Unknown";
    }
    
    public double getClaimSize(Location location) {
        for (ClaimHook hook : claimHooks) {
            double size = hook.getClaimSize(location);
            if (size > 0) {
                return size;
            }
        }
        return 0;
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
    
    public List<ClaimHook> getClaimHooks() {
        return claimHooks;
    }
}