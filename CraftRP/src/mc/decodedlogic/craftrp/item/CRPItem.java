package mc.decodedlogic.craftrp.item;

import static mc.decodedlogic.craftrp.util.MessageUtils.sendActionBarMessage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.Globals;
import mc.decodedlogic.craftrp.event.PlayerActivateItemEvent;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter.HoldMode;
import mc.decodedlogic.craftrp.mouse.MouseListener;
import mc.decodedlogic.craftrp.mouse.UserTracker;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import net.md_5.bungee.api.ChatColor;

public abstract class CRPItem implements CRPRegistrableObject, MouseListener {
    
    public enum ItemState {
        IDLE, ACTIVE
    }
    
    protected String name;
    protected List<String> description;
    
    protected ItemState state;
    
    protected final CRPItemType TYPE; 
    
    protected Material material;
    protected byte data;
    
    protected long id;
    
    protected HeldMouseAdapter mouseAdapter;
    
    protected CRPPlayer user;
    protected ItemStack usedItemStack;
    
    protected long activationCooldown;
    protected long nextActivateTimeMs;
    
    public CRPItem(CRPItemType type) {
        this.name = "";
        this.description = new ArrayList<String>();
        this.state = ItemState.IDLE;
        this.TYPE = type;
        this.material = Material.AIR;
        this.data = 0;
        this.id = CRPItemManager.getIdForItem(this);
        this.mouseAdapter = null;
        this.user = null;
        this.usedItemStack = null;
        this.activationCooldown = Globals.MILLISECONDS_PER_SECOND/2;
        this.nextActivateTimeMs = 0;
    }
    
    public void onMouseReleased(CRPPlayer player, UserTracker tracker) {
        if(activationCooldown > 0) {
            long now = System.currentTimeMillis();
            this.nextActivateTimeMs = now + activationCooldown;
        }
    }
    
    public void setupMouseAdapter(HoldMode mode, long desiredDownTimeMs, 
            long processTaskTicks, long cancelAfterTimeMs) {
        if(mouseAdapter == null) {
            mouseAdapter = new HeldMouseAdapter(this);
        }
        
        mouseAdapter.setDesiredDownTimeMilliseconds(desiredDownTimeMs);
        mouseAdapter.setProcessTaskStepTicks(processTaskTicks);
        mouseAdapter.setCancelAfterTimeMilliseconds(cancelAfterTimeMs);
    }
    
    public HeldMouseAdapter getMouseAdapter() {
        return this.mouseAdapter;
    }
    
    @SuppressWarnings("deprecation")
    public ItemStack generateMCItem() {
        ItemStack i = new ItemStack(material, 1);
        i.setDurability((short) data);
        
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(description);
        meta.getPersistentDataContainer().set(CRPItemManager.getItemIdKey(), 
                PersistentDataType.LONG, id);
        i.setItemMeta(meta);
        
        return i;
    }
    
    public ItemStack getUpdatedItem(ItemStack current) {
        return generateMCItem();
    }
    
    public void setName(String newName) {
        this.name = newName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setDescription(List<String> newDescription) {
        this.description = newDescription;
    }
    
    public List<String> getDescription() {
        return this.description;
    }
    
    public void setUser(CRPPlayer user) {
        this.user = user;
    }
    
    public CRPPlayer getUser() {
        return this.user;
    }
    
    public void setData(Material newMaterial, byte newData) {
        this.material = newMaterial;
        this.data = newData;
    }
    
    public Material getMaterial() {
        return this.material;
    }
    
    public byte getData() {
        return this.data;
    }
    
    public long getID() {
        return this.id;
    }
    
    public void onRegistered(long assignedID) {
        this.id = assignedID;
    }
    
    public void onUnregistered() {
        if(mouseAdapter != null) mouseAdapter.endAllTrackers();
    }
    
    public void onUnequip() {
        if(mouseAdapter != null) mouseAdapter.endAllTrackers();
    }
    
    public void setState(ItemState newState) {
        if(state == newState) return;
        this.state = newState;
        
        if(newState == ItemState.ACTIVE) {
            PlayerActivateItemEvent evt = new PlayerActivateItemEvent(user, this);
            CraftRP.get().getServer().getPluginManager().callEvent(evt);
        }
    }
    
    public ItemState getState() {
        return this.state;
    }
    
    public CRPItemType getType() {
        return this.TYPE;
    }
    
    public void __processUseAttempt(PlayerInteractEvent evt) {
        long now = System.currentTimeMillis();
        
        if(now > nextActivateTimeMs) {
            usedItemStack = evt.getPlayer().getInventory().getItemInMainHand();
            onPlayerUse(evt);
            
            if(mouseAdapter == null && activationCooldown > 0) {
                nextActivateTimeMs = now + activationCooldown;
            }
            
        }else {
            long timeLeft = nextActivateTimeMs - now;
            double seconds = Globals.millisecondsToSeconds(timeLeft);
            sendActionBarMessage(user.getPlayer(), ChatColor.RED + String.format("Wait %.1fs to use again!", seconds), Globals.SOUND_FAILURE);
        }
    }
    
    public abstract void onPlayerUse(PlayerInteractEvent evt);
    
}
