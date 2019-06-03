package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;

public class BalanceItem extends ShopItem {

	public BalanceItem() {
		super("&aBalance", new ArrayList<String>(Arrays.asList("&e{keys} &aKeys")), -1, Material.TRIPWIRE_HOOK, false);
	}

}
