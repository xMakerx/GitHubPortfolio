package com.coginvasion.minecraft.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.world.GameWorld;

public abstract class Entity extends Sprite {
	
	protected GameWorld world;
	protected Body body;
	protected double health;
	protected double maxHealth;
	protected float spriteOffsetY;
	protected boolean dead, deadAnimDone;
	private float deadTimer;
	private boolean flaggedForDeletion;
	
	// These variables are for fall damage.
	private float velocityY;
	private float fallBeginY;
	private Sound fallSmall, fallBig;
	
	protected Sound hurtSound;
	
	// These variables are used for color scaling.
	protected Color spriteColor;
	private float elapsedColorScaleTime;
	private float showColorTime;
	private float colorScaleIncrement;
	
	public Entity(GameWorld world, AssetManager assetMgr) {
		this.world = world;
		this.velocityY = 0.0f;
		this.fallBeginY = 0.0f;
		this.fallSmall = assetMgr.get("audio/damage/fallsmall.ogg", Sound.class);
		this.fallBig = assetMgr.get("audio/damage/fallbig.ogg", Sound.class);
		this.spriteColor = Globals.DEFAULT_COLOR;
		this.elapsedColorScaleTime = 0.0f;
		this.showColorTime = 0.0f;
		this.colorScaleIncrement = 0.0f;
		this.spriteOffsetY = 0.0f;
		this.dead = false;
		this.deadAnimDone = false;
		this.flaggedForDeletion = false;
	}
	
	public void flagForDeletion() {
		this.flaggedForDeletion = true;
	}
	
	public boolean isFlaggedForDeletion() {
		return this.flaggedForDeletion;
	}
	
	public void beginColorScale(Color newColor, float showTime, float increment) {
		elapsedColorScaleTime = 0.0f;
		showColorTime = showTime;
		colorScaleIncrement = increment;
		spriteColor = newColor;
	}
	
	public void resetColorScale() {
		elapsedColorScaleTime = 0.0f;
		showColorTime = 0.0f;
		colorScaleIncrement = 0.0f;
		spriteColor = Globals.DEFAULT_COLOR;
	}
	
	public void damaged(double damage) {
		world.playSound(hurtSound);
		
		if((health - damage) <= 0 && !dead) {
			//spriteColor = Globals.DAMAGE_COLOR;
			dead = true;
			deadAnimDone = false;
			deadTimer = 0.0f;
			rotate(12);
		}//else {
			beginColorScale(Globals.DAMAGE_COLOR, 0.45f, 0.035f);
		//}
	}
	
	public void update(float deltaTime) {
	    if(maxHealth < Integer.MAX_VALUE) {
    		// Code for handling falling.
    		float prevVY = velocityY;
    		velocityY = body.getLinearVelocity().y;
    		
    		if(prevVY >= 0 && velocityY < 0) {
    			fallBeginY = Math.abs(body.getPosition().y);
    		}
    		
    		if(prevVY < 0 && velocityY == 0) {
    			float posNow = Math.abs(body.getPosition().y);
    			float heavier = Math.max(posNow, fallBeginY);
    			float lower = Math.min(posNow, fallBeginY);
    			int blocksFell = (int) Math.ceil((heavier - lower) / 0.5f);
    			if(blocksFell >= 5) {
    				double healthLoss = (blocksFell - 5) * 1.5;
    				setHealth(health - healthLoss);
    				world.playSound(fallBig);
    			}else if(blocksFell < 5 && blocksFell >= 2) {
    				world.playSound(fallSmall);
    			}
    		}
    		
    		// Code for color scaling.
    		setColor(spriteColor);
    		
    		if(spriteColor != Globals.DEFAULT_COLOR) {
    			elapsedColorScaleTime += deltaTime;
    			
    			if(elapsedColorScaleTime >= showColorTime) {
    				float[] values = {spriteColor.r, spriteColor.g, spriteColor.b, spriteColor.a};
    				int valuesFixed = 0;
    				
    				for(int i = 0; i < values.length; i++) {
    					float v = values[i];
    					if(v != 1f) {
    						if((v + colorScaleIncrement) <= 1f) {
    							v += colorScaleIncrement;
    						}else {
    							v = 1f;
    						}
    						
    						if(v == 1f) valuesFixed++;
    					}else {
    						valuesFixed++;
    					}
    					
    					values[i] = v;
    				}
    				
    				if(valuesFixed != 4) {
    					spriteColor = new Color(values[0], values[1], values[2], values[3]);
    				}else {
    					resetColorScale();
    				}
    			}
    		}
    		
    		// Let's handle the death animation.
    		if(dead && !deadAnimDone) {
    			float rotation = getRotation();
    			int increment = 4;
    			deadTimer += deltaTime;
    			
    			if(deadTimer >= 0.015) {
    				if((rotation + increment) <= 90) {
    					rotate(increment);
    					spriteOffsetY -= 0.35;
    				}else {
    					rotate(2);
    					deadAnimDone = true;
    				}
    				
    				deadTimer = 0.0f;
    			}
    		}
	    }
	}
	
	public void render(SpriteBatch batch) {
	}
	
	public abstract void generate(World b2dWorld);
	
	public void setHealth(double h) {
		if(h < this.health) damaged(this.health - h);
		this.health = h;
	}
	
	public double getHealth() {
		return this.health;
	}
	
	public void setMaxHealth(double mH) {
		this.maxHealth = mH;
	}
	
	public double getMaxHealth() {
		return this.maxHealth;
	}
	
	public boolean isDead() {
		return health <= 0;
	}
	
	public Body getBody() {
		return this.body;
	}

}
