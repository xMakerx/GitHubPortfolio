package mc.decodedlogic.craftrp.registry;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mc.decodedlogic.craftrp.CraftRP;

public interface CRPRegistrableObjectManager {
    
    public default void register(CRPRegistrableObject object) {
        register(object, assignUniqueId());
    }
    
    public default void register(CRPRegistrableObject object, long assignedID) {
        if(object == null) return;
        
        if(!getRegistry().values().contains(object)) {
            long id = assignedID;
            
            getRegistry().put(id, object);
            object.onRegistered(id);
            
            if(object instanceof Listener) {
                // Register events if necessary.
                CraftRP.get().getServer().getPluginManager().registerEvents(((Listener) object), CraftRP.get());
            }
        }
    }
    
    public default void unregister(CRPRegistrableObject object) {
        if(object == null) return;
        
        if(getRegistry().remove(object.getID()) != null) {
            object.onUnregistered();
            
            if(object instanceof Listener) {
                // Unregister events if necessary.
                HandlerList.unregisterAll(((Listener) object));
            }
        }
    }
    
    public default void __unregisterAll() {
        Iterator<CRPRegistrableObject> iterator = getRegistry().values().iterator();
        
        while(iterator.hasNext()) {
            unregister(iterator.next());
        }
    }
    
    public default long assignUniqueId() {
        // Generate a new and unique id for this entity.
        Random rand = new Random();
        long id = 0L;
        
        do {
            id = rand.nextLong();
        } while (getRegistry().containsKey(id));
        
        return id;
    }
    
    public ConcurrentHashMap<Long, CRPRegistrableObject> getRegistry();
    
}
