package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class Perk extends ShopItem {
	
	protected final boolean activateOnDeath;

	public Perk(String name, ArrayList<String> desc, int cost, Material icon,
			boolean allowDisable, boolean activateOnDeath) {
		super(name, desc, cost, icon, allowDisable);
		this.activateOnDeath = activateOnDeath;
	}
	
	public abstract void activate(final Player player);
	
	public boolean doesActivateOnDeath() {
		return this.activateOnDeath;
	}

}
