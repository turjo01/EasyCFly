package dev.turjo.easycfly.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFlightToggleEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean enabled;
    private boolean cancelled = false;
    
    public PlayerFlightToggleEvent(Player player, boolean enabled) {
        this.player = player;
        this.enabled = enabled;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}