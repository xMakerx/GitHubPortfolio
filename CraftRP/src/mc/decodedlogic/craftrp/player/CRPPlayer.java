package mc.decodedlogic.craftrp.player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.Globals;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.event.PlayerChangeJobEvent;
import mc.decodedlogic.craftrp.event.PlayerFocusEntityEvent;
import mc.decodedlogic.craftrp.event.PlayerWantedRatingChangeEvent;
import mc.decodedlogic.craftrp.gui.RPGUI;
import mc.decodedlogic.craftrp.gui.StatsScoreboard;
import mc.decodedlogic.craftrp.hologram.HologramContainer;
import mc.decodedlogic.craftrp.hologram.RPHologram;
import mc.decodedlogic.craftrp.item.CRPItem;
import mc.decodedlogic.craftrp.job.Job;
import mc.decodedlogic.craftrp.mouse.UserTracker;
import mc.decodedlogic.craftrp.ownable.Owner;
import mc.decodedlogic.craftrp.ownable.Property;
import mc.decodedlogic.craftrp.particle.ParticleEffects;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import mc.decodedlogic.craftrp.util.MessageUtils;

public class CRPPlayer implements CRPRegistrableObject, Owner {
    
    public static final int PROP_LIMIT = 50;
    public static final int MAX_WANTED_RATING = 100;
    public static final int WANTED_RATING_DECLINE_PER_STEP = 3;
    public static final long WANTED_RATING_DECLINE_TICKS = 400L;
    public static final int MAX_FOCUS_DISTANCE = 20;
    
    private final Player BUKKIT_PLAYER;
    private final PlayerPointer POINTER;
    
    protected Job job;
    protected int wantedRating;
    protected StatsScoreboard statsBoard;
    
    protected CRPEntity lastFocusedOn;
    
    protected HashSet<RPHologram> visibleHolograms;
    
    protected long id;
    
    protected UserTracker curMouseTracker;
    
    protected RPGUI currentGUI;
    
    protected CRPItem ITEM_held;
    
    // Timers
    protected BukkitTask wantedRtgTimer;
    protected BukkitTask heartbeatTimer;
    protected long lastDrawRing;
    
    protected BukkitTask helpTask;
    
    protected HashSet<Property> properties;
    
    public CRPPlayer(Player p) {
        this.BUKKIT_PLAYER = p;
        this.POINTER = new PlayerPointer(p);
        this.job = Job.CITIZEN;
        this.wantedRating = 0;
        this.statsBoard = new StatsScoreboard(this);
        
        this.lastFocusedOn = null;
        this.visibleHolograms = new HashSet<RPHologram>();
        this.id = -1;
        
        this.curMouseTracker = null;
        
        this.currentGUI = null;
        
        this.ITEM_held = null;
        
        this.wantedRtgTimer = null;
        this.heartbeatTimer = null;
        this.lastDrawRing = 0;
        this.helpTask = null;
        this.properties = new HashSet<Property>();
        this.setupHeartbeatTimer();
    }
    
