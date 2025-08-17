package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    
    private final EasyCFly plugin;
    private final Map<UUID, Long> cooldowns;
    
    public CooldownManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
        
        // Start cleanup task
        startCleanupTask();
    }
    
    public void setCooldown(UUID player) {
        int cooldownSeconds = plugin.getConfigManager().getConfig().getInt("flight.cooldown.seconds", 30);
        if (cooldownSeconds > 0) {
            cooldowns.put(player, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }
    }
    
    public boolean isOnCooldown(UUID player) {
        Long cooldownEnd = cooldowns.get(player);
        if (cooldownEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(player);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(UUID player) {
        Long cooldownEnd = cooldowns.get(player);
        if (cooldownEnd == null) {
            return 0;
        }
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
    
    public void removeCooldown(UUID player) {
        cooldowns.remove(player);
    }
    
    public void clearAllCooldowns() {
        cooldowns.clear();
    }
    
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                cooldowns.entrySet().removeIf(entry -> currentTime >= entry.getValue());
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Run every minute
    }
}