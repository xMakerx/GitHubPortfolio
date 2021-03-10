package com.coginvasion.minecraft.item;

import java.util.*;

import com.coginvasion.minecraft.world.Material;

public enum ItemType {
	COLLECTIBLE(null), 
	PICKAXE(new ArrayList<Material>(Arrays.asList(Material.COBBLESTONE, Material.STONE))),
	SWORD(null),
	AXE(null), 
	SHOVEL(null), 
	HOE(null);
	
	private final List<Material> uses;
	
	private ItemType(List<Material> uses) {
		this.uses = uses;
	}
	
	public List<Material> getUses() {
		return this.uses;
	}
}
