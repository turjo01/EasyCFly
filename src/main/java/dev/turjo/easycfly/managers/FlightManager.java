package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import dev.turjo.easycfly.events.PlayerFlightToggleEvent;
import dev.turjo.easycfly.models.FlightData;
import dev.turjo.easycfly.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager {
    
    private final EasyCFly plugin;
    private final Map<UUID, FlightData> flightData;
    private final Map<UUID, BukkitTask> flightTasks;
    private final Map<UUID, Integer> flightTime;
    
    public FlightManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.flightData = new ConcurrentHashMap<>();
        this.flightTasks = new HashMap<>();
        this.flightTime = new HashMap<>();
        
        startFlightChecker();
    }
    
    public boolean canFly(Player player, Location location) {
        // Check basic permission
        if (!player.hasPermission("easycfly.fly")) {
            return false;
        }
        
        // Check if in claim
        if (!plugin.getHookManager().isInOwnClaim(player, location) && 
            !plugin.getTrustManager().isTrusted(location, player.getUniqueId())) {
            return false;
        }
        
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId()) && 
            !player.hasPermission("easycfly.bypass.cooldown")) {
            return false;
        }
        
        // Check economy
        if (plugin.getEconomyManager().isEnabled() && 
            !plugin.getEconomyManager().hasEnoughMoney(player) &&
            !player.hasPermission("easycfly.bypass.cost")) {
            return false;
        }
        
        return true;
    }
    
    public void enableFlight(Player player) {
        if (isFlying(player)) {
            plugin.getMessageUtil().sendMessage(player, "flight.already-enabled");
            return;
        }
        
        if (!canFly(player, player.getLocation())) {
            plugin.getMessageUtil().sendMessage(player, "flight.cannot-fly");
            return;
        }
        
        // Call event
        PlayerFlightToggleEvent event = new PlayerFlightToggleEvent(player, true);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Charge economy
        if (plugin.getEconomyManager().isEnabled() && 
            !player.hasPermission("easycfly.bypass.cost")) {
            plugin.getEconomyManager().chargeFlight(player);
        }
        
        // Enable flight
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Create flight data
        FlightData data = new FlightData(player.getUniqueId(), System.currentTimeMillis());
        flightData.put(player.getUniqueId(), data);
        
        // Start flight timer if time limit is enabled
        if (plugin.getConfigManager().getConfig().getBoolean("flight.time-limit.enabled") &&
            !player.hasPermission("easycfly.fly.unlimited")) {
            startFlightTimer(player);
        }
        
        // Set cooldown
        plugin.getCooldownManager().setCooldown(player.getUniqueId());
        
        // Send message
        plugin.getMessageUtil().sendMessage(player, "flight.enabled");
        
        // Play sound
        if (plugin.getConfigManager().getConfig().getBoolean("sounds.enabled")) {
            player.playSound(player.getLocation(), 
                org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        }
        
        // Particle effects
        if (plugin.getConfigManager().getConfig().getBoolean("effects.particles.enabled")) {
            spawnFlightParticles(player);
        }
    }
    
    public void disableFlight(Player player) {
        disableFlight(player, true);
    }
    
    public void disableFlight(Player player, boolean sendMessage) {
        if (!isFlying(player)) {
            if (sendMessage) {
                plugin.getMessageUtil().sendMessage(player, "flight.already-disabled");
            }
            return;
        }
        
        // Call event
        PlayerFlightToggleEvent event = new PlayerFlightToggleEvent(player, false);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Disable flight
        player.setAllowFlight(false);
        player.setFlying(false);
        
        // Remove flight data
        flightData.remove(player.getUniqueId());
        
        // Cancel flight timer
        BukkitTask task = flightTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Remove flight time
        flightTime.remove(player.getUniqueId());
        
        // Send message
        if (sendMessage) {
            plugin.getMessageUtil().sendMessage(player, "flight.disabled");
        }
        
        // Play sound
        if (plugin.getConfigManager().getConfig().getBoolean("sounds.enabled")) {
            player.playSound(player.getLocation(), 
                org.bukkit.Sound.ENTITY_BAT_TAKEOFF, 1.0f, 0.8f);
        }
    }
    
    public void toggleFlight(Player player) {
        if (isFlying(player)) {
            disableFlight(player);
        } else {
            enableFlight(player);
        }
    }
    
    public boolean isFlying(Player player) {
        return flightData.containsKey(player.getUniqueId());
    }
    
    public FlightData getFlightData(Player player) {
        return flightData.get(player.getUniqueId());
    }
    
    public int getRemainingFlightTime(Player player) {
        return flightTime.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void disableAllFlying() {
        for (UUID uuid : flightData.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                disableFlight(player, false);
            }
        }
    }
    
    private void startFlightTimer(Player player) {
        int timeLimit = plugin.getConfigManager().getConfig().getInt("flight.time-limit.seconds", 300);
        flightTime.put(player.getUniqueId(), timeLimit);
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                int remaining = flightTime.getOrDefault(player.getUniqueId(), 0);
                
                if (remaining <= 0) {
                    disableFlight(player);
                    plugin.getMessageUtil().sendMessage(player, "flight.time-expired");
                    cancel();
                    return;
                }
                
                flightTime.put(player.getUniqueId(), remaining - 1);
                
                // Warning messages
                if (remaining == 60 || remaining == 30 || remaining == 10 || remaining <= 5) {
                    plugin.getMessageUtil().sendMessage(player, "flight.time-warning", 
                        "%time%", String.valueOf(remaining));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        
        flightTasks.put(player.getUniqueId(), task);
    }
    
    private void startFlightChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : flightData.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        flightData.remove(uuid);
                        continue;
                    }
                    
                    // Check if still in valid area
                    if (!canFly(player, player.getLocation())) {
                        disableFlight(player);
                        plugin.getMessageUtil().sendMessage(player, "flight.left-claim");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    private void spawnFlightParticles(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isFlying(player)) {
                    cancel();
                    return;
                }
                
                Location loc = player.getLocation().add(0, -0.5, 0);
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.CLOUD, 
                    loc, 5, 0.3, 0.1, 0.3, 0.02
                );
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}