package mc.decodedlogic.craftrp.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lib.decodedlogic.gui.GUI;
import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public abstract class RPGUI extends GUI {
    
    public class ItemBuilder {
        
        private ItemStack item;
        
        public ItemBuilder(Material type) {
            this.item = new ItemStack(type, 1);
        }
        
        public ItemBuilder(Material type, String name) {
            this(type);
            this.setName(name);
        }
        
        public ItemBuilder setAmount(int amount) {
            item.setAmount(amount);
            return this;
        }
        
        public ItemBuilder setName(String name) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            
            item.setItemMeta(meta);
            return this;
        }
        
        public ItemBuilder setDescription(List<String> description) {
            List<String> clone = new ArrayList<String>();
            clone.addAll(description);
            
            ItemMeta meta = item.getItemMeta();
            meta.setLore(clone);
            
            item.setItemMeta(meta);
            return this;
        }
        
        public ItemStack get() {
            return this.item;
        }
        
    }
    
    protected final CRPPlayer PLAYER;
    
    public RPGUI(CRPPlayer player) {
        super(CraftRP.get(), player.getPlayer());
        this.PLAYER = player;
    }
    
    public CRPPlayer getPlayer() {
        return this.PLAYER;
    }

    public abstract void generate();
    
    public abstract void onClosed();
    
    public void update() {
        super.update();
    }

}
