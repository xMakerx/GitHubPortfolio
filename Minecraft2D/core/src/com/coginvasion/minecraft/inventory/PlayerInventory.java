package com.coginvasion.minecraft.inventory;

import com.coginvasion.minecraft.entity.Player;
import com.coginvasion.minecraft.item.ItemStack;

public class PlayerInventory extends Inventory {
	
	private Player player;
	private ItemStack itemInHand;
	
	public PlayerInventory(Player player, String name, int size) {
		super(name, size);
		this.player = player;
		this.itemInHand = null;
	}
	
	public void setItemInHand(ItemStack item) {
		this.itemInHand = item;
		this.player.item.setMaterial(item.getMaterial());
	}
	
	public ItemStack getItemInHand() {
		return this.itemInHand;
	}
	
}
