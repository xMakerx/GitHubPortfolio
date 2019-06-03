package com.coginvasion.stride.barrier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.coginvasion.stride.StrideBarriers;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

public class BarrierUtil {
	
	/**
	 * Checks if it's safe for a player to build a barrier.
	 * @param Player player (The player attempting to build the barrier)
	 * @param List<Block> The outline blocks.
	 * @return
	 */
	
	public static boolean canBuild(final Player player, final List<Block> blocks) {
		for(final Block b : blocks) {
			final Faction f = BoardColl.get().getFactionAt(PS.valueOf(b.getLocation()));
			final MPlayer mPlayer = MPlayer.get(player);
			final Faction plyFaction = mPlayer.getFaction();
			
			if(plyFaction.getName().equalsIgnoreCase(FactionColl.get().getNone().getName())) {
				player.sendMessage(StrideBarriers.getSettings().getMessage("mustHaveFaction"));
				return false;
			}else if(plyFaction.getLeader() != mPlayer) {
				player.sendMessage(StrideBarriers.getSettings().getMessage("mustBeLeader"));
				return false;
			}else if(f != plyFaction || !StrideBarriers.getWorldGuard().canBuild(player, b)) {
				player.sendMessage(StrideBarriers.getSettings().getMessage("mustBeInFactionLand"));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the first block at a location's Y that is safe.
	 * @param Location (the location to check)
	 * @return A new location that is of a safe material.
	 */
	
	public static Location getSafeLocation(Location loc) {
		while(!getSafeMaterials().contains(loc.getBlock().getType())) {
			loc = loc.add(0, 1, 0);
		}
		
		return loc;
	}
	
	/**
	 * Used to get the remaining two corners between two points.
	 * @param Location l1 (The first corner)
	 * @param Location l2 (The second corner)
	 * @return List<Location> The four corners between two points.
	 */
	
	public static List<Location> getCorners(final Location l1, final Location l2) {
		final ArrayList<Location> corners = new ArrayList<Location>();
		corners.add(getSafeLocation(l1));
		corners.add(getSafeLocation(l2));
		
		Location corner3 = new Location(l1.getWorld(), l1.getBlockX(), l1.getBlockY(), l2.getBlockZ());
		corners.add(getSafeLocation(corner3));
		
		Location corner4 = new Location(l1.getWorld(), l2.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		corners.add(getSafeLocation(corner4));
		
		return corners;
	}
	
	/**
	 * The materials that are safe to destroy with a barrier.
	 * @return List<Material>
	 */
	
	public static List<Material> getSafeMaterials() {
		return Arrays.asList(
				Material.LONG_GRASS,
				Material.LEAVES,
				Material.AIR,
				Material.YELLOW_FLOWER,
				Material.RED_ROSE,
				Material.WATER_LILY,
				Material.VINE,
				Material.RED_MUSHROOM,
				Material.BROWN_MUSHROOM,
				Material.SUGAR_CANE_BLOCK,
				Material.POTATO,
				Material.CARROT,
				Material.CROPS,
				Material.PUMPKIN_STEM,
			Material.MELON_STEM);
	}
	
	@SuppressWarnings("deprecation")
	public static List<Block> getOutlineBlocksOfType(final Location l1, final Location l2, final BarrierType type) {
		final ArrayList<Block> blocks = new ArrayList<Block>();
		
		int minX = Math.min(l1.getBlockX(), l2.getBlockX());
		int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
		int minY = Math.min(l1.getBlockY(), l2.getBlockY());
		int maxY = Math.min(l1.getBlockY(), l2.getBlockY());
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y < maxY + 12; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					final Block block = l1.getWorld().getBlockAt(x, y, z);
					
					if(x == minX || x == maxX || z == minZ || z == maxZ) {
						if(block.getType() == Material.STAINED_CLAY && block.getData() == type.getColor().getData()) {
							blocks.add(block);
						}
					}
				}
			}
		}
		
		return blocks;
	}
	
	/**
	 * Returns an outline of blocks between two points.
	 * @param Location l1 (The first point/corner)
	 * @param Location l2 (The second point/corner)
	 * @param BarrierType getType (Returns actual outline parts if not null)
	 * @return List<Block> outline blocks
	 */
	
	public static List<Block> getOutlineBlocks(final Location l1, final Location l2) {
		final ArrayList<Block> blocks = new ArrayList<Block>();
		
		int minX = Math.min(l1.getBlockX(), l2.getBlockX());
		int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
		int useY = Math.min(l1.getBlockY(), l2.getBlockY());
		
		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				final Block block = l1.getWorld().getBlockAt(x, useY, z);
				
				if(x == minX || x == maxX || z == minZ || z == maxZ) {
					int y = block.getY();
					
					while(!getSafeMaterials().contains(l1.getWorld().getBlockAt(x, y, z).getType()) && 
							!(y > (Math.max(l1.getBlockY(), l2.getBlockY()) + 3))) {
						y += 1;
					}
					
					// We don't want blocks being higher than 3 blocks from the maximum.
					if(y > (Math.max(l1.getBlockY(), l2.getBlockY()) + 3)) continue;
					blocks.add(l1.getWorld().getBlockAt(x, y, z));
					
					for(int i = 1; i < 3; i++) {
						blocks.add(l1.getWorld().getBlockAt(x, y + i, z));
					}
				}
			}
		}
		
		return blocks;
	}
}
