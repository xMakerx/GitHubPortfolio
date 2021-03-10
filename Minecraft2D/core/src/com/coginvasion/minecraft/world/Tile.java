package com.coginvasion.minecraft.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.coginvasion.minecraft.Globals;

import static com.coginvasion.minecraft.Globals.BLOCK_SCALE;

public abstract class Tile implements ITile {
	
	protected final GameWorld world;
	protected final World b2dWorld;
	protected float lightLevel;
	protected boolean mouseDown;
	private int destroyIndex;
	protected float destroySpeed;
	private float elapsedDestroyTime;
	
	public Tile(GameWorld world, World b2dWorld) {
		this.world = world;
		this.b2dWorld = b2dWorld;
		this.lightLevel = 1.0f;
		this.mouseDown = false;
		this.destroyIndex = 0;
		this.elapsedDestroyTime = 0.0f;
		this.destroySpeed = Globals.DEFAULT_DIG_SPEED;
	}
	
	public void update(float deltaTime) {
		if(mouseDown) {
			elapsedDestroyTime += deltaTime;
			if(elapsedDestroyTime >= destroySpeed) {
				if(destroyIndex < 9) {
					destroyIndex++;
				}else {
					destroy();
					mouseDown = false;
				}
				
				elapsedDestroyTime = 0;
			}
		}
	}
	
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		if(mouseDown) {
			Texture texture = world.getGame().assetMgr.get("images/tileset.png", Texture.class);
			Vector2 position = world.toBlockUnits(getMyPosition());
			batch.draw(new TextureRegion(texture, destroyIndex * BLOCK_SCALE, 240, BLOCK_SCALE, BLOCK_SCALE), 
				position.x, position.y, BLOCK_SCALE, BLOCK_SCALE);
		}
	}
	
	public void mouseDown() {
		mouseDown = true;
		destroyIndex = 0;
		elapsedDestroyTime = 0.0f;
	}
	
	public void mouseUp() {
		mouseDown = false;
	}
	
	public void setLightLevel(float level) {
		this.lightLevel = level;
	}
	
	public float getLightLevel() {
		return this.lightLevel;
	}
	
	public abstract void onRightClick();

}
