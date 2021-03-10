package mc.decodedlogic.craftrp.item;

import java.util.Optional;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.entity.CRPEntityManager;
import mc.decodedlogic.craftrp.entity.CRPProp;
import mc.decodedlogic.craftrp.entity.CRPWindow;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter;
import mc.decodedlogic.craftrp.mouse.HeldMouseAdapter.HoldMode;
import mc.decodedlogic.craftrp.mouse.UserTracker;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;
import net.md_5.bungee.api.ChatColor;

public class CRPThrowableDebris extends CRPItem {
    
    public static final String METADATA_DEBRIS_NAME = "CRP_Debris";
    public static final String METADATA_DEBRIS_OBJECT_NAME = "CRP_Thrown_Debris";
    public static final String METADATA_DEBRIS_THROWER_ID = "CRP_Thrower";
    
    public static final long DEBRIS_LIFESPAN = 3500L;
    public static final long CARDBOARD_BOX_EFFECT_DURATION_TICKS = 60L;
    
    public enum Debris {
        BRICK(Material.BRICK, (byte) 0, 3.5d),
        STICK(Material.STICK, (byte) 1, 0.5d),
        PEBBLE(Material.STONE_BUTTON, (byte) 2, 1.5d),
        CARDBOARD_BOX(Material.PLAYER_HEAD, (byte) 3, 1.5d);
        
        private final Material ITEM_MATERIAL;
        private final byte ID;
        private final double DAMAGE;
        
        private Debris(Material material, byte id, double damage) {
            this.ITEM_MATERIAL = material;
            this.ID = id;
            this.DAMAGE = damage;
        }
        
        public Material getMaterial() {
            return this.ITEM_MATERIAL;
        }
        
        public byte getID() {
            return this.ID;
        }
        
        public double getDamage() {
            return this.DAMAGE;
        }
        
    }
    
    public CRPThrowableDebris() {
        super(CRPItemType.DEBRIS);
        this.name = ChatColor.DARK_GREEN + "Garbage";
        this.description.add(ChatColor.WHITE + "It's better than nothing");
        this.id = CRPItemManager.getIdForItem(this);
        this.activationCooldown = 500;
        this.mouseAdapter = new HeldMouseAdapter(this);
        this.mouseAdapter.setHoldMode(HoldMode.CHARGE_AND_WAIT);
        this.mouseAdapter.setDesiredDownTimeMilliseconds(1000);
        this.mouseAdapter.setProcessTaskStepTicks(1L);
        this.mouseAdapter.setCancelAfterTimeMilliseconds(205);
    }
    
