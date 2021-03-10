package mc.decodedlogic.skybattlesuspawners.event.listener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import io.netty.util.internal.ThreadLocalRandom;
import mc.decodedlogic.skybattlesuspawners.Settings;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.api.StackedEntityData;
import mc.decodedlogic.skybattlesuspawners.api.USpawnersAPI;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerStackedEntitySpawnEvent;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerStackedEntityUpdateEvent;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityExperienceOrb;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;

public class EntityEvents implements Listener {
    
    public static final String METADATA_SPAWNER_KEY_NAME = USpawnersAPI.ENT_SPAWNER_KEY;
    public static final String METADATA_STACK_KEY_NAME = USpawnersAPI.ENT_QUANTITY_KEY;
    public static final String METADATA_UPGRADE_KEY_NAME = USpawnersAPI.ENT_UPGRADE_KEY;
    public static final String METADATA_ITEM_STACK_KEY_NAME = USpawnersAPI.STACK_META_KEY;
    
    final USpawners MAIN;
    
    public EntityEvents(USpawners main) {
        this.MAIN = main;
    }
    
    private List<ItemStack> drop(@Nonnull Location loc, @Nonnull Material mat, int amount) {
        return drop(loc, new ItemStack(mat, 1), amount);
    }
    
    private List<ItemStack> drop(@Nonnull Location loc, @Nonnull ItemStack drop, int amount) {
        final List<ItemStack> DS = new ArrayList<ItemStack>();
        
        int tmp = amount;
        double stackRadius = USpawnersAPI.getStackRadius();
        
        for(Entity ent : loc.getWorld().getNearbyEntities(loc, stackRadius, stackRadius, stackRadius)) {
            if(ent.getType() == EntityType.DROPPED_ITEM) {
                Item i = (Item) ent;
                ItemStack is = i.getItemStack();
                
                if(!is.isSimilar(drop)) continue;
                
                int availableSpace = is.getMaxStackSize() - is.getAmount();
                
                if(availableSpace >= tmp) {
                    is.setAmount(is.getAmount() + tmp);
                    tmp = 0;
                    break;
                }else if(availableSpace > 0) {
                    is.setAmount(is.getMaxStackSize());
                    tmp = tmp - availableSpace;
                }
                
                int stackedAmt = USpawnersAPI.getItemsInStack(i);
                availableSpace = USpawnersAPI.getEntityStackLimit() - stackedAmt;
                
                int newQuantity = stackedAmt;
                
                if(availableSpace >= tmp) {
                    newQuantity = newQuantity + tmp;
                    tmp = 0;
                }else {
                    newQuantity = USpawnersAPI.getEntityStackLimit();
                    tmp = tmp - availableSpace;
                }
                
                
                if(stackedAmt != 0) i.removeMetadata(METADATA_ITEM_STACK_KEY_NAME, MAIN);
                i.setMetadata(METADATA_ITEM_STACK_KEY_NAME, new FixedMetadataValue(MAIN, newQuantity));
                if(tmp == 0) break;
            }
        }
        
        if(tmp > 0) {
            int availableMcSpace = drop.getMaxStackSize();
            if(tmp < availableMcSpace) {
                drop.setAmount(tmp); 
                tmp = 0;
                DS.add(drop);
            }else {
                drop.setAmount(drop.getMaxStackSize());
                tmp = tmp - availableMcSpace;
                
                final int INIT_SIZE = tmp;
                
                Bukkit.getScheduler().runTaskLater(MAIN, () -> {
                    Item n = loc.getWorld().dropItemNaturally(loc, drop);
                    n.setMetadata(METADATA_ITEM_STACK_KEY_NAME, new FixedMetadataValue(MAIN, INIT_SIZE));
                }, 1L);
            }
        }
        
        return DS;
        
    }
    
