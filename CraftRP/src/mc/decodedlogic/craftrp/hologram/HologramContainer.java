package mc.decodedlogic.craftrp.hologram;

import java.util.HashMap;

import mc.decodedlogic.craftrp.entity.CRPEntity;

public class HologramContainer {
    
    private final CRPEntity PARENT;
    
    // Key: Hologram id -> Value: RPHologram object
    private HashMap<String, RPHologram> holograms;
    
    public HologramContainer(CRPEntity parent) {
        this.PARENT = parent;
        this.holograms = new HashMap<String, RPHologram>();
    }
    
    public void addHologram(String name, RPHologram hologram) {
        if(hologram != null) {
            if(!hologram.getName().equalsIgnoreCase(name)) {
                hologram.setName(name);
            }
            
            holograms.put(name, hologram);
        }
    }
    
    public void removeHologram(String name) {
        if(holograms.containsKey(name)) {
            RPHologram hologram = holograms.get(name);
            hologram.destroy();
            holograms.remove(name);
        }
    }
    
    public RPHologram getHologramByName(String name) {
        if(holograms.containsKey(name)) {
            return holograms.get(name);
        }
        
        return null;
    }
    
    /**
     * Hides a specified {@link RPHologram} by name from all viewers.
     * @param name
     */
    
    public void hideFromAll(String name) {
        if(holograms.containsKey(name)) {
            RPHologram hologram = holograms.get(name);
            hologram.hideFromViewers();
        }
    }
    
    public CRPEntity getParent() {
        return this.PARENT;
    }
    
    public void clear() {
        for(RPHologram hologram : holograms.values()) {
            hologram.destroy();
        }
        
        holograms.clear();
    }
    
    public HashMap<String, RPHologram> getHolograms() {
        return this.holograms;
    }
}
