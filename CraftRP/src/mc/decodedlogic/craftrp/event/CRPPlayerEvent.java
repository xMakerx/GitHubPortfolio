package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.player.CRPPlayer;

public abstract class CRPPlayerEvent extends CRPEvent {
    
    protected final CRPPlayer PLAYER;
    
    public CRPPlayerEvent(CRPPlayer p) {
        super();
        this.PLAYER = p;
    }
    
    public CRPPlayer getCRPPlayer() {
        return this.PLAYER;
    }

}
