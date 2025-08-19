package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager {
    
    private final EasyCFly plugin;
    private final Set<UUID> flyingPlayers;
    private final Map<UUID, BukkitTask> flightTasks;
    private final Map<UUID, Integer> flightTime;
    private final Map<UUID, Location> lastLocation;
    
    public FlightManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.flyingPlayers = ConcurrentHashMap.newKeySet();
        this.flightTasks = new HashMap<>();
        this.flightTime = new HashMap<>();
        this.lastLocation = new HashMap<>();
        
        startFlightChecker();
    }
    
    public boolean canFly(Player player, Location location) {
        // Check basic permission
        if (!player.hasPermission("easycfly.fly")) {
            plugin.getLogger().info("Player " + player.getName() + " lacks easycfly.fly permission");
            return false;
        }
        
        // Check world restrictions
        String worldName = location.getWorld().getName();
        if (plugin.getConfigManager().getConfig().getStringList("worlds.disabled-worlds").contains(worldName)) {
            plugin.getLogger().info("Flight disabled in world: " + worldName);
            return false;
        }
        
        // Check claim permissions
        if (!plugin.getClaimManager().canFlyAtLocation(player, location)) {
            plugin.getLogger().info("Player " + player.getName() + " cannot fly at location due to claim restrictions");
            return false;
        }
        
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId()) && 
            !player.hasPermission("easycfly.bypass.cooldown")) {
            plugin.getLogger().info("Player " + player.getName() + " is on cooldown");
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
            plugin.getLogger().info("Cannot enable flight for " + player.getName() + " - failed canFly check");
            return;
        }
        
        // Charge economy
        if (plugin.getEconomyManager().isEnabled() && 
            !player.hasPermission("easycfly.bypass.cost")) {
            if (!plugin.getEconomyManager().chargeFlight(player)) {
                plugin.getMessageUtil().sendMessage(player, "economy.insufficient-funds");
                return;
            }
        }
        
        // Enable flight
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Add to flying players
        flyingPlayers.add(player.getUniqueId());
        lastLocation.put(player.getUniqueId(), player.getLocation());
        
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
                Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        }
        
        plugin.getLogger().info("Flight enabled for " + player.getName());
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
        
        // Disable flight
        player.setAllowFlight(false);
        player.setFlying(false);
        
        // Remove from flying players
        flyingPlayers.remove(player.getUniqueId());
        lastLocation.remove(player.getUniqueId());
        
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
                Sound.ENTITY_BAT_TAKEOFF, 1.0f, 0.8f);
        }
        
        plugin.getLogger().info("Flight disabled for " + player.getName());
    }
    
    public void toggleFlight(Player player) {
        if (isFlying(player)) {
            disableFlight(player);
        } else {
            enableFlight(player);
        }
    }
    
    public boolean isFlying(Player player) {
        return flyingPlayers.contains(player.getUniqueId());
    }
    
    public int getRemainingFlightTime(Player player) {
        return flightTime.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void disableAllFlying() {
        for (UUID uuid : new HashSet<>(flyingPlayers)) {
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
                for (UUID uuid : new HashSet<>(flyingPlayers)) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        flyingPlayers.remove(uuid);
                        lastLocation.remove(uuid);
                        continue;
                    }
                    
                    Location currentLoc = player.getLocation();
                    Location lastLoc = lastLocation.get(uuid);
                    
                    // Only check if player moved to a different block
                    if (lastLoc == null || 
                        lastLoc.getBlockX() != currentLoc.getBlockX() ||
                        lastLoc.getBlockZ() != currentLoc.getBlockZ() ||
                        !lastLoc.getWorld().equals(currentLoc.getWorld())) {
                        
                        lastLocation.put(uuid, currentLoc);
                        
                        // Check if still in valid area
                        if (!canFly(player, currentLoc)) {
                            disableFlight(player);
                            plugin.getMessageUtil().sendMessage(player, "flight.left-claim");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Check every 2 seconds
    }
}
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