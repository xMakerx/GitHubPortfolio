package org.xmakerx.raidpractice.arena;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.Settings;
import org.xmakerx.raidpractice.arena.Spawn.SpawnType;
import org.xmakerx.raidpractice.menu.MenuManager;
import org.xmakerx.raidpractice.shop.DoubleHealth;
import org.xmakerx.raidpractice.shop.Perk;

public class GamePlayer {
	
	public enum PlayerState {
		PLAYING, SPECTATING;
	}
	
	final RaidPractice instance;
	final Settings settings;
	final Arena arena;
	private final Player player;
	private final ItemStack[] items;
	private final ItemStack[] armor;
	private final float exp;
	private final int level;
	private final Location lastLocation;
	private Location home;
	private final GameMode gamemode;
	private final Collection<PotionEffect> potionEffects;
	private final HashSet<Perk> perks;
	private PlayerState mode;
	private int pointsEarned;
	
	public GamePlayer(final RaidPractice main, final Player player, final Arena arena) {
		this.instance = main;
		this.settings = main.getSettings();
		this.player = player;
		this.arena = arena;
		this.items = player.getInventory().getContents();
		this.armor = player.getInventory().getArmorContents();
		this.exp = player.getExp();
		this.level = player.getLevel();
		this.lastLocation = player.getLocation();
		this.home = null;
		this.potionEffects = player.getActivePotionEffects();
		this.perks = new HashSet<Perk>();
		this.mode = PlayerState.PLAYING;
		this.pointsEarned = 0;
		this.gamemode = player.getGameMode();
		this.prepare();
	}
	
	// Allows the player to set a home.
	public void setHome(final Location loc) {
		this.home = loc;
	}
	
	public Location getHome() {
		return this.home;
	}
	
	public void prepare() {
		// We need to close any menus this player has open.
		MenuManager.closeMenu(player, MenuManager.getMenuFromPlayer(player));
		
		// Remove all potion effects.
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType()); 
		}
		
		// Let's set exp and level.
		player.setExp(0);
		player.setLevel(0);
		
		// Clear the inventory.
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setGameMode(GameMode.SURVIVAL);
	}
	
	public void startSpectating() {
		final Game game = instance.getArenaManager().getGameFromArena(arena);
		// Let's clear the inventory.
		player.getInventory().clear();
		player.setAllowFlight(true);
		player.setFlying(true);
		this.mode = PlayerState.SPECTATING;
		
		int slot = 0;
		
		// Let's add the items.
		final ItemStack tpItem = new ItemStack(settings.getTeleportItem());
		final ItemMeta tpMeta = tpItem.getItemMeta();
		tpMeta.setDisplayName(instance.getLocalizer().getColoredString("teleportToPlayer"));
		tpItem.setItemMeta(tpMeta);
		player.getInventory().setItem(slot, tpItem);
		
		// Let's keep our inventory organized.
		slot = 8;
		
		final ItemStack exitItem = new ItemStack(settings.getExitItem());
		final ItemMeta exitMeta = exitItem.getItemMeta();
		exitMeta.setDisplayName(instance.getLocalizer().getColoredString("exitArena"));
		exitItem.setItemMeta(exitMeta);
		player.getInventory().setItem(slot, exitItem);
		
		// Let's teleport to a random spawn.
		player.teleport(game.getRandomSpawn(SpawnType.PLAYER).getLocation(), TeleportCause.PLUGIN);
		player.sendMessage(instance.getLocalizer().getMessage(instance.getLocalizer().getColoredString("spectating")));
	}
	
	public void start(final Spawn spawn) {
		mode = PlayerState.PLAYING;
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setGameMode(GameMode.SURVIVAL);
		
		// Let's go to the main game area.
		if(spawn != null) {
			player.teleport(spawn.getLocation(), TeleportCause.PLUGIN);
		}
		
		// Let's give the player our gear.
		if(arena.getGear() != null) {
			player.getInventory().setContents(arena.getGear().getContents());

			for(int i = 0; i < player.getInventory().getContents().length; i++) {
				final ItemStack item = player.getInventory().getContents()[i];
				if(item != null) {
					if(item.getType().name().contains("BOOTS") && player.getInventory().getBoots() == null) {
						player.getInventory().setBoots(item);
						player.getInventory().setItem(i, null);
					}else if(item.getType().name().contains("CHESTPLATE") && player.getInventory().getChestplate() == null) {
						player.getInventory().setChestplate(item);
						player.getInventory().setItem(i, null);
					}else if(item.getType().name().contains("HELMET") && player.getInventory().getHelmet() == null) {
						player.getInventory().setHelmet(item);
						player.getInventory().setItem(i, null);
					}else if(item.getType().name().contains("LEGGINGS") && player.getInventory().getLeggings() == null) {
						player.getInventory().setLeggings(item);
						player.getInventory().setItem(i, null);
					}
				}
				player.updateInventory();
			}
		}
		
		for(final Perk perk : getPerks()) {
			if(!perk.doesActivateOnDeath()) {
				perk.activate(getPlayer());
			}
		}
	}
	
	public void reset() {
		// We need to close any menus this player has open.
		MenuManager.closeMenu(player, MenuManager.getMenuFromPlayer(player));		

		// Remove all potion effects.
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType()); 
		}
		
		// Return their other stats.
		player.setLevel(level);
		player.setExp(exp);
		player.setGameMode(gamemode);
		
		// We have to return their inventory.
		player.getInventory().setContents(items);
		player.getInventory().setArmorContents(armor);
		player.updateInventory();
		
		player.addPotionEffects(potionEffects);
		
		// Teleport player to either the world spawn or to their
		// previous location. Option is configurable.
		if(!instance.getSettings().doesUseWorldSpawn()) {
			player.teleport(lastLocation, TeleportCause.PLUGIN);
		}else {
			player.teleport(arena.getWorld().getSpawnLocation(), TeleportCause.PLUGIN);
		}
		
		for(final Perk perk : getPerks()) {
			if(perk instanceof DoubleHealth) {
				player.setMaxHealth(20.0D);
				removePerk(perk);
				return;
			}
		}
		
		player.setHealth(player.getMaxHealth());
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		this.home = null;
	}
	
	public void addPerk(final Perk perk) {
		this.perks.add(perk);
	}
	
	public void removePerk(final Perk perk) {
		this.perks.remove(perk);
	}
	
	public HashSet<Perk> getPerks() {
		return this.perks;
	}
	
	public void setPointsEarned(int points) {
		this.pointsEarned = points;
	}
	
	public int getPointsEarned() {
		return this.pointsEarned;
	}
	
	public PlayerState getState() {
		return this.mode;
	}
	
	public GameMode getGamemode() {
		return this.gamemode;
	}
	
	public Player getPlayer() {
		return this.player;
	}
}
