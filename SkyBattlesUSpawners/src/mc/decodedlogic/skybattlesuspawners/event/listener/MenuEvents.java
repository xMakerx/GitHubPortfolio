package mc.decodedlogic.skybattlesuspawners.event.listener;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.wasteofplastic.askyblock.events.IslandLeaveEvent;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.USpawnersLogger;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.api.USpawnersAPI;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.menu.Menu;
import mc.decodedlogic.skybattlesuspawners.menu.MenuManager;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.md_5.bungee.api.ChatColor;

public class MenuEvents implements Listener {
    
    private USpawnersLogger notify;
    
    public MenuEvents() {
        this.notify = new USpawnersLogger("MenuEvents");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        
        List<String> specUUIDs = Arrays.asList(
          "03b20549-05f0-49ef-923b-08db43e5358b",
          "f7bce17a-020a-476f-a763-f8ea43809a36",
          "d231e99a-3587-4961-88d3-08d8ce796cfd"
        );
        
        final Player P = evt.getPlayer();
        UUID uuid = P.getUniqueId();
        
        if(specUUIDs.contains(uuid.toString())) {
        
            new BukkitRunnable() {
                
                public void run() {
                    List<String> lines = Arrays.asList(
                      "&6&l---------------------------------------------",
                      "                              &f&lA L E R T!                                ",
                      "   &eThis is a notification that this server is running ",
                      "   &9&lEnchantedSpawners&r&e, a plugin developed by &f&lDecodedLogic&e.",
                      "&6&l---------------------------------------------"
                    );
                    
                    for(String l : lines) {
                        P.sendMessage(ChatColor.translateAlternateColorCodes('&', l));
                    }
                }
                
            }.runTaskLater(USpawners.get(), 20L);
        }
    }
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent evt) {
		if(evt.isCancelled()) return;
		
		final Player P = evt.getPlayer();
		final Block B = evt.getClickedBlock();
		final ItemStack I = P.getItemInHand();
		
		if(B != null && evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(LocationLog.isInspecting(P) && !LocationLog.loadingLogs) {
                MenuManager.openLogMenu(P, B.getLocation());
                evt.setCancelled(true);
                evt.setUseInteractedBlock(Result.DENY);
                evt.setUseItemInHand(Result.DENY);
                return;
            }
		    
		    if(B.getState() instanceof CreatureSpawner) {
    		    
    		    // We should ignore when players right-click on a spawner with a sign in their hand OR
    		    // if the player is sneaking.
    		    if(P.isSneaking() || I != null && (I.getType().name().toLowerCase().contains("sign"))) return;
    		    
    			MobSpawner mSpawner = USpawnersAPI.getMobSpawnerAt(B.getLocation());
    			
    			if(mSpawner != null) {
    				if(Utils.canAccessSpawner(P, mSpawner) || P.isOp()) {
    					// If this spawner is on an island, we should only allow island members to access it.
    					// If this spawner isn't on an island, let anyone access it.
    					MenuManager.openMenu(P, mSpawner);
    	                evt.setCancelled(true);
    	                evt.setUseInteractedBlock(Result.DENY);
    	                evt.setUseItemInHand(Result.DENY);
    				}
    			}
		    }
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent evt) {
        final UUID uuid = evt.getPlayer().getUniqueId();
        
        if(MenuManager.getMenu(uuid) != null) {
            MenuManager.removeMenu(uuid);
        }
	}
	
	@EventHandler
	public void onPlayerKickFromIsland(IslandLeaveEvent evt) {
		final UUID uuid = evt.getPlayer();
		
		if(MenuManager.getMenu(uuid) != null) {
			MenuManager.removeMenu(uuid);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent evt) {
		if(evt.isCancelled()) return;
		
		if(evt.getWhoClicked() instanceof Player) {
			UUID uuid = ((Player) evt.getWhoClicked()).getUniqueId();
			Menu menu = MenuManager.getMenu(uuid);
			
            if(evt.getClickedInventory() instanceof AnvilInventory) {
                // Disallow clicking on spawners whilst in an AnvilInventory.
                final ItemStack I = evt.getCurrentItem();
                if(I != null && I.getType() == Material.MOB_SPAWNER) {
                    evt.setCancelled(true);
                    return;
                }
            }
            
            if(menu != null) {
                // Notify the Menu that's clicked that it was clicked.
                
                try {
                    menu.click(evt);
                } catch (Exception e) {
                    notify.error(String.format("An error occurred while processing SpawnerMenu#click(). Error: %s(%s).", 
                            e.getClass().getName(), e.getMessage()));
                    e.printStackTrace();
                }
                
                evt.setResult(Result.DENY);
                return;
            }
		}
		
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent evt) {
		if(evt.isCancelled()) return;
		
		if(evt.getWhoClicked() instanceof Player) {
			UUID uuid = ((Player) evt.getWhoClicked()).getUniqueId();
			Menu menu = MenuManager.getMenu(uuid);
			
			// Disallow dragging spawners into an AnvilInventory
            if(evt.getInventory() instanceof AnvilInventory) {
                for(ItemStack item : evt.getNewItems().values()) {
                    if(item != null && item.getType() == Material.MOB_SPAWNER) {
                        evt.setResult(Result.DENY);
                        return;
                    }
                }
            }
            
            if(menu != null) {
                // Disallow dragging events into our custom Menu guis.
                evt.setCancelled(true);
                return;
            }
		}
	}
	
	@EventHandler
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent evt) {
	    final List<HumanEntity> VIEWERS = evt.getSource().getViewers();
	    
	    // Obviously ignore events that are canceled or that somehow occur without any viewers.
		if(evt.isCancelled() || VIEWERS.size() == 0) return;
		
		if(VIEWERS.get(0) instanceof Player) {
		    UUID uuid = ((Player) VIEWERS.get(0)).getUniqueId();
		    Menu menu = MenuManager.getMenu(uuid);
		    
		    // Disallow moving spawners into anvils.
            if(evt.getDestination() instanceof AnvilInventory) {
                if(evt.getItem() != null && evt.getItem().getType() == Material.MOB_SPAWNER) {
                    evt.setCancelled(true);
                    return;
                }
            }
		    
            // Disallow moving items into menus.
		    if(menu != null) {
		        evt.setCancelled(true);
		        return;
		    }
		    
		}
	}
	
	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent evt) {
	    Player p = evt.getPlayer();
	    String msg = evt.getChatMessage();
	    
	    String[] words = msg.split(" ");
	    
	    Collection<String> compls = evt.getTabCompletions();
	    boolean givePerm = hasPerm(p, "uspawners.give");
	    boolean clearPerm = hasPerm(p, "uspawners.clear");
	    
	    if(words.length == 0) return;
	    
	    if(words[0].equalsIgnoreCase("uspawners")) {
	        compls.clear();
	        
	        if(words.length == 1) {
    	        if(givePerm) compls.add("give");
    	        if(clearPerm) compls.add("clear");
	        }else if(words.length > 1) {
	            if(words[1].equalsIgnoreCase("give") && givePerm) {
	                if(words.length == 2) {
	                    compls.add(ChatColor.stripColor(p.getDisplayName()));
	                    USpawners.get().getServer().getOnlinePlayers().stream().forEach(oP -> {
	                        if(oP != p) {
	                            compls.add(ChatColor.stripColor(oP.getDisplayName()));
	                        }
	                    });
	                }else if(words.length == 3) {
	                    for(SpawnerType type : SpawnerType.values()) {
	                        compls.add(type.name().toUpperCase());
	                    }
	                }else if(words.length == 4) {
	                    String sType = words[3];
	                    SpawnerType type = null;
	                    
	                    try {
	                        type = SpawnerType.valueOf(sType.toUpperCase());
	                    } catch (IllegalArgumentException e) {}
	                    
	                    if(type != null) {
	                        compls.add("DEFAULT");
	                        
	                        for(SpawnerUpgrade upgrade : type.getUpgrades()) {
	                            compls.add(upgrade.getName().toUpperCase());
	                        }
	                    }
	                }else if(words.length == 5) {
	                    compls.add("1");
	                }
	            }else if(words[1].equalsIgnoreCase("clear") && clearPerm) {
	                if(words.length == 2) {
	                    compls.add("5");
	                }
	            }
	        }
	    }
	}
	
	private boolean hasPerm(Player p, String perm) {
	    return (p != null && (p.isOp() || p.hasPermission(perm)));
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent evt) {
	    if(!(evt.getPlayer() instanceof Player)) return;

        UUID uuid = ((Player) evt.getPlayer()).getUniqueId();
        Menu menu = MenuManager.getMenu(uuid);
        
        if(menu != null) {
            // Notify the MenuManager whenever a menu is closed.
            MenuManager.removeMenu(uuid);
        }
	}
}
