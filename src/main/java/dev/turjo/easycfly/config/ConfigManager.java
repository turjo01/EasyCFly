package dev.turjo.easycfly.config;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigManager {
    
    private final EasyCFly plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration data;
    
    public ConfigManager(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // Create plugin folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Load main config
        loadConfig();
        
        // Load messages config
        loadMessages();
        
        // Load data config
        loadData();
    }
    
    public void reloadConfigs() {
        loadConfigs();
    }
    
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            saveResource("config.yml");
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load defaults
        InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(defConfigStream, java.nio.charset.StandardCharsets.UTF_8)
            );
            config.setDefaults(defConfig);
        }
    }
    
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            saveResource("messages.yml");
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Load defaults
        InputStream defMessagesStream = plugin.getResource("messages.yml");
        if (defMessagesStream != null) {
            YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(defMessagesStream, java.nio.charset.StandardCharsets.UTF_8)
            );
            messages.setDefaults(defMessages);
        }
    }
    
    private void loadData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
            }
        }
        
        data = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    private void saveResource(String resourcePath) {
        try {
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                plugin.getLogger().warning("Resource " + resourcePath + " not found in jar!");
                return;
            }
            
            File outFile = new File(plugin.getDataFolder(), resourcePath);
            Files.copy(in, outFile.toPath());
            in.close();
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + resourcePath, e);
        }
    }
    
    public void saveData() {
        try {
            data.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
        }
    }
    
    // Getters
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getMessages() {
        return messages;
    }
    
    public FileConfiguration getData() {
        return data;
    }
}