    private void handleNewSpawn(@Nonnull Entity entity, @Nonnull MobSpawner spawner) {
        if(entity == null || spawner == null) return;
        
        int neededEntities = 1;
        int stackLimit = USpawnersAPI.getEntityStackLimit();
        
        // Let's calculate the needed quantity.
        int min = spawner.size();
        int max = (min * 2) <= stackLimit ? (min * 2) : stackLimit;
        if(max == min) max++;
        
        // Don't allow the spawning of babies by simply
        // setting spawned babies as adults.
        
        if(entity instanceof Ageable) {
            Ageable a = (Ageable) entity;
            
            if(!a.isAdult()) {
                a.setAdult();
            }
        }
        
        neededEntities = ThreadLocalRandom.current().nextInt(min, max);
        
        double stackRadius = USpawnersAPI.getStackRadius();
        
        for(Entity e : entity.getWorld().getNearbyEntities(spawner.getLocation(), stackRadius, stackRadius, stackRadius)) {
            
            if(e.getType() == entity.getType()) {
                StackedEntityData sd = USpawnersAPI.getStackedEntityData(e);
                if(sd == null) continue;
                
                SpawnerType sType = SpawnerType.getTypeFromEntityType(e.getType());
                SpawnerUpgrade upgrade = (sd.getUpgradeIndex() > -1) ? sType.getUpgrades().get(sd.getUpgradeIndex()) : SpawnerUpgrade.DEFAULT;
                
                if(upgrade.equals(spawner.getUpgrade())) {
                    int availableSpace = (stackLimit - sd.getQuantity());
                    int newQuantity = sd.getQuantity();
                    
                    if(availableSpace >= neededEntities) {
                        // This is the only entity we need.
                        newQuantity = newQuantity + neededEntities;
                        neededEntities = 0;
                    }else {
                        newQuantity = stackLimit;
                        neededEntities = neededEntities - availableSpace;
                    }
                    
                    
                    updateEntityQuantity(e, newQuantity);
                    MAIN.callEvent(new MobSpawnerStackedEntityUpdateEvent(e, spawner));
                    
                    if(neededEntities == 0) break;
                }
            }
        }
        
        if(neededEntities > 0) {
            entity.setMetadata(METADATA_SPAWNER_KEY_NAME, new FixedMetadataValue(MAIN, spawner.getId()));
            entity.setMetadata(METADATA_UPGRADE_KEY_NAME, new FixedMetadataValue(MAIN, spawner.getUpgrade().getIndex()));
            entity.setMetadata(METADATA_STACK_KEY_NAME, new FixedMetadataValue(MAIN, neededEntities));
            updateEntity(entity);
            updateEntityHealth(entity, neededEntities);
            Utils.disableMobAI(entity);
            MAIN.callEvent(new MobSpawnerStackedEntitySpawnEvent(entity, neededEntities, spawner));
        }else {
            entity.remove();
        }
    }
    
    private void updateEntityHealth(@Nonnull Entity entity, int quantity) {
        if(USpawnersAPI.doesUseStackedHealth() && entity instanceof LivingEntity) {
            LivingEntity lE = (LivingEntity) entity;
            SpawnerType sType = SpawnerType.getTypeFromEntityType(lE.getType());
            lE.setMaxHealth(!sType.isOneByOne() ? lE.getMaxHealth() * quantity : lE.getMaxHealth());
            lE.setHealth(lE.getMaxHealth());
        }
    }
    
    private void updateEntityQuantity(@Nonnull Entity entity, int newQuantity) {
        try { entity.removeMetadata(METADATA_STACK_KEY_NAME, MAIN); } catch (Exception e) {}
        entity.setMetadata(METADATA_STACK_KEY_NAME, new FixedMetadataValue(MAIN, newQuantity));
        
        updateEntityHealth(entity, newQuantity);
        updateEntity(entity);
    }
    
    private void updateEntity(@Nonnull Entity entity) {
        SpawnerType sType = SpawnerType.getTypeFromEntityType(entity.getType());
        MetadataValue sValue = USpawnersAPI.getFirstValueInKey(entity, METADATA_UPGRADE_KEY_NAME);
        int upgradeId = (sValue != null) ? sValue.asInt() : -1;
        
        SpawnerUpgrade upgrade = (upgradeId > -1) ? sType.getUpgrades().get(upgradeId) : SpawnerUpgrade.DEFAULT;
        
        Settings s = MAIN.getSettings();
        String displayName = s.getEntityDisplayName();
        String typeName = Utils.makePrettyStringFromEnum(sType.getEntityType().name(), true);
        
        MetadataValue qValue = USpawnersAPI.getFirstValueInKey(entity, METADATA_STACK_KEY_NAME);
        int q = (qValue != null) ? qValue.asInt() : 0;
        
        displayName = Utils.replaceVariableWith(displayName, "amount", q);
        displayName = Utils.replaceVariableWith(displayName, "spawnerType", typeName);
        displayName = Utils.replaceVariableWith(displayName, "upgrade", upgrade.getDisplayName());
        
        entity.setCustomName(Utils.mkDisplayReady(displayName));
        entity.setCustomNameVisible(true);
    }
    
