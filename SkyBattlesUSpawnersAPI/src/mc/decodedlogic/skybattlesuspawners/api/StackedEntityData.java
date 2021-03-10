package mc.decodedlogic.skybattlesuspawners.api;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;

public class StackedEntityData {
    
    private final Entity entity;
    private final MobSpawner spawner;
    private final int quantity;
    private final int upgradeIndex;
    
    public StackedEntityData() {
        this(null, null, 0, -1);
    }
    
    public StackedEntityData(Entity entity) {
        this.entity = entity;
        if(entity != null) {
            
            MetadataValue mdSpawner, mdQuantity, mdUpgrade;
            mdSpawner = USpawnersAPI.getFirstValueInKey(entity, 
                    USpawnersAPI.ENT_SPAWNER_KEY);
            mdQuantity = USpawnersAPI.getFirstValueInKey(entity, USpawnersAPI.ENT_QUANTITY_KEY);
            mdUpgrade = USpawnersAPI.getFirstValueInKey(entity, USpawnersAPI.ENT_UPGRADE_KEY);
            
            if(mdSpawner != null) {
                this.spawner = SpawnerManager.getSpawner(mdSpawner.asInt());
                this.quantity = mdQuantity.asInt();
                this.upgradeIndex = mdUpgrade.asInt();
            }else {
                this.spawner = null;
                this.quantity = 0;
                this.upgradeIndex = -1;
            }

        }else {
            this.spawner = null;
            this.quantity = 0;
            this.upgradeIndex = -1;
        }
    }
    
    public StackedEntityData(Entity entity, MobSpawner spawner, int quantity, int upgradeIndex) {
        this.entity = entity;
        this.spawner = spawner;
        this.quantity = quantity;
        this.upgradeIndex = upgradeIndex;
    }
    
    /**
     * Fetches the base {@link Entity} that holds this data.
     * @return Entity
     */
    
    public Entity getEntity() {
        return this.entity;
    }
    
    /**
     * Fetches the {@link MobSpawner} that spawned this entity.
     * @return MobSpawner
     */
    
    public MobSpawner getMobSpawner() {
        return this.spawner;
    }
    
    /**
     * Fetches the amount of entities in the stack.
     * @return int
     */
    
    public int getQuantity() {
        return this.quantity;
    }
    
    /**
     * Fetches the index of the upgrade on this entity.
     * @return
     */
    
    public int getUpgradeIndex() {
        return this.upgradeIndex;
    }

}