    public void clearHeartbeatTimer() {
        if(heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }
    
    public void setupHeartbeatTimer() {
        clearHeartbeatTimer();
        
        heartbeatTimer = new BukkitRunnable() {
            
            public void run() {
                
                double perct = ((double) wantedRating) / ((double) MAX_WANTED_RATING);
                
                if((System.currentTimeMillis() > lastDrawRing + 800) && perct >= 0.9) {
                    
                    lastDrawRing = ParticleEffects.drawDustCircle(BUKKIT_PLAYER.getLocation(), 
                            Color.RED, 1, 1.05f, 0, 
                            0.0f, 0, 2).getDrawTime();
                }
            }
            
            
        }.runTaskTimer(CraftRP.get(), 1L, 1L);
    }
    
    public void setJob(Job newJob, boolean silent) {
        Job lastJob = job;
        
        if(lastJob != newJob && !silent) {
            // Let's call our event.
            PlayerChangeJobEvent evt = new PlayerChangeJobEvent(this, newJob, lastJob);
            CraftRP.get().getServer().getPluginManager().callEvent(evt);
            
            if(evt.isCancelled()) return;
        }
        
        this.job = newJob;
        this.statsBoard.update();
    }
    
    public void setJob(Job newJob) {
        setJob(newJob, false);
    }
    
    public Job getJob() {
        return this.job;
    }
    
    public void setWantedRating(int newRating, boolean silent) {
        int oldRating = wantedRating;
        newRating = (newRating < 0) ? 0 : newRating;
        
        if(oldRating == newRating) return;
        
        if(!silent) {
            // Let's call our event.
            PlayerWantedRatingChangeEvent evt = new PlayerWantedRatingChangeEvent(this, wantedRating, oldRating);
            CraftRP.get().getServer().getPluginManager().callEvent(evt);
            
            if(evt.isCancelled()) return;
        }
        
        this.wantedRating = (newRating >= 0 && newRating <= MAX_WANTED_RATING) ? newRating : wantedRating;
        this.statsBoard.update();
        
        if(newRating > oldRating) {
            BUKKIT_PLAYER.playSound(BUKKIT_PLAYER.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 2F, 0.8F);
        }
        
        if(wantedRating == 0) {
            clearWantedRatingTimer();
        }else if(wantedRtgTimer == null) {
            setupWantedRatingTimer();
        }
        
    }
    
    public void setWantedRating(int newRating) {
        setWantedRating(newRating, false);
    }
    
    public int getWantedRating() {
        return this.wantedRating;
    }
    
    public void clearWantedRatingTimer() {
        if(wantedRtgTimer != null) {
            wantedRtgTimer.cancel();
            wantedRtgTimer = null;
        }
    }
    
    public void setupWantedRatingTimer() {
        clearWantedRatingTimer();
        
        if(wantedRating > 0) {
            wantedRtgTimer = new BukkitRunnable() {
                
                public void run() {
                    setWantedRating(wantedRating - WANTED_RATING_DECLINE_PER_STEP);
                }
                
            }.runTaskTimer(CraftRP.get(), 
                    WANTED_RATING_DECLINE_TICKS, 
                    WANTED_RATING_DECLINE_TICKS);
        }
    }
    
    public StatsScoreboard getStatsScoreboard() {
        return this.statsBoard;
    }
    
    public HashSet<RPHologram> getCRPEntityHolograms(CRPEntity entity) {
        HashSet<RPHologram> holograms = new HashSet<RPHologram>();
        
        for(RPHologram hologram : visibleHolograms) {
            HologramContainer container = hologram.getContainer();
            
            if(container != null && container.getParent() == entity) {
                holograms.add(hologram);
            }
        }
        
        return holograms;
    }
    
    public RPHologram getHologramByName(String name) {
        Optional<RPHologram> hologram = visibleHolograms.stream().filter(h -> h.getName().equalsIgnoreCase(name)).findFirst();
        
        if(hologram.isPresent()) {
            return (RPHologram) hologram.get();
        }
        
        return null;
    }
    
    public void addVisibleHologram(RPHologram hologram) {
        this.visibleHolograms.add(hologram);
    }
    
    public void removeVisibleHologram(RPHologram hologram) {
        if(hologram != null && visibleHolograms.contains(hologram)) {
            this.visibleHolograms.remove(hologram);
        }
    }
    
    public HashSet<RPHologram> getVisibleHolograms() {
        return this.visibleHolograms;
    }
    
    private void unfocusHolograms(CRPEntity possibleHGParent) {
        Iterator<RPHologram> iterator = visibleHolograms.iterator();
        
        while(iterator.hasNext()) {
            RPHologram hologram = iterator.next();
            HologramContainer container = hologram.getContainer();
            
            if(container != null && container.getParent() == possibleHGParent) {
                hologram.hideFrom(this, false);
                iterator.remove();
            }
        }
    }
    
    public void startHelpInfoTask() {
        final CRPPlayer F_PLAYER = this;
        long ticks = (long) Globals.millisecondsToTicks(Globals.DISPLAY_HELP_MENU_TIME);
        
        clearHelpInfoTask();
        helpTask = new BukkitRunnable() {
            
            public void run() {
                String helpInfo = lastFocusedOn.getHelpInformation(F_PLAYER);
                
                if(helpInfo != null && !helpInfo.isEmpty()) {
                    MessageUtils.sendActionBarMessage(BUKKIT_PLAYER, helpInfo, Globals.SOUND_POP_UP);
                }
            }
            
        }.runTaskTimer(CraftRP.get(), ticks, 
                ticks);
    }
    
    public void clearHelpInfoTask() {
        if(helpTask != null) {
            helpTask.cancel();
            helpTask = null;
        }
    }
    
    public void setFocusedEntity(CRPEntity entity) {
        if(entity != lastFocusedOn) {
            
            PlayerFocusEntityEvent focusEvt = new PlayerFocusEntityEvent(this, entity, lastFocusedOn);
            CraftRP.get().getServer().getPluginManager().callEvent(focusEvt);
            
            if(focusEvt.isCancelled()) return;
            
            if(lastFocusedOn != null) {
                lastFocusedOn.onPlayerFocusLost(this);
                unfocusHolograms(lastFocusedOn);
                clearHelpInfoTask();
            }
            
            this.lastFocusedOn = entity;
            
            if(entity != null) {
                startHelpInfoTask();
                entity.onPlayerFocusGained(this);
            }
            
        }else if(lastFocusedOn != null) {
            getCRPEntityHolograms(lastFocusedOn).stream().forEach(h -> h.update(this));
        }
    }
    
    public CRPEntity getFocusedEntity() {
        return this.lastFocusedOn;
    }

    @Override
    public void onRegistered(long assignedID) {
        this.id = assignedID;
        
        BUKKIT_PLAYER.setMetadata(CRPPlayerManager.METADATA_CRPPLAYER_ID_KEY, 
                new FixedMetadataValue(CraftRP.get(), id));
    }

    @Override
    public void onUnregistered() {
        BUKKIT_PLAYER.removeMetadata(CRPPlayerManager.METADATA_CRPPLAYER_ID_KEY, CraftRP.get());
        clearWantedRatingTimer();
        clearHeartbeatTimer();
        clearHelpInfoTask();
        
        if(curMouseTracker != null) {
            curMouseTracker.stop();
            curMouseTracker = null;
        }
    }
    
    public void setMouseTracker(UserTracker tracker) {
        this.curMouseTracker = tracker;
    }
    
    public UserTracker getMouseTracker() {
        return this.curMouseTracker;
    }
    
    public void setCurrentGUI(RPGUI gui) {
        this.currentGUI = gui;
    }
    
    public RPGUI getCurrentGUI() {
        return this.currentGUI;
    }
    
    public void setHeldItem(CRPItem baseItem) {
        if(baseItem == ITEM_held) return;
        
        if(ITEM_held != null) {
            ITEM_held.onUnequip();
        }
        
        if(baseItem != null) {
            try {
                this.ITEM_held = baseItem.getClass().newInstance();
                this.ITEM_held.setUser(this);
            } catch (InstantiationException | IllegalAccessException e) {   
                e.printStackTrace();
            }
            
            return;
        }
        
        this.ITEM_held = baseItem;
    }
    
    public CRPItem getHeldItem() {
        return this.ITEM_held;
    }
    
    public Player getPlayer() {
        return this.BUKKIT_PLAYER;
    }
    
    public PlayerPointer getPointer() {
        return this.POINTER;
    }

    public long getID() {
        return this.id;
    }

    public HashSet<Property> getProperties() {
        return this.properties;
    }
    
    public String getName() {
        return this.BUKKIT_PLAYER.getDisplayName();
    }
    
}
