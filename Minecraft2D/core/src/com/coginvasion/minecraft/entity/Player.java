package com.coginvasion.minecraft.entity;

import static com.coginvasion.minecraft.Globals.PPM;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.Minecraft2D;
import com.coginvasion.minecraft.inventory.PlayerInventory;
import com.coginvasion.minecraft.item.HeldItem;
import com.coginvasion.minecraft.item.ItemType;
import com.coginvasion.minecraft.meta.ItemMeta;
import com.coginvasion.minecraft.world.GameWorld;

public class Player extends Entity {
	
	public enum State {
		IDLE, RUNNING, JUMPING, FALLING;
	}
	
	final Minecraft2D game;
	private State state;
	private State previousState;
	private Sprite head;
	private TextureRegion stand;
	private Animation<TextureRegion> runAnim;
	private PlayerInventory inventory;
	public HeldItem item;
	private float stateTimer;
	private boolean runningRight;
	private boolean isOnLadder;
	private int lastMouseX;
	
	public Player(Minecraft2D game, GameWorld world, AssetManager assetMgr) {
		super(world, assetMgr);
		this.game = game;
		this.setMaxHealth(50000.0d);
		this.setHealth(maxHealth);
		this.inventory = new PlayerInventory(this, "Player Inventory", 9);
		this.hurtSound = assetMgr.get("audio/mob/classic_hurt.ogg", Sound.class);
		this.state = previousState = State.IDLE;
		this.head = new Sprite();
		this.isOnLadder = false;
		this.item = new HeldItem(this);
		this.lastMouseX = 0;
	
		Array<TextureRegion> frames = new Array<TextureRegion>();
		Texture baseTexture = assetMgr.get("images/player-headless.png", Texture.class);
		
		// Let's setup our run animation.
		for(int i = 1; i < 12; i++) {
			frames.add(new TextureRegion(baseTexture, i * 32, 0, 32, 64));
		}
		
		runAnim = new Animation<TextureRegion>(0.1f, frames);
		stand = new TextureRegion(baseTexture, 0, 0, 32, 64);
		
		// Let's setup our head
		head.setBounds(0, 0, 7, 7);
		head.setRegion(new TextureRegion(baseTexture, 250, 73, 14, 14));
		//head.setOrigin(3.5f, 7f);
		head.setOriginCenter();
		
		setBounds(0, 0, (Globals.BLOCK_SCALE * 2) / 2, (Globals.BLOCK_SCALE * 2));
		setRegion(stand);
	}
	
	public void setOnLadder(boolean flag) {
		this.isOnLadder = flag;
		if(!flag) {
			body.setGravityScale(1);
		}
	}
	
	public boolean isOnLadder() {
		return this.isOnLadder;
	}
	
	public void jump() {
		if(state != State.JUMPING && body.getLinearVelocity().y == 0) {
			body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
			state = State.JUMPING;
		}
	}
	
	public void generate(World b2dWorld) {
		BodyDef def = new BodyDef();
		def.type = BodyDef.BodyType.DynamicBody;
		def.fixedRotation = true;
		body = b2dWorld.createBody(def);
		
		Vector2[] spawnLocations = world.getSpawnLocations();
		
		// Let's make the edge shapes
		float width = Globals.PHYS_BLOCK_SCALE - (Globals.PHYS_BLOCK_SCALE / 4);
		float offsetX = 0f;
		
		// Bottom
		createEdgeShape(new Vector2(-width + offsetX, -0.45f), new Vector2(width + offsetX, -0.45f));
		
		// Left side
		createEdgeShape(new Vector2(-width + offsetX, -0.45f), new Vector2(-width + offsetX, 0.45f));
		
		// Right side
		createEdgeShape(new Vector2(width + offsetX, -0.45f), new Vector2(width + offsetX, 0.45f));
		
		// Top
		createEdgeShape(new Vector2(-width + offsetX, 0.45f), new Vector2(width + offsetX, 0.45f));
		
		Vector2 pos = spawnLocations[ThreadLocalRandom.current().nextInt(0, spawnLocations.length)];
		//body.setTransform(pos, 0f);
		body.setTransform(new Vector2(35, 60), 0f);
	}
	
