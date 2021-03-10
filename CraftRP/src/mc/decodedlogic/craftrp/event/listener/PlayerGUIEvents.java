package mc.decodedlogic.craftrp.event.listener;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import lib.decodedlogic.gui.ButtonElement;
import lib.decodedlogic.gui.Element;
import mc.decodedlogic.craftrp.gui.RPGUI;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;

public class PlayerGUIEvents implements Listener {
    
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        Player p = ((Player) evt.getPlayer());
        CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
        
        if(rpPlayer != null) {
            RPGUI curGUI = rpPlayer.getCurrentGUI();
            if(curGUI != null) {
                curGUI.onClosed();
                rpPlayer.setCurrentGUI(null);
            }
        }
    }
    
    private void handleInCustomInventory(InventoryInteractEvent evt) {
        if(evt.getWhoClicked() instanceof Player) {
            Player p = (Player) evt.getWhoClicked();
            CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
            
            if(rpPlayer != null) {
                RPGUI curGUI = rpPlayer.getCurrentGUI();
                if(curGUI != null) {
                    
                    if(evt instanceof InventoryClickEvent) {
                        InventoryClickEvent cEvt = (InventoryClickEvent) evt;
                        int slot = cEvt.getSlot();
                        
                        Element e = curGUI.get(slot);
                        if(e != null && e instanceof ButtonElement) {
                            ((ButtonElement) e).onClick(cEvt);
                        }
                    }
                    
                    evt.setResult(Result.DENY);
                    evt.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        handleInCustomInventory(evt);
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent evt) {
        handleInCustomInventory(evt);
    }
    
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent evt) {
        final List<HumanEntity> VIEWERS = evt.getSource().getViewers();
        
        // Obviously ignore events that are canceled or that somehow occur without any viewers.
        if(evt.isCancelled() || VIEWERS.size() == 0) return;
        
        if(VIEWERS.get(0) instanceof Player) {
            Player p = (Player) VIEWERS.get(0);
            CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
            
            if(rpPlayer != null) {
                RPGUI curGUI = rpPlayer.getCurrentGUI();
                if(curGUI != null) {
                    evt.setCancelled(true);
                    evt.setItem(null);
                }
            }
        }
    }
    
}
