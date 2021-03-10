package mc.decodedlogic.skybattlesuspawners.spawner;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.skybattlesuspawners.IPluginObject;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.USpawnersLogger;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerUpdateEvent;
import mc.decodedlogic.skybattlesuspawners.stack.Stackable;

public class MobSpawner extends Stackable implements IPluginObject {
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // Static methods, variables, etc
    ////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Fetches all the {@link MobSpawner}s occupying the specified {@link Island}
     * @param island - The Island that the spawners call home.
     * @return
     */
    
    public static List<MobSpawner> fromIsland(@Nonnull Island island) {
        List<MobSpawner> spawners = SpawnerManager.getMobSpawners().stream()
                .filter(s -> (s.getIsland() != null && s.getIsland().equals(island)))
                .collect(Collectors.toList());
        
        return spawners;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    
    enum State {
        LOADING, IDLE, DELETED
    }
	
	private Island island;
	private SpawnerType type;
	private Location location;
	private CreatureSpawner spawner;
	private int id;
	
	private USpawnersLogger notify;
	
	private SpawnerUpgrade upgrade;
	private Hologram hologram;
	
	private State state = State.LOADING;
	
	private long allowSpawnAfterMs;
	
	public MobSpawner(Location location) {
	    this(location, -1, -1, 1);
	    
	    double delaySecs = USpawners.get().getSettings().getFirstSpawnDelaySeconds();
	    long ms = (long) (delaySecs * 1000);
	    
	    this.allowSpawnAfterMs = System.currentTimeMillis() + ms;
	}
	
	public MobSpawner(Location loc, int id, int upgradeIndex, int size) {
	    super(size, USpawners.get().getSettings().getSpawnerStackLimit());
	    boolean isNew = id == -1;
	    
	    this.notify = new USpawnersLogger("MobSpawner");
	    this.id = (!isNew) ? id : SpawnerManager.registerNew(this);
	    this.location = loc;
        this.island = Utils.getIslandAt(location);
        this.hologram = null;
	    
	    // Let's fetch the creature spawner
	    Block b = loc.getBlock();
	    
	    if(!(b.getState() instanceof CreatureSpawner)) {
	           // This isn't a spawner, scream!
            throw new IllegalArgumentException(String.format("Block @ %s is not a CreatureSpawner!", loc.toString()));
	    }
	    
        this.spawner = (CreatureSpawner) b.getState();
        this.type = SpawnerType.getTypeFromEntityType(spawner.getSpawnedType());
        
        if(type == null) {
            this.notify.error(String.format("Unknown SpawnerType definition for EntityType %s.", 
                    spawner.getSpawnedType().name()));
            return;
        }
        
        // Let's handle upgrades
        this.upgrade = SpawnerUpgrade.DEFAULT;
        
        if(upgradeIndex != -1) {
            // Upgrade index isn't -1, this means this spawner should be upgraded.
            try {
                this.upgrade = type.getUpgrades().get(upgradeIndex);
            } catch (IndexOutOfBoundsException e) {
                this.notify.error(String.format("Upgrade Index: %d no longer exists. Using default.", 
                        upgradeIndex));
            }
        }
	    
	    if(isNew) save();
	    SpawnerManager.register(this);
	    this.update();
	    
	    this.state = State.IDLE;
	    this.allowSpawnAfterMs = System.currentTimeMillis();
	}
	
	/**
	 * Checks whether or not the specified {@link MobSpawner} is similar enough
	 * to this one allowing deposits.
	 * @param MobSpawner other - The spawner to check
	 * @return
	 */
	
	public boolean isInstanceOf(MobSpawner other) {
	    return (other != null && (other.type == type && other.upgrade.equals(upgrade)));
	}
	
	/**
	 * Verifies that the spawner hasn't been compromised and "updates"
	 * the hologram displayed above it.
	 */
	
	public void update() {
	    // Let's make sure that this spawner hasn't been compromised.
		if(compromised()) {
		    boolean setToAir = (location.getBlock() != null 
		            && location.getBlock().getType() == Material.MOB_SPAWNER);
		    delete(setToAir);
		    return;
		}
		
		USpawners main = USpawners.get();
		
		if(main.getHolographicDisplays() != null) makeHologram(main);
		main.callEvent(new MobSpawnerUpdateEvent(this));
	}
	
	/**
	 * Creates the hologram that is displayed above the spawner.
	 * @param {@link USpawners} Main plugin instance
	 */
	
	private void makeHologram(USpawners main) {
	    deleteHologram();
	    
	    this.hologram = HologramsAPI.createHologram(main, getHologramLocation());
	    
	    String title = main.getSettings().getSpawnerName();
	    
	    // Let's display the correct size
	    title = Utils.replaceVariableWith(title, "amount", size);
	    
	    // Let's display the correct spawner type
	    title = Utils.replaceVariableWith(title, "spawnerType", getTypeName(true));
	    
	    // Let's display the upgrade
	    title = Utils.replaceVariableWith(title, "upgrade", upgrade.getDisplayName());

	    this.hologram.insertTextLine(0, Utils.mkDisplayReady(title));
	}
	
	/**
	 * Deletes the hologram that is displayed above the spawner.
	 */
	
	private void deleteHologram() {
	    if(hologram != null) {
	        hologram.delete();
	        hologram = null;
	    }
	}
	
	/**
	 * Fetches the {@link Location} for the spawner hologram.
	 * @return
	 */
	
	private Location getHologramLocation() {
	    double xOffset = 0.5;
	    double yOffset = 1.5;
	    double zOffset = 0.5;
	    
	    return location.clone().add(xOffset, yOffset, zOffset);
	}
	
	/**
	 * Checks whether or not this spawner has been "compromised" meaning
	 * the stack has been set to 0, the spawner block is no longer there, or
	 * the spawner type is suddenly incorrect.
	 * @return
	 */
	
	private boolean compromised() {
	    // If the id hasn't been set, we haven't been created yet or were destroyed.
	    if(state != State.IDLE) return false;
	    
        Block b = location.getBlock();
	    boolean compromised = empty() || b == null;
	    
	    if(!compromised) {
	        if(b != null && b.getState() instanceof CreatureSpawner) {
	            CreatureSpawner s = (CreatureSpawner) b.getState();
	            compromised = !(s.getSpawnedType() == type.getEntityType());
	        }else {
	            compromised = true;
	        }
	    }
	    
	    return compromised;
	}
	
	public void sizeChanged(int oldSize, int newSize) {
	    if(state == State.IDLE && compromised()) delete(true);
	}
	
	/**
	 * Deletes the hologram (if it exists) and cleans up our references.
	 * 
	 * NOTE: <b>This does not call {@link SpawnerManager#unregister(MobSpawner)}!!!</b>
	 */
	
	public void cleanup() {
	    if(state == State.IDLE) {
	        notify.debug(String.format("Cleaning up MobSpawner @ %s", location.toString()));

	        deleteHologram();
    		island = null;
    		type = null;
    		location = null;
    		spawner = null;
    		id = -1;
    		notify = null;
    		upgrade = null;
	    }
	}
	
	/**
	 * Resets this spawner's upgrade to Default, invokes update, and saves
	 * changes to the disk.
	 */
	
	public void reset() {
	    this.setUpgrade(SpawnerUpgrade.DEFAULT);
	    this.update();
	    
	    // Let's save our changes
	    this.save();
	}
	
	/**
	 * Tells the SpawnerManager to update and save this spawner's information to the disk.
	 */
	
	public void save() {
	    SpawnerManager.storeSpawnerData(this);
	}
	
	/**
	 * Deletes this {@link MobSpawner} instance, including its data on the disk, and unregisters it from the
	 * {@link SpawnerManager}. This is only successful if spawner is in the idle state.
	 * @param setToAir - If true, will set the physical {@link CreatureSpawner} to AIR.
	 * 
	 * NOTE: Upon a successful call, this instance is <b>not</b> reusable.
	 */
	
	public void delete(boolean setToAir) {
	    if(this.state == State.IDLE) {
    		SpawnerManager.deleteSpawnerFile(SpawnerManager.getSpawnerFile(this));
    		SpawnerManager.unregister(this);
    		
            if(setToAir && location != null) {
                Block b = location.getBlock();
                
                if(b != null) {
                    new BukkitRunnable() {
                        
                        public void run() {
                            b.setType(Material.AIR);
                        }
                        
                    }.runTaskLater(USpawners.get(), 1L);
                }
            }
            
    		this.cleanup();
    		this.size = 0;
    		this.state = State.DELETED;
	    }
	}
	
	/**
	 * Fetches the {@link Island} this MobSpawner is a part of.
	 * @return
	 */
	
	public Island getIsland() {
		return this.island;
	}
	
	/**
	 * Fetches the {@link SpawnerType} of this spawner. SpawnerType is similar to
	 * {@link EntityType} but it stores more information such as available upgrades.
	 */
	
	public SpawnerType getType() {
		return this.type;
	}
	
	public String getTypeName(boolean allCaps) {
	    String entityName = type.getEntityType().name();
	    return Utils.makePrettyStringFromEnum(entityName, allCaps);
	}
	
	/**
	 * Fetches the location of this MobSpawner; you could also do
	 * {@link #getCreatureSpawner().getLocation())} both should have the same location.
	 * @return
	 */
	
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Fetches the {@link CreatureSpawner} that this spawner is referring to.
	 * The CreatureSpawner is the MC physical spawner in which of whom is having
	 * its behavior overwritten/modified by this plugin.
	 * @return
	 */
	
	public CreatureSpawner getCreatureSpawner() {
		return this.spawner;
	}
	
	/**
	 * The id of this spawner inside of the {@link SpawnerManager}.
	 * Each spawner has a unique id.
	 * @return
	 */
	
	public int getId() {
		return this.id;
	}
	
	/**
	 * Sets the {@link SpawnerUpgrade} on this spawner.
	 * If provided NULL, it will use {@link SpawnerUpgrade.DEFAULT}.
	 * @param upgrade
	 */
	
	public void setUpgrade(SpawnerUpgrade upgrade) {
	    if(upgrade == null) upgrade = SpawnerUpgrade.DEFAULT;
		this.upgrade = upgrade;
	}
	
	/**
	 * Gets the {@link SpawnerUpgrade} instance, i.e. the upgrade on this spawner.
	 * Default counts as an "upgrade" technically. {@link SpawnerUpgrade.DEFAULT}
	 * @return
	 */
	
	public SpawnerUpgrade getUpgrade() {
		return this.upgrade;
	}
	
	
	/**
	 * Gets the index of the upgrade on this spawner.
	 * Default upgrade will return -1
	 * @return
	 */
	
	public int getUpgradeIndex() {
	    return upgrade.getIndex();
	}
	
	/**
	 * Returns whether or not spawning is allowable right now.
	 * @return
	 */
	
	public boolean canSpawnNow() {
	    return (System.currentTimeMillis() >= allowSpawnAfterMs);
	}

}
