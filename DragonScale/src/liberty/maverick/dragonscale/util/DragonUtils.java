package liberty.maverick.dragonscale.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleLogger;

public class DragonUtils {
	
	private static DragonScaleLogger logger = null;
	private static WorldGuardPlugin worldGuard = null;
	
	/**
	 * Converts a number to Roman numerals. Ex: 8 -> VIII
	 * @param The number to convert to Roman numerals.
	 * @return
	 */
	
	public static String toRomanNumerals(int number) {
		return String.valueOf(new char[number]).replace('\0', 'I')
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
	}
	
	/**
	 * Fetches what we should display in the lore of a pickaxe to represent an attribute.
	 * @param The code name of the attribute.
	 * @param The amplifier/level of the attribute.
	 * @return A neat title string to represent the attribute.
	 */
	
	public static String getPickaxeAttributeTitle(String attributeName, int amplifier) {
		// We need to title-ize the attribute name. Ex: converting SPEED to Speed.
		attributeName = fromCodeNameToServerName(attributeName);
		return String.format("%s %s", attributeName, toRomanNumerals(amplifier));
	}
	
	/**
	 * Converts a code name string containing underscores to a nice string
	 * we can display. Example: night_vision -> Night Vision
	 * @param The code name
	 * @return
	 */
	
	public static String fromCodeNameToServerName(String codeName) {
		codeName = codeName.replaceAll("\\_", " ");
		String[] strings = codeName.split("\\s+");
		String newString = "";
		
		for(int i = 0; i < strings.length; i++) {
			String s = strings[i];
			s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
			
			if(i > 0) {
				// We need to concat a space to represent the underscores in the original code name string.
				newString = newString.concat(" ");
			}
			
			newString = newString.concat(s);
		}
		
		return newString;
	}
	
	/**
	 * This sets up this utility class by giving us a handy logger
	 * and our WorldGuard instance.
	 * @param {@link DragonScale} The plugin instance.
	 * @param {@link WorldGuardPlugin} The WorldGuard plugin instance if one is found.
	 */
	
	public static void setup(final DragonScale mainInstance, 
			final WorldGuardPlugin wgInstance) {
		DragonUtils.logger = new DragonScaleLogger(mainInstance, "DragonUtils");
		DragonUtils.worldGuard = wgInstance;
		
		logger.debug("Setup utility class!");
	}
	
	/**
	 * Tries to give the specified items to the specified player. If the player has
	 * a full inventory, the remaining items will be dropped where the player is located.
	 * {@link #givePlayerItems(Player, List, Location)}
	 * @param {@link Player} The player to give items to.
	 * @param {@link ItemStack...} The items to give to the player.
	 * @return A HashSet of {@link Item} instances that couldn't be placed in the player's inventory.
	 */
	
	public static HashSet<Item> givePlayerItems(final Player player, final ItemStack... items) {
		return givePlayerItems(player, player.getLocation(), items);
	}
	
	/**
	 * Tries to give the specified items to the specified player. If the
	 * player has a full inventory, the remaining items will be dropped on the ground
	 * at the specified drop location.
	 * @param {@link Player} The player we're giving the items to.
	 * @param {@link Location} The drop location for items that couldn't be placed in the player's inventory.
	 * @param {@link List<ItemStack>} The list of items to give the player.
	 * @return A HashSet of {@link Item} instances that couldn't be placed in the player's inventory.
	 */
	
	public static HashSet<Item> givePlayerItems(final Player player, Location dropLocation, final ItemStack... drops) {
		final HashMap<Integer, ItemStack> remainder = player.getInventory().addItem(drops);
		
		// If a drop location wasn't specified, let's just use the player's location.
		if(dropLocation == null) {
			dropLocation = player.getLocation();
		}
		
		HashSet<Item> droppedItems = new HashSet<Item>();
		
		// Let's drop the left over items in the world.
		for(final ItemStack item : remainder.values()) {
			droppedItems.add(dropLocation.getWorld().dropItem(dropLocation, item));
		}
		
		DragonScale.singleton.getSettings().getSoundByName("ITEM_DROPPED").play(dropLocation);
		
		return droppedItems;
	}
	
	/**
	 * Converts a collection of {@link ItemStack} objects to an ItemStack array.
	 * @param items
	 * @return
	 */
	
	public static ItemStack[] itemstackCollectionToArray(final Collection<ItemStack> items) {
		final ItemStack[] array = new ItemStack[items.size()];
		final Iterator<ItemStack> it = items.iterator();
		
		int i = 0;
		while(it.hasNext()) {
			array[i] = it.next();
			i++;
		}
		
		return array;
	}
	
	/**
	 * Checks to see if the specified player can edit blocks at the specified location.
	 * @param {@link Player} The player we're checking the permissions of.
	 * @param {@link Location} The location of the area the player is trying to build/edit at.
	 * @return
	 */
	
	public static boolean canBuildHere(final Player player, final Location location) {
		if(worldGuard != null) {
		    RegionManager regMgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
			ApplicableRegionSet regions = regMgr.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
			boolean canBuild = true;
			
			Iterator<ProtectedRegion> it = regions.iterator();
			
			while(it.hasNext()) {
			    ProtectedRegion r = it.next();
			    State f = r.getFlag(Flags.BLOCK_BREAK);
			    
			    if(f == State.DENY) {
			        canBuild = false;
			    }
			}
			
			return regions.size() == 0 || canBuild;
		}
		
		return true;
	}
	
	/**
	 * Checks whether or not the specified blocks have the same block data.
	 * @param b1 - The first block to compare
	 * @param b2 - The second block to compare.
	 * @return If the two blocks have identical types and data.
	 */
	
	public static boolean isSameType(final Block b1, final Block b2) {
		return b1.getBlockData().matches(b2.getBlockData());
	}
	
	/**
	 * Fetches the exp dropped by the specified block type.
	 * See {@link liberty.maverick.dragonscale.DragonScaleSettings#getOreExp(BlockData)} for more information.
	 * @param block - The block to check.
	 * @return int
	 */
	
	public static int getOreExp(final Block block) {
		return DragonScale.singleton.getSettings().getOreExp(block.getBlockData());
	}
	
	/**
	 * Checks whether or not the String input is an int.
	 * @param str - The string to check
	 * @return
	 */
	
	public static boolean isInt(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
}
