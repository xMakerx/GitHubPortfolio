package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;

import org.bukkit.Material;

public class EmptySlotItem extends ShopItem {

	public EmptySlotItem() {
		super("Empty Slot", new ArrayList<String>(), -1, Material.STAINED_GLASS_PANE, false);
		this.metadata.put("data", 8);
	}

}
