package mc.decodedlogic.craftrp.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public final class BlockUtils {
    
    public static Location getFrontCenter(Block block) {
        Location frontCenter = null;
        
        if(block.getBlockData() instanceof Directional) {
            Directional d = (Directional) block.getBlockData();
            return getFaceCenter(block, d.getFacing());
        }
        
        return frontCenter;
    }
    
    public static Location getRearCenter(Block block) {
        Location frontCenter = null;
        
        if(block.getBlockData() instanceof Directional) {
            Directional d = (Directional) block.getBlockData();
            return getFaceCenter(block, getOppositeFace(d.getFacing()));
        }
        
        return frontCenter;
    }
    
    
    public static Location getFaceCenter(Block block, BlockFace face) {
        Location c = block.getLocation().clone();
        c.add(0.5, 0, 0.5);
        
        Block other = block.getRelative(face);
        Location oC = other.getLocation().clone();
        oC.add(0.5, 0, 0.5);
        
        return getMidpoint(c, oC);
    }
    
    public static Location getMidpoint(Location l1, Location l2) {
        return new Location(l1.getWorld(), (l1.getX() + l2.getX()) / 2, 
                (l1.getY() + l2.getY()) / 2, 
                (l1.getZ() + l2.getZ()) / 2);
    }
    
    public static BlockFace getOppositeFace(BlockFace face) {
        
        if(face.name().contains("_")) {
            face = BlockFace.valueOf(face.name().substring(0, face.name().indexOf("_")));
        }
        
        switch(face) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            case DOWN:
                return BlockFace.UP;
            case UP:
                return BlockFace.DOWN;
            default:
                return face;
        }
    }
}
