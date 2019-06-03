package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RespawnItem extends Perk {

	public RespawnItem() {
		super("&a&lRespawn", new ArrayList<String>(Arrays.asList("&eRespawn and get another chance.")), 25, Material.BED, true, true);
	}

	@Override
	public void activate(Player player) {
		
	}

}
