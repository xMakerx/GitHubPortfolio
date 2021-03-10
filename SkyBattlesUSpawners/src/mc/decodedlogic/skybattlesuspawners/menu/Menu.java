package mc.decodedlogic.skybattlesuspawners.menu;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import mc.decodedlogic.skybattlesuspawners.IPluginObject;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton.Data;

public abstract class Menu implements IPluginObject {
    
    protected final Player VIEWER;
    protected int pageIndex;
    
    protected Inventory inv;
    protected Set<MenuButton> buttons;
    
    public Menu(@Nonnull Player viewer) {
        this.VIEWER = viewer;
        this.pageIndex = 0;
        this.buttons = new HashSet<MenuButton>();
        this.inv = null;
    }
    
    public abstract void click(InventoryClickEvent evt);
    public abstract void open(boolean openInv);

    public void close() {
        new BukkitRunnable() {
            
            public void run() {
                VIEWER.closeInventory();
            }
            
        }.runTaskLater(USpawners.get(), 1L);
    }
    
    protected Data getClickedButtonType(int slot) {
        for(MenuButton btn : buttons) {
            if(btn.getSlot() == slot) {
                return btn.getButtonData();
            }
        }
        
        return null;
    }
    
    public void cleanup() {
        this.buttons.clear();
    }
    
    public Player getViewer() {
        return this.VIEWER;
    }
    
    public int getPageIndex() {
        return this.pageIndex;
    }
    
    public Inventory getCraftInventory() {
        return this.inv;
    }

}
