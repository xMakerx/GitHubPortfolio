package mc.decodedlogic.craftrp.player;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import mc.decodedlogic.craftrp.registry.CRPRegistrableObject;
import mc.decodedlogic.craftrp.registry.CRPRegistrableObjectManager;

public class CRPPlayerManager implements CRPRegistrableObjectManager {
    
    public static final String METADATA_CRPPLAYER_ID_KEY = "CRP_PlayerId";
    protected static CRPPlayerManager hook;
    protected ConcurrentHashMap<Long, CRPRegistrableObject> registry;
    
    static {
        hook = null;
    }
    
    public CRPPlayerManager() {
        if(hook != null) return;
        CRPPlayerManager.hook = this;
        this.registry = new ConcurrentHashMap<Long, CRPRegistrableObject>();
    }
    
    public static void registerPlayer(CRPPlayer p) {
        CRPPlayerManager.hook.register(p);
    }
    
    public static void unregisterPlayer(CRPPlayer p) {
        CRPPlayerManager.hook.unregister(p);
    }
    
    public static void unregisterAll() {
        hook.__unregisterAll();
    }
    
    public static CRPPlayer getCRPPlayerFromPlayer(Player p) {
        if(p == null) return null;
        
        if(p.isOnline() && p.hasMetadata(METADATA_CRPPLAYER_ID_KEY)) {
            if(p.getMetadata(METADATA_CRPPLAYER_ID_KEY).size() > 0) {
                long id = p.getMetadata(METADATA_CRPPLAYER_ID_KEY).get(0).asLong();
                return getCRPPlayerFromId(id);
            }
        }
        
        return null;
    }
    
    public static CRPPlayer getCRPPlayerFromId(long id) {
        return (CRPPlayer) hook.registry.get(id);
    }

    @Override
    public ConcurrentHashMap<Long, CRPRegistrableObject> getRegistry() {
        return this.registry;
    }
    
}
