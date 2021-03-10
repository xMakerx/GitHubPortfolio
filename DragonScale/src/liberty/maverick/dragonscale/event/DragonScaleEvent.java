package liberty.maverick.dragonscale.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is a base class for custom events created and operated by this
 * plugin.
 */

public abstract class DragonScaleEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
