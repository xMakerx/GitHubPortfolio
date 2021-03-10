package com.coginvasion.minecraft.item;

import com.coginvasion.minecraft.meta.ItemMeta;
import com.coginvasion.minecraft.world.Material;

public class ItemStack {
	
	private final Material type;
	private final ItemMeta meta;
	private int amount;
	
	public ItemStack(Material type, int amount) {
		this.type = type;
		this.meta = type.getItemMeta();
		this.amount = amount;
	}
	
	public Material getMaterial() {
		return this.type;
	}
	
	public void setAmount(int a) {
		this.amount = a;
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	public ItemMeta getItemMeta() {
		return this.meta;
	}

}
