package com.coginvasion.minecraft.world;

import static com.coginvasion.minecraft.Globals.BLOCK_SCALE;
import static com.coginvasion.minecraft.Globals.PPM;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.entity.Player;
import com.coginvasion.minecraft.item.ItemStack;
import com.coginvasion.minecraft.item.ItemType;
import com.coginvasion.minecraft.meta.BlockMeta;
import com.coginvasion.minecraft.meta.ItemMeta;

public class Block extends Tile {
	
	private Vector2 position;
	protected Body block;
	protected Material type;
	
	protected Material lastType;
	
	private float elapsedDestroyTimer;
	private float elapsedLitTimer;
	
	private BlockMeta meta;
	
	public boolean destroyed;
	public int boardX, boardY;
	
	// These variables are used for color scaling.
	protected Color spriteColor;
	private float elapsedColorScaleTime;
	private float showColorTime;
	private float colorScaleIncrement;
	
	public Block(GameWorld world, World b2dWorld, Material type, int boardX, int boardY, float x, float y) {
		super(world, b2dWorld);
		this.type = type;
		this.destroyed = false;
		this.position = new Vector2(x / PPM, y / PPM);
		this.elapsedDestroyTimer = 0.0f;
		this.elapsedLitTimer = 0.0f;
		this.boardX = boardX;
		this.boardY = boardY;
		this.meta = type.getBlockMeta();
		
		this.spriteColor = Globals.DEFAULT_COLOR;
		this.elapsedColorScaleTime = 0.0f;
		this.showColorTime = 0.0f;
		this.colorScaleIncrement = 0.0f;

		this.generate();
	}
	
	public void onRightClick() {}
	
	public void generate() {
		if(!isInView()) return;
		
		BodyDef def = new BodyDef();
		def.type = BodyDef.BodyType.StaticBody;
		def.fixedRotation = true;
		def.position.set(position);
		block = b2dWorld.createBody(def);
		
		FixtureDef fixDef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(Globals.PHYS_BLOCK_SCALE, Globals.PHYS_BLOCK_SCALE);
		fixDef.shape = shape;
		fixDef.density = 1.0f;
		fixDef.friction = 1.0f;
		fixDef.isSensor = !meta.isCanCollide();
		fixDef.filter.categoryBits = Globals.BLOCK_BIT;
		block.createFixture(fixDef).setUserData(this);
		shape.dispose();
		
		EdgeShape edge = new EdgeShape();
		float edgeY = Globals.PHYS_BLOCK_SCALE;
		edge.set(new Vector2(-Globals.PHYS_BLOCK_SCALE, edgeY), new Vector2(Globals.PHYS_BLOCK_SCALE, edgeY));
		fixDef.shape = edge;
		fixDef.filter.categoryBits = Globals.BLOCK_EDGE_BIT;
		fixDef.isSensor = true;
		
		block.createFixture(fixDef).setUserData(this);;
		edge.dispose();
	}
	
	public boolean isInView() {
		if(world.getGame().getPlayer() == null) return false;
		
		Vector2 pPos = world.getGame().getPlayer().getBody().getPosition();
		Vector2 mPos = getMyPosition();
		return Globals.getDistanceFrom(mPos.x, pPos.x, mPos.y, pPos.y) < Globals.RENDER_DISTANCE;
	}
	
	public void mouseDown() {
		super.mouseDown();
		ItemStack item = world.getGame().getPlayer().getInventory().getItemInHand();
		
		float base = 1.0f;
		
		if(item != null) {
			final ItemMeta itemMeta = item.getItemMeta();
			if(itemMeta.getType() == ItemType.PICKAXE && type.getName().toLowerCase().contains("stone")) {
				base = (meta.getDurability() / item.getItemMeta().getStrength());
			}else if(itemMeta.getType() == ItemType.SHOVEL && Arrays.asList(Material.DIRT, Material.GRASS, Material.GRAVEL, Material.SAND).contains(type)) {
				base = (meta.getDurability() / item.getItemMeta().getStrength());
			}
		}
		
		destroySpeed = base * Globals.DEFAULT_DIG_SPEED;
		elapsedDestroyTimer = 0.0f;
		System.out.println(destroySpeed);
	}
	
	public boolean isMouseInside(float x, float y) {
		Vector2 bPos = world.toBlockUnits(position);
		return (x > bPos.x && x < (bPos.x + BLOCK_SCALE) && y > bPos.y && y < bPos.y + BLOCK_SCALE);
	}
	
