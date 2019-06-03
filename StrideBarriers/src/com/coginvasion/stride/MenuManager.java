package com.coginvasion.stride;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuManager implements Listener {
	
	final StrideBarriers instance;
	final HashMap<UUID, Menu> menus;
	
	public MenuManager(final StrideBarriers main) {
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
}
