package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrustManager {
    
    private final EasyCFly plugin;
    private final Map<UUID, Set<UUID>> trustData; // Owner UUID -> Set of trusted player UUIDs
    
    public TrustManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.trustData = new ConcurrentHashMap<>();
        loadTrustData();
    }
    
    public void trustPlayer(UUID owner, UUID trusted) {
        trustData.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(trusted);
        saveTrustData();
    }
    
    public void untrustPlayer(UUID owner, UUID trusted) {
        Set<UUID> trustedPlayers = trustData.get(owner);
        if (trustedPlayers != null) {
            trustedPlayers.remove(trusted);
            if (trustedPlayers.isEmpty()) {
                trustData.remove(owner);
            }
            saveTrustData();
        }
    }
    
    public boolean isTrusted(Location location, UUID player) {
        // Get claim owner at location  
        UUID owner = plugin.getClaimManager().getClaimOwner(location);
        if (owner == null) {
            // No claim owner means wilderness - allow if wilderness is enabled
            return plugin.getConfigManager().getConfig().getBoolean("claims.allow-wilderness", true);
        }
        
        // Check if player is trusted by owner
        Set<UUID> trustedPlayers = trustData.get(owner);
        return trustedPlayers != null && trustedPlayers.contains(player);
    }
    
    public boolean isTrustedAtLocation(UUID player, Location location) {
        return isTrusted(location, player);
    }
    
    public boolean isTrusted(UUID owner, UUID player) {
        Set<UUID> trustedPlayers = trustData.get(owner);
        return trustedPlayers != null && trustedPlayers.contains(player);
    }
    
    public Set<UUID> getTrustedPlayers(UUID owner) {
        return trustData.getOrDefault(owner, Collections.emptySet());
    }
    
    public List<String> getTrustedPlayerNames(UUID owner) {
        Set<UUID> trusted = getTrustedPlayers(owner);
        List<String> names = new ArrayList<>();
        
        for (UUID uuid : trusted) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                names.add(player.getName());
            } else {
                // Try to get offline player name
                String name = plugin.getServer().getOfflinePlayer(uuid).getName();
                if (name != null) {
                    names.add(name);
                }
            }
        }
        
        return names;
    }
    
    public void clearTrust(UUID owner) {
        trustData.remove(owner);
        saveTrustData();
    }
    
    public int getTrustCount(UUID owner) {
        Set<UUID> trusted = trustData.get(owner);
        return trusted != null ? trusted.size() : 0;
    }
    
    public boolean hasMaxTrust(UUID owner) {
        int maxTrust = plugin.getConfigManager().getConfig().getInt("trust.max-trusted-players", 10);
        return getTrustCount(owner) >= maxTrust;
    }
    
    private void loadTrustData() {
        if (plugin.getConfigManager().getData().contains("trust")) {
            for (String ownerStr : plugin.getConfigManager().getData().getConfigurationSection("trust").getKeys(false)) {
                try {
                    UUID owner = UUID.fromString(ownerStr);
                    List<String> trustedList = plugin.getConfigManager().getData().getStringList("trust." + ownerStr);
                    
                    Set<UUID> trusted = ConcurrentHashMap.newKeySet();
                    for (String trustedStr : trustedList) {
                        try {
                            trusted.add(UUID.fromString(trustedStr));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid UUID in trust data: " + trustedStr);
                        }
                    }
                    
                    if (!trusted.isEmpty()) {
                        trustData.put(owner, trusted);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid owner UUID in trust data: " + ownerStr);
                }
            }
        }
    }
    
    private void saveTrustData() {
        // Clear existing data
        plugin.getConfigManager().getData().set("trust", null);
        
        // Save current data
        for (Map.Entry<UUID, Set<UUID>> entry : trustData.entrySet()) {
            List<String> trustedList = new ArrayList<>();
            for (UUID trusted : entry.getValue()) {
                trustedList.add(trusted.toString());
            }
            plugin.getConfigManager().getData().set("trust." + entry.getKey().toString(), trustedList);
        }
        
        plugin.getConfigManager().saveData();
    }
}