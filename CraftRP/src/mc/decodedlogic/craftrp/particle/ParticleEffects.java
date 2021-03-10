package mc.decodedlogic.craftrp.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.Vector;

public class ParticleEffects {
    
    public static DrawReceipt drawDustCircle(Location origin, Color dustColor, float dustSize, float radius, 
            float offsetX, float offsetY, float offsetZ, int numParticles) {

        float angle = 0f;
        DustOptions dustOptions = new DustOptions(dustColor, dustSize);
        
        do {
            double x = (radius * Math.sin(angle));
            double z = (radius * Math.cos(angle));
            angle += 10.0; //orig: 0.1
            
            // Shorthand way to get coordinates of the origin.
            double oX = origin.getX();
            double oY = origin.getY();
            double oZ = origin.getZ();
            
            Location l = new Location(origin.getWorld(), 
                    oX+x+offsetX, 
                    oY+offsetY, 
                    oZ+z+offsetZ);
            
            l.getWorld().spawnParticle(Particle.REDSTONE, l, numParticles, dustOptions);
            
        } while (angle <= 360);
        
        Vector offsetVec = new Vector(offsetX, offsetY, offsetZ);
        
        DrawReceipt receipt = new DrawReceipt(ParticleType.CIRCLE, origin, 
                offsetVec, System.currentTimeMillis());
        
        System.out.println("Num Particles generated: " + ((360.0/10.0) * numParticles));
        return receipt;
        
    }
    
    public static void drawDustAt(Location origin, Color dustColor, float dustSize, int numParticles) {
        DustOptions dustOptions = new DustOptions(dustColor, dustSize);
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin, numParticles, dustOptions);
    }
    
}
