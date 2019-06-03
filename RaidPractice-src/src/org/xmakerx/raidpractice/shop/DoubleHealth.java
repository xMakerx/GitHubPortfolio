package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DoubleHealth extends Perk {

	public DoubleHealth() {
		super("Double Health", new ArrayList<String>(), 50, Material.NETHER_STAR, true, false);
	}
	
	public void activate(final Player player) {
		player.setMaxHealth(40.0D);
		player.setHealth(40.0D);
	}
	
}
