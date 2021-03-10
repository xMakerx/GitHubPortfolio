package com.coginvasion.minecraft.inventory;

import com.coginvasion.minecraft.item.ItemStack;

public interface IInventory {
	
	public void addItem(ItemStack item);
	public void addItems(ItemStack... item);
	public ItemStack[] getContents();
	
	public void setDisplayName(String newName);
	public String getDisplayName();
	public int getSlots();
	
	public int getAvailableSlot();
}
