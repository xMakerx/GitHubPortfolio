package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.player.CRPPlayer;

public class PlayerWantedRatingChangeEvent extends CRPPlayerEvent {
    
    private int newWantedRating;
    private final int OLD_WANTED_RATING;
    
    public PlayerWantedRatingChangeEvent(CRPPlayer p, int newWantedRating, int oldWantedRating) {
        super(p);
        this.newWantedRating = newWantedRating;
        this.OLD_WANTED_RATING = oldWantedRating;
    }
    
    public void setNewWantedRating(int updatedRating) {
        if(updatedRating == newWantedRating) return;
        
        this.PLAYER.setWantedRating(updatedRating, true);
        
        if(this.PLAYER.getWantedRating() != newWantedRating) {
            this.newWantedRating = this.PLAYER.getWantedRating();
        }
    }
    
    public int getNewWantedRating() {
        return this.newWantedRating;
    }
    
    public int getOldWantedRating() {
        return this.OLD_WANTED_RATING;
    }
    
    
}
