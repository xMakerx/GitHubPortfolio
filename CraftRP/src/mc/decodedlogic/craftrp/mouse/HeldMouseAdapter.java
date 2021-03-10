package mc.decodedlogic.craftrp.mouse;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import mc.decodedlogic.craftrp.Globals;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class HeldMouseAdapter {
    
    public enum HoldMode {
        ACTION_ON_FILL,
        POWER_BAR,
        CHARGE_AND_WAIT
    }
    
    protected final MouseListener LISTENER;
    protected HoldMode mode;
    protected long desiredDownTimeMs;
    protected long processTaskStepTicks;
    protected long cancelAfterTimeMs;
    protected HashMap<UUID, UserTracker> trackers;
    
    public HeldMouseAdapter(MouseListener listener) {
        this.LISTENER = listener;
        this.mode = HoldMode.ACTION_ON_FILL;
        this.desiredDownTimeMs = 0L;
        this.processTaskStepTicks = 1L;
        this.cancelAfterTimeMs = Globals.MILLISECONDS_PER_TICK*4;
        this.trackers = new HashMap<UUID, UserTracker>();
    }
    
    public void handleDown(CRPPlayer player, PlayerInteractEvent evt) {
        UserTracker tracker = player.getMouseTracker();
        
        if(tracker == null) {
            tracker = new UserTracker(this, player, evt);
            trackers.put(player.getPlayer().getUniqueId(), tracker);
            tracker.start();
        }else {
            PlayerInteractEvent initEvt = tracker.getInitialEvent();
            
            if(evt.getAction() == initEvt.getAction() && evt.getHand() == initEvt.getHand()) {
                tracker.lastDownMs = System.currentTimeMillis();
            }
        }
    }
    
    public void endAllTrackers() {
        for(UserTracker tracker : trackers.values()) {
            tracker.stop();
        }
        
        trackers.clear();
    }
    
    public MouseListener getListener() {
        return this.LISTENER;
    }
    
    public void setHoldMode(HoldMode newMode) {
        this.mode = newMode;
    }
    
    public HoldMode getHoldMode() {
        return this.mode;
    }
    
    public void setDesiredDownTimeMilliseconds(long milliseconds) {
        this.desiredDownTimeMs = milliseconds;
    }
    
    public long getDesiredDownTimeMilliseconds() {
        return this.desiredDownTimeMs;
    }
    
    public void setDesiredDownTimeSeconds(double seconds) {
        this.desiredDownTimeMs = Globals.secondsToMilliseconds(seconds);
    }
    
    public double getDesiredDownTimeSeconds() {
        return Globals.millisecondsToSeconds(desiredDownTimeMs);
    }
    
    public void setProcessTaskStepTicks(long ticks) {
        this.processTaskStepTicks = ticks;
    }
    
    public long getProcessTaskStepTicks() {
        return this.processTaskStepTicks;
    }
    
    public void setCancelAfterTimeMilliseconds(long milliseconds) {
        this.cancelAfterTimeMs = milliseconds;
    }
    
    public long getCancelAfterTimeMilliseconds() {
        return this.cancelAfterTimeMs;
    }
    
    public UserTracker getTrackerFor(UUID uuid) {
        return (trackers.containsKey(uuid)) ? trackers.get(uuid) : null;
    }
    
    public UserTracker getTrackerFor(CRPPlayer player) {
        UserTracker tracker = null;
        
        if(player != null) {
            tracker = getTrackerFor(player.getPlayer().getUniqueId());
        }
        
        return tracker;
    }
    
    public UserTracker getTrackerFor(Player player) {
        UserTracker tracker = null;
        
        if(player != null) {
            tracker = getTrackerFor(player.getUniqueId());
        }
        
        return tracker;
    }
    
}
