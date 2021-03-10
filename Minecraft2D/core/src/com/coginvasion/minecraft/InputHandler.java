package com.coginvasion.minecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.coginvasion.minecraft.item.ItemStack;
import com.coginvasion.minecraft.world.Block;
import com.coginvasion.minecraft.world.Material;

public class InputHandler implements InputProcessor {
	
	final Minecraft2D game;
	private Block mouseOver;
	private Block mouseDown;
	
	// The position of the player when the mouse over begun.
	private Vector2 startMOverPos;
	
	private Material buildMaterial;

	public InputHandler(Minecraft2D game) {
		this.game = game;
		this.mouseOver = null;
		this.buildMaterial = Material.OAK_PLANKS;
		
		this.startMOverPos = null;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int newParam) {
		Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.getCamera().unproject(touchPos);
        Vector2 checkPos = new Vector2(touchPos.x, touchPos.y);
        //if(Globals.DEBUG) System.out.println(String.format("X: %f, Y: %f", checkPos.x, checkPos.y));
        
        for(Block b : game.getWorld().getBlocks()) {
        	if(b != null && b.isMouseInside(checkPos.x, checkPos.y)) {
        		mouseDown = b;
        		//if(Globals.DEBUG) System.out.println(String.format("Mouse is inside block of type %s", b.getType().getName()));
        		if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && mouseOver == mouseDown) {
        			if(b.getType() == Material.AIR) game.getWorld().placeBlock(b, buildMaterial);
        		}else if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && mouseOver == mouseDown) {
        			if(b.getType() != Material.BEDROCK && b.getType() != Material.AIR) {
        				b.mouseDown();
        			}
        		}else if(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
        			if(b.getType() != Material.AIR) setBuildMaterial(b.getType());
        			System.out.println(b.getLightLevel());
        		}
        		break;
        	}
        }

		return false;
	}
	
	public void setBuildMaterial(Material mat) {
		this.buildMaterial = mat;
		game.getPlayer().getInventory().setItemInHand(new ItemStack(mat, 1));
		//game.getPlayer().getInventory().setItemInHand(new ItemStack(Material.STONE_PICKAXE, 1));
	}
	
	public Material getBuildMaterial() {
		return this.buildMaterial;
	}
	
	public void update(float deltaTime) {
		if(startMOverPos != null) {
			Vector2 pPos = game.getPlayer().getBody().getPosition();
			
			if(!startMOverPos.equals(pPos)) {
				Vector2 v1 = game.getWorld().toBlockUnits(pPos);
				Vector2 v2 = game.getWorld().toBlockUnits(startMOverPos);
				
			}
		}
	}

	public boolean keyDown(int keycode) {
		return false;
	}

	public boolean keyUp(int keycode) {
		return false;
	}

	
	public boolean keyTyped(char character) {
		return false;
	}
	
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(mouseDown != null) {
			mouseDown.mouseUp();
			mouseDown = null;
		}
		
		return false;
	}
	
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	public boolean mouseMoved(int screenX, int screenY) {
		Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.getCamera().unproject(touchPos);
        Vector2 checkPos = new Vector2(touchPos.x, touchPos.y);
        //if(Globals.DEBUG) System.out.println(String.format("X: %f, Y: %f", checkPos.x, checkPos.y));
        
        for(Block b : game.getWorld().getBlocks()) {
        	if(b != null && b.isMouseInside(checkPos.x, checkPos.y)) {
        		//if(Globals.DEBUG) System.out.println(String.format("Mouse is inside block of type %s", b.getType().getName()));
        		Block right = game.getWorld().getBlockRelativeTo(b, 1, 0);
        		Block left = game.getWorld().getBlockRelativeTo(b, -1, 0);
        		Block up = game.getWorld().getBlockRelativeTo(b, 0, 1);
        		Block below = game.getWorld().getBlockRelativeTo(b, 0, -1);
        		Block[] blocks = {right, left, up, below};
        		
        		if(game.getWorld().getDistanceFromPlayer(b.getMyPosition()) > 1.8) {
        			mouseOver = null;
        			break;
        		}
        		
        		for(Block c : blocks) {
        			if(c != null && c.getType() != Material.AIR) {
        				mouseOver = b;
        				
        				this.startMOverPos = game.getPlayer().getBody().getPosition();
        				
        				break;
        			}
        		}
        		
        		if(mouseOver != b) {
        			mouseOver = null;
        			this.startMOverPos = null;
        		}
        		
        		break;
        	}
        }
        
        return false;
	}

	public boolean scrolled(int amount) {
		return false;
	}
	
	public Block getMouseOver() {
		return this.mouseOver;
	}

}
