package mc.decodedlogic.craftrp.entity;

import static mc.decodedlogic.craftrp.Globals.handleAction;
import static mc.decodedlogic.craftrp.Globals.GameAction.PURCHASE;
import static mc.decodedlogic.craftrp.util.MessageUtils.sendActionBarMessage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.gui.DoorGUI;
import mc.decodedlogic.craftrp.hologram.HologramMovement;
import mc.decodedlogic.craftrp.hologram.HologramVisibility;
import mc.decodedlogic.craftrp.hologram.RPHologram;
import mc.decodedlogic.craftrp.job.Job;
import mc.decodedlogic.craftrp.ownable.Owner;
import mc.decodedlogic.craftrp.ownable.Property;
import mc.decodedlogic.craftrp.ownable.PropertyType;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.util.BlockUtils;
import net.md_5.bungee.api.ChatColor;

public class CRPDoor extends CRPEntity implements Property {
    
    public static final int MAX_DOOR_OWNERS = 6;

    public static final String UNOWNED_HOLOGRAM_NAME = "Unowned";
    public static final String OWNED_HOLOGRAM_NAME = "Owned";
    
    private String title;
    
    private int cost;
    
    private boolean locked;
    
    // Whether or not this door has a lockpick alarm.
    private boolean hasLockpickAlarm;
    
    private List<Owner> owners;
    private int maximumOwners;
    
    private long lastTimeToggledMs;
    
    public CRPDoor(Location origin) {
        super(CRPEntityType.DOOR);
        this.name = "Door";
        this.origin = origin;
        this.title = null;
        this.cost = 20;
        this.locked = false;
        this.hasLockpickAlarm = false;
        this.owners = new ArrayList<Owner>();
        this.maximumOwners = MAX_DOOR_OWNERS;
        this.accessCooldownMs = 0;
        this.lastTimeToggledMs = 0;
        
        Block b = origin.getBlock();
        Block above = b.getRelative(BlockFace.UP);
        Block below = b.getRelative(BlockFace.DOWN);
        
        if(above.getType().name().endsWith("_DOOR")) {
            blocks.add(above);
        }
        
        if(below.getType().name().endsWith("_DOOR")) {
            origin = below.getLocation();
            blocks.add(b);
        }
        
        if(b != null && b.getType().name().endsWith("_DOOR")) {
            CRPEntityManager.registerEntity(this);
            generate();
        }
    }
    
    public void generate() {
        CraftRP main = CraftRP.get();
        
        RPHologram unowned = new RPHologram(hologramContainer);
        unowned.setName(UNOWNED_HOLOGRAM_NAME);
        unowned.setPopupSound(RPHologram.DEFAULT_APPEAR_SOUND);
        unowned.setVisibility(HologramVisibility.SINGLE_VIEWER);
        unowned.setMovement(HologramMovement.DYNAMIC);
        unowned.setOrigin(origin);
        
        Location l = new Location(origin.getWorld(), origin.getX(), origin.getY() + 1.5, origin.getZ() + 0.5);
        Hologram unownedHG = HologramsAPI.createHologram(main, l);
        unownedHG.getVisibilityManager().setVisibleByDefault(false);
        unownedHG.appendTextLine(ChatColor.RED + "Unowned");
        unownedHG.appendTextLine(ChatColor.RED + "SNEAK + RIGHT-CLICK to Own");
        unowned.setHologram(unownedHG);
        
        hologramContainer.addHologram(UNOWNED_HOLOGRAM_NAME, unowned);
        
        RPHologram owned = new RPHologram(hologramContainer);
        owned.setName(OWNED_HOLOGRAM_NAME);
        owned.setPopupSound(RPHologram.DEFAULT_APPEAR_SOUND);
        owned.setVisibility(HologramVisibility.SINGLE_VIEWER);
        owned.setMovement(HologramMovement.DYNAMIC);
        owned.setOrigin(origin);
        
        Hologram ownedHG = HologramsAPI.createHologram(main, l);
        ownedHG.getVisibilityManager().setVisibleByDefault(false);
        ownedHG.appendTextLine(ChatColor.GREEN + "Owned");
        owned.setHologram(ownedHG);
        
        hologramContainer.addHologram(OWNED_HOLOGRAM_NAME, owned);
    }
    
    public void onPlayerFocusGained(CRPPlayer p) {
        super.onPlayerFocusGained(p);
        
        if(owners.size() == 0) {
            // The door is unowned.
            processHologramShow(p, UNOWNED_HOLOGRAM_NAME);
            
            sendActionBarMessage(p.getPlayer(), "Cost: $" + ((int) cost));
        }else {
            processHologramShow(p, OWNED_HOLOGRAM_NAME);
        }
        
    }
    
