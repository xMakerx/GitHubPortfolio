package com.coginvasion.minecraft.meta;

import com.coginvasion.minecraft.item.ItemStack;
import com.coginvasion.minecraft.world.Material;

public class TemporaryItemStack {
	
	private String materialName;
	private int amount;
	
	public TemporaryItemStack(String material, int amount) {
		this.materialName = material;
		this.amount = amount;
	}
	
	public ItemStack getRealItemStack() {
		for(Material mat : Material.values()) {
			if(mat.name().equalsIgnoreCase(materialName)) {
				return new ItemStack(mat, amount);
			}
		}
		
		return null;
	}
}
