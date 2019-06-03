package com.coginvasion.stride;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.coginvasion.stride.barrier.BarrierManager;
import com.coginvasion.stride.event.BarrierEvents;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class StrideBarriers extends JavaPlugin {
	
	private static StrideBarriers instance;
	private static Settings settings;
	private static MenuManager menuMgr;
	private static BarrierManager barMgr;
	private static WorldEditPlugin worldEdit;
	private static WorldGuardPlugin worldGuard;
	private static Economy econ;
	
	public void onEnable() {
		// Let's make sure our dependencies are here.
		StrideBarriers.worldEdit = (WorldEditPlugin) getRequiredDependancy("WorldEdit", WorldEditPlugin.class);
		StrideBarriers.worldGuard = (WorldGuardPlugin) getRequiredDependancy("WorldGuard", WorldGuardPlugin.class);
		
		// Let's set up the economy.
		if(getRequiredDependancy("Vault", Vault.class) != null) {
			final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
			
			if(rsp != null) {
				// Found an economy to hook into!
				StrideBarriers.econ = rsp.getProvider();
				getLogger().info(String.format("Successfully hooked into Economy %s [%s]", econ.getName(), rsp.getPlugin().getDescription().getVersion()));
			}else {
				// Whoops! They haven't installed an economy.
				getLogger().severe("You must install an economy plugin to use this plugin.");
				setEnabled(false);
				return;
			}
		}
		
		if(worldEdit == null || worldGuard == null) return;
		StrideBarriers.instance = this;
		StrideBarriers.settings = new Settings(this);
		StrideBarriers.menuMgr = new MenuManager(this);
		StrideBarriers.barMgr = new BarrierManager();
		
		final Commands cmds = new Commands();
		getCommand("barriers").setExecutor(cmds);
		
		// Register events
		getServer().getPluginManager().registerEvents(menuMgr, this);
		getServer().getPluginManager().registerEvents(barMgr, this);
		getServer().getPluginManager().registerEvents(new BarrierEvents(this), this);
	}
	
	public void onDisable() {
		if(StrideBarriers.barMgr != null) {
			StrideBarriers.barMgr.saveData();
			StrideBarriers.instance = null;
			StrideBarriers.settings = null;
			StrideBarriers.menuMgr = null;
			StrideBarriers.barMgr = null;
			StrideBarriers.worldEdit = null;
			StrideBarriers.worldGuard = null;
			StrideBarriers.econ = null;
		}
	}
	
	public JavaPlugin getRequiredDependancy(final String name, final Class<? extends JavaPlugin> returnType) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(name);
		
		if(plugin != null && returnType.isInstance(plugin)) {
			getLogger().info(String.format("Successfully hooked into %s [%s]!", 
					plugin.getDescription().getName(), 
			plugin.getDescription().getVersion()));
			
			return returnType.cast(plugin);
		}
		
		getLogger().severe(String.format("%s could not be found. You must install it to use this plugin.", name));
		setEnabled(false);
		return null;
	}
	
	public static WorldEditPlugin getWorldEdit() {
		return StrideBarriers.worldEdit;
	}
	
	public static WorldGuardPlugin getWorldGuard() {
		return StrideBarriers.worldGuard;
	}
	
	public static Economy getEconomy() {
		return StrideBarriers.econ;
	}
	
	public static Settings getSettings() {
		return StrideBarriers.settings;
	}
	
	public static MenuManager getMenuManager() {
		return StrideBarriers.menuMgr;
	}
	
	public static BarrierManager getBarrierManager() {
		return StrideBarriers.barMgr;
	}
	
	public static StrideBarriers getInstance() {
		return StrideBarriers.instance;
	}
}