    public void onPlayerInteract(CRPPlayer rpPlayer, PlayerInteractEntityEvent evt) {
        Player p = rpPlayer.getPlayer();
        
        if(evt.isRightClick()) {
            
            if(isOwner(rpPlayer)) {
                
                if(p.isSneaking() && !isOpen()) {
                    setLocked(!locked, p);
                    evt.setCancelled(true);
                    return;
                }
                
            }else if(p.isSneaking() && owners.size() == 0) {
                // Add owner
                addOwner(rpPlayer);
                handleAction(rpPlayer, PURCHASE);
                evt.setCancelled(true);
                return;
            }
            
            if(!isLocked() && isIronDoor()) {
                setOpen(!isOpen(), true);
                evt.setCancelled(false);
            }
        }else if(isOwner(rpPlayer)) {
            DoorGUI doorGUI = new DoorGUI(rpPlayer, this);
            rpPlayer.setCurrentGUI(doorGUI);
            doorGUI.open();
            evt.setCancelled(true);
            return;
        }
        
        if(isLocked()) {
            Sound sound = (isIronDoor()) ? Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR : Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
            sendActionBarMessage(p, "Door is " + getStateName() + "!", sound);
            
            Location l2 = evt.getClickedBlock().getLocation().clone();
            l2.add(0.5, 0, 0.5);
            Location rightHand = BlockUtils.getMidpoint(p.getEyeLocation(), l2);
            p.getWorld().spawnParticle(Particle.BLOCK_DUST, rightHand, 10, origin.getBlock().getType().createBlockData());
            
            evt.setCancelled(true);
            return;
        }
    }
    
    public String getHelpInformation(CRPPlayer rpPlayer) {
        if(rpPlayer == null) return "";
        
        if(isOwner(rpPlayer)) {
            return "PUNCH to open menu. SNEAK + RIGHT CLICK to lock/unlock";
        }else if(owners.size() == 0) {
            return "SNEAK + RIGHT CLICK to purchase";
        }else {
            Owner firstOwner = owners.get(0);
            String name = (firstOwner instanceof Job) ? firstOwner.getName() : ChatColor.stripColor(firstOwner.getName());
            
            return "Owned by: " + name;
        }
    }
    
    public void setOpen(boolean flag, boolean playSound) {
        long now = System.currentTimeMillis();
        if(flag == isOpen() || now < this.lastTimeToggledMs + 250) return;
        
        Block b = origin.getBlock();
        BlockData data = b.getBlockData();
        
        ((Openable) data).setOpen(flag);
        b.setBlockData(data, true);
        
        if(!playSound) return;
        
        Sound sound = null;
        
        if(isIronDoor()) {
            sound = (flag) ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
        }else {
            sound = (flag) ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE;
        }
        
        origin.getWorld().playSound(origin, sound, 1F, 1F);
        
        this.lastTimeToggledMs = System.currentTimeMillis();
    }
    
    public boolean isOpen() {
        Block b = origin.getBlock();
        
        return ((Openable) b.getBlockData()).isOpen();
    }
    
    public boolean isIronDoor() {
        return (origin != null && origin.getBlock().getType() == Material.IRON_DOOR);
    }
    
    public String getStateName() {
        String state = (locked) ? "locked" : "unlocked";
        ChatColor color = (!locked) ? ChatColor.GREEN : ChatColor.RED;
        return color + state + ChatColor.RESET;
    }
    
    public void reset() {
        super.reset();
        
        this.removeAllOwners();
        this.locked = false;
        this.title = null;
        this.lastTimeToggledMs = 0;
        this.setOpen(false, true);
    }
    
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setCost(int newCost) {
        this.cost = newCost;
    }
    
    public double getCost() {
        return this.cost;
    }
    
    public void onOwnerAdded(Owner owner) {
        RPHologram ownedHG = hologramContainer.getHologramByName(OWNED_HOLOGRAM_NAME);
        
        if(ownedHG.getHologram().size() == 1) {
            ChatColor defColor = ChatColor.GOLD;
            ownedHG.getHologram().appendTextLine(defColor + owner.getName());
        }
        
        switchHologramTo(UNOWNED_HOLOGRAM_NAME, OWNED_HOLOGRAM_NAME);
    }
    
    public void onOwnerRemoved(Owner owner) {
        if(owners.size() == 0) switchHologramTo(OWNED_HOLOGRAM_NAME, UNOWNED_HOLOGRAM_NAME);
    }
    
    public List<Owner> getOwners() {
        return this.owners;
    }
    
    public void setMaximumOwners(int max) {
        this.maximumOwners = max;
    }
    
    public int getMaximumOwners() {
        return this.maximumOwners;
    }
    
    public void setLocked(boolean flag, Player locker) {
        setLocked(flag);
        
        if(locker != null) {
            Sound sound = (!locked) ? Sound.BLOCK_NOTE_BLOCK_HAT : Sound.ITEM_ARMOR_EQUIP_ELYTRA;
            sendActionBarMessage(locker, "Door is now " + getStateName() + ".", sound);
        }
    }
    
    public void setLocked(boolean flag) {
        this.locked = flag;
    }
    
    public boolean isLocked() {
        return this.locked;
    }
    
    public void setHasLockpickAlarm(boolean flag) {
        this.hasLockpickAlarm = flag;
    }
    
    public boolean hasLockpickAlarm() {
        return this.hasLockpickAlarm;
    }
    
    public int getLockpickAlarmCost() {
        return (int) Math.ceil(((double) getValue()) * 1.25);
    }
    
    public int getValue() {
        return (int) Math.ceil(((double) this.cost) * 0.75) + (this.owners.size() * 5);
    }

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.DOOR;
    }
    
}
