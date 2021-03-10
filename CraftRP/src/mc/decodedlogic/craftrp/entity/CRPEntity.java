package mc.decodedlogic.craftrp.entity;

import static mc.decodedlogic.craftrp.Globals.DEFAULT_ACCESS_COOLDOWN_TIME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.event.CRPEntityHealthChangeEvent;
import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.hologram.HologramContainer;
import mc.decodedlogic.craftrp.hologram.RPHologram;
import mc.decodedlogic.craftrp.mouse.MouseListener;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockBreakAnimation;

public abstract class CRPEntity implements CRPRegistrableObject, MouseListener {
    
    protected String name;
    protected String description;
    
    protected final CRPEntityType TYPE;
    
    // Blocks that are contained within this entity
    protected HashSet<Block> blocks;
    
    // Properties of this entity
    protected double maxHealth;
    protected double health;
    protected boolean showDmgProgress;
    
    protected Location origin;
    
    protected boolean invincible;
    
    protected HologramContainer hologramContainer;
    
    // The time (in milliseconds) between interactions allowed.
    protected long accessCooldownMs;
    
    protected final Map<UUID, Long> ACCESS_COOLDOWNS;
    
    // Players looking at this entity.
    protected List<Long> viewers;
    
    protected long id;
    
    public CRPEntity(CRPEntityType type) {
        this.name = "";
        this.description = "";
        this.TYPE = type;
        this.blocks = new HashSet<Block>();
        this.maxHealth = 1.0;
        this.health = maxHealth;
        this.showDmgProgress = false;
        this.origin = null;
        this.invincible = false;
        this.hologramContainer = new HologramContainer(this);
        
        this.ACCESS_COOLDOWNS = new HashMap<UUID, Long>();
        this.accessCooldownMs = DEFAULT_ACCESS_COOLDOWN_TIME;
                
        this.viewers = new ArrayList<Long>();
        this.id = -1;
    }
    
    /**
     * Sets the name of this entity.
     * @param newName
     */
    
    public void setName(String newName) {
        this.name = newName;
    }
    
    /**
     * Fetches the name of this entity.
     * @return
     */
    
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the description of this entity.
     * @param newDesc
     */
    
    public void setDescription(String newDesc) {
        this.description = newDesc;
    }
    
    /**
     * Fetches the description of this entity.
     * @return
     */
    
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Fetches the {@link CRPEntityType} of this entity.
     * @return
     */
    
    public CRPEntityType getType() {
        return this.TYPE;
    }
    
    /**
     * Adds a {@link Block} to this entity. Block will be assigned metadata with this
     * entity's id to make it easier/more efficient to get to the entity's class without
     * iteration code.
     * @param b
     */
    
    public void addBlock(Block b) {
        if(!contains(b)) blocks.add(b);
    }
    
    /**
     * Fetches the {@link Block}s that represent this entity.
     * 
     * NOTE: Does not include the block at this entity's origin. See: {@link #getAllBlocks()}
     * @return
     */
    
    public HashSet<Block> getBlocks() {
        return this.blocks;
    }
    
    /**
     * Fetches all the blocks within {@link #getBlocks()} along WITH the block at this
     * entity's origin.
     * @return
     */
    
