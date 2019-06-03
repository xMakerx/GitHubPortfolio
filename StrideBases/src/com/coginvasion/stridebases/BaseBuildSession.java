package com.coginvasion.stridebases;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BaseBuildSession {
	
	final Player player;
	final Base base;
	final HashMap<Location, HashMap<Integer, Byte>> previousData;
	boolean baseComplete;
	
	public BaseBuildSession(final Player player, final Base base) {
		this.player = player;
		this.base = base;
		this.previousData = new HashMap<Location, HashMap<Integer, Byte>>();
		this.baseComplete = false;
	}
	
	public void setBaseComplete(final boolean flag) {
		this.baseComplete = flag;
		if(flag) {
			new BukkitRunnable() {
				
				public void run() {
					previousData.clear();
					StrideBases.getSettings().removeBuildSession(player.getUniqueId());
					player.sendMessage(StrideBases.getSettings().getMessage("buildTimeout"));
				}
				
			}.runTaskLater(StrideBases.getInstance(), 20L * 30);
		}
	}
	
	public boolean isBaseComplete() {
		return this.baseComplete;
	}
	
	public void addPreviousData(final Location loc, final int typeId, final byte data) {
		final HashMap<Integer, Byte> map = new HashMap<Integer, Byte>();
		map.put(typeId, data);
		previousData.put(loc, map);
	}
	
	@SuppressWarnings("deprecation")
	public void restorePreviousData() {
		for(Map.Entry<Location, HashMap<Integer, Byte>> entry : previousData.entrySet()) {
			final Location loc = entry.getKey();
			final HashMap<Integer, Byte> pair = entry.getValue();
			final int typeId = (int) pair.keySet().toArray()[0];
			final byte data = pair.get(typeId);
			
			loc.getBlock().setTypeIdAndData(typeId, data, true);
		}
		
		StrideBases.getSettings().addOwnedBase(player.getUniqueId(), base);
		StrideBases.getSettings().removeBuildSession(player.getUniqueId());
		player.sendMessage(StrideBases.getSettings().getMessage("baseUndone"));
	}
	
	public Base getBase() {
		return this.base;
	}
}