	public void createEdgeShape(Vector2 begin, Vector2 end) {
		FixtureDef fDef = new FixtureDef();
		EdgeShape shape = new EdgeShape();
		shape.set(begin, end);
		fDef.shape = shape;
		fDef.filter.categoryBits = Globals.STEVE_BIT;
		fDef.density = 1.0f;
		
		body.createFixture(fDef).setUserData(this);
		shape.dispose();
	}
	
	public Vector2 getHeadOrigin() {
	    return new Vector2(head.getX() + (head.getWidth() / 2), head.getY() + (head.getHeight() / 2));
	}

	public void update(float deltaTime) {
		super.update(deltaTime);
		setPosition((body.getPosition().x * Globals.PPM) - getWidth() / 2f, 
			(body.getPosition().y * Globals.PPM) - (getHeight() / 2) + (1 + spriteOffsetY));
		setRegion(getRegion(deltaTime));
		
        if(!isDead()) {
        	head.setPosition(this.getX() + 4.3f, this.getY() + 23);
        }else {
        	head.setPosition(this.getX() - 30f, this.getY() + 5.3f);
        	item.setSize(0f, 0f);
        }
        
        Vector2 origin = getHeadOrigin();
        int mouseX = Gdx.input.getX();
        int mouseY = (Gdx.app.getGraphics().getHeight() - Gdx.input.getY());
        
        lastMouseX = mouseX;
        
        //head.setRotation(MathUtils.radiansToDegrees * MathUtils.atan2(mouseY - this.getY(), mouseX - this.getX()));
        
        //head.setRotation((float) Math.toDegrees(Math.atan2((Gdx.graphics.getHeight() - Gdx.input.getY()) - head.getY(), Gdx.input.getX() - head.getX())));
        //head.setRotation((float) (Math.toDegrees(Math.atan2(mouseY - head.getY(), mouseX - head.getX()))) + 45);
        //head.setRotation((float) Math.toDegrees(Math.atan2(mouseY - body.getPosition().y, Gdx.input.getX() - body.getPosition().x)));
        
       // head.setRotation(360 - (float) ((Math.toDegrees(Math.atan2(Gdx.input.getX() - head.getX(), head.getY() - Gdx.input.getY())) + 360.0) % 360.0) + 10);
		
		OrthographicCamera camera = game.getCamera();
		camera.position.set(getBody().getPosition().x * PPM, getBody().getPosition().y * PPM, 0);
		camera.update();
		

		item.update(deltaTime);
		
        if(!runningRight) {
        	item.setPosition(item.getX() - 8.5f, item.getY());
        	
        	if(item.getMaterial().getItemMeta() != null) {
        		ItemMeta meta = item.getMaterial().getItemMeta();
        		
        		if(meta.getType() != ItemType.COLLECTIBLE) {
        			item.setFlip(true, false);
        		}
        	}
        	
        }else if(item.isFlipX()) {
        	item.setFlip(false, false);
        }
	}
	
	public TextureRegion getRegion(float deltaTime) {
		state = getState();
		
		TextureRegion region;
		
		switch(state) {
			case RUNNING:
				region = runAnim.getKeyFrame(stateTimer, true);
				break;
			case JUMPING:
			case FALLING:
			case IDLE:
			default:
				region = stand;
				break;
		}
		
		if((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
			region.flip(true, false);
			head.setFlip(true, false);
			runningRight = false;
		}else if((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
			region.flip(true, false);
			head.setFlip(false, false);
			runningRight = true;
		}
		
		stateTimer = (state == previousState) ? stateTimer + deltaTime : 0;
		previousState = state;
		
		return region;
	}
	
	public State getState() {
		
		Vector2 vel = body.getLinearVelocity();
		
		if(vel.x != 0) {
			return State.RUNNING;
		}else if((vel.y > 0 && state == State.JUMPING) || vel.y < 0 && previousState == State.JUMPING) {
			return State.JUMPING;
		}else if(vel.y < 0) {
			return State.FALLING;
		}else {
			return State.IDLE;
		}
	}

	public void render(SpriteBatch batch) {
		super.draw(batch);
		head.draw(batch);
		item.render(batch);
	}
	
	public PlayerInventory getInventory() {
		return this.inventory;
	}
	
}
