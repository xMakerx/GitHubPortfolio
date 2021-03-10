package mc.decodedlogic.craftrp.mouse;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter.HoldMode;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class UserTracker {
    
    protected final HeldMouseAdapter ADAPTER;
    protected final CRPPlayer PLAYER;
    protected final PlayerInteractEvent INITIAL_EVT;
    protected final long DOWN_BEGIN_MS;
    
    protected long lastDownMs;
    protected int downEvtsFired;
    protected boolean chargingUp;
    protected double power;
    
    // The task to continue to process if the mouse is down.
    private BukkitTask processTask;
    
    public UserTracker(HeldMouseAdapter adapter, CRPPlayer player, PlayerInteractEvent intEvt) {
        this(adapter, player, intEvt, System.currentTimeMillis());
    }
    
    public UserTracker(HeldMouseAdapter adapter, 
            CRPPlayer player, PlayerInteractEvent intEvt, long downBeginMs) {
        this.ADAPTER = adapter;
        this.PLAYER = player;
        this.PLAYER.setMouseTracker(this);
        this.INITIAL_EVT = intEvt;
        
        this.DOWN_BEGIN_MS = downBeginMs;
        this.lastDownMs = downBeginMs;
        this.downEvtsFired = 0;
        this.chargingUp = true;
        this.power = 0;
        
        this.processTask = null;
    }
    
    public void start() {
        stop();
        
        long ticks = ADAPTER.processTaskStepTicks;
        UserTracker tracker = this;
        
        processTask = new BukkitRunnable() {
            
            public void run() {
                long now = System.currentTimeMillis();
                long timeDownNeeded = ADAPTER.desiredDownTimeMs;
                long elapsedFromStart = now - DOWN_BEGIN_MS;
                
                if(now > (lastDownMs + ADAPTER.cancelAfterTimeMs)) {
                    reset();
                    ADAPTER.LISTENER.onMouseReleased(PLAYER, tracker);
                    return;
                }else {
                    downEvtsFired++;
                    
                    boolean fullyCharged = (elapsedFromStart > timeDownNeeded);
                    
                    if(fullyCharged) {
                        ADAPTER.LISTENER.onFullChargeObtained(PLAYER, tracker);
                        
                        if(ADAPTER.mode == HoldMode.ACTION_ON_FILL) {
                            reset();
                            return;
                        }
                    }
                    
                    double perct = getPercentage();
                    power = (chargingUp) ? perct : 1.0-perct;
                    ADAPTER.LISTENER.onMouseDown(PLAYER, tracker);
                }
            }
            
        }.runTaskTimer(CraftRP.get(), ticks, ticks);
    }
    
    public void stop() {
        if(processTask != null) {
            processTask.cancel();
            processTask = null;
        }
    }
    
    public void reset() {
        stop();
        PLAYER.setMouseTracker(null);
        ADAPTER.trackers.remove(PLAYER.getPlayer().getUniqueId());
    }
    
    public void sendChargeBar(String barTitle, int numBars, long elapsedFromStart) {
        double perct = getPercentage();
        int progressBars = (int) (((double) numBars) * perct);
        int greenBars = (chargingUp) ? progressBars : numBars-progressBars;
        int redBars = (chargingUp) ? numBars-greenBars : progressBars;
        
        String msg = "";
        
        for(int i = 0; i < greenBars; i++) {
            msg = msg.concat(ChatColor.GREEN + "|");
        }
        
        for(int i = 0; i < redBars; i++) {
            msg = msg.concat(ChatColor.RED + "|");
        }
        
        PLAYER.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(barTitle + ": " + msg));
    }
    
    public double getPower() {
        return power;
    }
    
    public double getPercentage() {
        long now = System.currentTimeMillis();
        long elapsedFromStart = now - DOWN_BEGIN_MS;
        long phaseTime = getPhaseTime(elapsedFromStart);
        
        double perct = ((double) phaseTime) / ((double) ADAPTER.desiredDownTimeMs);
        perct = (perct > 1.0) ? 1.0 : perct;
        
        return perct;
    }
    
    public long getPhaseTime(long elapsedFromStart) {
        long phaseTime = elapsedFromStart;
        long timeDownNeeded = ADAPTER.desiredDownTimeMs;
        
        if(phaseTime > timeDownNeeded && ADAPTER.mode == HoldMode.POWER_BAR) {
            phaseTime -= timeDownNeeded;
            
            long phases = (phaseTime/timeDownNeeded);
            chargingUp = !(phases % 2 == 0);
            
            phaseTime -= phases * timeDownNeeded;
        }
        
        return phaseTime;
    }
    
    public HeldMouseAdapter getAdapter() {
        return this.ADAPTER;
    }
    
    public CRPPlayer getPlayer() {
        return this.PLAYER;
    }
    
    public long getDownBeginTime() {
        return this.DOWN_BEGIN_MS;
    }
    
    public PlayerInteractEvent getInitialEvent() {
        return this.INITIAL_EVT;
    }
    
    public long getLastDownTime() {
        return this.lastDownMs;
    }
    
    public int getDownEventsFired() {
        return this.downEvtsFired;
    }
    
    public boolean isChargingUp() {
        return this.chargingUp;
    }
    
}
