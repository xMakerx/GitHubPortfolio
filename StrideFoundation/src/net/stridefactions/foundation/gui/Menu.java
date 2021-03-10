package net.stridefactions.foundation.gui;

import net.stridefactions.foundation.Settings;
import net.stridefactions.foundation.StrideFoundation;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
	
	protected final StrideFoundation instance;
	protected final Settings settings;
	protected final Player player;
	
	protected Inventory inv;
	protected boolean opened;
	
	public Menu(final Player player) {
		this.instance = StrideFoundation.getInstance();
		this.settings = instance.getSettings();
		this.player = player;
		this.opened = false;
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
