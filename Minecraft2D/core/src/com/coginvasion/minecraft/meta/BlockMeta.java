package com.coginvasion.minecraft.meta;

import java.util.HashSet;
import java.util.Random;

import com.badlogic.gdx.audio.Sound;
import com.coginvasion.minecraft.item.ItemStack;

public class BlockMeta implements IMetadata {
	
	private float durability;
	private float lightLoss;
	private double contactDmg;
	private boolean canCollide;
	private boolean flattensPlants;
	
	// Whether or not to cover the background when placed
	private boolean opaque;
	
	private Sound[] stepSounds, digSounds;
	
	private HashSet<ItemStack> drops;
	private HashSet<TemporaryItemStack> tempDrops;
	
	Random random;
	
	public BlockMeta() {
		this.durability = 1.0f;
		this.lightLoss = 0.0f;
		this.contactDmg = 0.0d;
		this.drops = new HashSet<ItemStack>();
		this.tempDrops = new HashSet<TemporaryItemStack>();
		this.canCollide = true;
		this.flattensPlants = true;
		this.opaque = true;
		this.random = new Random();
	}
	
	public BlockMeta setDurability(float d) {
		this.durability = d;
		return this;
	}
	
	public float getDurability() {
		return this.durability;
	}
	
	public BlockMeta setLightLoss(float f) {
		this.lightLoss = f;
		return this;
	}
	
	public float getLightLoss() {
		return this.lightLoss;
	}
	
	public BlockMeta setContactDamage(double cd) {
		this.contactDmg = cd;
		return this;
	}
	
	public double getContactDamage() {
		return this.contactDmg;
	}
	
	public BlockMeta setCanCollide(boolean flag) {
		this.canCollide = flag;
		return this;
	}
	
	public boolean isCanCollide() {
		return this.canCollide;
	}
	
	public BlockMeta setFlattensPlants(boolean flag) {
		this.flattensPlants = flag;
		return this;
	}
	
	public boolean doesFlattenPlants() {
		return this.flattensPlants;
	}
	
	public void setStepSounds(Sound[] sounds) {
		this.stepSounds = sounds;
	}
	
	public Sound[] getStepSounds() {
		return this.stepSounds;
	}
	
	public Sound getStepSound() {
		if(stepSounds.length > 0) {
		    return stepSounds[random.nextInt(stepSounds.length)];
		}
		return null;
	}
	
	public void setDigSounds(Sound[] sounds) {
		this.digSounds = sounds;
	}
	
	public Sound[] getDigSounds() {
		return this.digSounds;
	}
	
	public Sound getDigSound() {
		if(digSounds.length > 0) return digSounds[random.nextInt(digSounds.length)];
		return null;
	}
	
	public void fixTemporaryDrops() {
		for(TemporaryItemStack drop : tempDrops) {
			ItemStack d = drop.getRealItemStack();
			if(d != null) drops.add(d);
		}
		
		tempDrops.clear();
	}
	
	public BlockMeta addDrop(ItemStack drop) {
		this.drops.add(drop);
		return this;
	}
	
	public BlockMeta addDrop(String matName, int amount) {
		tempDrops.add(new TemporaryItemStack(matName, amount));
		return this;
	}
	
	public BlockMeta addDrops(ItemStack... drops) {
		for(ItemStack drop : drops) {
			addDrop(drop);
		}
		
		return this;
	}
	
	public HashSet<ItemStack> getDrops() {
		return this.drops;
	}
	
	public BlockMeta setOpaque(boolean flag) {
		this.opaque = flag;
		return this;
	}
	
	public boolean isOpaque() {
		return this.opaque;
	}
	
}
