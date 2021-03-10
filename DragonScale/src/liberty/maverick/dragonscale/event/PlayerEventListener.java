package liberty.maverick.dragonscale.event;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleDatabase;
import liberty.maverick.dragonscale.DragonScaleLogger;
import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;
import net.md_5.bungee.api.ChatColor;

public class PlayerEventListener implements Listener {
	
	final DragonScale main;
	final DragonScaleDatabase database;
	final DragonScaleLogger logger;
	
	public PlayerEventListener(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.database = main.getSystemDatabase();
		this.logger = new DragonScaleLogger(main, "PlayerEventListener");
	}
	
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent evt) {
		final Player player = evt.getPlayer();
		
		if(evt.getAction() == Action.RIGHT_CLICK_AIR || evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final ItemStack item = evt.getItem();
			final boolean isMainHand = evt.getHand() == EquipmentSlot.HAND;
			
			if(item != null && item.getType() == Material.NOTE_BLOCK && item.hasItemMeta()) {
				final String rawName = item.getItemMeta().getDisplayName();
				final String lootBoxName = ChatColor.translateAlternateColorCodes('&', main.getLootBoxFactory().getDisplayName());
				final boolean namesEquivalent = rawName.equalsIgnoreCase(lootBoxName);
				
				if(namesEquivalent) {
					evt.setCancelled(true);
					
					if(isMainHand) {
						// Unlocking should only be done with the main hand.
						item.setAmount(item.getAmount() - 1);
						player.getInventory().setItemInMainHand(item);
						
						Location location = (evt.getClickedBlock() != null) ? evt.getClickedBlock().getLocation() : player.getLocation();
						main.getLootBoxFactory().processPlayerOpen(player, location);
					}
					
					player.updateInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent evt) {
		final Player player = evt.getPlayer();
		
		new BukkitRunnable() {
			
			public void run() {
				final String uuid = player.getUniqueId().toString();
				boolean loaded = false;
				
				if(database.getPlayerData(uuid) == null) database.savePlayerData(uuid);
				loaded = database.loadPlayerData(uuid);
				
				if(loaded) {
					givePickaxeIfNotPresent(player);
					logger.debug(String.format("Loaded data for Player \"%s\"!", player.getDisplayName()));
				}else {
					logger.debug(String.format("Failed to load data for Player \"%s\"!", player.getDisplayName()));
				}
			}
			
		}.runTaskLater(main, 1L);
	}
	
	private void givePickaxeIfNotPresent(final Player player) {
		final DragonScalePickaxe pickaxe = database.getPickaxeData(player.getUniqueId().toString());
		int pickaxeSlot = -1;
		
		for(int i = 0; i < player.getInventory().getContents().length; i++) {
			final ItemStack item = player.getInventory().getContents()[i];
			if(DragonScalePickaxe.isPluginPickaxe(main, item)) {
				pickaxeSlot = i;
				break;
			}
		}
		
		int heldItemSlot = player.getInventory().getHeldItemSlot();
		final ItemStack pickaxeItem = pickaxe.generatePickaxe(main.getSettings());
		
		// Let's add the pickaxe if we don't have it.
		if(pickaxeSlot == -1) {
			player.getInventory().addItem(pickaxeItem);
			player.updateInventory();
		}else {
			// We need to make sure the player has the correct pickaxe at all times.
			player.getInventory().setItem(pickaxeSlot, pickaxeItem);
		}
		
		// Let's call onEquip() if the player is holding our special pickaxe.
		if(heldItemSlot == pickaxeSlot) {
			pickaxe.onEquip();
		}

	}
	
	/**
	 * When the player respawns, let's give them our pickaxe.
	 * @param {@link PlayerRespawnEvent}
	 */
	
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent evt) {
		final Player player = evt.getPlayer();
		
		new BukkitRunnable() {
			
			public void run() {
				final DragonScalePickaxe pickaxe = database.getPickaxeData(player.getUniqueId().toString());
				
				if(pickaxe != null) {
					givePickaxeIfNotPresent(player);
				}
			}
			
		}.runTaskLater(main, 1L);
	}
	
	@EventHandler
	public void onPlayerLeave(final PlayerQuitEvent evt) {
		final Player player = evt.getPlayer();
		
		final DragonScalePickaxe pickaxe = database.getPickaxeData(player.getUniqueId().toString());
		
		if(pickaxe != null && isSlotSpecialPickaxe(player, player.getInventory().getHeldItemSlot())) {
			pickaxe.onDequip();
		}
		
		// Let's save the player's data.
		if(database.getPickaxeData(player.getUniqueId().toString()) != null) {
			database.savePlayerData(player.getUniqueId().toString());
			logger.debug(String.format("Saved data for Player \"%s\"!", player.getDisplayName()));
			
			// Let's stop keeping track of the player's pickaxe object.
			database.getData().remove(player.getUniqueId().toString());
		}
	}
	
	@EventHandler
	public void onPlayerDamagedByCustomExplosion(final EntityDamageByBlockEvent evt) {
		if(evt.getCause() == DamageCause.BLOCK_EXPLOSION && evt.getDamager() == null) {
			evt.setCancelled(true);
		}
	}
	
	/**
	 * This prevents the dropping of the special pickaxes from death events.
	 * @param {@link PlayerDeathEvent}
	 */
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent evt) {
		final Iterator<ItemStack> it = evt.getDrops().iterator();
		
		while(it.hasNext()) {
			final ItemStack item = it.next();
			if(DragonScalePickaxe.isPluginPickaxe(main, item)) {
				it.remove();
			}
		}
	}
	
	/**
	 * This prevents damage to the special pickaxes.
	 * @param {@link PlayerItemDamageEvent}
	 */
	
	@EventHandler
	public void onPlayerItemDamageEvent(final PlayerItemDamageEvent evt) {
		final ItemStack item = evt.getItem();
		
		if(DragonScalePickaxe.isPluginPickaxe(main, item)) {
			evt.setDamage(0);
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerItemHeldEvent(final PlayerItemHeldEvent evt) {
		final Player player = evt.getPlayer();
		final int prevSlot = evt.getPreviousSlot();
		final int newSlot = evt.getNewSlot();
		final DragonScalePickaxe pickaxe = database.getPickaxeData(player.getUniqueId().toString());
		
		if(pickaxe != null) {
			if(isSlotSpecialPickaxe(player, newSlot) && prevSlot != newSlot) {
				pickaxe.onEquip();
				main.getSettings().getSoundByName("PICKAXE_EQUIP").play(player.getLocation());
	
			}else if(isSlotSpecialPickaxe(player, prevSlot) && prevSlot != newSlot) {
				pickaxe.onDequip();
				main.getSettings().getSoundByName("PICKAXE_DEQUIP").play(player.getLocation());
			}
		}
	}
	
	/**
	 * This prevents the dropping of the special pickaxes.
	 * @param {@link PlayerDropItemEvent}
	 */
	
	@EventHandler
	public void onPlayerDropItemEvent(final PlayerDropItemEvent evt) {
		final ItemStack item = evt.getItemDrop().getItemStack();
		
		if(DragonScalePickaxe.isPluginPickaxe(main, item)) {
			evt.setCancelled(true);
		}
	}
	
	/**
	 * Checks to see if the specified slot in the specified player's inventory
	 * is a special pickaxe.
	 * @param The player whose inventory we're going to check.
	 * @param The slot number to check.
	 * @return true/false flag.
	 */
	
	private boolean isSlotSpecialPickaxe(Player player, int slot) {
		return DragonScalePickaxe.isPluginPickaxe(main, player.getInventory().getItem(slot));
	}
	
}
