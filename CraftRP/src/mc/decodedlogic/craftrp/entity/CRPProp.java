package mc.decodedlogic.craftrp.entity;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class CRPProp extends CRPEntity {
    
    protected Material origMat;
    protected BlockData origData;
    
    public CRPProp(Location origin, double health, double maxHealth) {
        super(CRPEntityType.PROP);
        this.name = "Prop";
        this.origin = origin;
        this.health = health;
        this.maxHealth = maxHealth;
        this.origMat = origin.getBlock().getType();
        this.origData = origin.getBlock().getBlockData();
        
        if(origin.getBlock().getType() != Material.AIR) {
            CRPEntityManager.registerEntity(this);
        }
    }

    @Override
    public void reset() {
        super.reset();
        
        Block b = origin.getBlock();
        b.setType(origMat);
        b.setBlockData(origData);
    }
    
    public void setHealth(double newHealth, boolean silent) {
        double oldHealth = health;
        super.setHealth(newHealth, silent);
        
        if(oldHealth != health) {
            if(health != 0) {
                origin.getWorld().playSound(origin, Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            }else {
                origin.getBlock().setType(Material.AIR);
                origin.getWorld().playEffect(origin, Effect.WITHER_BREAK_BLOCK, 1);
            }
        }
    }

    @Override
    public void onPlayerInteract(CRPPlayer p, PlayerInteractEntityEvent evt) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getHelpInformation(CRPPlayer rpPlayer) {
        // TODO Auto-generated method stub
        return "";
    }

}
