package mc.decodedlogic.craftrp.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class PlayerInteractEntityEvent extends CRPEntityEvent {
    
    protected final CRPPlayer PLAYER;
    protected final PlayerInteractEvent INTERACT_EVENT;
    
    public PlayerInteractEntityEvent(CRPPlayer player, CRPEntity entity, PlayerInteractEvent evt) {
        super(entity);
        this.PLAYER = player;
        this.INTERACT_EVENT = evt;
        
        this.cancelled = (evt != null) ? evt.useInteractedBlock() == Result.DENY : false;
    }
    
    public void setCancelled(boolean flag) {
        this.cancelled = flag;
        
        if(flag && INTERACT_EVENT != null) {
            INTERACT_EVENT.setUseInteractedBlock(Result.DENY);
            INTERACT_EVENT.setUseItemInHand(Result.DENY);
        }
    }
    
    public CRPPlayer getPlayer() {
        return this.PLAYER;
    }
    
    public Action getAction() {
        Action action = (INTERACT_EVENT != null) ? INTERACT_EVENT.getAction() : null;
        return action;
    }
    
    public boolean isLeftClick() {
        Action action = getAction();
        return (action != null && action == Action.LEFT_CLICK_BLOCK);
    }
    
    public boolean isRightClick() {
        Action action = getAction();
        return (action != null && action == Action.RIGHT_CLICK_BLOCK);
    }
    
    public Block getClickedBlock() {
        Block b = (INTERACT_EVENT != null) ? INTERACT_EVENT.getClickedBlock() : null;
        return b;
    }
    
    public double getDistance() {
        double distance = -1.0d;
        
        final Block CLICKED = getClickedBlock();
        Location origin = (CLICKED != null) ? CLICKED.getLocation() : ENTITY.getOrigin();
        
        distance = PLAYER.getPlayer().getLocation().distance(origin);
        return distance;
    }
    
    public PlayerInteractEvent getInteractEvent() {
        return this.INTERACT_EVENT;
    }
    
}
