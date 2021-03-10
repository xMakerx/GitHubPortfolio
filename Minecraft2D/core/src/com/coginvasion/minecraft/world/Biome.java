package com.coginvasion.minecraft.world;

public enum Biome {
	OCEAN,
	BEACH,
	SCORCHED,
	BARE,
	TUNDRA,
	TEMPERATE_DESERT,
	SHRUBLAND,
	GRASSLAND,
	TEMPERATE_DECIDUOUS_FOREST,
	TEMPERATE_RAIN_FOREST,
	SNOW,
	TAIGA, 
	SUBTROPICAL_DESERT, 
	TROPICAL_RAIN_FOREST, 
	TROPICAL_SEASONAL_FOREST;
	
	/*
	 * 					if(p > 0.75d) {
						type = Material.AIR;
					}else if(p > 0.40d) {
						if(result > 0.5) {
							type = Material.DIRT;
						}else {
							type = Material.AIR;
							if((y-1) >= 0) {
								Block below = blocks[x][y-1];
								
								if(below != null && below.getType() == Material.AIR && i < 20) {
									//safeSpawnLocations[i] = new Vector2(x, y-1);
									i++;
								}
							}
						}
					}else if(p > 0.31d) {
						if(result < 0.5) {
							type = Material.DIRT;
						}else {
							type = Material.STONE;
						}
					}else if(p > 0.27d) {
						// We're above the cave/underground portion
						if(inRange(result, 0.85, 1)) {
							type = Material.DIRT;
						}else if(inRange(result, 0.70, 0.85)) {
							type = Material.SAND;
						}else if(inRange(result, 0.55, 0.70)) {
							type = Material.WATER;
						}else {
							type = Material.AIR;
						}
						
					}else if(0.27d >= p && p > 0.03d) {
						// We're within the underground portion
						if(inRange(result, 0.95, 1)) {
							type = Material.GRAVEL;
						}else if(inRange(result, 0.90, 0.95)) {
							type = Material.DIRT;
						}else if(inRange(result, 0.2, 0.9)){
							type = Material.STONE;
						}else {
							type = Material.AIR;
						}
					}else if(p <= 0.03d) {
						// We're heading towards the edge of the world.
						if(y <= 2) {
							// This is the edge of the world.
							type = Material.BEDROCK;
						}else {
							if(result > 0) {
								type = Material.STONE;
							}else {
								type = Material.BEDROCK;
							}
						}
					}
	 */
}
