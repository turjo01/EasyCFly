package dev.turjo.easycfly;

import dev.turjo.easycfly.commands.CFlyCommand;
import dev.turjo.easycfly.commands.CFlyReloadCommand;
import dev.turjo.easycfly.commands.CFlyTrustCommand;
import dev.turjo.easycfly.commands.CFlyUntrustCommand;
import dev.turjo.easycfly.commands.CFlyInfoCommand;
import dev.turjo.easycfly.config.ConfigManager;
import dev.turjo.easycfly.database.DatabaseManager;
import dev.turjo.easycfly.hooks.HookManager;
import dev.turjo.easycfly.listeners.FlightListener;
import dev.turjo.easycfly.listeners.PlayerListener;
import dev.turjo.easycfly.managers.FlightManager;
import dev.turjo.easycfly.managers.TrustManager;
import dev.turjo.easycfly.managers.CooldownManager;
import dev.turjo.easycfly.managers.EconomyManager;
import dev.turjo.easycfly.placeholders.CFlyPlaceholders;
import dev.turjo.easycfly.utils.MessageUtil;
import dev.turjo.easycfly.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * EasyCFly - Advanced Claim-Based Flying Plugin
 * 
 * @author Turjo
 * @version 2.0.0
 */
public final class EasyCFly extends JavaPlugin {
    
    private static EasyCFly instance;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private FlightManager flightManager;
    private TrustManager trustManager;
    private CooldownManager cooldownManager;
    private EconomyManager economyManager;
    private HookManager hookManager;
    
    // Utils
    private MessageUtil messageUtil;
    private UpdateChecker updateChecker;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Setup hooks
        setupHooks();
        
        // Check for updates
        checkForUpdates();
        
        // Startup message
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║           EasyCFly v" + getDescription().getVersion() + "            ║");
        getLogger().info("║     Advanced Claim Flying Plugin     ║");
        getLogger().info("║         Developer: Turjo             ║");
        getLogger().info("║                                      ║");
        getLogger().info("║    Successfully Enabled! ✓          ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }
    
    @Override
    public void onDisable() {
        // Disable all flying players
        if (flightManager != null) {
            flightManager.disableAllFlying();
        }
        
        // Close database connections
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("EasyCFly has been disabled successfully!");
    }
    
    private void initializeManagers() {
        try {
            // Configuration
            configManager = new ConfigManager(this);
            configManager.loadConfigs();
            
            // Message utility
            messageUtil = new MessageUtil(this);
            
            // Database
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            
            // Core managers
            trustManager = new TrustManager(this);
            cooldownManager = new CooldownManager(this);
            economyManager = new EconomyManager(this);
            flightManager = new FlightManager(this);
            
            // Hook manager
            hookManager = new HookManager(this);
            
            getLogger().info("All managers initialized successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize managers!", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    private void registerCommands() {
        getCommand("cfly").setExecutor(new CFlyCommand(this));
        getCommand("cflyreload").setExecutor(new CFlyReloadCommand(this));
        getCommand("cflytrust").setExecutor(new CFlyTrustCommand(this));
        getCommand("cflyuntrust").setExecutor(new CFlyUntrustCommand(this));
        getCommand("cflyinfo").setExecutor(new CFlyInfoCommand(this));
        
        getLogger().info("Commands registered successfully!");
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new FlightListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("Event listeners registered successfully!");
    }
    
    private void setupHooks() {
        hookManager.setupHooks();
        
        // PlaceholderAPI
        if (hookManager.isPlaceholderAPIEnabled()) {
            new CFlyPlaceholders(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
    }
    
    private void checkForUpdates() {
        if (configManager.getConfig().getBoolean("settings.check-updates", true)) {
            updateChecker = new UpdateChecker(this);
            updateChecker.checkForUpdates();
        }
    }
    
    public void reload() {
        try {
            configManager.reloadConfigs();
            messageUtil.reload();
            getLogger().info("EasyCFly reloaded successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload EasyCFly!", e);
        }
    }
    
    // Getters
    public static EasyCFly getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public FlightManager getFlightManager() {
        return flightManager;
    }
    
    public TrustManager getTrustManager() {
        return trustManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public HookManager getHookManager() {
        return hookManager;
    }
    
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
    
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}