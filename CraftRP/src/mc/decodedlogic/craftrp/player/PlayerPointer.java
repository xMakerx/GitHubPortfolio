package mc.decodedlogic.craftrp.player;

import java.util.UUID;

import org.bukkit.entity.Player;

import mc.decodedlogic.craftrp.CraftRP;

public final class PlayerPointer {
    
    private final String NAME;
    private final UUID ID;
    
    public PlayerPointer(Player p) {
        this.NAME = p.getName();
        this.ID = p.getUniqueId();
    }
    
    public PlayerPointer(String name, UUID id) {
        this.NAME = name;
        this.ID = id;
    }
    
    public PlayerPointer(String name, String uuid) {
        this.NAME = name;
        this.ID = UUID.fromString(uuid);
    }
    
    public String getName() {
        return this.NAME;
    }
    
    public UUID getUUID() {
        return this.ID;
    }
    
    public String getUUIDAsString() {
        return this.ID.toString();
    }
    
    public boolean isOnline() {
        CraftRP main = CraftRP.get();
        
        for(Player p : main.getServer().getOnlinePlayers()) {
            if(p == null) continue;
            
            if(p.getUniqueId().equals(ID)) return true;
            if(p.getName().equalsIgnoreCase(NAME) || p.getDisplayName().equalsIgnoreCase(NAME)) return true;
        }
        
        return false;
    }
    
}
