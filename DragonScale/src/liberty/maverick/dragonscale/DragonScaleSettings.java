package liberty.maverick.dragonscale;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import liberty.maverick.dragonscale.util.DragonUtils;

public class DragonScaleSettings {
	
	final DragonScale main;
	final DragonScaleLogger logger;
	final File configFile;
	YamlConfiguration config;
	
	private String pickaxeName;
	private List<String> pickaxeLore;
	private final Map<BlockData, Integer> oreExp;
	private final List<Integer> neededExp;
	private final Map<Integer, ConfigurationSection> upgradesData;
	
	// This is a map containing all the plugin's sounds. The key is
	// the config key of the sound. The value is the Minecraft sound
	// object.
	private final Map<String, DragonScaleSound> sounds;
	
	private String lootboxOpenMsg;
	private String broadcastLevelUpMsg;
	
	private int maxLevel;
	
	public DragonScaleSettings(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.logger = new DragonScaleLogger(main, "Settings");
		this.configFile = new File(main.getDataFolder() + "/config.yml");
		
		this.pickaxeName = null;
		this.pickaxeLore = null;
		this.oreExp = new HashMap<BlockData, Integer>();
		this.neededExp = new ArrayList<Integer>();
		this.upgradesData = new HashMap<Integer, ConfigurationSection>();
		this.sounds = new HashMap<String, DragonScaleSound>();
		this.lootboxOpenMsg = null;
		this.broadcastLevelUpMsg = null;
		this.maxLevel = -1;
		
		if(!configFile.exists()) {
			logger.info("Could not find a configuration file. Creating a new one...");
			main.saveDefaultConfig();
		}
		
		loadConfigAndSettings();
	}
	
	public void loadConfigAndSettings() {
		this.config = YamlConfiguration.loadConfiguration(configFile);
		
		if(config == null) {
			logger.error("Failed to load configuration file!!!");
			main.disable();
		}else {
			loadSettings();
		}
	}
	
	private void loadSettings() {
		this.pickaxeName = config.getString("DragonScaleName");
		this.pickaxeLore = config.getStringList("DragonScaleLore");
		
		// Let's load up the exp each ore gives.
		for(String line : config.getStringList("ExpOres")) {
			// This is the initial split, splitting at the space
			// in between the type and the experience amount.
			String[] split = line.split("\\s+");
			
			if(split.length != 2) {
				logger.error(String.format("Illegal entry in \"ExpOres\" found: %s", line));
				main.disable();
				return;
			}
			
			int exp = Integer.valueOf(split[1]);
			boolean usingID = DragonUtils.isInt(split[0]);
			
			if(!usingID && split[0].contains(":")) {
			    usingID = DragonUtils.isInt(split[0].split(":")[0]);
			}
			
			if(usingID) {
                logger.error(String.format("Found Minecraft id in \"ExpOres.\" Replace %s with name", split[0]));
                main.disable();
                return;
			}
			
            Material type = null;
            
            String matName = split[0];
            
            try {
                type = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                logger.error(String.format("Illegal Minecraft material in \"ExpOres\" found: %s", matName));
                main.disable();
                return;
            }
			
			oreExp.put(Bukkit.createBlockData(type), exp);
		}
		
		// Let's fetch the exp needed to get to the next level.
		final List<String> neededExpRaw = config.getStringList("NeededExp");
		for(int i = 0; i < neededExpRaw.size(); i++) {
			final String neededExpLine = neededExpRaw.get(i);
			neededExp.add(Integer.valueOf(neededExpLine));
			logger.debug(String.format("Level %d, NeededExp: %d", (i+1), Integer.valueOf(neededExpLine)));
		}
		
		// Let's fetch our upgrades sections.
		final ConfigurationSection upgradesSection = config.getConfigurationSection("Upgrades");
		for(String key : upgradesSection.getKeys(false)) {
			int level = Integer.valueOf(key);
			upgradesData.put(level, upgradesSection.getConfigurationSection(key));
		}
		
		// Let's load our sounds.
		final ConfigurationSection soundsSection = config.getConfigurationSection("Sounds");
		for(String key : soundsSection.getKeys(false)) {
			String rawValue = soundsSection.getString(key);
			String[] split1 = rawValue.split(":");
			float volume = 1.0F;
			float pitch = 1.0F;
			Sound sound = null;
			
			try {
				sound = Sound.valueOf(split1[0].toUpperCase());
			} catch (IllegalArgumentException | NullPointerException e) {
				logger.error(String.format("The specified Sound \"%s\"'s does not exist!", split1[0]));
				main.disable();
				break;
			}
			
			if(split1.length > 1) {
				// A volume value was specified.
				String[] split2 = split1[1].split(":");
				
				try {
					volume = Float.parseFloat(split2[0]);
				} catch (NumberFormatException e) {
					logger.error(String.format("An error occurred while formatting Sound \"%s\"'s data.", key));
					main.disable();
					break;
				}
				
				if(split2.length > 1) {
					// A pitch value was specified.
					try {
						pitch = Float.parseFloat(split2[1]);
					} catch (NumberFormatException e) {
						logger.error(String.format("An error occurred while formatting Sound \"%s\"'s data.", key));
						main.disable();
						break;
					}
				}
			}
			
			sounds.put(key, new DragonScaleSound(sound, volume, pitch));
		}
		
		this.broadcastLevelUpMsg = config.getString("LevelUpBroadcast");
		this.lootboxOpenMsg = config.getString("LootBoxOpen");
		
		final ConfigurationSection lootBoxDataSection = config.getConfigurationSection("LootBoxData");
		main.getLootBoxFactory().readConfigData(lootBoxDataSection);
		
		this.maxLevel = config.getInt("MaxLevel", -1);
		
		if(maxLevel <= 1) {
			logger.error("Maximum level must be greater than 1!");
			main.disable();
		}
	}
	
