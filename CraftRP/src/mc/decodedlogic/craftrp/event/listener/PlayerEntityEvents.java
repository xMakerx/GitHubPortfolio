package mc.decodedlogic.craftrp.event.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import mc.decodedlogic.craftrp.event.PlayerActivateItemEvent;
import mc.decodedlogic.craftrp.gui.DeveloperPanel;
import mc.decodedlogic.craftrp.item.CRPItem;
import mc.decodedlogic.craftrp.item.CRPItem.ItemState;
import mc.decodedlogic.craftrp.item.CRPItemManager;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;

public class PlayerEntityEvents implements Listener {
    
    final CraftRP MAIN;
    
    public PlayerEntityEvents(CraftRP inst) {
        this.MAIN = inst;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Player p = evt.getPlayer();
        CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
        
        if(rpPlayer != null) {
            ItemStack held = p.getInventory().getItemInMainHand();
            CRPItem rpItem = CRPItemManager.getCRPItemFromItemStack(held);
            
            if(rpItem == null || (rpItem != null && rpItem.getState() != ItemState.ACTIVE)) {
                Block b = p.getTargetBlock(null, CRPPlayer.MAX_FOCUS_DISTANCE);
                
                CRPEntity ent = CRPEntityManager.getEntityFromBlock(b);
                rpPlayer.setFocusedEntity(ent);
            }
        }
    }
    
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent evt) {
        Player p = evt.getPlayer();
        CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
        
        if(rpPlayer != null) {
            if(rpPlayer.getHeldItem() != null) {
                evt.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerActivateItem(PlayerActivateItemEvent evt) {
        CRPPlayer rpPlayer = evt.getCRPPlayer();
        
        if(rpPlayer != null) {
            rpPlayer.setFocusedEntity(null);
        }
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent evt) {
        Player p = evt.getPlayer();
        
        if(!evt.isCancelled()) {
            ItemStack item = p.getInventory().getItem(evt.getNewSlot());
            
            CRPItem rpItem = CRPItemManager.getCRPItemFromItemStack(item);
            CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
            
            if(rpPlayer != null) rpPlayer.setHeldItem(rpItem);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player p = evt.getPlayer();
        CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
        
        if(rpPlayer != null) {
            
            ItemStack item = p.getInventory().getItemInMainHand();
            CRPItem rpItem = rpPlayer.getHeldItem();
            
            if(rpItem != null) {
                if(CRPItemManager.getCRPItemFromItemStack(item) == null) {
                    rpPlayer.setHeldItem(null);
                }else {
                    rpItem.__processUseAttempt(evt);
                    evt.setUseInteractedBlock(Result.DENY);
                    evt.setUseItemInHand(Result.DENY);
                    evt.setCancelled(true);
                    return;
                }
            }else if(item != null && item.getType() == Material.COMMAND_BLOCK) {
                DeveloperPanel panel = new DeveloperPanel(rpPlayer);
                rpPlayer.setCurrentGUI(panel);
                panel.open();
                evt.setUseInteractedBlock(Result.DENY);
                evt.setUseItemInHand(Result.DENY);
                evt.setCancelled(true);
                return;
            }
            
            Block b = evt.getClickedBlock();
            
            if(CRPEntityManager.isEntityBlock(b)) {
                CRPEntity entity = CRPEntityManager.getEntityFromBlock(b);
                
                if(entity != null) entity.__processInteractAttempt(rpPlayer, evt);
            }
        }
    }
    
}
