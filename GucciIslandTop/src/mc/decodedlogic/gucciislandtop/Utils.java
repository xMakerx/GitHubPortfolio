package mc.decodedlogic.gucciislandtop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.valuable.Valuable;
import mc.decodedlogic.gucciislandtop.valuable.ValuableProcessor;
import net.md_5.bungee.api.ChatColor;

public class Utils {
    
    public static final int CALCULATE_PER_CHUNK = 5;
	
	static Map<UUID, Double> topIslands;
	static Map<UUID, Collection<Valuable>> islandValuables;
	static Map<UUID, BukkitTask> activeTasks;
	static Map<UUID, Integer> islandLevels;
	
	static int islandsLeftToCalculate;
	static int exitIndex;
	static int calculatedThisChunk;
	static int chunkSize;
	static boolean reachedEnd;
	static double totalWorth;
	static BukkitTask calculateTask;
	
	static {
		topIslands = new ConcurrentHashMap<UUID, Double>();
		islandValuables = new ConcurrentHashMap<UUID, Collection<Valuable>>();
		activeTasks = new ConcurrentHashMap<UUID, BukkitTask>();
		islandLevels = new ConcurrentHashMap<UUID, Integer>();
		islandsLeftToCalculate = 0;
	}
	
	public static void addTopIslandEntry(UUID ownerUUID, double worth, Collection<Valuable> collection, int level, boolean lateCalculation) {
		islandValuables.put(ownerUUID, collection);
		activeTasks.remove(ownerUUID);
		topIslands.put(ownerUUID, worth);
		islandLevels.put(ownerUUID, level);
		islandsLeftToCalculate -= 1;
		
		totalWorth += worth;
		calculatedThisChunk++;

		if(!lateCalculation && islandsLeftToCalculate == 0 && reachedEnd) {
		    sendCalculationBroadcast(true, topIslands.keySet().size(), totalWorth);
		}
	}
	
	public static void removeTopIslandEntry(UUID ownerUUID) {
		topIslands.remove(ownerUUID);
	}
	
