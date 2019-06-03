package org.xmakerx.raidpractice.arena;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.xmakerx.raidpractice.RaidPractice;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Arena {
	
	final RaidPractice instance;
	private final String name;
	private final World world;
	private final HashSet<Spawn> spawns;
	private final ProtectedCuboidRegion region;
	private final HashSet<ArenaFlag> flags;
	private final HashSet<ArenaSign> signs;
	private HashSet<ProtectedCuboidRegion> blacklist;
	private HashSet<ProtectedCuboidRegion> claims;
	private Inventory gear;
	private String schematicName;
	
	// This method is called when a new arena is created.
	public Arena(final RaidPractice main, final String name, final World world, final ProtectedCuboidRegion region) {
		this.instance = main;
		this.name = name;
		this.world = world;
		this.region = region;
		this.gear = null;
		this.spawns = new HashSet<Spawn>();
		this.flags = new HashSet<ArenaFlag>();
		this.signs = new HashSet<ArenaSign>();
		this.blacklist = new HashSet<ProtectedCuboidRegion>();
		this.claims = new HashSet<ProtectedCuboidRegion>();
		this.addDefaultFlags();
	}
	
	// This method is called when loading an arena.
	public Arena(final RaidPractice main, final String name, final World world, final ProtectedCuboidRegion region, final Inventory gear) {
		this.instance = main;
		this.name = name;
		this.world = world;
		this.region = region;
		this.gear = gear;
		this.spawns = new HashSet<Spawn>();
		this.flags = new HashSet<ArenaFlag>();
		this.signs = new HashSet<ArenaSign>();
		this.blacklist = new HashSet<ProtectedCuboidRegion>();
		this.claims = new HashSet<ProtectedCuboidRegion>();
		this.addDefaultFlags();
	}
	
	private void addDefaultFlags() {
		final HashMap<Flag<?>, Object> regionFlags = new HashMap<Flag<?>, Object>();
		regionFlags.put(DefaultFlag.BUILD, State.ALLOW);
		region.setFlags(regionFlags);
		flags.add(new ArenaFlag(false, new String[] {"pvp"}));
		flags.add(new ArenaFlag(false, new String[] {"respawn"}));
		flags.add(new ArenaFlag(false, new String[] {"inventory_drops", "drops"}));
		flags.add(new ArenaFlag(true, new String[] {"randomSpawns"}));
		flags.add(new ArenaFlag(false, new String[] {"hunger"}));
		flags.add(new ArenaFlag(false, new String[] {"itemDrops"}));
		flags.add(new ArenaFlag(120, new String[] {"timelimit"}));
		flags.add(new ArenaFlag(5, new String[] {"maxPlayers"}));
		flags.add(new ArenaFlag(name, new String[] {"displayName"}));
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setGear(final Inventory gear) {
		this.gear = gear;
	}
	
	public Inventory getGear() {
		return this.gear;
	}
	
	public void setSchematicName(final String schematic) {
		this.schematicName = schematic;
	}
	
	public String getSchematicName() {
		return this.schematicName;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public ProtectedCuboidRegion getRegion() {
		return this.region;
	}
	
	public void addSpawn(final Spawn spawn) {
		this.spawns.add(spawn);
	}
	
	public void delSpawn(final Spawn spawn) {
		this.spawns.remove(spawn);
	}
	
	public HashSet<Spawn> getSpawns() {
		return this.spawns;
	}
	
	public ArenaFlag getFlag(final String flag) {
		for(ArenaFlag type : flags) {
			if(type.isAlias(flag)) {
				return type;
			}
		}
		return null;
	}
	
	public HashSet<ArenaFlag> getFlags() {
		return this.flags;
	}
	
	public void addArenaSign(final ArenaSign arenaSign) {
		this.signs.add(arenaSign);
	}
	
	public HashSet<ArenaSign> getArenaSigns() {
		return this.signs;
	}
	
	public void blacklistRegion(final ProtectedCuboidRegion region) {
		this.blacklist.add(region);
	}
	
	public boolean isBlacklisted(final Location location) {
		ApplicableRegionSet regions = instance.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		for(ProtectedRegion region : regions) {
			if(region instanceof ProtectedCuboidRegion) {
				if(blacklist.contains((ProtectedCuboidRegion)region)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setBlacklist(final HashSet<ProtectedCuboidRegion> blacklist) {
		this.blacklist = blacklist;
	}
	
	public HashSet<ProtectedCuboidRegion> getBlacklist() {
		return this.blacklist;
	}
	
	public void claimRegion(final ProtectedCuboidRegion region) {
		this.claims.add(region);
	}
	
	public boolean isClaimed(final Location location) {
		ApplicableRegionSet regions = instance.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		for(ProtectedRegion region : regions) {
			if(region instanceof ProtectedCuboidRegion) {
				if(claims.contains((ProtectedCuboidRegion)region)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public HashSet<ProtectedCuboidRegion> getClaims() {
		return this.claims;
	}
}
