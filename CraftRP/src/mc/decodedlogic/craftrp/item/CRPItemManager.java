package mc.decodedlogic.craftrp.item;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObjectManager;

public class CRPItemManager implements CRPRegistrableObjectManager {
    
    public static final String METADATA_ITEM_ID_KEY = "CRP_ItemId";
    
    protected static CRPItemManager hook;
    protected ConcurrentHashMap<Long, CRPRegistrableObject> registry;
    
    static {
        hook = null;
    }
    
    public CRPItemManager() {
        if(hook != null) return;
        
        CRPItemManager.hook = this;
        this.registry = new ConcurrentHashMap<Long, CRPRegistrableObject>();
    }
    
    public static void registerItem(CRPItem item, long id) {
        hook.register(item, id);
    }
    
    public static void unregisterItem(CRPItem item) {
        hook.unregister(item);
    }
    
    public static void unregisterAll() {
        hook.__unregisterAll();
    }
    
    public static CRPItem getItemById(long id) {
        return (CRPItem) hook.registry.get(id);
    }
    
    public static long getIdForItem(CRPItem item) {
        for(long id : hook.registry.keySet()) {
            CRPItem catItem = getItemById(id);
            
            if(catItem.getName().equals(item.getName())) {
                return catItem.getID();
            }
        }
        
        return -1;
    }
    
    public static CRPItem getCRPItemFromItemStack(ItemStack i) {
        if(i == null || (i != null && !i.hasItemMeta())) return null;
        
        PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = getItemIdKey();
        CRPItem item = null;
        
        if(container.has(key, PersistentDataType.LONG)) {
            long id = container.get(key, PersistentDataType.LONG);
            item = getItemById(id);
        }
        
        return item;
        
    }
    
    public static NamespacedKey getItemIdKey() {
        return new NamespacedKey(CraftRP.get(), CRPItemManager.METADATA_ITEM_ID_KEY);
    }

    @Override
    public ConcurrentHashMap<Long, CRPRegistrableObject> getRegistry() {
        return this.registry;
    }

}
