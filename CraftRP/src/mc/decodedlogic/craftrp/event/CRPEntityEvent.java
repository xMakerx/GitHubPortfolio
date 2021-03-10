package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.entity.CRPEntity;

public abstract class CRPEntityEvent extends CRPEvent {
    
    protected final CRPEntity ENTITY;
    
    public CRPEntityEvent(CRPEntity entity) {
        this.ENTITY = entity;
    }
    
    public CRPEntity getEntity() {
        return this.ENTITY;
    }
    
}
