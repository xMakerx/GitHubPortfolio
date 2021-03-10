package mc.decodedlogic.craftrp.item;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import net.md_5.bungee.api.ChatColor;

public class CRPDiagnostic extends CRPItem {
    
    public CRPDiagnostic() {
        super(CRPItemType.TOOL);
        this.name = ChatColor.GREEN + "Diagnostic Tool";
        this.material = Material.COMPARATOR;
        this.id = CRPItemManager.getIdForItem(this);
    }
    
    public void onPlayerUse(PlayerInteractEvent evt) {
        Action a = evt.getAction();
        
        if(a == Action.RIGHT_CLICK_BLOCK) {
            Block b = evt.getClickedBlock();
            CRPEntity ent = CRPEntityManager.getEntityFromBlock(b);
            
            if(ent != null) {
                user.getPlayer().sendMessage("That block is an entity!");
                user.getPlayer().sendMessage("Num of blocks in entity: " + ent.getBlocks().size());
            }
        }
    }

    @Override
    public ItemStack getUpdatedItem(ItemStack current) {
        return this.generateMCItem();
    }

}
