package mc.decodedlogic.craftrp.entity;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Block;

import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObjectManager;

public class CRPEntityManager implements CRPRegistrableObjectManager {
    
    public static final String METADATA_ENTITY_ID_KEY = "CRP_EntityId";
    protected static CRPEntityManager hook;
    protected ConcurrentHashMap<Long, CRPRegistrableObject> registry;
    
    static {
        hook = null;
    }
    
    public CRPEntityManager() {
        if(hook != null) return;
        CRPEntityManager.hook = this;
        this.registry = new ConcurrentHashMap<Long, CRPRegistrableObject>();
    }
    
    public static void registerEntity(CRPEntity entity) {
        hook.register(entity);
    }
    
    public static void unregisterEntity(CRPEntity entity) {
        hook.unregister(entity);
    }
    
    public static void unregisterAll() {
        hook.__unregisterAll();
    }
    
    public static boolean isEntityBlock(Block b) {
        return (b != null && b.hasMetadata(METADATA_ENTITY_ID_KEY));
    }
    
    public static CRPEntity getEntityFromBlock(Block b) {
        if(isEntityBlock(b) && b.getMetadata(METADATA_ENTITY_ID_KEY).size() > 0) {
            long id = b.getMetadata(METADATA_ENTITY_ID_KEY).get(0).asLong();
            return getEntityByID(id);
        }
        
        return null;
    }
    
    public static CRPEntity getEntityByID(long id) {
        return (CRPEntity) hook.registry.get(id);
    }

    @Override
    public ConcurrentHashMap<Long, CRPRegistrableObject> getRegistry() {
        return this.registry;
    }
    
}
