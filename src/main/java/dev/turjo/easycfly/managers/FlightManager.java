package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final Map<UUID, Long> fallProtectionEnd;
    private final Map<String, Boolean> claimCache;
    private final Map<String, Long> cacheExpiry;
    
    public FlightManager(EasyCFly plugin) {
        this.plugin = plugin;
        this.flyingPlayers = ConcurrentHashMap.newKeySet();
        this.flightTasks = new HashMap<>();
        this.flightTime = new HashMap<>();
        this.lastLocation = new HashMap<>();
        this.fallProtectionEnd = new ConcurrentHashMap<>();
        this.claimCache = new ConcurrentHashMap<>();
        this.cacheExpiry = new ConcurrentHashMap<>();
        
        startFlightChecker();
        startFallProtectionChecker();
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
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId());
            plugin.getMessageUtil().sendMessage(player, "flight.cooldown-active", 
                "%time%", String.valueOf(remaining));
            return false;
        }
        
        // Check economy
        if (plugin.getEconomyManager().isEnabled() && 
            !plugin.getEconomyManager().hasEnoughMoney(player) &&
            !player.hasPermission("easycfly.bypass.cost")) {
            double cost = plugin.getEconomyManager().getFlightCost(player);
            plugin.getMessageUtil().sendMessage(player, "economy.insufficient-funds", 
                "%cost%", plugin.getEconomyManager().formatMoney(cost));
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
        
        // Apply feather falling protection if enabled and player was flying
        if (sendMessage && plugin.getConfigManager().getConfig().getBoolean("flight.feather-falling-on-leave.enabled", true)) {
            applyFeatherFallingProtection(player);
        }
        
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
    
    private void applyFeatherFallingProtection(Player player) {
        int duration = plugin.getConfigManager().getConfig().getInt("flight.feather-falling-on-leave.duration", 10);
        int amplifier = plugin.getConfigManager().getConfig().getInt("flight.feather-falling-on-leave.amplifier", 4);
        
        // Apply slow falling effect
        PotionEffect slowFalling = new PotionEffect(PotionEffectType.SLOW_FALLING, duration * 20, amplifier, false, false);
        player.addPotionEffect(slowFalling);
        
        // Store protection end time for fall damage prevention
        fallProtectionEnd.put(player.getUniqueId(), System.currentTimeMillis() + (duration * 1000L));
        
        // Visual effect
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        
        // Sound effect
        if (plugin.getConfigManager().getConfig().getBoolean("sounds.enabled")) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.5f);
        }
        
        plugin.getMessageUtil().sendMessage(player, "flight.fall-damage-protected", 
            "%time%", String.valueOf(duration));
    }
    
    private void startFallProtectionChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                fallProtectionEnd.entrySet().removeIf(entry -> currentTime >= entry.getValue());
            }
        }.runTaskTimer(plugin, 200L, 200L); // Clean up every 10 seconds
    }
    
    public boolean hasFallProtection(Player player) {
        Long protectionEnd = fallProtectionEnd.get(player.getUniqueId());
        if (protectionEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= protectionEnd) {
            fallProtectionEnd.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    private void startFlightChecker() {
        int checkInterval = plugin.getConfigManager().getConfig().getInt("flight.performance.location-check-interval", 40);
        int batchSize = plugin.getConfigManager().getConfig().getInt("flight.performance.batch-size", 10);
        boolean useCache = plugin.getConfigManager().getConfig().getBoolean("flight.performance.cache-claim-lookups", true);
        
        new BukkitRunnable() {
            private int playerIndex = 0;
            
            @Override
            public void run() {
                List<UUID> playerList = new ArrayList<>(flyingPlayers);
                if (playerList.isEmpty()) return;
                
                // Process players in batches for better performance
                int endIndex = Math.min(playerIndex + batchSize, playerList.size());
                
                for (int i = playerIndex; i < endIndex; i++) {
                    UUID uuid = playerList.get(i);
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
                        boolean canFlyHere = canFly(player, currentLoc);
                        
                        if (!canFlyHere) {
                            disableFlight(player);
                            plugin.getMessageUtil().sendMessage(player, "flight.left-claim");
                        }
                    }
                }
                
                // Move to next batch
                playerIndex = endIndex;
                if (playerIndex >= playerList.size()) {
                    playerIndex = 0; // Reset to beginning
                }
            }
        }.runTaskTimer(plugin, checkInterval, checkInterval);
    }
}