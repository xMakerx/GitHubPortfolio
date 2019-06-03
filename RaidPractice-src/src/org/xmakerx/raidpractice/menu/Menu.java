package org.xmakerx.raidpractice.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
	
	protected final Player viewer;
	protected final Inventory ui;
	protected Menu previousMenu;
	
	public Menu(final Player viewer, final Inventory gui) {
		this.viewer = viewer;
		this.ui = gui;
		this.previousMenu = null;
	}
	
	public void setPreviousMenu(final Menu previousMenu) {
		this.previousMenu = previousMenu;
	}
	
	public Menu getPreviousMenu() {
		return this.previousMenu;
	}
	
	public abstract void show();
	
	public abstract void clickPerformed(final InventoryClickEvent evt);
	
	public abstract void closed();
	
	public Inventory getUI() {
		return this.ui;
	}
}
