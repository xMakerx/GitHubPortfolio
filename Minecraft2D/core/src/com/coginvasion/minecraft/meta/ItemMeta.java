package com.coginvasion.minecraft.meta;

import com.coginvasion.minecraft.item.ItemType;

public class ItemMeta implements IMetadata {
	
	private final ItemType type;
	private float strength;
	
	public ItemMeta(ItemType type) {
		this.type = type;
		this.strength = 0.0f;
	}
	
	public ItemMeta(ItemType type, float strength) {
		this.type = type;
		this.strength = strength;
	}
	
	public ItemType getType() {
		return this.type;
	}
	
	public float getStrength() {
		return this.strength;
	}

}
