package dev.turjo.easycfly.utils;

import dev.turjo.easycfly.EasyCFly;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker {
    
    private final EasyCFly plugin;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;
    
    public UpdateChecker(EasyCFly plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }
    
    public void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // This would typically check a GitHub API or similar
                    // For now, we'll simulate an update check
                    URL url = new URL("https://api.github.com/repos/turjo/EasyCFly/releases/latest");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    
                    if (connection.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                        
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        
                        // Parse JSON response to get version
                        // This is a simplified version - in reality you'd use a JSON library
                        String jsonResponse = response.toString();
                        if (jsonResponse.contains("\"tag_name\"")) {
                            int start = jsonResponse.indexOf("\"tag_name\":\"") + 12;
                            int end = jsonResponse.indexOf("\"", start);
                            latestVersion = jsonResponse.substring(start, end);
                            
                            if (!currentVersion.equals(latestVersion)) {
                                updateAvailable = true;
                                plugin.getLogger().info("Update available! Current: " + currentVersion + 
                                    ", Latest: " + latestVersion);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void notifyPlayer(Player player) {
        if (updateAvailable) {
            plugin.getMessageUtil().sendMessage(player, "update.available", 
                "%version%", latestVersion);
            plugin.getMessageUtil().sendMessage(player, "update.current-version", 
                "%current%", currentVersion);
            plugin.getMessageUtil().sendMessage(player, "update.latest-version", 
                "%latest%", latestVersion);
        }
    }
    
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
}