	public void update(float deltaTime) {
		if(!isInView()) return;
		
		if(block == null) {
			generate();
		}
		
		super.update(deltaTime);
		
		Player player = world.getGame().getPlayer();
		Vector2 playPos = player.getBody().getPosition();
		if(isMouseInside(playPos.x * PPM, playPos.y * PPM)) {
			if(type == Material.LADDER) {
				float velY = player.getBody().getLinearVelocity().y;
				if(Gdx.input.isKeyPressed(Input.Keys.W) && velY < 2) {
					player.setOnLadder(true);
					player.getBody().setGravityScale(0.0f);
					player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, velY + 0.05f);
				}else if(Gdx.input.isKeyPressed(Input.Keys.S) && velY > -2) {
					player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, velY - 0.05f);
				}
			}else if(player.isOnLadder()) {
				Block below = world.getBlockRelativeTo(this, 0, -1);
				if(below != null && below.getType() != Material.LADDER) {
					player.setOnLadder(false);
				}else {
					player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, 0);
				}
			}
		}
		
		if(Globals.USE_LIGHTING) {
	        int y = -1;
	        Block above = world.getBlockRelativeTo(this, 0, y);
	        Block left = world.getBlockRelativeTo(this, -1, 0);
	        Block right = world.getBlockRelativeTo(this, 1, 0);
	        float averageLight = 0.0f;
	        boolean found = false;
	        boolean isCovered = false;
	        
	        //System.out.println(String.format("Average light: %f", averageLight));
	        lightLevel = 1.0f;
		    
	        if(type != Material.AIR && left != null && right != null) {
	            averageLight = ((left.getLightLevel() + right.getLightLevel()) / 2f) * 0.005f;
	        }
	        
    		int checkY = 0;
    		while(checkY != boardY && !isCovered) {
    			Block b = world.getBlockRelativeToBoard(boardX, checkY);
    			if(b.getType().getBlockMeta().isCanCollide()) {
    				isCovered = true;
    				break;
    			}
    			
    			checkY++;
    		}
    		
    		while(!found) {
    			if(above != null) {
    				if(above.getType() != Material.AIR || isCovered) {
    					float loss = (above.getLightLevel() - above.getType().getBlockMeta().getLightLoss());
    					lightLevel = loss;
    					found = true;
    				}else {
    					y--;
    					if(above.getType() == Material.AIR && above.getBody().getPosition().y < (-32 / PPM)) lightLevel -= 0.075f;
    					above = world.getBlockRelativeTo(this, 0, y);
    				}
    			}else {
    				break;
    			}
    		}
    		
    		lightLevel -= (type == Material.AIR) ? averageLight*2 : averageLight;
    		
    		if(lightLevel < 1.0f) elapsedLitTimer = 0.0f;
    		if(lightLevel < 0.05f) lightLevel = 0.05f;
		}
		
		if(type == Material.GRASS) {
			Block dirA = world.getBlockRelativeTo(this, 0, 1);
			if(dirA != null && dirA.getType() != Material.AIR || lightLevel <= 0.85f) {
				setType(Material.DIRT);
			}
		}else if(type == Material.DIRT && lightLevel >= 1.0f) {
		    if(Globals.USE_LIGHTING) {
    			elapsedLitTimer += deltaTime;
    			if(elapsedLitTimer >= 2.8f) {
    				setType(Material.GRASS);
    				elapsedLitTimer = 0.0f;
    			}
		    }
		}
		
		// Code for color scaling.
		
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
					
					if(type == Material.TNT) {
						int radius = 10;
						int startX = -2;
						int startY = -2;
						
						this.destroy();
						
						Vector2 pPos = player.getBody().getPosition();
						double pDist = Globals.getDistanceFrom(position.x, pPos.x, position.y, pPos.y);
						
						if(pDist < 2.0d) {
							player.setHealth(player.getHealth() - 6.0f);
							
							float multiplier = (position.x < pPos.x) ? 1 : -1;
							float yMultiplier = (position.y < pPos.y) ? 1 : -1;
							System.out.println(multiplier);
							System.out.println(yMultiplier);
							
							float velX = (float) (Math.min(pDist, 2.0f) * (3.25f * multiplier));
							float velY = (float) (Math.min(pDist, 2.0f) * (3.25f * yMultiplier));
							
							player.getBody().applyLinearImpulse(new Vector2(velX, velY), player.getBody().getWorldCenter(), true);
						}
						
						for(int x = startX; x <= (startX + radius); x++) {
							for(int y = startY; y <= (startY + radius); y++) {
								Block b = world.getBlockRelativeTo(this, x, y); 
								
								if(b == null) continue;
								
								double bX = b.getMyPosition().x;
								double bY = b.getMyPosition().y;
								
								double dist = Globals.getDistanceFrom(position.x, bX, position.y, bY);
								
								if(dist < (radius / 4.0) * 1.2) {
									b.destroy();
								}
							}
						}
					}
				}
			}
		}
		
		if(mouseDown) {
			elapsedDestroyTimer += deltaTime;
			if(elapsedDestroyTimer >= destroySpeed) {
				world.playSound(type.getBlockMeta().getDigSound());
				elapsedDestroyTimer = 0.0f;
			}
		}
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
	
	public void destroy() {
		for(ItemStack item : type.getBlockMeta().getDrops()) {
			world.spawnDrop(item, getMyPosition());
		}
		setType(Material.AIR);
	}
	
	public boolean isExposedToElements() {
		Block above = world.getBlockRelativeTo(this, 0, -1);
		Block right = world.getBlockRelativeTo(this, 1, 0);
		Block left = world.getBlockRelativeTo(this, -1, 0);
		boolean isExposed = (above != null && above.getType().getBlockMeta().isCanCollide());
		isExposed = (isExposed && (right != null && right.getType().getBlockMeta().isCanCollide()));
		isExposed = (isExposed && (left != null && left.getType().getBlockMeta().isCanCollide()));
		return isExposed;
	}
	
	private void drawTypeSprite(Material t, SpriteBatch batch, Color c) {
		if(t.getTexture() != null) {
			float green = (t == Material.TALL_GRASS) ? 1f : 0f;
			Sprite sprite = new Sprite(t.getTexture());
			if(t != Material.TALL_GRASS) {
				if(c != null) {
					sprite.setColor(c);
				}else {
					if(spriteColor != Globals.DEFAULT_COLOR) {
						sprite.setColor(lightLevel, lightLevel, lightLevel, 1f);
					} else {
						sprite.setColor(spriteColor);
					}
				}
			}else {
				sprite.setColor(0f, green, 0f, 1f);
			}
			sprite.setBounds(block.getPosition().x * PPM - (PPM * Globals.PHYS_BLOCK_SCALE), 
				block.getPosition().y * PPM - (PPM * Globals.PHYS_BLOCK_SCALE), BLOCK_SCALE, BLOCK_SCALE);
			sprite.draw(batch);
		}
	}
	
	public void render(SpriteBatch batch, ShapeRenderer shapeRender) {
		if(!isInView() || block == null) return;
		
		if(!type.getBlockMeta().isOpaque() || type == Material.AIR) {
			drawTypeSprite(lastType, batch, new Color(128f / 255, 128f / 255, 128f / 255, 1.0f));
			
			if(Globals.USE_LIGHTING) {
	            if(lightLevel != 1.0f) {
	            	batch.end();
	            	Gdx.gl.glEnable(GL20.GL_BLEND);
	            	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	            	shapeRender.begin(ShapeType.Filled);
	            	shapeRender.setColor(lightLevel - 0.125f, lightLevel - 0.125f, lightLevel - 0.125f, 0.25f);
	            	shapeRender.rect(block.getPosition().x * PPM - (PPM * Globals.PHYS_BLOCK_SCALE), 
	            		block.getPosition().y * PPM - (PPM * Globals.PHYS_BLOCK_SCALE), 
	            	BLOCK_SCALE, BLOCK_SCALE);
	            	shapeRender.end();
	            	Gdx.gl.glDisable(GL20.GL_BLEND);
	            	batch.begin();
	            }
	        }
		}
		
		drawTypeSprite(type, batch, null);
		
		super.render(batch, shapeRender);
	}
	
	public void setType(Material type) {
		BlockMeta meta = type.getBlockMeta();
		if(meta == null) throw new IllegalArgumentException("Cannot set type to non-block type.");
		
		if(meta.isOpaque()) {
			this.lastType = this.type;
		}
		
		this.type = type;
		this.meta = meta;
		
		if(type == Material.TNT) {
			beginColorScale(Color.WHITE, 2.0f, (float) 0.035 * 3);
		}
		
		if(block != null) {
			for(Fixture fix : block.getFixtureList()) {
				if(fix.getFilterData().categoryBits == Globals.BLOCK_BIT) {
					fix.setSensor(!meta.isCanCollide());
					break;
				}
			}
		}
		
		Sound step = type.getBlockMeta().getStepSound();
		if(step != null) world.playSound(step);
	}
	
	public Material getType() {
		return this.type;
	}
	
	public Body getBody() {
		return this.block;
	}
	
	public Vector2 getMyPosition() {
		return (block != null) ? new Vector2(block.getPosition().x, block.getPosition().y) : position;
	}
}
