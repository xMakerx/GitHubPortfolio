package mc.decodedlogic.skybattlesuspawners.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerTransactionEvent extends Event {
    
    private Player player;
    private double cost;
    private boolean cancelled;
    
    private static final HandlerList handlers = new HandlerList();
    
    public SpawnerTransactionEvent(Player player, double tCost) {
        this.player = player;
        this.cost = tCost;
    }
    
    public void setCancelled(boolean flag) {
        this.cancelled = flag;
    }
    
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public double getCost() {
        return this.cost;
    }
    
    public HandlerList getHandlers() {
        return SpawnerTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return SpawnerTransactionEvent.handlers;
    }
    
}
