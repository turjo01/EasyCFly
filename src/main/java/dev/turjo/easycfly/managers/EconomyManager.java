package dev.turjo.easycfly.managers;

import dev.turjo.easycfly.EasyCFly;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    
    private final EasyCFly plugin;
    private Economy economy;
    private boolean enabled;
    
    public EconomyManager(EasyCFly plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found, economy features disabled.");
            enabled = false;
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
            .getRegistration(Economy.class);
        
        if (rsp == null) {
            plugin.getLogger().info("No economy plugin found, economy features disabled.");
            enabled = false;
            return;
        }
        
        economy = rsp.getProvider();
        enabled = plugin.getConfigManager().getConfig().getBoolean("economy.enabled", false);
        
        if (enabled) {
            plugin.getLogger().info("Economy integration enabled with " + economy.getName());
        }
    }
    
    public boolean isEnabled() {
        return enabled && economy != null;
    }
    
    public boolean hasEnoughMoney(Player player) {
        if (!isEnabled()) {
            return true;
        }
        
        double cost = getFlightCost(player);
        return economy.getBalance(player) >= cost;
    }
    
    public boolean chargeFlight(Player player) {
        if (!isEnabled()) {
            return true;
        }
        
        double cost = getFlightCost(player);
        if (cost <= 0) {
            return true;
        }
        
        if (!hasEnoughMoney(player)) {
            return false;
        }
        
        economy.withdrawPlayer(player, cost);
        plugin.getMessageUtil().sendMessage(player, "economy.charged", 
            "%cost%", economy.format(cost));
        
        return true;
    }
    
    public double getFlightCost(Player player) {
        if (!isEnabled()) {
            return 0;
        }
        
        // Check for permission-based costs
        for (String permission : plugin.getConfigManager().getConfig()
            .getConfigurationSection("economy.costs").getKeys(false)) {
            
            if (player.hasPermission("easycfly.cost." + permission)) {
                return plugin.getConfigManager().getConfig()
                    .getDouble("economy.costs." + permission, 0);
            }
        }
        
        // Default cost
        return plugin.getConfigManager().getConfig().getDouble("economy.default-cost", 10.0);
    }
    
    public double getBalance(Player player) {
        if (!isEnabled()) {
            return 0;
        }
        
        return economy.getBalance(player);
    }
    
    public String formatMoney(double amount) {
        if (!isEnabled()) {
            return String.valueOf(amount);
        }
        
        return economy.format(amount);
    }
}