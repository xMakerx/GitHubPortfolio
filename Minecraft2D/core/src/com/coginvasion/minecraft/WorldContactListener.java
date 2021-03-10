package com.coginvasion.minecraft;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.coginvasion.minecraft.entity.Item;
import com.coginvasion.minecraft.entity.Player;
import com.coginvasion.minecraft.world.Block;
import com.coginvasion.minecraft.world.GameWorld;
import com.coginvasion.minecraft.world.Material;

public class WorldContactListener implements ContactListener {
	
	final Minecraft2D game;
	final GameWorld world;
	final Player player;
	
	public WorldContactListener(Minecraft2D game) {
		this.game = game;
		this.world = game.getWorld();
		this.player = game.getPlayer();
	}

	public void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		
		if(cDef == (Globals.STEVE_BIT | Globals.BLOCK_EDGE_BIT)) {
			Block b = (fixA.getUserData() instanceof Block) ? ((Block) fixA.getUserData()) : ((Block) fixB.getUserData());
			if(b != null) {
				if(b.getType() == Material.CACTUS) {
					player.setHealth(player.getHealth() - b.getType().getBlockMeta().getContactDamage());
					player.getBody().applyLinearImpulse(new Vector2(player.getBody().getLinearVelocity().x * -1.8f, 0.0f), player.getBody().getWorldCenter(), true);
				}else if(b.getType() == Material.LADDER) {
					player.setOnLadder(true);
				}
				
				if((player.getBody().getPosition().y - Globals.PHYS_BLOCK_SCALE) >= b.getBody().getPosition().y) {
					float distance = world.getDistanceFromPlayer(b.getMyPosition());
					if(distance <= 0.84f && b.getType() != Material.AIR) world.playSound(b.getType().getBlockMeta().getStepSound());
				}
			}
		}else if(cDef == (Globals.STEVE_BIT | Globals.ITEM_BIT)) {
			Item i = (fixA.getUserData() instanceof Item) ? ((Item) fixA.getUserData()) : ((Item) fixB.getUserData());
			world.removeDrop(i);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		
		if(cDef == (Globals.STEVE_BIT | Globals.BLOCK_EDGE_BIT)) {
			Block b = (fixA.getUserData() instanceof Block) ? ((Block) fixA.getUserData()) : ((Block) fixB.getUserData());
			if(b != null && b.getType() == Material.LADDER) {
				player.setOnLadder(false);
			}
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}

}
