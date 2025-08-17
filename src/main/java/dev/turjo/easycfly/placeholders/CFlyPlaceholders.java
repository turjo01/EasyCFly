package dev.turjo.easycfly.placeholders;

import dev.turjo.easycfly.EasyCFly;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class CFlyPlaceholders extends PlaceholderExpansion {
    
    private final EasyCFly plugin;
    
    public CFlyPlaceholders(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "easycfly";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        switch (params.toLowerCase()) {
            case "flying":
                return plugin.getFlightManager().isFlying(player) ? 
                    plugin.getMessageUtil().getMessage("placeholder.flight-status-enabled") :
                    plugin.getMessageUtil().getMessage("placeholder.flight-status-disabled");
                    
            case "can_fly":
                return plugin.getFlightManager().canFly(player, player.getLocation()) ?
                    plugin.getMessageUtil().getMessage("placeholder.yes") :
                    plugin.getMessageUtil().getMessage("placeholder.no");
                    
            case "time_remaining":
                int time = plugin.getFlightManager().getRemainingFlightTime(player);
                return time > 0 ? String.valueOf(time) : 
                    plugin.getMessageUtil().getMessage("placeholder.unlimited");
                    
            case "cooldown":
                long cooldown = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId());
                return cooldown > 0 ? String.valueOf(cooldown) :
                    plugin.getMessageUtil().getMessage("placeholder.none");
                    
            case "trusted_count":
                return String.valueOf(plugin.getTrustManager().getTrustCount(player.getUniqueId()));
                
            case "flight_cost":
                if (plugin.getEconomyManager().isEnabled()) {
                    return plugin.getEconomyManager().formatMoney(
                        plugin.getEconomyManager().getFlightCost(player));
                }
                return plugin.getMessageUtil().getMessage("placeholder.none");
                
            default:
                return null;
        }
    }
}