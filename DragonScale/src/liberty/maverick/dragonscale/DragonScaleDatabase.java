package liberty.maverick.dragonscale;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;
import net.md_5.bungee.api.ChatColor;

/**
 * This is a flat-file database. Data is stored in /database
 */

public class DragonScaleDatabase {
	
	final DragonScale main;
	
	final DragonScaleLogger logger;
	
	final File databaseFolder;
	
	// This is the actual database.
	// The key is a UUID string and the value is a DragonScalePickaxe instance.
	private final HashMap<String, DragonScalePickaxe> database;
	
	public DragonScaleDatabase(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.logger = new DragonScaleLogger(main, "Database");
		this.database = new HashMap<String, DragonScalePickaxe>();
		this.databaseFolder = new File(main.getDataFolder() + "/database");
		
		if(!databaseFolder.exists()) {
			databaseFolder.mkdirs();
			
			logger.info(ChatColor.YELLOW + "Generated \"database\" folder for player data.");
		}
	}
	
	/**
	 * Saves the data of all players in the database and
	 * clears the HashMap to prevent memory leaks.
	 */
	
	public void saveAllDataAndClear() {
		for(String uuid : database.keySet()) {
			savePlayerData(uuid);
		}
		
		database.clear();
		logger.info("Saved all player data!");
	}
	
	/**
	 * Fetches the {@link YamlConfiguration} containing the data for the
	 * specified uuid.
	 * @param A string representing a uuid.
	 * @return A YamlConfiguration instance or null if no data has been stored for the
	 * specified player.
	 */
	
	public YamlConfiguration getPlayerData(String uuid) {
		final File dataFile = new File(databaseFolder + String.format("/%s.yml", uuid));
		
		if(dataFile.exists()) {
			return YamlConfiguration.loadConfiguration(dataFile);
		}
		
		return null;
	}
	
	public boolean loadPlayerData(String uuid) {
		final Player player = main.getServer().getPlayer(UUID.fromString(uuid));
		YamlConfiguration dataCfg = getPlayerData(uuid);
		
		final int maxLevel = main.getSettings().getMaxLevel();
		int level = dataCfg.getInt("level", 1);
		int exp = dataCfg.getInt("exp", 0);
		
		if(level > maxLevel) {
			level = maxLevel;
			exp = 0;
		}else if(level == maxLevel) {
			exp = 0;
		}
		
		final DragonScalePickaxe pickaxe = new DragonScalePickaxe(player, level, exp);
		database.put(uuid, pickaxe);

		return (dataCfg != null);
	}
	
	public boolean savePlayerData(String uuid) {
		final DragonScalePickaxe pickaxe = getPickaxeData(uuid);
		final File dataFile = new File(databaseFolder + String.format("/%s.yml", uuid));
		
		if(pickaxe != null) {
			return updateAndSaveData(getPlayerData(uuid), dataFile, uuid, pickaxe.getLevel(), pickaxe.getExp());
		}else {
			try {
				dataFile.createNewFile();
				return updateAndSaveData(getPlayerData(uuid), dataFile, uuid, 1, 0);
			} catch (IOException e) {
				logger.error(String.format("Failed to create new file to save data for UUID \"%s.\" Error: %s", uuid, e.getMessage()));
				return false;
			}
		}
	}
	
	private boolean updateAndSaveData(final YamlConfiguration dataCfg, final File dataFile, final String uuid, 
			final int level, final int exp) {
		final int maxLevel = main.getSettings().getMaxLevel();
		dataCfg.set("level", (level <= maxLevel) ? level : maxLevel);
		dataCfg.set("exp", (level < maxLevel) ? exp : 0);
		
		// Let's try to save the data file.
		try {
			dataCfg.save(dataFile);
			return true;
		} catch (IOException e) {
			logger.error(String.format("Failed to save data for UUID \"%s.\" Error: %s", uuid, e.getMessage()));
			return false;
		}
	}
	
	
	/**
	 * Fetches the {@link DragonScalePickaxe} instance associated with
	 * the specified UUID string.
	 * @param A string representing a UUID.
	 * @return
	 */
	
	public DragonScalePickaxe getPickaxeData(String uuid) {
		DragonScalePickaxe pickaxe = null;
		
		if(database.containsKey(uuid)) {
			// The specified player is online, let's just use that DragonScalePickaxe instance.
			pickaxe = database.get(uuid);
		}else {
			final YamlConfiguration config = getPlayerData(uuid);
			
			if(config != null) {
				pickaxe = new DragonScalePickaxe(null, 
						config.getInt("level", 1), config.getInt("exp", 0));
			}
		}
		
		return pickaxe;
	}
	
	/**
	 * Fetches the entire HashMap containing the currently loaded
	 * data.
	 * @return {@link HashMap<String, DragonScalePickaxe>}
	 */
	
	public HashMap<String, DragonScalePickaxe> getData() {
		return this.database;
	}

}
