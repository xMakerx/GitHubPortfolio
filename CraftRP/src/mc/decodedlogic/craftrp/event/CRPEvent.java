package mc.decodedlogic.craftrp.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class CRPEvent extends Event implements Cancellable {
    
    protected static final HandlerList HANDLERS_LIST = new HandlerList();
    
    protected boolean cancelled;
    
    public CRPEvent() {
        this.cancelled = false;
    }
    
    public void setCancelled(boolean flag) {
        this.cancelled = flag;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }

    public HandlerList getHandlers() {
        return CRPEvent.HANDLERS_LIST;
    }
    
    public static HandlerList getHandlerList() {
        return CRPEvent.HANDLERS_LIST;
    }

}
