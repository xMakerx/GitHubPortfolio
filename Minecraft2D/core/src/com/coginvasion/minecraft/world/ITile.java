package com.coginvasion.minecraft.world;

import com.badlogic.gdx.math.Vector2;

public interface ITile {
	
	public void onRightClick();
	public void mouseDown();
	public void mouseUp();
	
	public Vector2 getMyPosition();
	public void destroy();
}
