package mc.decodedlogic.skybattlesuspawners.event.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandPreDeleteEvent;
import com.wasteofplastic.askyblock.events.IslandResetEvent;

import mc.decodedlogic.skybattlesuspawners.Settings;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.USpawnersLogger;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.api.USpawnersAPI;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.logging.ModificationRecord;
import mc.decodedlogic.skybattlesuspawners.logging.Transaction;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class BlockEvents implements Listener {
	
	final USpawnersLogger notify;
	
	public BlockEvents() {
		this.notify = new USpawnersLogger("BlockEvents");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent evt) {
		if(evt.isCancelled()) return;
		final Block b = evt.getBlock();
		final Player p = evt.getPlayer();
		
		try {
			final WorldGuardPlugin wg = USpawners.get().getWorldGuard();
			
			if(wg != null && !wg.canBuild(p, b)) {
				evt.setCancelled(true);
				return;
			}
			
		} catch (Exception e) {}
		
		
		if(b.getState() instanceof CreatureSpawner) {
			final ItemStack itemInHand = evt.getItemInHand();
			final CreatureSpawner cSpawner = (CreatureSpawner) b.getState();
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemInHand);
            final NBTTagCompound nbt = nmsItem.getTag();
            EntityType type = null;
            int upgradeIndex = -1;
            
            if(nbt != null) {
                String s = nbt.getString("Type");
                if(s.isEmpty()) {
                    type = cSpawner.getSpawnedType();
                }else {
                    try {
                        type = EntityType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        if(s.equalsIgnoreCase("ZOMBIE_PIGMAN")) {
                            type = EntityType.PIG_ZOMBIE;
                        }else if(s.equalsIgnoreCase("MOOSHROOM")) {
                            type = EntityType.MUSHROOM_COW;
                        }else {
                            type = EntityType.PIG;
                        }
                    }
                }
                
                upgradeIndex = nbt.getInt("Upgrade") - 1;
                
                cSpawner.setSpawnedType(type);
            }
            
            EntityType TYPE = type;
			
			final SpawnerUpgrade upgrade = (upgradeIndex != -1) ? SpawnerType.getTypeFromEntityType(type).getUpgrades().get(upgradeIndex) : SpawnerUpgrade.DEFAULT;
			
			MobSpawner mergeWith = null;
			Settings settings = USpawners.get().getSettings();
			int mergeRadius = settings.getSpawnerMergeRadius();
			String mergeMsg = settings.getSpawnerMerged();
			
			for(MobSpawner s : SpawnerManager.getMobSpawners()) {
			    int dist = (int) Math.ceil(s.getLocation().distance(b.getLocation()));
			    boolean canAdd = s.size() < s.getMaxSize() && Utils.canAccessSpawner(p, s);
			    
			    if(dist <= mergeRadius && s.getType().getEntityType() == TYPE && s.getUpgradeIndex() == upgrade.getIndex() && canAdd) {
			        mergeWith = s;
			        break;
			    }
			}
			
			if(mergeWith == null) {
			    final MobSpawner spawner = new MobSpawner(b.getLocation());
                if(upgrade != null) spawner.setUpgrade(upgrade);
                
                spawner.update();
                
                LocationLog.addRecord(spawner.getLocation(), new ModificationRecord(p.getUniqueId(), true));
			}else {
			    mergeWith.setSize(mergeWith.size() + 1);
                mergeWith.update();
                
                LocationLog.addRecord(mergeWith.getLocation(), new Transaction(p.getUniqueId(), Transaction.SPAWNER_DEPOSIT, 0, 1));
			    
			    evt.setCancelled(true);
			    
			    if(p.getGameMode() != GameMode.CREATIVE) {
		             ItemStack n = itemInHand.clone();
			        
    			    if(itemInHand.getAmount() > 1) {
    			        n.setAmount(itemInHand.getAmount()-1);
    			    }else {
    			        n = null;
    			    }
    			    
    			    p.getInventory().setItemInHand(n);
    			    
    			    p.updateInventory();
			    }
			    
			    p.sendMessage(Utils.color(mergeMsg));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockExplode(BlockExplodeEvent evt) {
		if(evt.isCancelled()) return;
		final Block b = evt.getBlock();
		
		if(b != null && b.getType() == Material.MOB_SPAWNER) {
			for(MobSpawner spawner : SpawnerManager.getMobSpawners()) {
				final Location loc = spawner.getLocation();
				if(b.getLocation().equals(loc)) {
					evt.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onIslandReset(IslandResetEvent evt) {
	    Island i = Utils.getIslandAt(evt.getLocation());
	    
		// Let's delete the spawners that were on this island.
        MobSpawner.fromIsland(i).stream().forEach(s -> {
            Location l = s.getLocation();
            s.delete(true);
            LocationLog.addRecord(l, new ModificationRecord(false));
        });
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onIslandDelete(IslandPreDeleteEvent evt) {
        Island i = evt.getIsland();
	    
		// Let's delete the spawners that were on this island.
		MobSpawner.fromIsland(i).stream().forEach(s -> {
		    Location l = s.getLocation();
		    s.delete(true);
		    LocationLog.addRecord(l, new ModificationRecord(false));
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent evt) {
		if(evt.isCancelled()) return;
		final Block b = evt.getBlock();
		
		if(b.getState() instanceof CreatureSpawner) {
			MobSpawner mSpawner = USpawnersAPI.getMobSpawnerAt(b.getLocation());
			
			if(mSpawner != null) {
				final Player player = evt.getPlayer();
				
				if(player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
					boolean unregistered = SpawnerManager.unregister(mSpawner);
					if(unregistered) {
						final Location loc = b.getLocation();
						mSpawner.delete(false);
						
						LocationLog.addRecord(b.getLocation(), new ModificationRecord(player.getUniqueId(), false));
						player.sendMessage(ChatColor.YELLOW + String.format("Destroyed spawner @ Location (%s, %d, %d, %d)", loc.getWorld().getName(), 
								loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
					}
				}else {
					evt.setCancelled(true);
					player.sendMessage(Utils.color(USpawners.get().getSettings().getWithdrawCorrectly()));
				}
			}
		}
	}
}
