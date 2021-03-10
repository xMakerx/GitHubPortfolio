package mc.decodedlogic.skybattlesuspawners.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mc.decodedlogic.skybattlesuspawners.Settings;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.logging.ModificationRecord;
import mc.decodedlogic.skybattlesuspawners.logging.Record;
import mc.decodedlogic.skybattlesuspawners.logging.SpawnerLog;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton.Data;

public class LogMenu extends Menu {
    
    private final Location LOC;
    private final LocationLog LOG;
    private SpawnerLog curSpawner;
    
    public LogMenu(Player viewer, Location loc) {
        super(viewer);
        this.LOC = loc;
        this.LOG = LocationLog.get(LOC);
        this.curSpawner = null;
    }
    
    public void open(boolean openInv) {
        final Settings settings = USpawners.get().getSettings();
        boolean empty = LOG.getAllRecords().size() == 0;
        boolean oneSpawner = LOG.getSpawnerLogs().size() == 1;
        
        if(empty) {
            String msg = settings.getNoBlockData();
            VIEWER.sendMessage(Utils.color(msg));
            VIEWER.closeInventory();
            return;
        }
        
        // Go to next page if only one spawner log.
        if(oneSpawner && pageIndex == 0) pageIndex++;
        
        this.buttons.clear();
        
        int lastSlot = 0;
        int lastEmptySlot = 9;
        
        if(pageIndex == 0) {
            String name = settings.correctLongNames("Select which Spawner to view LOGS of...");
            this.inv = Bukkit.createInventory(VIEWER, 9, Utils.mkDisplayReady(name));
            
            for(int i = 0; i < LOG.getSpawnerLogs().size(); i++) {
                MenuButton btn = new MenuButton(this, Data.OPEN_SPAWNER_LOGS, i);
                btn.generate();
                buttons.add(btn);
                lastSlot++;
            }
        }else {
            if(curSpawner == null) curSpawner = LOG.getSpawnerLogs().get(0);
            lastEmptySlot = 36;
            if(pageIndex == 1) lastSlot++;
            int startIndex = (pageIndex-1) * 35 + lastSlot;
            int endIndex = startIndex + 35;
            if(pageIndex == 1) endIndex--;
            
            String name = settings.correctLongNames(String.format("Viewing Spawner LOGS - Page %s", pageIndex));
            this.inv = Bukkit.createInventory(VIEWER, 54, Utils.mkDisplayReady(name));
            
            if(pageIndex == 1) {
                MenuButton cBtn = new MenuButton(this, Data.CREATION_EVENT, 0);
                cBtn.generate();
                buttons.add(cBtn);
            }
            
            int i = startIndex;
            List<Record> list = curSpawner.getRecords();
            int records = curSpawner.getRecords().size();
            
            for(i = startIndex; i <= endIndex; i++) {
                if(i >= records) break;
                if(list.get(i) instanceof ModificationRecord) continue;
                MenuButton btn = new MenuButton(this, Data.TRANSACTION_EVENT, lastSlot++);
                btn.generate();
                buttons.add(btn);
            }
            
            boolean addDeleteRecord = startIndex <= records && records <= endIndex;
            
            if(addDeleteRecord && curSpawner.getDeletionRecord() != null) {
                MenuButton dBtn = new MenuButton(this, Data.DELETION_EVENT, lastSlot++);
                dBtn.generate();
                buttons.add(dBtn);
            }
            
            ItemStack backBtn = new ItemStack(Material.ARROW, 1);
            Utils.setItemNameAndDesc(backBtn, "&cPrevious Page", Arrays.asList());
            inv.setItem(48, backBtn);
            
            ItemStack sBtn = new ItemStack(Material.MOB_SPAWNER, 1);
            Utils.setItemNameAndDesc(sBtn, "&7View all Spawners", Arrays.asList());
            inv.setItem(49, sBtn);
            
            ItemStack nBtn = new ItemStack(Material.ARROW, 1);
            Utils.setItemNameAndDesc(nBtn, "&aNext Page", Arrays.asList());
            inv.setItem(50, nBtn);
        }
        
        for(int i = lastSlot; i < lastEmptySlot; i++) {
            MenuButton btn = new MenuButton(this, Data.EMPTY_SLOT, i);
            btn.generate();
            buttons.add(btn);
        }
        
        if(openInv) VIEWER.openInventory(inv);
        VIEWER.updateInventory();
        
        // We have to do this because a close inventory event was called when #openInventory() was called.
        MenuManager.setMenu(VIEWER.getUniqueId(), this);

    }
    
    public void click(InventoryClickEvent evt) {
        final Inventory clickedInv = evt.getClickedInventory();
        int slot = evt.getRawSlot();
        
        if(clickedInv != null && clickedInv.equals(this.inv)) {
            Data clickedBtn = getClickedButtonType(slot);
            
            if(clickedBtn == Data.OPEN_SPAWNER_LOGS) {
                curSpawner = LOG.getSpawnerLogs().get(slot);
                pageIndex++;
                open(true);
            }else if(slot == 49) {
                pageIndex = 0;
                curSpawner = null;
                open(true);
            }else if(slot == 50) {
                pageIndex++;
                open(true);
            }else if(slot == 48 && pageIndex != 1) {
                pageIndex--;
                open(true);
            }
        }else {
            evt.setCancelled(true);
            evt.setCurrentItem(null);
            evt.setResult(Result.DENY);
        }
    }
    
    public LocationLog getLog() {
        return this.LOG;
    }
    
    public SpawnerLog getSpawnerLog() {
        return this.curSpawner;
    }
    
}
