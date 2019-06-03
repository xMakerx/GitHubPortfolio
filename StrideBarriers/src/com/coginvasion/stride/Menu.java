package com.coginvasion.stride;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
	
	protected final StrideBarriers instance;
	protected final Player player;
	protected Inventory inv;
	
	public Menu(final StrideBarriers main, final Player player) {
		this.instance = main;
		this.player = player;
	}
	
	public abstract void show();
	
	public abstract void click(final InventoryClickEvent evt);
	
	public Player getViewer() {
		return this.player;
	}
	
	public Inventory getInventory() {
		return this.inv;
	}
}
