package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.item.CRPItem;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class PlayerActivateItemEvent extends CRPPlayerEvent {
    
    protected final CRPItem ITEM;
    
    public PlayerActivateItemEvent(CRPPlayer player, CRPItem item) {
        super(player);
        this.ITEM = item;
    }
    
    public CRPItem getItem() {
        return this.ITEM;
    }
}
