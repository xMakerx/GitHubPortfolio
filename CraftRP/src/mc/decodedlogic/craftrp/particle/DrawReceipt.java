package mc.decodedlogic.craftrp.particle;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class DrawReceipt {
    
    private ParticleType type;
    private Location origin;
    private Vector offset;
    private long drawTimeMs;
    
    public DrawReceipt(ParticleType type, Location origin, Vector offset, long drawTimeMs) {
        this.type = type;
        this.origin = origin;
        this.offset = offset;
        this.drawTimeMs = drawTimeMs;
    }
    
    public ParticleType getDrawnType() {
        return this.type;
    }
    
    public Location getOrigin() {
        return this.origin;
    }
    
    public Vector getOffset() {
        return this.offset;
    }
    
    public long getDrawTime() {
        return this.drawTimeMs;
    }
    
}
