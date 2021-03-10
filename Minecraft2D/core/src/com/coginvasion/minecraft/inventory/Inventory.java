package com.coginvasion.minecraft.inventory;

import com.coginvasion.minecraft.item.ItemStack;

public abstract class Inventory implements IInventory {
	
	protected String displayName;
	protected ItemStack[] contents;
	protected int slots;
	
	public Inventory(String name, int size) {
		this.displayName = name;
		this.slots = size;
		this.contents = new ItemStack[size];
	}
	
	public void addItem(ItemStack item) {
		int slot = getAvailableSlot();
		if(slot != -1) contents[slot] = item;
	}

	@Override
	public void addItems(ItemStack... items) {
		for(ItemStack item : items) {
			int slot = getAvailableSlot();
			if(slot != -1) {
				contents[slot] = item;
				continue;
			}
			
			break;
		}
	}

	public ItemStack[] getContents() {
		return this.contents;
	}
	
	public void setDisplayName(String newName) {
		this.displayName = newName;
	}

	public String getDisplayName() {
		return this.displayName;
	}
	
	public int getSlots() {
		return this.slots;
	}
	
	/**
	 * Gets the next available slot,
	 * if none are available, returns -1.
	 */
	
	public int getAvailableSlot() {
		for(int i = 0; i < slots; i++) {
			if(contents[i] == null) return i;
		}
		
		return -1;
	}

}
