package mc.decodedlogic.craftrp.entity;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class CRPWindow extends CRPEntity {

    public CRPWindow(Location origin) {
        super(CRPEntityType.WINDOW);
        this.origin = origin;
        this.maxHealth = 10.0;
        this.health = maxHealth;
        this.showDmgProgress = true;
        
        CRPEntityManager.registerEntity(this);
        considerNeighbors(origin.getBlock());
    }
    
    private void considerNeighbors(Block block) {
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN };
        
        if(!block.isEmpty() && block.getType().name().endsWith("GLASS_PANE")) {
            if(blocks.contains(block)) return;
            
            if(block != origin.getBlock()) blocks.add(block);
            
            for(BlockFace face : faces) {
                considerNeighbors(block.getRelative(face));
            }
        }
    }
    
    public void damage(double damage) {
        super.damage(damage);
        origin.getWorld().playSound(origin, Sound.BLOCK_GLASS_BREAK, 4F, 1F);
        
        for(Block b : getAllBlocks()) {
            Location l = b.getLocation().clone();
            l.add(0.5, 0.5, 0.5);
            b.getWorld().spawnParticle(Particle.BLOCK_DUST, b.getLocation(), 25, b.getType().createBlockData());
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
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
