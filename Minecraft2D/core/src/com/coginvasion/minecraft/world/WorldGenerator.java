package com.coginvasion.minecraft.world;

public class WorldGenerator {
	
	public static final int WORLD_WIDTH = 2048;
	public static final int WORLD_HEIGHT = 120;
	
	public static final int MAX_BIOME_LENGTH = 192;
	
	protected GameWorld world;
	protected OpenSimplexNoise noise;
	
	protected final long SEED;
	
	protected Block[][] blocks;
	
	public WorldGenerator(GameWorld gWorld, long seed) {
		this.world = gWorld;
		this.noise = new OpenSimplexNoise(seed);
		this.blocks = new Block[WORLD_WIDTH][WORLD_HEIGHT];
		this.SEED = seed;
	}
	
	public static void main(String[] args) {
		OpenSimplexNoise noise = new OpenSimplexNoise(System.currentTimeMillis());
		
		for(int x = 0; x < WORLD_WIDTH; x++) {
			double nx = (x / WORLD_WIDTH);
			System.out.println(String.format("X: %s. N: %s", x, noise.eval(nx, 0)));
		}
	}
}
