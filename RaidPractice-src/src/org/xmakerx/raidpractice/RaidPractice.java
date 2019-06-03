package org.xmakerx.raidpractice;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.xmakerx.raidpractice.arena.ArenaManager;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.cmd.Commands;
import org.xmakerx.raidpractice.event.ConnectionsListener;
import org.xmakerx.raidpractice.event.GameEvents;
import org.xmakerx.raidpractice.menu.MenuManager;
import org.xmakerx.raidpractice.shop.ShopData;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class RaidPractice extends JavaPlugin {
	
	private static Localizer localizer;
	private static ArenaManager arenaManager = null;
	private static MenuManager menuManager = null;
	private static WorldEditPlugin worldEdit;
	private static WorldGuardPlugin worldGuard;
	private static Database database;
	private static Settings settings;
	private static Commands cmds;
	private static GameEvents gEvents;
	private static ShopData shopData;
	
	public void onEnable() {
		
		// Let's make sure WorldEdit is installed.
		final Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldEditPlugin != null && (worldEditPlugin instanceof WorldEditPlugin)) {
			worldEdit = (WorldEditPlugin)worldEditPlugin;
			getLogger().info(String.format("Successfully hooked into WorldEdit %s.", worldEdit.getDescription().getVersion()));
		}else {
			getLogger().severe("Could not hook into WorldEdit, please install it and restart the plugin.");
			disable();
		}
		
		// Let's make sure WorldGuard is installed.
		final Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if(worldGuardPlugin != null && (worldGuardPlugin instanceof WorldGuardPlugin)) {
			worldGuard = (WorldGuardPlugin)worldGuardPlugin;
			getLogger().info(String.format("Successfully hooked into WorldGuard %s.", worldGuard.getDescription().getVersion()));
		}else {
			getLogger().severe("Could not hook into WorldGuard, please install it and restart the plugin.");
			disable();
		}
		
		// Let's load up our processes.
		RaidPractice.localizer = new Localizer(this);
		RaidPractice.settings = new Settings(this);
		RaidPractice.database = new Database(this);
		RaidPractice.arenaManager = new ArenaManager(this);
		RaidPractice.menuManager = new MenuManager(this);
		RaidPractice.cmds = new Commands(this);
		RaidPractice.gEvents = new GameEvents(this);
		RaidPractice.shopData = new ShopData(this);
		
		getCommand("rp").setExecutor(RaidPractice.cmds);
		getCommand("raidpractice").setExecutor(RaidPractice.cmds);
		getServer().getPluginManager().registerEvents(new ConnectionsListener(this), this);
		getServer().getPluginManager().registerEvents(RaidPractice.gEvents, this);
	}
	
	public void onDisable() {
		getLogger().fine("Saving player and arena data...");
		if(getArenaManager() != null) {
			for(Game game : arenaManager.getGames()) {
				game.reset();
			}
		}
		
		// Let's save our database.
		RaidPractice.database.save();
		
		// Let's close out our static instances.
		RaidPractice.localizer = null;
		RaidPractice.arenaManager = null;
		RaidPractice.menuManager = null;
		RaidPractice.cmds = null;
		RaidPractice.database = null;
		RaidPractice.settings = null;
		RaidPractice.worldEdit = null;
		RaidPractice.worldGuard = null;
		RaidPractice.shopData = null;
		RaidPractice.gEvents = null;
	}
	
	// This is so we can disable the plugin when we need to.
	public void disable() {
		setEnabled(false);
		return;
	}
	
	public Localizer getLocalizer() {
		return RaidPractice.localizer;
	}
	
	public GameEvents getGameEvents() {
		return RaidPractice.gEvents;
	}
	
	public ShopData getShopData() {
		return RaidPractice.shopData;
	}
	
	public void setArenaManager(final ArenaManager mgr) {
		RaidPractice.arenaManager = mgr;
	}
	
	public ArenaManager getArenaManager() {
		return RaidPractice.arenaManager;
	}
	
	public MenuManager getMenuManager() {
		return RaidPractice.menuManager;
	}
	
	public WorldEditPlugin getWorldEdit() {
		return RaidPractice.worldEdit;
	}
	
	public WorldGuardPlugin getWorldGuard() {
		return RaidPractice.worldGuard;
	}
	
	public Database getStatsDatabase() {
		return RaidPractice.database;
	}
	
	public Settings getSettings() {
		return RaidPractice.settings;
	}
	
	public Commands getCommands() {
		return RaidPractice.cmds;
	}
	
	public OfflinePlayer getTopPlayer() {
		if(RaidPractice.database != null) {
			for(Map.Entry<OfflinePlayer, Integer> entry : RaidPractice.database.getTopPoints().entrySet()) {
				return entry.getKey();
			}
		}else {
			getLogger().info("Attempted to contact the database before it was created or after the plugin was disabled.");
		}
		return null;
	}
}