    public HashSet<Block> getAllBlocks() {
        HashSet<Block> allBlocks = new HashSet<Block>();
        allBlocks.addAll(blocks);
        allBlocks.add(origin.getBlock());
        
        return allBlocks;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Health code -- because there's so much it needs a separate section.
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void damage(double damage) {
        setHealth(getHealth() - damage);
    }
    
    /**
     * Sets the maximum health of this entity.
     * 
     * @param newMaxHealth - New maximum health where n > 0
     * @param silent - Whether or not to propagate health change events.
     */
    
    public void setMaxHealth(double newMaxHealth, boolean silent) {
        if(newMaxHealth <= 0) return;
        
        double oldMaxHealth = maxHealth;
        
        if(oldMaxHealth != maxHealth) {
            if(!silent) {
                // Let's call our health change event
                CRPEntityHealthChangeEvent evt = new CRPEntityHealthChangeEvent(this, maxHealth, oldMaxHealth, true);
                CraftRP.get().getServer().getPluginManager().callEvent(evt);
                
                if(evt.isCancelled()) return;
            }
            
            this.maxHealth = newMaxHealth;
            if(health > maxHealth) setHealth(maxHealth, silent);
        }
    }
    
    /**
     * Sets the maximum health of this entity.
     * 
     * If current health is greater than new max health, the current health
     * will be set equal to the new max health.
     * 
     * @param newMaxHealth - New maximum health.
     * 
     * NOTE: This will propagate health change events which then can be canceled.
     */
    
    public void setMaxHealth(double newMaxHealth) {
        setMaxHealth(newMaxHealth, false);
    }
    
    /**
     * Gets the maximum health of this entity.
     * @return
     */
    
    public double getMaxHealth() {
        return this.maxHealth;
    }
    
    /**
     * Sets the health of this entity. If specified double is greater
     * than the max health, max health will be used instead.
     * 
     * @param newHealth - n where (0 <= n <= max health)
     * @param silent - Whether or not to propagate health change events.
     */
    
    public void setHealth(double newHealth, boolean silent) {
        if(newHealth > maxHealth || invincible) return;
        double oldHealth = health;
        
        if(oldHealth != health && !silent) {
            // Let's call our health change event
            CRPEntityHealthChangeEvent evt = new CRPEntityHealthChangeEvent(this, health, oldHealth, false);
            CraftRP.get().getServer().getPluginManager().callEvent(evt);
            
            if(evt.isCancelled()) return;
        }
        
        newHealth = (newHealth <= 0) ? 0 : newHealth;
        this.health = (newHealth <= maxHealth) ? newHealth : health;
        
        if(showDmgProgress) {
            double dist = CRPPlayer.MAX_FOCUS_DISTANCE;
            origin.getWorld().getNearbyEntities(origin, dist, dist, dist).stream().filter(e -> (e instanceof Player)).forEach(e -> {
                Player p = (Player) e;
                CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
                if(rpPlayer != null) {
                    sendDamageEffectIfNeeded(rpPlayer.getPlayer());
                }
            });
        }
    }
    
    /**
     * Sets the health of this entity. If specified double is greater
     * than the max health, max health will be used instead.
     * 
     * @param newHealth - n where (0 <= n <= max health)
     * 
     * NOTE: This will propagate health change events which can then be canceled.
     */
    
    public void setHealth(double newHealth) {
        setHealth(newHealth, false);
    }
    
    /**
     * Returns the current health of this entity.
     * @return n where (0 <= n <= max health)
     */
    
    public double getHealth() {
        return this.health;
    }
    
    /**
     * Resets health to maximum health.
     */
    
    public void resetHealth() {
        this.health = maxHealth;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Set whether or not to display the block crack effect when damage is taken.
     * @param flag - true/false
     */
    
    public void setShowDamageProgress(boolean flag) {
        this.showDmgProgress = flag;
    }
    
    /**
     * Whether or not the block crack effect is displayed when damage is taken.
     * @return true/false flag.
     */
    
    public boolean doesShowDamageProgress() {
        return this.showDmgProgress;
    }
    
    /**
     * Sets the origin/center of this entity.
     * @param newOrigin
     */
    
    public void setOrigin(Location newOrigin) {
        this.origin = newOrigin;
    }
    
    /**
     * Gets the origin/center of this entity.
     * @return
     */
    
    public Location getOrigin() {
        return this.origin;
    }
    
    /**
     * Set whether or not this entity can take damage. "God-mode"
     * @param flag
     */
    
    public void setInvincible(boolean flag) {
        this.invincible = flag;
    }
    
    /**
     * Whether or not this entity can take damage. "God-mode"
     * @return
     */
    
    public boolean isInvincible() {
        return this.invincible;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Player/User interaction code
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Called when a player starts looking at one of the blocks that represents this entity.
     * 
     * Adds the player as a viewer and sends them the damage effect if they need it.
     * 
     * @param p - Valid CRPPlayer instance.
     */
    
    public void onPlayerFocusGained(CRPPlayer p) {
        if(!viewers.contains(p.getID())) {
            addViewer(p.getID());
        }
        
        sendDamageEffectIfNeeded(p.getPlayer());
    }
    
    /**
     * Called when a player stops looking at one of the blocks that represents this entity.
     * 
     * Removes the player as a viewer and removes the damage effect as well.
     * 
     * @param p - Valid CRPPlayer instance.
     */
    
    public void onPlayerFocusLost(CRPPlayer p) {
        removeViewer(p.getID());
        removeDamageEffectIfNeeded(p.getPlayer());
    }
    
    public abstract void onPlayerInteract(CRPPlayer p, PlayerInteractEntityEvent evt);
    
    public void __processInteractAttempt(CRPPlayer p, PlayerInteractEvent intEvt) {
        if(p == null) return;
        
        long lastAccessTime = -1;
        
        UUID uuid = p.getPlayer().getUniqueId();
        
        if(ACCESS_COOLDOWNS.keySet().contains(uuid)) {
            lastAccessTime = ACCESS_COOLDOWNS.get(uuid);
        }
        
        long now = System.currentTimeMillis();
        
        if(now > lastAccessTime) {
            // The user can access this entity.
            PlayerInteractEntityEvent rpIntEvt = new PlayerInteractEntityEvent(p, this, intEvt);
            CraftRP.get().getServer().getPluginManager().callEvent(rpIntEvt);
            
            if(!rpIntEvt.isCancelled()) this.onPlayerInteract(p, rpIntEvt);
            
            // Let's set a new cooldown, if necessary.
            if(accessCooldownMs > 0) {
               ACCESS_COOLDOWNS.put(uuid, now + accessCooldownMs); 
            }
        }

    }
    
    public long getAccessCooldownTime() {
        return this.accessCooldownMs;
    }
    
    public abstract String getHelpInformation(CRPPlayer rpPlayer);
    
    /**
     * Adds the specified {@link CRPPlayer}'s id to the viewer list.
     * @param crpPlayerId - Id to be added.
     * 
     * Note: Does not call {@link CRPEntity#onPlayerFocusGained(CRPPlayer)}. Inversely,
     * that method calls this one.
     */
    
    public void addViewer(long crpPlayerId) {
        if(!viewers.contains(crpPlayerId)) this.viewers.add(crpPlayerId);
    }
    
    /**
     * Removes the specified {@link CRPPlayer} id from the viewer list.
     * @param crpPlayerId - Id to be removed.
     * 
     * NOTE: Does not call {@link CRPEntity#onPlayerFocusLost(CRPPlayer)}. Inversely,
     * that method calls this one.
     * @return
     */
    
    public boolean removeViewer(long crpPlayerId) {
        return this.viewers.remove(crpPlayerId);
    }
    
    /**
     * Fetches the list of ids of the {@link CRPPlayer}s focusing on this entity.
     * @return List<Long>
     */
    
    public List<Long> getViewers() {
        return this.viewers;
    }
    
    /**
     * Sends the block crack effect to the specified {@link Player} to be displayed on this
     * entity's blocks.
     * 
     * The severity of the crack/number of cracks increases/decreases based on the
     * percentage of health left on this entity.
     * 
     * Must be sent periodically sense the effect will disappear after awhile (Not sure how long)
     * 
     * Show only be used/called after this entity is registered so a unique packet id can
     * be assigned.
     * @param p - A valid Player instance to send the effect to.
     */
    
    protected void sendDamageEffectIfNeeded(Player p) {
        double perctLeft = health / maxHealth;
        int state = 9 - (int) (9.0 * perctLeft);
        
        if(health < maxHealth && health >= 0) {
            int i = 0;
            for(Block b : getAllBlocks()) {
                PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(getPacketId()+i, 
                        new BlockPosition(b.getX(), b.getY(), b.getZ()), state);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                i++;
            }
        }
    }
    
    /**
     * Removes/resets the block crack effect on this entity's blocks for the specified {@link Player}.
     * 
     * Show only be used/called after this entity is registered so a unique packet id can
     * be assigned.
     * @param p - A valid Player instance to send the effect to.
     */
    
    protected void removeDamageEffectIfNeeded(Player p) {
        if(health == maxHealth || origin.getBlock().getType() == Material.AIR) {
            for(Block b : getAllBlocks()) {
                PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(getPacketId(), 
                        new BlockPosition(b.getX(), b.getY(), b.getZ()), -1);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }
    
    /**
     * Returns a unique integer based off of this 
     * entity's id to be used as a packet id.
     * @return
     */
    
    public int getPacketId() {
        int idLength = 4;
        String strId = String.valueOf(getID());
        return Integer.valueOf(strId.substring(strId.length()-idLength));
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Hologram handling code
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Shows or updates the specified {@link RPHologram} for the specified {@link CRPPlayer} by name.
     * The hologram must be registered to this entity's {@link HologramContainer} to propogate.
     * 
     * This will call {@link RPHologram#showTo(CRPPlayer)} if the hologram hasn't been shown to the
     * player yet; {@link RPHologram#update(CRPPlayer)} is called otherwise.
     * @param p - Valid CRPPlayer instance
     * @param hgName - Registered name of the hologram to show/update.
     */
    
    protected void processHologramShow(CRPPlayer p, String hgName) {
        RPHologram existing = p.getHologramByName(hgName);
        
        if(existing == null) {
            RPHologram hg = hologramContainer.getHologramByName(hgName);
            if(hg != null) hg.showTo(p);
        }else {
            existing.update(p);
        }
    }
    
    /**
     * Switches the active hologram for viewers of this entity to the hologram represented
     * by "newHologramStr." Hides the hologram represented by "curHologramStr" from all viewers
     * prior to showing the new hologram. See {@link #processHologramShow(CRPPlayer, String)} for more
     * information on what happens when the new hologram is displayed.
     * 
     * @param curHologramStr - The name of the hologram to hide.
     * @param newHologramStr - The name of the hologram to start displaying.
     */
    
    public void switchHologramTo(String curHologramStr, String newHologramStr) {
        hologramContainer.hideFromAll(curHologramStr);
        
        viewers.stream().forEach(v -> {
            CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromId(v);
            if(rpPlayer != null) {
                processHologramShow(rpPlayer, newHologramStr);
            }
        });
    }
    
    /**
     * Returns this entity's {@link HologramContainer} instance.
     * @return
     */
    
    public HologramContainer getHologramContainer() {
        return this.hologramContainer;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns whether or not the specified {@link Block} is
     * part of this entity.
     * @param Block b
     * @return true/false
     */
    
    public boolean contains(Block b) {
        return blocks.contains(b);
    }
    
    public void generate() {}
    
    public void reset() {
        if(hologramContainer != null) {
            hologramContainer.getHolograms().keySet().forEach(name -> {
                hologramContainer.hideFromAll(name);
            });
        }
        
        // Remove every viewer correctly
        List<Long> viewersClone = new ArrayList<Long>();
        viewersClone.addAll(viewers);
        
        viewersClone.forEach(v -> {
            CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromId(v);
            if(rpPlayer != null) rpPlayer.setFocusedEntity(null);
        });
        
        // Let's reset use cooldowns and health.
        ACCESS_COOLDOWNS.clear();
        resetHealth();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Entity Manager code
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Called upon a successful registration in the {@link CRPEntityManager}.
     * 
     * Sets the assigned unique id and assigns metadata with the id to all of the blocks
     * that represent this entity.
     */
    
    public void onRegistered(long assignedID) {
        this.id = assignedID;
        
        for(Block b : getAllBlocks()) {
            b.setMetadata(CRPEntityManager.METADATA_ENTITY_ID_KEY, 
                    new FixedMetadataValue(CraftRP.get(), id));
        }
    }
    
    /**
     * Called when this entity is unregistered from the {@link CRPEntityManager}
     * 
     * Resets the id to -1, clears all holograms, and deletes metadata on blocks that represent
     * this entity.
     */
    
    public void onUnregistered() {
        this.id = -1;
        
        for(Block b : getAllBlocks()) {
            b.removeMetadata(CRPEntityManager.METADATA_ENTITY_ID_KEY, CraftRP.get());
        }
        
        if(hologramContainer != null) {
            hologramContainer.clear();
        }
    }
    
    /**
     * Returns this entity's unique id set upon registration within {@link CRPEntityManager}.
     * Returns -1 if not registered yet.
     */
    
    public long getID() {
        return this.id;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////

}
