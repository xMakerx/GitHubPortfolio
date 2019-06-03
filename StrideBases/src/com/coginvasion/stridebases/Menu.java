package com.coginvasion.stridebases;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
	
	protected final StrideBases instance;
	protected final Player player;
	protected Inventory inv;
	
	public Menu(final StrideBases main, final Player player) {
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
