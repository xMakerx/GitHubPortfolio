package mc.decodedlogic.craftrp;

import lib.decodedlogic.core.DecodedPlugin;
import lib.decodedlogic.dependency.Dependency;
import lib.decodedlogic.dependency.VaultDependency;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import mc.decodedlogic.craftrp.event.listener.PlayerEntityEvents;
import mc.decodedlogic.craftrp.event.listener.PlayerGUIEvents;
import mc.decodedlogic.craftrp.event.listener.PlayerManagerEvents;
import mc.decodedlogic.craftrp.item.CRPDiagnostic;
import mc.decodedlogic.craftrp.item.CRPItemManager;
import mc.decodedlogic.craftrp.item.CRPLockpick;
import mc.decodedlogic.craftrp.item.CRPRPG;
import mc.decodedlogic.craftrp.item.CRPThrowableDebris;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;

public class CraftRP extends DecodedPlugin {
    
    private static CraftRP hook;
    
    static {
        hook = null;
    }
    
    public static CraftRP get() {
        return CraftRP.hook;
    }
    
    public CraftRP() {
        super();
        
        new VaultDependency(this, true);
        new Dependency(this, "HolographicDisplays", true);
        
        // Setup managers
        new CRPEntityManager();
        new CRPItemManager();
        new CRPPlayerManager();
        
        CraftRP.hook = this;
    }
    
    public void onEnable() {
        super.onEnable();
        
        CRPItemManager.registerItem(new CRPRPG(), 0);
        CRPItemManager.registerItem(new CRPThrowableDebris(), 1);
        CRPItemManager.registerItem(new CRPLockpick(), 2);
        CRPItemManager.registerItem(new CRPDiagnostic(), 3);
        
        getServer().getPluginManager().registerEvents(new PlayerGUIEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerEntityEvents(this), this);
        getServer().getPluginManager().registerEvents(new PlayerManagerEvents(this), this);
    }
    
    public void onDisable() {
        super.onDisable();
        
        CRPEntityManager.unregisterAll();
        CRPItemManager.unregisterAll();
        CRPPlayerManager.unregisterAll();
    }
    
}
