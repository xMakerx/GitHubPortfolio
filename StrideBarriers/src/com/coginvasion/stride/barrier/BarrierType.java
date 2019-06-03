package com.coginvasion.stride.barrier;

import org.bukkit.DyeColor;

public class BarrierType {
	
	final DyeColor color;
	final double cost;
	final int durability;
	
	public BarrierType(final DyeColor color, final double cost, final int durability) {
		this.color = color;
		this.cost = cost;
		this.durability = durability;
	}
	
	public DyeColor getColor() {
		return this.color;
	}
	
	public double getCost() {
		return this.cost;
	}
	
	public int getDurability() {
		return this.durability;
	}
}
