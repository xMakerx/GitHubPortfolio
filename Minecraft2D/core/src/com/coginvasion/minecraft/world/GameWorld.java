package com.coginvasion.minecraft.world;

import static com.coginvasion.minecraft.Globals.PHYS_BLOCK_SCALE;
import static com.coginvasion.minecraft.Globals.PPM;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.coginvasion.minecraft.Globals;
import com.coginvasion.minecraft.Minecraft2D;
import com.coginvasion.minecraft.entity.Item;
import com.coginvasion.minecraft.item.ItemStack;

public class GameWorld {
	
	public static final int WORLD_WIDTH = 1024;
	public static final int WORLD_HEIGHT = 120;
	
	final Minecraft2D game;
	final Random random;
	
	final OpenSimplexNoise noise;
	
	private Music bgm;
	private OrthographicCamera camera;
	private ShapeRenderer shapeRender;
	private Block[][] blocks;
	private Array<Item> drops;
	
	private Vector2[] safeSpawnLocations;
	
	public GameWorld(Minecraft2D game) {
		this.game = game;
		this.random = new Random();
		this.blocks = new Block[2048][120];
		this.camera = game.getCamera();
		this.shapeRender = new ShapeRenderer();
		this.drops = new Array<Item>();
		this.safeSpawnLocations = new Vector2[20];

		this.noise = new OpenSimplexNoise(System.currentTimeMillis());
	}
	
	public void spawnDrop(ItemStack item, Vector2 position) {
		double offsetX = 3.5 * random.nextDouble();//ThreadLocalRandom.current().nextDouble(0, 3.5);
		Vector2 pos = new Vector2(position.x * PPM, position.y * PPM);
		
		for(int i = 0; i < item.getAmount(); i++) {
			Item drop = new Item(item.getMaterial(), this, game.assetMgr);
			drop.spawn(game.getBox2DWorld(), new Vector2(pos.x + (float) offsetX, pos.y + 2));
			drops.add(drop);
		}
		
		((Sound) game.assetMgr.get("audio/random/pop.ogg", Sound.class)).play();
	}
	
	public void removeDrop(Item item) {
		item.flagForDeletion();
		((Sound) game.assetMgr.get("audio/random/pop.ogg", Sound.class)).play();
	}
	
	/**
	 * Renders the white box over the block
	 * the mouse is over.
	 */
	
	public void renderBlockHover(SpriteBatch batch) {
		batch.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if(game.input.getMouseOver() != null && game.input.getMouseOver().getBody() != null) {
			Vector2 bPos = toBlockUnits(game.input.getMouseOver().getBody().getPosition());
			shapeRender.begin(ShapeType.Filled);
			shapeRender.setColor(1f, 1f, 1f, 0.35f);
			shapeRender.rect(bPos.x, bPos.y, Globals.BLOCK_SCALE, Globals.BLOCK_SCALE);
			shapeRender.end();
		}
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
		batch.begin();
	}
	
	public void renderSky(SpriteBatch batch) {
		batch.end();
		Color lightBlue = new Color(0.298f, 0.717f, 1f, 1f);
		Color firstColor = Color.CYAN;
		shapeRender.begin(ShapeType.Filled);
		shapeRender.rect(camera.position.x - (camera.viewportWidth / 2), camera.position.y - (camera.viewportHeight / 2), camera.viewportWidth, camera.viewportHeight, 
				lightBlue, lightBlue, firstColor, firstColor);
		shapeRender.end();
		batch.begin();
	}
	
