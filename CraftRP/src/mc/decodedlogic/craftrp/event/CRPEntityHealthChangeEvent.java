package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.entity.CRPEntity;

public class CRPEntityHealthChangeEvent extends CRPEntityEvent {
    
    protected double newHealth;
    protected final double OLD_HEALTH;
    protected final boolean IS_MAX_HEALTH;
    
    public CRPEntityHealthChangeEvent(CRPEntity entity, double newHealth, double oldHealth, boolean isMaxHealth) {
        super(entity);
        this.newHealth = newHealth;
        this.OLD_HEALTH = oldHealth;
        this.IS_MAX_HEALTH = isMaxHealth;
    }
    
    public void setNewHealth(double updatedHealth) {
        double checkHealth = 0;
        
        if(IS_MAX_HEALTH) {
            this.ENTITY.setMaxHealth(updatedHealth, true);
            checkHealth = this.ENTITY.getMaxHealth();
        }else {
            this.ENTITY.setHealth(updatedHealth, true);
            checkHealth = this.ENTITY.getHealth();
        }
        
        if(checkHealth == updatedHealth) {
            this.newHealth = updatedHealth;
        }
    }
    
    public double getNewHealth() {
        return this.newHealth;
    }
    
    public double getOldHealth() {
        return this.OLD_HEALTH;
    }
    
    public boolean isMaxHealthChange() {
        return this.IS_MAX_HEALTH;
    }

}