    @SuppressWarnings("deprecation")
    public ItemStack generateMCItem(Debris debris) {
        this.material = debris.getMaterial();
        
        ItemStack generated = super.generateMCItem();
        
        if(debris == Debris.CARDBOARD_BOX) {
            SkullMeta sm = (SkullMeta) generated.getItemMeta();;
            sm.setOwner("BoxMan01234");
            generated.setItemMeta(sm);
        }
        
        ItemMeta meta = generated.getItemMeta();
        
        NamespacedKey key = new NamespacedKey(CraftRP.get(), METADATA_DEBRIS_NAME);
        
        meta.getPersistentDataContainer().set(key, 
                PersistentDataType.BYTE, debris.getID());
        generated.setItemMeta(meta);
        
        return generated;
    }
    
    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent evt) {
        Entity e = evt.getEntity();
        Item i = evt.getItem();
        
        if(i.hasMetadata(METADATA_DEBRIS_OBJECT_NAME) && i.getMetadata(METADATA_DEBRIS_OBJECT_NAME).size() > 0) {
            
            if(e instanceof Player) {
                Player p = ((Player) e);
                
                handleCollision(i, p);
                
            }
            
            evt.setCancelled(true);
        }
    }
    
    public void handleCollision(Item item, LivingEntity hit) {
        if(item == null) return;
        
        String debrisTypeName = item.getMetadata(METADATA_DEBRIS_OBJECT_NAME).get(0).asString();
        long boxEffectDuration = CARDBOARD_BOX_EFFECT_DURATION_TICKS;

        Debris debrisType = Debris.valueOf(debrisTypeName);
        if(hit != null) {
            hit.damage(debrisType.getDamage());
            
            if(debrisType == Debris.CARDBOARD_BOX) {
                hit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) boxEffectDuration, 1));
                hit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) boxEffectDuration, 1));
            }
            
            Location l = hit.getLocation().clone();
            
            hit.setVelocity(l.getDirection().multiply(-1)
                    .multiply(1.35).add(new Vector(0, 0.225, 0)));
            
            if(hit instanceof Player) {
                long throwerId = item.getMetadata(METADATA_DEBRIS_THROWER_ID).get(0).asLong();
                
                if(debrisType == Debris.CARDBOARD_BOX) {
                    ItemStack box = generateMCItem(debrisType);
                    Player p = (Player) hit;
                    
                    p.getInventory().setHelmet(box);
                    p.updateInventory();
                    
                    new BukkitRunnable() {
                        
                        public void run() {
                            p.getInventory().setHelmet(null);
                            p.updateInventory();
                        }
                        
                    }.runTaskLater(CraftRP.get(), boxEffectDuration);
                }
                
                CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromId(throwerId);
               
                if(rpPlayer != null) rpPlayer.setWantedRating(rpPlayer.getWantedRating()+5);
            }
        }
        
        
        item.getWorld().spawnParticle(Particle.SMOKE_NORMAL, item.getLocation(), 20);
        item.getWorld().playSound(item.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 5F, 4F);
        item.remove();
    }
    
    public void onMouseDown(CRPPlayer rpPlayer, UserTracker tracker) {
        tracker.sendChargeBar("Power", 40, System.currentTimeMillis()-tracker.getDownBeginTime());
        setState(ItemState.ACTIVE);
    }
    
    public void onMouseReleased(CRPPlayer rpPlayer, UserTracker tracker) {
        super.onMouseReleased(rpPlayer, tracker);
        
        Player p = rpPlayer.getPlayer();
        double power = tracker.getPower();
        
        ItemStack held = this.usedItemStack;
        
        if(held.getAmount() == 1) {
            p.getInventory().setItemInMainHand(null);
            user.setHeldItem(null);
        }else {
            held.setAmount(held.getAmount()-1);
            p.getInventory().setItemInMainHand(held);
        }
        
        Debris debrisType = getDebrisTypeFromItem(held); 
        ItemStack toThrow = this.generateMCItem(debrisType);
        Item thrown = p.getWorld().dropItem(p.getEyeLocation(), toThrow);
        
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 2F, 4F);
        
        thrown.setMetadata(METADATA_DEBRIS_OBJECT_NAME, new FixedMetadataValue(CraftRP.get(), debrisType.name()));
        thrown.setMetadata(METADATA_DEBRIS_THROWER_ID, new FixedMetadataValue(CraftRP.get(), rpPlayer.getID()));
        thrown.setVelocity(p.getEyeLocation().getDirection());
        thrown.getVelocity().multiply(power);
        
        thrown.setPickupDelay(120);
        
        long start = System.currentTimeMillis();
        
        new BukkitRunnable() {
            
            public void run() {
                Location l = thrown.getLocation();
                Block inside = l.getBlock();
                Block inFront = l.clone().add(l.getDirection()).multiply(1.5).getBlock();
                Optional<Entity> hit = l.getWorld().getNearbyEntities(l, 0.5, 0.5, 0.5).stream().filter(e -> e instanceof Player).findFirst();
                
                if(hit.isPresent() && hit != null) {
                    if(hit.get() instanceof LivingEntity) {
                        LivingEntity le = ((LivingEntity) hit.get());
                        
                        if(le.getEntityId() != p.getEntityId()) {
                            handleCollision(thrown, le);
                            this.cancel();
                            return;
                        }
                    }
                }
                
                if((inFront != null && inFront.getType() != Material.AIR) || (inside != null && inside.getType() != Material.AIR) || thrown.isOnGround()) {
                    Block blockHit = (inFront != null && !inFront.isEmpty()) ? inFront : (inside != null && !inside.isEmpty()) ? inside : null;
                    
                    if(blockHit != null) {
                        if(CRPEntityManager.isEntityBlock(blockHit)) {
                            CRPEntity entity = CRPEntityManager.getEntityFromBlock(blockHit);
                            
                            if(entity != null && (entity instanceof CRPProp || entity instanceof CRPWindow)) {
                                entity.damage(debrisType.DAMAGE);
                            }
                        }
                    }
                    
                    handleCollision(thrown, null);
                    
                    this.cancel();
                    return;
                }
                
                if(System.currentTimeMillis() >= (start + DEBRIS_LIFESPAN)) {
                    this.cancel();
                    
                    thrown.getWorld().playEffect(l, Effect.SMOKE, 20);
                    thrown.getWorld().playSound(l, Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1F);
                    thrown.remove();
                }
            }
            
        }.runTaskTimer(CraftRP.get(), 0L, 1L);
        
        setState(ItemState.IDLE);
    }
    
    @Override
    public void onPlayerUse(PlayerInteractEvent evt) {
        
        Action a = evt.getAction();
        
        if(a == Action.RIGHT_CLICK_AIR && user != null) {
            mouseAdapter.handleDown(user, evt);
        }
        
        evt.setCancelled(true);
    }
    
    public Debris getDebrisTypeFromItem(ItemStack item) {
        Debris debris = null;
        
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(CraftRP.get(), METADATA_DEBRIS_NAME);
        
        if(container.has(key, PersistentDataType.BYTE)) {
            byte id = container.get(key, PersistentDataType.BYTE);
            
            for(Debris d : Debris.values()) {
                if(d.getID() == id) {
                    debris = d;
                    break;
                }
            }
        }
        
        return debris;
    }

    @Override
    public ItemStack getUpdatedItem(ItemStack current) {
        Debris dType = getDebrisTypeFromItem(current);
        return generateMCItem(dType);
    }
    
}
