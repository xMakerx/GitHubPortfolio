package com.coginvasion.minecraft.entity;

import static com.coginvasion.minecraft.Globals.PPM;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.world.GameWorld;
import com.coginvasion.minecraft.world.Material;

public class Item extends Entity {
	
	private final Material type;
	private Vector2 position;

	public Item(Material type, GameWorld world, AssetManager assetMgr) {
		super(world, assetMgr);
		this.type = type;
		this.position = new Vector2(0, 0);
		this.maxHealth = Integer.MAX_VALUE;
		this.health = maxHealth;
		setBounds(0, 0, Globals.BLOCK_SCALE / 2, Globals.BLOCK_SCALE / 2);
		setRegion(type.getTexture());
	}
	
	public void spawn(World b2dWorld, Vector2 position) {
		this.position = position;
		generate(b2dWorld);
	}
	
	public void generate(World b2dWorld) {
		BodyDef def = new BodyDef();
		def.type = BodyDef.BodyType.DynamicBody;
		def.fixedRotation = true;
		def.position.set(new Vector2(position.x / PPM, position.y / PPM));
		body = b2dWorld.createBody(def);
		
		FixtureDef fDef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(Globals.PHYS_BLOCK_SCALE / 2, Globals.PHYS_BLOCK_SCALE / 2);
		fDef.shape = shape;
		fDef.filter.categoryBits = Globals.ITEM_BIT;
		fDef.filter.groupIndex = -2;
		fDef.density = 1.0f;
		body.createFixture(fDef).setUserData(this);
		
		shape.setAsBox(Globals.PHYS_BLOCK_SCALE / 2, Globals.PHYS_BLOCK_SCALE / 2);
		fDef.shape = shape;
		fDef.filter.categoryBits = Globals.ITEM_BIT;
		fDef.filter.groupIndex = -2;
		fDef.density = 1.0f;
		fDef.isSensor = true;
		body.createFixture(fDef).setUserData(this);
		
		shape.dispose();
	}
	
	public void render(SpriteBatch batch) {
		super.draw(batch);
	}
	
	public void update(float deltaTime) {
		super.update(deltaTime);
		position = body.getPosition();
		setPosition((position.x * PPM) - getWidth() / 2f, 
			(position.y * PPM) - (getHeight() / 2f));
	}
	
	public Material getMaterial() {
		return this.type;
	}

}
