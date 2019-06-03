package com.coginvasion.stridebases;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.coginvasion.stridebases.schematic.SchematicUtils;

public class StrideBases extends JavaPlugin {
	
	private static StrideBases instance;
	private static Settings settings;
	private static MenuManager menuMgr;
	
	public void onEnable() {		
		// Let's start our required services.
		StrideBases.instance = this;
		StrideBases.settings = new Settings(this);
		StrideBases.menuMgr = new MenuManager(this);
		new SchematicUtils(this);
		
		// Let's register our events.
		getServer().getPluginManager().registerEvents(menuMgr, this);
		
		// Register our commands.
		final Commands cmds = new Commands(this);
		getCommand("bases").setExecutor(cmds);
		getCommand("bundo").setExecutor(cmds);
	}
	
	public void onDisable() {
		
		if(StrideBases.settings != null) {
			// Let's save our data.
			StrideBases.settings.saveData();
			
			// Let's close out all our menus.
			for(final Player player : getServer().getOnlinePlayers()) {
				StrideBases.menuMgr.removeMenu(player);
			}
		}
		
		// Let's set all our services to null.
		StrideBases.settings = null;
		StrideBases.menuMgr = null;
	}
	
	public static StrideBases getInstance() {
		return StrideBases.instance;
	}
	
	public static Settings getSettings() {
		return StrideBases.settings;
	}
	
	public static MenuManager getMenuManager() {
		return StrideBases.menuMgr;
	}
	
}
