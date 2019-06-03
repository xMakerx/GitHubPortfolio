package org.xmakerx.raidpractice.arena;

import org.bukkit.Location;

public class Spawn {
	
	// Is this a player spawn, lobby spawn, or an egg spawn?
	public enum SpawnType {
		EGG, PLAYER, LOBBY;
	}
	
	private final String name;
	private final Location location;
	private final SpawnType type;
	
	public Spawn(final String name, final Location location, final SpawnType type) {
		this.name = name;
		this.location = location;
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public SpawnType getType() {
		return this.type;
	}
}
