package com.coginvasion.stridebases;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuManager implements Listener {
	
	final StrideBases instance;
	final HashMap<UUID, Menu> menus;
	
	public MenuManager(final StrideBases main) {
		this.instance = main;
		this.menus = new HashMap<UUID, Menu>();
	}
	
	public void addMenu(final Player player, final Menu menu) {
		this.menus.put(player.getUniqueId(), menu);
		menu.show();
	}
	
	public void removeMenu(final Player player) {
		if(menus.containsKey(player.getUniqueId())) {
			player.closeInventory();
			menus.remove(player.getUniqueId());
		}
	}
	
	public boolean hasMenu(final Player player) {
		return menus.containsKey(player.getUniqueId());
	}
	
	@EventHandler
	public void onInventoryDrag(final InventoryDragEvent evt) {
		final Player player = (Player) evt.getWhoClicked();
		
		if(hasMenu(player)) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent evt) {
		final Player player = (Player) evt.getWhoClicked();
		
		if(hasMenu(player)) {
			menus.get(player.getUniqueId()).click(evt);
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent evt) {
		final Player player = (Player) evt.getPlayer();
		if(hasMenu(player)) {
			menus.remove(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent evt) {
		for(final BaseBuildSession session : StrideBases.getSettings().getBuildSessions()) {
			for(final Block b : session.getBase().getBlocks()) {
				if(b.getLocation().equals(evt.getBlock().getLocation())) {
					evt.setCancelled(true);
					evt.getPlayer().sendMessage(StrideBases.getSettings().getMessage("cantBreak"));
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPhysics(final BlockPhysicsEvent evt) {
		for(final BaseBuildSession session : StrideBases.getSettings().getBuildSessions()) {
			for(final Block b : session.getBase().getBlocks()) {
				if(b.getLocation().equals(evt.getBlock().getLocation())) {
					evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockFade(final BlockFadeEvent evt) {
		for(final BaseBuildSession session : StrideBases.getSettings().getBuildSessions()) {
			for(final Block b : session.getBase().getBlocks()) {
				if(b.getLocation().equals(evt.getBlock().getLocation())) {
					evt.setCancelled(true);
				}
			}
		}
	}
}
