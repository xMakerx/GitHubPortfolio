package org.xmakerx.raidpractice.menu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.xmakerx.raidpractice.RaidPractice;

public class MenuManager implements Listener {
	
	final RaidPractice instance;
	private static HashSet<Menu> menus;
	private static HashMap<UUID, Menu> checkedOutMenus;
	
	public MenuManager(final RaidPractice main) {
		MenuManager.menus = new HashSet<Menu>();
		MenuManager.checkedOutMenus = new HashMap<UUID, Menu>();
		this.instance = main;
		
		// Register the events.
		this.instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	public static Menu getMenuFromPlayer(final Player player) {
		return checkedOutMenus.get(player.getUniqueId());
	}
	
	public static HashSet<Menu> getMenus() {
		return MenuManager.menus;
	}
	
	public static void checkoutMenu(final Player player, final Menu menu) {
		final Menu checkedOutMenu = getMenuFromPlayer(player);
		if(checkedOutMenu != null) {
			closeMenu(player, checkedOutMenu);
			menu.setPreviousMenu(checkedOutMenu);
		}
		
		menu.show();
		MenuManager.checkedOutMenus.put(player.getUniqueId(), menu);
	}
	
	public static void closeMenu(final Player player, final Menu menu) {
		if(menu == null) return;
		MenuManager.checkedOutMenus.remove(player.getUniqueId(), menu);
		menu.closed();
	}
	
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent evt) {
		final Player player = (Player)evt.getWhoClicked();
		final Menu checkedOutMenu = getMenuFromPlayer(player);
		
		if(checkedOutMenu != null) {
			checkedOutMenu.clickPerformed(evt);
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent evt) {
		final Player player = (Player)evt.getPlayer();
		final Menu checkedOutMenu = getMenuFromPlayer(player);
		
		if(checkedOutMenu != null) {
			closeMenu(player, checkedOutMenu);
		}
	}
}
