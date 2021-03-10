package mc.decodedlogic.craftrp.entity;

import org.bukkit.Location;
import org.bukkit.Material;

import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.PlayerPointer;

public abstract class CRPPlaceableEntity extends CRPEntity {
    
    // The player that originally placed this entity
    protected PlayerPointer owner;
    
    // Whether or not this entity has been placed yet
    protected boolean hasBeenPlaced;
    
    public CRPPlaceableEntity(CRPEntityType type) {
        super(type);
    }
    
    public abstract void generate();
    
    public abstract void onPlayerPlace(CRPPlayer p, Location origin);
    
    public abstract boolean canPlaceHere(CRPPlayer p, Location location);

    @Override
    public abstract void onPlayerInteract(CRPPlayer p, PlayerInteractEntityEvent evt);
    
    public void reset() {
        super.reset();
        
        blocks.forEach(b -> {
            b.setType(Material.AIR);
        });
        
        if(!origin.getBlock().isEmpty()) {
            origin.getBlock().setType(Material.AIR);
        }
    }
    
    public void onPlayerFocusGained(CRPPlayer p) {
        super.onPlayerFocusGained(p);
    }
    
    public void onPlayerFocusLost(CRPPlayer p) {
        super.onPlayerFocusLost(p);
    }
    
    public PlayerPointer getOwner() {
        return this.owner;
    }
    
    public boolean hasBeenPlaced() {
        return this.hasBeenPlaced;
    }

}