	/**
	 * Fetches the unformatted name of the pickaxe.
	 * @return Unformatted string
	 */
	
	public String getPickaxeName() {
		return this.pickaxeName;
	}
	
	/**
	 * Fetches the unformatted lore for the pickaxe.
	 * @return Unformatted String list
	 */
	
	public List<String> getPickaxeLore() {
		final List<String> lore = new ArrayList<String>();
		lore.addAll(pickaxeLore);
		return lore;
	}
	
	/**
	 * Fetches the experience earned from mining a certain ore.
	 * @param {@link BlockData} The data to check against.
	 * @return n >= 0
	 */
	
	public int getOreExp(BlockData data) {
		if(oreExp.containsKey(data)) {
			return oreExp.get(data);
		}else {
			return 0;
		}
	}
	
	/**
	 * Returns whether or not the specified {@link Block} is in the
	 * block table (meaning that an exp amount has been defined for mining
	 * that block)
	 * @param block - The block to check
	 * @return true/false flag
	 */
	
	public boolean isInBlockTable(Block block) {
		return oreExp.containsKey(block.getBlockData());
	}
	
	/**
	 * Fetches the needed experience to get to the next level.
	 * @param The level we're currently at.
	 * @return The needed experience or -1 if the needed experience
	 * couldn't be fetched.
	 */
	
	public int getNeededExp(int level) {
		final int index = (level - 1);
		
		if(0 <= index && index < neededExp.size()) {
			return neededExp.get(index);
		}
		
		return -1;
	}
	
	/**
	 * Calculates and fetches the {@link ConfigurationSection} that
	 * pertains to the specified level.
	 * @param level - The numeric level to fetch data for.
	 * @return
	 */
	
	public ConfigurationSection getLevelData(final int level) {
		if(upgradesData.containsKey(level)) {
			return upgradesData.get(level);
		}
		
		// If we can't find a specific set of data for the specified level,
		// we need to find the last available upgrade data.
		final List<Integer> upgradeLevels = new ArrayList<Integer>(upgradesData.keySet());
		Collections.sort(upgradeLevels);
		
		for(int i = upgradeLevels.size() - 1; i >= 0; i--) {
			int cfgLevel = (int) upgradeLevels.toArray()[i];
			
			if(cfgLevel <= level) {
				return upgradesData.get(cfgLevel);
			}
		}
		
		return null;
	}
	
	/**
	 * Fetches a {@link DragonScaleSound} by its code name.
	 * @param name - The code name of the sound needed.
	 * @return DragonScaleSound instance
	 * @throws IllegalArugmentException if the specified sound could not be found.
	 */
	
	public DragonScaleSound getSoundByName(final String name) {
		if(sounds.containsKey(name)) {
			return sounds.get(name);
		}
		
		throw new IllegalArgumentException(String.format("Could not find specified Sound \"%s\"!", name));
	}
	
	/**
	 * Fetches the message that is broadcasted when a player reaches
	 * milestone levels, levels divisible by 10 after level 5.
	 * @return String
	 */
	
	public String getLevelUpBroadcastMessage() {
		return this.broadcastLevelUpMsg;
	}
	
	/**
	 * Fetches the message that is sent to a player when they open a loot
	 * box!
	 * @return String
	 */
	
	public String getLootBoxOpenMessage() {
		return this.lootboxOpenMsg;
	}
	
	/**
	 * Fetches the maximum achievable level.
	 * @return int
	 */
	
	public int getMaxLevel() {
		return this.maxLevel;
	}
	
	/**
	 * Fetches the actual {@link YamlConfiguration} instance.
	 * @return YamlConfiguration
	 */
	
	public YamlConfiguration getConfig() {
		return this.config;
	}
	
}