	public static Map<UUID, Double> getLimitedTopIslands() {
		final int limit = IslandTop.get().getSettings().getDisplayTopIslands();
		return topIslands.entrySet().stream()
		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
		.collect(Collectors.toMap(Map.Entry<UUID, Double>::getKey, 
				Map.Entry<UUID, Double>::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	public static void sendCalculationBroadcast(boolean isDoneMsg, int numIslands, double totalWorth) {
		final Settings settings = IslandTop.get().getSettings();
		List<String> lines = new ArrayList<String>();
		lines.addAll((isDoneMsg) ? settings.getCalculatingDoneBroadcast() : settings.getCalculatingBroadcast());
		
		new BukkitRunnable() {
			
			public void run() {
				for(String line : lines) {
					line = replaceVariableWith(line, "amount", numIslands);
					line = replaceVariableWith(line, "total_worth", totalWorth);
					line = color(line);
					
					IslandTop.get().getServer().broadcastMessage(line);
				}
			}
			
		}.runTask(IslandTop.get());
	}
	
	public static void calculateChunk() {
        final IslandTop main = IslandTop.get();
        final ASkyBlockAPI api = ASkyBlockAPI.getInstance();
        
        final List<UUID> owners = new ArrayList<UUID>();
        owners.addAll(api.getOwnedIslands().keySet());
        
        final List<Island> islands = new ArrayList<Island>();
        islands.addAll(api.getOwnedIslands().values());
        
        calculatedThisChunk = 0;
        chunkSize = 0;
        if(calculateTask != null) calculateTask.cancel();
	    
	    // The following code is derived from the code present in ASkyBlock to create the
        // top ten map.
        calculateTask = new BukkitRunnable() {
            
            public void run() {
                
                for(int i = exitIndex; i < (exitIndex + CALCULATE_PER_CHUNK); i++) {
                    
                    if(i >= api.getIslandCount()) {
                        reachedEnd = true;
                        if(islandsLeftToCalculate == 0) sendCalculationBroadcast(true, topIslands.keySet().size(), totalWorth);
                        break;
                    }
                    
                    final UUID playerUUID = owners.get(i);
                    final Island island = islands.get(i);
                    
                    if(island != null) {
                        @SuppressWarnings("deprecation")
                        BukkitTask islandTask = main.getServer().getScheduler().runTask(main, 
                                new ValuableProcessor(island, api.getIslandLevel(playerUUID), false));
                        activeTasks.put(playerUUID, islandTask);
                        chunkSize++;
                        islandsLeftToCalculate++;
                    }
                }
                
                exitIndex += CALCULATE_PER_CHUNK;
                
                if(!reachedEnd) {
                    new BukkitRunnable() {
                        
                        public void run() {
                            calculateChunk();
                        }
                        
                    }.runTaskLater(main, 10L + (10L * chunkSize));
                }
            }
        }.runTaskAsynchronously(main);
	}
	
	public static void createTopIslands() {
		final ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		
		// Let's reset our maps to prepare for this.
		endAllCalculationTasks();
		topIslands.clear();
		islandValuables.clear();
		islandLevels.clear();
		islandsLeftToCalculate = 0;
		totalWorth = 0.0;
		exitIndex = 0;
		reachedEnd = false;
		
		// Let's send out that we're about to calculate.
		sendCalculationBroadcast(false, api.getIslandCount(), 0.0);
		calculateChunk();
	}
	
	public static void calculateIslandWorth(Island island) {
		if(island == null) return;
		
		final IslandTop main = IslandTop.get();
		
		new BukkitRunnable() {
			
			@SuppressWarnings("deprecation")
			public void run() {
				
				islandsLeftToCalculate += 1;
		    	BukkitTask islandTask = main.getServer().getScheduler().runTask(main, new ValuableProcessor(island, 0, true));
		    	activeTasks.put(island.getOwner(), islandTask);
			}
			
		}.runTaskAsynchronously(main);
	}
	
	public static Map<UUID, Double> getTopIslands() {
		return topIslands;
	}
	
	public static void endAllCalculationTasks() {
		for(BukkitTask task : activeTasks.values()) {
			task.cancel();
		}
		
		activeTasks.clear();
		
		try {
			calculateTask.cancel();
		} catch (NullPointerException e) {}
	}
	
	public static int getIslandLevel(UUID ownerUUID) {
		if(islandLevels.containsKey(ownerUUID)) {
			return islandLevels.get(ownerUUID);
		}
		
		return 0;
	}
	
	public static int getIslandsLeftToCalculate() {
		return islandsLeftToCalculate;
	}
	
	public static Collection<Valuable> getIslandValuables(UUID ownerUUID) {
		return islandValuables.get(ownerUUID);
	}
	
	public static Map<UUID, Integer> getIslandLevels() {
		return islandLevels;
	}
	
	/**
	 * Limits the specified string's length to the specified length.
	 * Appends ... to the end of the string to indicate that its been truncated. 
	 * @param message - The message to truncate. (No effect is done if the string satisfies the limitation).
	 * @param length - The maximum length allowed.
	 * @return Truncated string or passed string if it satisfied length limitation.
	 */
	
	public static String limitLengthTo(String message, int length) {
		if(message.length() > length) {
			message = message.substring(0, (length - 1) - 3).concat("...");
		}
		
		return message;
	}
	
	public static String prettyFormatDouble(final double number) {
	    double numMillions = (number / 1000000.0);

	    String formattedNum = null;
	    if(numMillions >= 1000000.0) {
	    	formattedNum = String.format("%,.2fT", (number / 1000000000000.0));
	    }else if(numMillions >= 1000.0) {
	    	formattedNum = String.format("%,.2fB", (number / 1000000000.0));
	    }else if(numMillions >= 1.0) {
	    	formattedNum = String.format("%,.2fM", (number / 1000000.0));
	    }else if(numMillions >= 0.001) {
	    	formattedNum = String.format("%,.2fK", (number / 1000.0));
	    }else {
	    	formattedNum = String.format("%,.2f", number);
	    }
	    
	    return formattedNum;
	}
	
	public static String replaceVariableWith(final String baseString, final String variableName, 
			Object replacement) {
		
		if(replacement == null) {
			replacement = "";
		}
		
		String strReplacement = String.valueOf(replacement);
		
		try {
			Integer.parseInt(strReplacement);
		} catch (NumberFormatException e) {
			try {
				// If we're replacing a variable with a double, let's pretty format it.
				double amount = Double.parseDouble(strReplacement);
				strReplacement = Utils.prettyFormatDouble(amount);
			} catch (NumberFormatException e2) {}
		}
		
		final String variable = "%" + variableName + "%";
		int varIndex = baseString.indexOf(variable);
		String result = baseString;
		
		if(strReplacement.isEmpty() && varIndex != -1) {
			final List<String> startBlockChars = Arrays.asList("(", "{", "[");
			final List<String> endBlockChars = Arrays.asList(")", "}", "]");
			int oParenthesesIndex = findIndexOf(result, startBlockChars, varIndex-1, endBlockChars);
			int endParenthesesIndex = findIndexOf(result, endBlockChars, result.length() - 1, startBlockChars);
			
			String afterVariableStr = (endParenthesesIndex != -1) ? result.substring(endParenthesesIndex) : result.substring(varIndex);
			if(afterVariableStr.replaceAll("\\s","").isEmpty()) {
				int endIndex = oParenthesesIndex;
				
				if(result.substring(endIndex-1, endIndex).equals(" ")) {
					endIndex = endIndex - 1;
				}
				
				result = result.substring(0, endIndex);
				return result;
			}else if(oParenthesesIndex != -1 && endParenthesesIndex != -1) {
				result = result.substring(0, oParenthesesIndex) + result.substring(endParenthesesIndex+1);
				return result;
			}
		}
		
		return result.replaceAll("\\%" + variableName + "\\%", strReplacement);
	}
	
	private static int findIndexOf(String baseString, List<String> characters, 
			int startIndex, List<String> breakChars) {
		int index = startIndex;
		do {
			final String cChar = baseString.substring(index, index+1);
			if(characters.contains(cChar)) {
				return index;
			}else if(breakChars.contains(cChar) || cChar.equals("%")) {
				break;
			}
			
			index--;
		} while(index >= 0);
		
		return -1;
	}
	
	public static String color(String baseString) {
		return ChatColor.translateAlternateColorCodes('&', baseString);
	}
	
	@SuppressWarnings("deprecation")
	public static MaterialData buildMaterialData(final String materialName, byte data) {
		Material material = null;
		
		try {
			material = Material.valueOf(materialName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Material type \"%s\" could not be found! Double check that it's valid!", materialName));
		}
		
		return new MaterialData(material, data);
	}
	
	public static MaterialData processRawMaterialData(String matData, String dataName) {
		String[] split = matData.split(":");
		String matName = null;
		byte data = 0;
		
		MaterialData result = null;
		
		if(split.length > 0 && split.length <= 2) {
			matName = split[0];
			
			if(split.length == 2) {
				try {
					data = Byte.valueOf(split[1]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
					+ "Incorrect data argument for icon. Expected byte, got \"%s\".", dataName, split[1]));
				}
			}
			
			result = buildMaterialData(matName, data);
		}else if(split.length > 2) {
			throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
			+ "Too many arguments! Expected 2 at most!", dataName));
		}
		
		return result;
	}
	
	/**
	 * Generates a player head with the specified player's skin.
	 * @param playerName - The name of the player of which to derive the skull's skin from.
	 * @return ItemStack of a head.
	 */
	
	public static ItemStack generatePlayerHead(final String playerName) {
		final ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		
		// Let's set the skin on the skull.
		SkullMeta skull = (SkullMeta) item.getItemMeta();
		skull.setOwner(playerName);
		
		item.setItemMeta(skull);
		
		return item;
	}

}
