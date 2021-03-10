package com.coginvasion.minecraft;

import com.badlogic.gdx.graphics.Color;

public class Globals {
	
	public static final float GRAVITY = -9.8f;
	public static final boolean DEBUG = false;
	public static final boolean USE_SINGLE_BLOCK_WORLD = false;
    public static final boolean USE_LIGHTING = false;
	public static final int PPM = 32;
	public static final int BLOCK_SCALE = 16;
	public static final float PHYS_BLOCK_SCALE = 0.25f;
	public static final String TITLE = "Minecraft 2D - FPS: ";
	public static final Color DAMAGE_COLOR = new Color(1f, 0.458f, 0.458f, 1f);
	public static final Color DEFAULT_COLOR = new Color(1f, 1f, 1f, 1f);
	public static final float DEFAULT_DIG_SPEED = 0.25f;
	public static final short STEVE_BIT = 8;
	public static final short BLOCK_EDGE_BIT = 16;
	public static final short BLOCK_BIT = 24;
	public static final short ITEM_BIT = 32;
	
	public static final float RENDER_DISTANCE = 8.0f;
	
	// Stuff for materials
	public static final String[] STONE_STEP_SOUND_PATHS = {
		"audio/step/stone1.ogg", 
		"audio/step/stone2.ogg", 
		"audio/step/stone3.ogg",
		"audio/step/stone4.ogg",
		"audio/step/stone5.ogg",
		"audio/step/stone6.ogg"
	};
	
	public static final String[] WOOD_STEP_SOUND_PATHS = {
		"audio/step/wood1.ogg", 
		"audio/step/wood2.ogg", 
		"audio/step/wood3.ogg",
		"audio/step/wood4.ogg",
		"audio/step/wood5.ogg",
		"audio/step/wood6.ogg"
	};
	
	public static final String[] SAND_STEP_SOUND_PATHS = {
		"audio/step/sand1.ogg", 
		"audio/step/sand2.ogg", 
		"audio/step/sand3.ogg",
		"audio/step/sand4.ogg",
		"audio/step/sand5.ogg"
	};
	
	public static final String[] GRASS_STEP_SOUND_PATHS = {
		"audio/step/grass1.ogg", 
		"audio/step/grass2.ogg", 
		"audio/step/grass3.ogg",
		"audio/step/grass4.ogg",
		"audio/step/grass5.ogg",
		"audio/step/grass6.ogg"
	};
	
	public static final String[] GRAVEL_STEP_SOUND_PATHS = {
		"audio/step/gravel1.ogg", 
		"audio/step/gravel2.ogg", 
		"audio/step/gravel3.ogg",
		"audio/step/gravel4.ogg"
	};
	
	public static final String[] LADDER_SOUND_PATHS = {
		"audio/step/ladder1.ogg", 
		"audio/step/ladder2.ogg", 
		"audio/step/ladder3.ogg",
		"audio/step/ladder4.ogg",
		"audio/step/ladder5.ogg"
	};
	
	public static final String[] STONE_DIG_SOUND_PATHS = {
		"audio/dig/stone1.ogg", 
		"audio/dig/stone2.ogg", 
		"audio/dig/stone3.ogg",
		"audio/dig/stone4.ogg"
	};
	
	public static final String[] GRASS_DIG_SOUND_PATHS = {
		"audio/dig/grass1.ogg", 
		"audio/dig/grass2.ogg", 
		"audio/dig/grass3.ogg",
		"audio/dig/grass4.ogg"
	};
	
	public static final String[] WOOD_DIG_SOUND_PATHS = {
		"audio/dig/wood1.ogg", 
		"audio/dig/wood2.ogg", 
		"audio/dig/wood3.ogg",
		"audio/dig/wood4.ogg"
	};
	
	public static final String[] SAND_DIG_SOUND_PATHS = {
		"audio/dig/sand1.ogg", 
		"audio/dig/sand2.ogg", 
		"audio/dig/sand3.ogg",
		"audio/dig/sand4.ogg"
	};
	
	public static final String[] CLOTH_DIG_SOUND_PATHS = {
		"audio/dig/cloth1.ogg", 
		"audio/dig/cloth2.ogg", 
		"audio/dig/cloth3.ogg",
		"audio/dig/cloth4.ogg"
	};
	
	public static final String[] GRAVEL_DIG_SOUND_PATHS = {
		"audio/dig/gravel1.ogg", 
		"audio/dig/gravel2.ogg", 
		"audio/dig/gravel3.ogg",
		"audio/dig/gravel4.ogg"
	};
	
	public static final String[] TNT_SOUND_PATHS = {
		"audio/random/tnt.ogg"
	};
	
	public static double getDistanceFrom(double x1, double x2, double y1, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

}