    /**
     * Let's disable enderman teleporting.
     * @param evt
     */
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(EntityTeleportEvent evt) {
        Entity e = evt.getEntity();
        StackedEntityData d = USpawnersAPI.getStackedEntityData(e);
        
        if(d != null && e.getType() == EntityType.ENDERMAN) {
            evt.setCancelled(true);
            evt.setTo(evt.getFrom());
        }
    }
	
    @EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(final EntityDeathEvent evt) {
		final Entity ENTITY = evt.getEntity();
        StackedEntityData sd = USpawnersAPI.getStackedEntityData(ENTITY);
        
        if(sd != null) {
            Settings s = MAIN.getSettings();
            SpawnerType sType = SpawnerType.getTypeFromEntityType(ENTITY.getType());
            
            Location loc = ENTITY.getLocation();
            SpawnerUpgrade upgrade = (sd.getUpgradeIndex() > -1) ? sType.getUpgrades().get(sd.getUpgradeIndex()) : SpawnerUpgrade.DEFAULT;
            
            DamageCause lastCause = (ENTITY.getLastDamageCause() != null) ? ENTITY.getLastDamageCause().getCause() : DamageCause.CUSTOM;
            boolean oneByOne = sType.isOneByOne() && !s.getKillOptions().contains(lastCause);
            
            int baseDropAmt = 1;
            for(ItemStack i : evt.getDrops()) baseDropAmt += i.getAmount();
            
            int useQuantity = oneByOne ? 1 : sd.getQuantity();
            int numDrops = baseDropAmt * useQuantity;
            
            evt.setDroppedExp(evt.getDroppedExp() * useQuantity);
            
            List<ItemStack> oldDrops = new ArrayList<ItemStack>(evt.getDrops());
            evt.getDrops().clear();
            
            if(upgrade != SpawnerUpgrade.DEFAULT) {
                evt.getDrops().addAll(drop(loc, upgrade.getDrop(), numDrops));
            }else if(sType.getDefaultDrop() != null) {
                evt.getDrops().addAll(drop(loc, sType.getDefaultDrop(), numDrops));
            }else {
                int n = oldDrops.size();
                
                if(n > 0) {
                    int each = numDrops / n;
                    
                    for(ItemStack i : oldDrops) {
                        evt.getDrops().addAll(drop(loc, i.getType(), each));
                    }
                }
            }
        }
	}
    
