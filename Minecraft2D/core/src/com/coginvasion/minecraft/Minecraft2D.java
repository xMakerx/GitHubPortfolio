package com.coginvasion.minecraft;

import static com.coginvasion.minecraft.Globals.PPM;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.coginvasion.minecraft.entity.Player;
import com.coginvasion.minecraft.world.GameWorld;
import com.coginvasion.minecraft.world.Material;

public class Minecraft2D extends ApplicationAdapter {
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private World b2dWorld;
	//private RayHandler rayHandler;
	
	public AssetManager assetMgr;
	private Box2DDebugRenderer b2dDebugRenderer;
	private Player player = null;
	private GameWorld gWorld;
	public InputHandler input;
	private boolean loading;

	public void create() {
		b2dWorld = new World(new Vector2(0, Globals.GRAVITY), false);
		//rayHandler = new RayHandler(b2dWorld);
		camera = new OrthographicCamera();
		b2dDebugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();
		input = new InputHandler(this);
		
		// Here is where we're going to load our assets.
		assetMgr = new AssetManager();
		assetMgr.load("images/tileset.png", Texture.class);
		assetMgr.load("images/items.png", Texture.class);
		assetMgr.load("images/player.png", Texture.class);
        assetMgr.load("images/player-headless.png", Texture.class);
		assetMgr.load("audio/bgm/math.ogg", Music.class);
		assetMgr.load("audio/mob/classic_hurt.ogg", Sound.class);
		assetMgr.load("audio/damage/fallsmall.ogg", Sound.class);
		assetMgr.load("audio/damage/fallbig.ogg", Sound.class);
		assetMgr.load("audio/random/pop.ogg", Sound.class);
		
		for(Material mat : Material.values()) {
			for(String path : mat.getStepSoundPaths()) {
				if(!assetMgr.getAssetNames().contains(path, true)) {
					assetMgr.load(path, Sound.class);
				}
			}
			
			for(String path : mat.getDigSoundPaths()) {
				if(!assetMgr.getAssetNames().contains(path, true)) {
					assetMgr.load(path, Sound.class);
				}
			}
		}
		
		loading = true;
		
		gWorld = new GameWorld(this);
		
		Gdx.gl.glClearColor(0.298f, 0.717f, 1f, 0f);
		setupCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//rayHandler.set
	}
	
	private void doneLoading() {
		loading = false;
		Material.load(assetMgr);
		gWorld.load();
		
		player = new Player(this, gWorld, assetMgr);
		player.generate(b2dWorld);
		b2dWorld.setContactListener(new WorldContactListener(this));
		Gdx.input.setInputProcessor(input);
	}
	
	public void handleInput() {
		if(player != null) {
			Vector2 vel = player.getBody().getLinearVelocity();
			
			if(Gdx.input.isKeyPressed(Input.Keys.D) && !player.isDead()) {
				if(vel.x <= 2) {
					player.getBody().applyLinearImpulse(new Vector2(0.1f, 0), player.getBody().getWorldCenter(), true);
				}else {
					player.getBody().setLinearVelocity(vel.x, vel.y);
				}
			}
			
			if(Gdx.input.isKeyPressed(Input.Keys.A) && !player.isDead()) {
				if(vel.x >= -2) {
					player.getBody().applyLinearImpulse(new Vector2(-0.1f, 0), player.getBody().getWorldCenter(), true);
				}else {
					player.getBody().setLinearVelocity(vel.x, vel.y);
				}
			}
			
			if(Gdx.input.isKeyPressed(Input.Keys.K)) {
				System.out.println(vel.y);
				if(!(vel.y > 4f)) {
					player.getBody().applyLinearImpulse(new Vector2(0, 0.8f), player.getBody().getWorldCenter(), true);
				}
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.I)) {
				Vector2[] spawnLocations = gWorld.getSpawnLocations();
				Vector2 pos = spawnLocations[ThreadLocalRandom.current().nextInt(0, spawnLocations.length)];
				player.getBody().setTransform(pos, 0f);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.B)) {
				ArrayList<Material> mats = new ArrayList<Material>();
				for(Material mat : Material.values()) {
					if(mat != Material.AIR) mats.add(mat);
				}
				int currentMat = mats.indexOf(input.getBuildMaterial());
				Material mat;
				if(currentMat + 1 < mats.size()) {
					mat = mats.get(currentMat + 1);
				}else if(currentMat + 1 >= mats.size()) {
					mat = mats.get(0);
				}else {
					mat = Material.OAK_PLANKS;
				}
				
				input.setBuildMaterial(mat);
				System.out.println(mat);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
				input.setBuildMaterial(Material.STONE_PICKAXE);
			}

			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
				input.setBuildMaterial(Material.STONE_SHOVEL);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
				input.setBuildMaterial(Material.DIAMOND_PICKAXE);
			}

			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
				input.setBuildMaterial(Material.DIAMOND_SHOVEL);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
				input.setBuildMaterial(Material.GOLD_PICKAXE);
			}

			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
				input.setBuildMaterial(Material.GOLD_SHOVEL);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
				input.setBuildMaterial(Material.TNT);
			}
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !player.isDead()) {
				player.jump();
			}
		}
	}
	
	public void update(float deltaTime) {
		handleInput();
		input.update(deltaTime);

		b2dWorld.step(1 / 60f, 6, 2);
		//rayHandler.update();
		gWorld.update(deltaTime);
		
		if(player != null) {
			player.update(deltaTime);
		}
	}
	
	public void postLoadRender() {
		gWorld.render(batch);
		player.render(batch);
        //rayHandler.setCombinedMatrix(camera);
        //rayHandler.updateAndRender();
	}
	
	public void render() {
		update(Gdx.graphics.getDeltaTime());
		if(loading && assetMgr.update()) doneLoading();
		batch.setProjectionMatrix(camera.combined);
		Gdx.graphics.setTitle(Globals.TITLE + String.valueOf(Gdx.graphics.getFramesPerSecond()));
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if(!loading) {
			batch.begin();
			postLoadRender();
			batch.end();
		}
		
		if(Globals.DEBUG) b2dDebugRenderer.render(b2dWorld, camera.combined.scl(PPM));
	}
	
	public void resize(int width, int height) {
		setupCamera(width, height);
	}
	
	private void setupCamera(int width, int height) {
		camera.setToOrtho(false, width / 2f, height / 2f);
	}
	
	public OrthographicCamera getCamera() {
		return this.camera;
	}
	
	public World getBox2DWorld() {
		return this.b2dWorld;
	}
	
	/*
	public RayHandler getRayHandler() {
	    return this.rayHandler;
	}*/
	
	public GameWorld getWorld() {
		return this.gWorld;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public AssetManager getAssetManager() {
		return this.assetMgr;
	}
	
	public void dispose() {
		super.dispose();
		b2dDebugRenderer.dispose();
		b2dWorld.dispose();
		gWorld.dispose();
		batch.dispose();
		assetMgr.dispose();
	}

}
