package mc.decodedlogic.craftrp.mouse;

import mc.decodedlogic.craftrp.player.CRPPlayer;

public interface MouseListener {
    
    public default void onMouseReleased(CRPPlayer player, UserTracker tracker) {}
    
    public default void onMouseDown(CRPPlayer player, UserTracker tracker) {}
    
    public default void onFullChargeObtained(CRPPlayer player, UserTracker tracker) {}
}