    public void handlePickup(Inventory inv, Item mcItem, Cancellable evt) {
        if(inv == null) return;
        
        ItemStack IS = mcItem.getItemStack();
        
        int neededSpace = USpawnersAPI.getItemsInStack(mcItem);
        
        while(neededSpace > 0) {
            int amt = (neededSpace > IS.getMaxStackSize()) ? IS.getMaxStackSize() : neededSpace;
            ItemStack n = IS.clone();
            n.setAmount(amt);
            neededSpace -= amt;
            
            HashMap<Integer, ItemStack> r = inv.addItem(n);
            if(!r.isEmpty()) {
                evt.setCancelled(true);
                break;
            }
        }
        
        if(neededSpace > 0) {
            mcItem.removeMetadata(METADATA_ITEM_STACK_KEY_NAME, MAIN);
            mcItem.setMetadata(METADATA_ITEM_STACK_KEY_NAME, new FixedMetadataValue(MAIN, neededSpace));
        }
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent evt) {
		final Player p = evt.getPlayer();
		final Item i = evt.getItem();
		
		if(USpawnersAPI.getItemsInStack(i) != 0) {
		    handlePickup(p.getInventory(), i, evt);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperPickupItem(InventoryPickupItemEvent evt) {
	    final Item i = evt.getItem();
	    final Inventory inv = evt.getInventory();
        
        if(USpawnersAPI.getItemsInStack(i) != 0) {
            handlePickup(inv, i, evt);
        }
	}
	
	@EventHandler
	public void onEntityTarget(final EntityTargetLivingEntityEvent evt) {
	    Entity ent = evt.getEntity();
	    
	    if(ent.hasMetadata("USpawner")) {
	        evt.setTarget(null);
	        evt.setCancelled(true);
	    }
	}
	
	@SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent evt) {
	    Entity ent = evt.getEntity();
	    StackedEntityData sd = USpawnersAPI.getStackedEntityData(ent);
	    
	    if(sd != null) {
	        Settings s = MAIN.getSettings();
	        SpawnerType sType = SpawnerType.getTypeFromEntityType(ent.getType());
	        DamageCause cause = evt.getCause();
	        LivingEntity lE = ((LivingEntity) ent);
	        
	        double dmg = evt.getDamage();
	        boolean isDead = (lE.getHealth() - dmg) <= 0d;
	        
	        if(Arrays.asList(EntityType.BLAZE, EntityType.ENDERMAN).contains(ent.getType()) 
	                && cause == DamageCause.DROWNING) {
	            evt.setCancelled(true);
	            evt.setDamage(0.0d);
	            return;
	        }
	        
	        boolean oneByOne = sType.isOneByOne();
	        
	        if(isDead) {
	            List<DamageCause> blacklistCauses = sType.getBlacklistDeathCauses();
	            boolean cannotDie = blacklistCauses.contains(cause);
	            boolean ignoreOBO = false;
	            
	            ent.setLastDamageCause(evt);
	            
	            // Kill Options will ignore the one-by-one.
	            if(s.getKillOptions().contains(cause) || cause == DamageCause.VOID) {
	                if((cause == DamageCause.LAVA || cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) 
	                        && ent.getFireTicks() > 0) {
	                    ent.setFireTicks(0);
	                }
	                
	                ignoreOBO = true;
	                cannotDie = false;
	            }
	            
	            cannotDie = cannotDie || (oneByOne && !ignoreOBO && sd.getQuantity() > 1);
	            
	            int newSize = sd.getQuantity() - 1;
	            evt.setCancelled(cannotDie);
	            evt.setDamage(cannotDie ? 0.0d : dmg);
	            
	            if(newSize == 0) ignoreOBO = true;
	            
	            if(oneByOne && !ignoreOBO) {
	                
	                Player playerDamager = null;
	                int en = 0;
	                
                    net.minecraft.server.v1_8_R3.Entity pE = null;
                    EntityHuman eH = null;
	                
	                if(evt.getCause() == DamageCause.ENTITY_ATTACK) {
	                    EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) evt;
	                    
	                    if(edbe.getDamager() instanceof Player) {
	                        playerDamager = (Player) edbe.getDamager();
	                        pE = ((CraftEntity) playerDamager).getHandle();
	                        eH = (EntityHuman) pE;
	                        EntityLiving pEL = (EntityLiving) ((CraftEntity) playerDamager).getHandle();
	                        en = EnchantmentManager.getBonusMonsterLootEnchantmentLevel(pEL);
	                    }
	                }
	                
	                
	                try {
	                    
	                    net.minecraft.server.v1_8_R3.Entity mE = ((CraftEntity) ent).getHandle();
	                    EntityLiving eL = (EntityLiving) ((CraftEntity) ent).getHandle();
	                    
                        Field fDrops = getField(eL.getClass(), "drops");
                        fDrops.setAccessible(true);
                        
                        fDrops.set(eL, new ArrayList<ItemStack>());
                        
                        int i = 0;
                        int xp = 0;
                        
                        for(Method m : EntityLiving.class.getDeclaredMethods()) {
                            if(m.getName().equalsIgnoreCase("dropDeathLoot") || m.getName().equalsIgnoreCase("dropEquipment")) {
                                m.setAccessible(true);
                                m.invoke(mE, true, en);
                                i++;
                            }else if(m.getName().equalsIgnoreCase("getExpValue") && playerDamager != null) {
                                m.setAccessible(true);
                                
                                xp = (int) m.invoke(eL, eH);
                                i++;
                            }
                            
                            if(playerDamager == null && i == 2 || (playerDamager != null && i == 3)) break;
                        }
                        
                        
                        Field fKiller = getField(eL.getClass(), "killer");
                        fKiller.setAccessible(true);
                        fKiller.set(eL, eH);
                        
                        CraftEventFactory.callEntityDeathEvent(eL, (List<ItemStack>) fDrops.get(eL));
                        fKiller.set(eL, null);
                        
                        // Let's drop exp.
                        
                        while(xp > 0) {
                            int j = EntityExperienceOrb.getOrbValue(xp);
                            xp -= j;
                            ExperienceOrb orb = (ExperienceOrb) ent.getWorld().spawnEntity(ent.getLocation(), EntityType.EXPERIENCE_ORB);
                            orb.setExperience(j);
                        }
                        
                        fDrops.set(eL, null);
                        
                        updateEntityQuantity(ent, newSize);
                        
                        
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
	            }
	        }
	    }
	}
	
	private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
	    
	    try {
	        return clazz.getDeclaredField(fieldName);
	    } catch (NoSuchFieldException e) {
	        Class<?> superClass = clazz.getSuperclass();
	        if(superClass == null) {
	            throw e;
	        }else {
	            return getField(superClass, fieldName);
	        }
	    }
	    
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onContainerBreak(BlockBreakEvent evt) {
	    if(evt.isCancelled()) return;
	    
	    Block b = evt.getBlock();
	    
	    if(b.getState() instanceof InventoryHolder) {
	        InventoryHolder ih = (InventoryHolder) b.getState();
	        Inventory inv = ih.getInventory();
	        
	        Map<ItemStack, Integer> map = new HashMap<ItemStack, Integer>();
	        
	        for(ItemStack i : inv.getContents()) {
	            if(i == null || (i != null && i.getType() == Material.AIR)) continue;
	            
                ItemStack c = i.clone();
                c.setAmount(1);
	            
	            int existing = map.getOrDefault(c, 0);
	            
	            map.put(c, existing + i.getAmount());
	        }
	        
	        inv.clear();
	        
	        for(ItemStack i : map.keySet()) {
	            int amt = map.get(i);
	            int tmp = amt;
	            
	            if(amt > i.getMaxStackSize()) {
	                i.setAmount(i.getMaxStackSize());
	                tmp = tmp - i.getMaxStackSize();
	                
	                Item mcI = b.getWorld().dropItemNaturally(b.getLocation(), i);
	                mcI.setMetadata(METADATA_ITEM_STACK_KEY_NAME, new FixedMetadataValue(MAIN, tmp));
	            }else {
	                i.setAmount(amt);
	                b.getWorld().dropItemNaturally(b.getLocation(), i);
	            }
	            
	        }
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSpawnerSpawn(final SpawnerSpawnEvent evt) {
		if(evt.isCancelled()) return;
		
		MobSpawner spawner = USpawnersAPI.getMobSpawnerAt(evt.getSpawner().getLocation());
		
		if(spawner != null) {
		    
		    // Disallow spawning before the initial delay has elapsed.
		    if(!spawner.canSpawnNow()) {
		        evt.setCancelled(true);
		        return;
		    }
		    
			Entity entity = evt.getEntity();
			handleNewSpawn(entity, spawner);
		}
	}
}

class DeadEntityData {
    
    final Entity ENTITY;
    final int DROPPED_EXP;
    final List<ItemStack> DROPS;
    
    public DeadEntityData(Entity ent, int droppedExp, List<ItemStack> drops) {
        this.ENTITY = ent;
        this.DROPPED_EXP = droppedExp;
        this.DROPS = drops;
    }
    
    public Entity getEntity() {
        return this.ENTITY;
    }
    
    public int getDroppedExp() {
        return this.DROPPED_EXP;
    }
    
    public List<ItemStack> getDrops() {
        return this.DROPS;
    }
    
}
