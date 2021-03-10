package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class PlayerFocusEntityEvent extends CRPPlayerEvent {
    
    protected final CRPEntity ENTITY;
    protected final CRPEntity LAST_ENTITY;
    
    public PlayerFocusEntityEvent(CRPPlayer p, CRPEntity newFocus, CRPEntity lostFocus) {
        super(p);
        this.ENTITY = newFocus;
        this.LAST_ENTITY = lostFocus;
    }
    
    public CRPEntity getEntity() {
        return this.ENTITY;
    }
    
    public CRPEntity getLastFocusedEntity() {
        return this.LAST_ENTITY;
    }

}