	public void load() {
		
		float heightMulitplier = 10f;
		int heightAddition = (int) ((WORLD_HEIGHT/2f) - heightMulitplier);
		float smoothness = 5f;
		
		safeSpawnLocations[0] = new Vector2(35, 60);
		
		Random r = new Random();
		int seed = r.nextInt(10000);
		
		for(int x = 0; x < WORLD_WIDTH; x++) {
			Biome b = Biome.BARE;
			
			Material surfaceType = Material.GRASS;
			int ha = heightAddition;
			float s = smoothness;
			
			if(0 <= x && x <= 30) {
				b = Biome.TEMPERATE_DESERT;
				s = 15f;
				
				surfaceType = Material.SAND;
				ha = (int) Math.ceil((float) heightAddition * 0.9f);
			}
			
			int y = (int) (Math.round(noise.eval(seed, (x / s)) * heightMulitplier)) + ha;
			for(int j = 0; j < y; j++) {
				Material type = Material.GRASS;
				Vector2 p = new Vector2(x, j);
				
				double n = noise.eval(x, j);
				System.out.println(String.format("X: %s, Y: %s, N: %s", x, y, n));
				
				if(j < y - 4) {
					int distFromMax = (y - j);
					
					if(distFromMax > 8) {
						float f = r.nextFloat();
						
						if(f < 0.5f) {
							if(b == Biome.TEMPERATE_DESERT) {
								type = Material.SAND;
							}else {
								type = Material.DIRT;
							}
						}else {
							type = Material.STONE;
						}
					}else {
						type = Material.STONE;
					}
				}else if(j < y - 1) {
					type = (b == Biome.TEMPERATE_DESERT) ? Material.SAND : Material.DIRT;
				}else {
					type = surfaceType;
					//safeSpawnLocations[0] = new Vector2(x, 30);
				}
				
				if(j <= 2) {
					type = Material.BEDROCK;
				}
				
				blocks[x][j] = new Block(this, game.getBox2DWorld(), type, x, j, x*16, j*16);
			}
		}
		
		bgm = game.getAssetManager().get("audio/bgm/math.ogg", Music.class);
		bgm.setVolume(0.65f);
		beginBGM();
	}
	
	public Vector2[] getSpawnLocations() {
		return this.safeSpawnLocations;
	}
	
	public void playSound(Sound sound) {
		if(sound != null) sound.play();
	}
	
	public Vector2 toBlockUnits(Vector2 position) {
		return new Vector2(position.x * PPM - (PPM * PHYS_BLOCK_SCALE), 
			position.y * PPM - (PPM * PHYS_BLOCK_SCALE));
	}
	
	public void update(float deltaTime) {
		for(int x = 0; x < blocks.length; x++) {
			for(int y = 0; y < blocks[0].length; y++) {
				Block b = blocks[x][y];
				if(b != null) blocks[x][y].update(deltaTime);
			}
		}
		
		for(int i = 0; i < drops.size; i++) {
			Item item = drops.get(i);
			if(item.isFlaggedForDeletion()) {
				game.getBox2DWorld().destroyBody(item.getBody());
				drops.removeValue(item, true);
			}
			item.update(deltaTime);
		}
	}
	
	public void render(SpriteBatch batch) {
		shapeRender.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRender.setTransformMatrix(batch.getTransformMatrix());

		renderSky(batch);
		
		for(int x = 0; x < blocks.length; x++) {
			for(int y = 0; y < blocks[0].length; y++) {
				Block b = blocks[x][y];
				
				if(b != null) b.render(batch, shapeRender);
			}
		}
		
		for(Item item : drops) {
			item.render(batch);
		}
		
		renderBlockHover(batch);
	}
	
	public void beginBGM() {
		bgm.setVolume(0.85f);
		bgm.setLooping(true);
		bgm.play();
	}
	
	public void endBGM() {
		bgm.stop();
	}
	
	public void placeBlock(Block b, Material mat) {
		//Block above = getBlockRelativeTo(b, 0, -1);
		Block below = getBlockRelativeTo(b, 0, 1);
		
		if(mat == Material.CACTUS && (below.getType() != Material.SAND && below.getType() != Material.CACTUS)) return;
		b.setType(mat);
	}
	
	public Block getBlockRelativeToBoard(int boardX, int boardY) {
		if(boardX >= 0 && boardX < blocks.length && boardY >= 0 && boardY < blocks.length) {
			return blocks[boardX][boardY];
		}
		
		return null;
	}
	
	public Block getBlockRelativeTo(Block block, int x, int y) {
		int myX = block.boardX, myY = block.boardY;
		
		if(myX == -5 || myY + y < 0 || myY + y >= blocks[0].length || myX + x < 0 || myX + x >= blocks.length) return null;
		return blocks[myX + x][myY + y];
	}
	
	public Array<Block> getBlocks() {
		Array<Block> b = new Array<Block>();
		for(int x = 0; x < blocks.length; x++) {
			for(int y = 0; y < blocks[0].length; y++) {
				b.add(blocks[x][y]);
			}
		}
		
		return b;
	}
	
	public float getDistanceFromPlayer(Vector2 position) {
		Vector2 distance = position.sub(game.getPlayer().getBody().getPosition());
		return distance.len();
	}
	
	public Minecraft2D getGame() {
		return this.game;
	}
	
	public void dispose() {
		bgm.dispose();
	}
	
}
