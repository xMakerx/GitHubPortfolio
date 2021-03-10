package mc.decodedlogic.craftrp.item;

import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.netty.util.internal.ThreadLocalRandom;
import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import mc.decodedlogic.craftrp.entity.CRPProp;
import mc.decodedlogic.craftrp.entity.CRPWindow;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_15_R1.Vec3D;

public class CRPRPG extends CRPItem implements Listener {
    
    public static final int FLIGHT_TIME = 4;
    public static final float EXPLOSION_RADIUS = 6.0F;
    public static final String METADATA_ROCKET_NAME = "CRP_Rocket";
    public static final double MAXIMUM_DAMAGE = 150.0;
    
    public CRPRPG() {
        super(CRPItemType.WEAPON);
        this.name = ChatColor.RED + "RPG";
        this.description.add(ChatColor.YELLOW + "An RPG, very destructive");
        this.material = Material.BOW;
        this.activationCooldown = 5000;
        this.id = CRPItemManager.getIdForItem(this);
    }
    
    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent evt) {
        Firework f = evt.getEntity();
        
        if(f.hasMetadata(METADATA_ROCKET_NAME)) {
            f.getWorld().createExplosion(f.getLocation(), EXPLOSION_RADIUS, false, true, f);
            
            f.getWorld().getNearbyEntities(f.getLocation(), 3, 3, 3).stream().filter(e -> e instanceof Player)
                .forEach(e -> {
                    Player p = (Player) e;
                    
                    if(p != null) {
                        p.damage(5.0d);
                    }
                });
        }
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent evt) {
        if(evt.getCause() == RemoveCause.EXPLOSION) {
            evt.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent evt) {
        Entity ent = evt.getEntity();
        
        if(ent instanceof Firework) {
            Firework f = (Firework) ent;
            
            if(f.hasMetadata(METADATA_ROCKET_NAME)) {
                Iterator<Block> iterator = evt.blockList().iterator();
                
                while(iterator.hasNext()) {
                    Block b = iterator.next();
                    
                    if(CRPEntityManager.isEntityBlock(b)) {
                        CRPEntity entity = CRPEntityManager.getEntityFromBlock(b);
                        
                        if(entity != null && (entity instanceof CRPProp || entity instanceof CRPWindow)) {
                            double dist = f.getLocation().distance(entity.getOrigin());
                            double randOffset = ThreadLocalRandom.current().nextDouble(15.0, 30.0);
                            double damage = MAXIMUM_DAMAGE - (randOffset * dist);
                            
                            entity.damage(damage);
                        }
                    }
                    
                    iterator.remove();
                }
            }
        }
        
        evt.blockList().clear();
    }

    @Override
    public void onPlayerUse(PlayerInteractEvent evt) {
        boolean fire = false;
        Player p = user.getPlayer();
        
        for(ItemStack item : p.getInventory().getContents()) {
            if(item == null || (item != null && item.getType() == Material.AIR)) continue;
            
            if(item.getType() == Material.FIREWORK_ROCKET) {
                item.setAmount(item.getAmount() - 1);
                fire = true;
                break;
            }
        }
        
        
        Location l = p.getEyeLocation();
        Vector v = l.getDirection();
        
        double speed = 0.5d;
        l.add(l.getDirection().multiply(2));
        v.normalize().multiply(speed);
        
        if(!fire) {
            l.getWorld().playSound(l, Sound.UI_BUTTON_CLICK, 1F, 1F);
            return;
        }
        
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BREAK, 25F, 0.6F);
        l.getWorld().playEffect(l, Effect.STEP_SOUND, Material.STONE_BRICKS);
        
        Firework f = l.getWorld().spawn(l, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.setPower(FLIGHT_TIME * 2);
        fm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).flicker(false).trail(false).build());
        f.setMetadata(METADATA_ROCKET_NAME, new FixedMetadataValue(CraftRP.get(), true));
        f.setFireworkMeta(fm);
        
        long startTime = System.currentTimeMillis();
        
        new BukkitRunnable() {
            
            boolean flySoundPlayed = false;
            
            public void run() {
                f.setTicksLived(1);
                
                l.getWorld().spawnParticle(Particle.CLOUD, l, 10, 0.6, 0.6, 0.6, 0);
                
                l.add(v);
                l.setPitch(90);
                f.teleport(l);
                
                if(System.currentTimeMillis() > startTime + ((FLIGHT_TIME * 1000) + 1000)) {
                    this.cancel();
                    return;
                }
                
                if(System.currentTimeMillis() > (startTime + 250) && !flySoundPlayed) {
                    l.getWorld().playSound(l, Sound.ENTITY_GHAST_SCREAM, 30F, 0.625F);
                    flySoundPlayed = true;
                }
                
                if(f.isOnGround() || f.getLocation().getBlock().getType() != Material.AIR) {
                    f.teleport(l.subtract(v));
                    f.detonate();
                    this.cancel();
                }
                
                Vec3D v3 = new Vec3D(l.getX(), l.getY(), l.getZ());
                
                for(Entity e : f.getNearbyEntities(2, 2, 2)) {
                    if(((CraftEntity) e).getHandle().getBoundingBox().c(v3)) {
                        f.detonate();
                        this.cancel();
                        return;
                    }
                }
            }
            
        }.runTaskTimer(CraftRP.get(), 0, 1);
    }

    @Override
    public ItemStack getUpdatedItem(ItemStack current) {
        return generateMCItem();
    }

}
