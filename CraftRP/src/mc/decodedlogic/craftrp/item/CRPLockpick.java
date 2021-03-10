package mc.decodedlogic.craftrp.item;

import static mc.decodedlogic.craftrp.util.MessageUtils.sendActionBarMessage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import io.netty.util.internal.ThreadLocalRandom;
import mc.decodedlogic.craftrp.Globals;
import mc.decodedlogic.craftrp.entity.CRPDoor;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter.HoldMode;
import mc.decodedlogic.craftrp.mouse.UserTracker;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.util.BlockUtils;
import net.md_5.bungee.api.ChatColor;

public class CRPLockpick extends CRPItem {
    
    public static final long MIN_PLAY_EFFECT_EVERY = 500;
    public static final long MAX_PLAY_EFFECT_EVERY = 1800;
    
    private CRPDoor picking;
    private Block clicked;
    private long nextEffectTimeMs;
    
    public CRPLockpick() {
        super(CRPItemType.TOOL);
        this.name = ChatColor.RED + "Lockpick";
        this.description.add(ChatColor.WHITE + "RIGHT-CLICK a door to \"disable\" its lock.");
        this.material = Material.WOODEN_HOE;
        this.activationCooldown = 5000;
        
        this.setupMouseAdapter(HoldMode.ACTION_ON_FILL, 8000, 1L, 205L);
        
        this.picking = null;
        this.nextEffectTimeMs = 0;
        this.id = CRPItemManager.getIdForItem(this);
    }
    
    public void onMouseDown(CRPPlayer rpPlayer, UserTracker tracker) {
        long now = System.currentTimeMillis();
        long elapsedTime = now-tracker.getDownBeginTime();
        tracker.sendChargeBar("Picking", 40, elapsedTime);
        
        if(now >= nextEffectTimeMs) {
            Location l2 = clicked.getLocation().clone();
            l2.add(0.5, 0, 0.5);
            
            Location result = BlockUtils.getMidpoint(rpPlayer.getPlayer().getEyeLocation(), l2);
            rpPlayer.getPlayer().getWorld().spawnParticle(Particle.BLOCK_DUST, result, 10, 
                    picking.getOrigin().getBlock().getType().createBlockData());
            
            rpPlayer.getPlayer().playSound(rpPlayer.getPlayer().getLocation(), Sound.BLOCK_METAL_BREAK, 1F, 1F);
            
            double perctPicked = ((double) elapsedTime) / ((double) mouseAdapter.getDesiredDownTimeMilliseconds());

            long bound = MAX_PLAY_EFFECT_EVERY+1;
            
            bound -= 1600 * perctPicked;
            
            long origin = bound / 3;
            
            nextEffectTimeMs = now + ThreadLocalRandom.current().nextLong(origin, bound);
        }
    }
    
    public void onMouseReleased(CRPPlayer rpPlayer, UserTracker tracker) {
        super.onMouseReleased(rpPlayer, tracker);
        this.reset();
        
        sendActionBarMessage(rpPlayer.getPlayer(), ChatColor.RED + "Picking stopped!");
        rpPlayer.getPlayer().playSound(rpPlayer.getPlayer().getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 1F, 1F);
    }
    
    public void onFullChargeObtained(CRPPlayer player, UserTracker tracker) {
        if(picking != null) {
            picking.setOpen(true, true);
            picking.setLocked(false);
            sendActionBarMessage(player.getPlayer(), ChatColor.GREEN + "Lock successfully picked!");
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1F, 1F);
            
            this.reset();
        }
    }
    
    public void onPlayerUse(PlayerInteractEvent evt) {
        Action a = evt.getAction();
        
        if(a == Action.RIGHT_CLICK_BLOCK && user != null) {
            Block b = evt.getClickedBlock();
            
            if(b == null) return;
            
            CRPEntity ent = CRPEntityManager.getEntityFromBlock(b);
            
            if(ent != null && ent instanceof CRPDoor) {
                CRPDoor door = (CRPDoor) ent;
                
                if(door.isOwner(user)) {
                    this.reset();
                    sendActionBarMessage(user.getPlayer(), ChatColor.RED + "You own that door!", Globals.SOUND_FAILURE);
                    return;
                }
                
                if(door.isOpen()) {
                    this.reset();
                    sendActionBarMessage(user.getPlayer(), ChatColor.RED + "That door is already open!", Globals.SOUND_FAILURE);
                    return;
                }
                
                this.picking = door;
                this.clicked = b;
                mouseAdapter.handleDown(user, evt);
                this.setState(ItemState.ACTIVE);
                return;
            }
            
        }
        
        this.reset();
    }
    
    public void reset() {
        this.picking = null;
        this.clicked = null;
        this.nextEffectTimeMs = 0;
        this.setState(ItemState.IDLE);
    }

    @Override
    public ItemStack getUpdatedItem(ItemStack current) {
        return this.generateMCItem();
    }

}
