package com.coginvasion.minecraft.item;

import static com.coginvasion.minecraft.Globals.PPM;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.entity.Player;
import com.coginvasion.minecraft.meta.ItemMeta;
import com.coginvasion.minecraft.world.Material;

public class HeldItem extends Sprite {
	
	private Player player;
	private Material type;
	
	public HeldItem(Player player) {
		this.player = player;
		this.type = Material.AIR;
		this.setBounds(0, 0, Globals.BLOCK_SCALE / 2f, Globals.BLOCK_SCALE / 2f);
		this.setScale(1.25f);
	}
	
	public void setMaterial(Material mat) {
		this.type = mat;
		if(mat.getTexture() != null) {
			setRegion(mat.getTexture());
			
			if(mat.getMetadata()[0] instanceof ItemMeta) {
				this.setScale(1.25f);
			}else {
				this.setScale(0.925f);
				if(isFlipX()) setFlip(false, false);
			}
		}
	}
	
	public Material getMaterial() {
		return this.type;
	}
	
	public void update(float deltaTime) {
		Vector2 position = player.getBody().getPosition();
		setPosition((position.x * PPM), 
				(position.y * PPM) + (getHeight() / 2f) - 8);
	}
	
	public void render(SpriteBatch batch) {
		if(super.getTexture() != null) super.draw(batch);
	}
}
