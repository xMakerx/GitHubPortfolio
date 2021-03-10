package mc.decodedlogic.gucciislandtop;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import mc.decodedlogic.gucciislandtop.IslandTopLogger.LogLevel;
import mc.decodedlogic.gucciislandtop.menu.ElementData;
import mc.decodedlogic.gucciislandtop.valuable.ValuableManager;

public class Settings {
	
	final IslandTop main;
	final File file;
	final YamlConfiguration config;
	final IslandTopLogger logger;
	
	private String top16MenuName;
	private String islandMenuName;
	private String islandMemberEntry;
	private String calculating;
	private String valuable;
	private String quantity;
	private String attribute;
	private String totalWorth;
	private String worth;
	private String defaultTxt;
	private String playerNotFound;
	private List<String> calculatingBroadcast;
	private List<String> calculatingDoneBroadcast;
	private List<Integer> topIslandSlots;
	private boolean fillEmptySlotsWithGlass;
	private long calculateEvery;
	private int displayTopIslands;
	
	public Settings(final IslandTop mainInstance) {
		this.main = mainInstance;
		this.file = new File(main.getDataFolder() + "/config.yml");
		this.logger = new IslandTopLogger("Settings");
		this.main.saveDefaultConfig();
		
		this.top16MenuName = null;
		this.islandMenuName = null;
		this.islandMemberEntry = null;
		this.calculating = null;
		this.valuable = null;
		this.quantity = null;
		this.attribute = null;
		this.totalWorth = null;
		this.topIslandSlots = null;
		this.worth = null;
		this.defaultTxt = null;
		this.calculatingBroadcast = null;
		this.calculatingDoneBroadcast = null;
		this.displayTopIslands = 16;
		this.fillEmptySlotsWithGlass = true;
		this.calculateEvery = 1800;
		this.playerNotFound = null;
		
		this.config = YamlConfiguration.loadConfiguration(this.file);
		this.load();
	}
	
	public void load() {
		final String llName = this.config.getString("NotifyLevel");
		final LogLevel userLogLevel = IslandTopLogger.LogLevel.getByName(llName);
		if(userLogLevel != null) {
			main.setLogLevel(userLogLevel);
		}else {
			throw new IllegalArgumentException(String.format("Invalid log level type: \"%s\"!", llName));
		}
		
		final List<Integer> defaultList = Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25);
		topIslandSlots = config.getIntegerList("TopIslandSlots");
		if(topIslandSlots == null) {
			topIslandSlots = defaultList;
		}
		
		for(int slot : topIslandSlots) {
			if(slot < 0 || slot > 44) {
				logger.error(String.format("You must specify a slot between 0 and 44 (inclusive). "
						+ "Instead received: %d.", slot));
				main.disable();
				return;
			}
		}
		
		displayTopIslands = config.getInt("DisplayTopIslands", 16);
		fillEmptySlotsWithGlass = config.getBoolean("FillEmptySlots", true);
		calculateEvery = config.getLong("CalculateTopIslandsEvery", 1800);
		
		if(calculateEvery < 900) {
			logger.error("You must specify a calculation time (in seconds) of at least 15 minutes (900 seconds).");
			main.disable();
			return;
		}
		
		if(displayTopIslands > 44 || displayTopIslands < 1) {
			logger.error("You must specify a number of top islands to display between 1 and 44 (inclusive).");
			main.disable();
			return;
		}
		
		final ConfigurationSection strings = this.config.getConfigurationSection("Strings");
		this.top16MenuName = strings.getString("TopSixteenMenuName");
		this.islandMenuName = strings.getString("IslandMenuName");
		this.islandMemberEntry = strings.getString("IslandMemberEntry");
		this.calculating = strings.getString("Calculating");
		this.valuable = strings.getString("Valuable");
		this.quantity = strings.getString("Quantity");
		this.attribute = strings.getString("Attribute");
		this.totalWorth = strings.getString("TotalWorth");
		this.worth = strings.getString("Worth");
		this.playerNotFound = strings.getString("PlayerNotFound");
		this.defaultTxt = strings.getString("Default");
		this.calculatingBroadcast = strings.getStringList("CalculatingBroadcast");
		this.calculatingDoneBroadcast = strings.getStringList("CalculatingDoneBroadcast");
		
		for(ElementData elementData : ElementData.values()) {
			final ConfigurationSection section = this.config.getConfigurationSection(elementData.getConfigName()); 
			elementData.loadDataFrom(section);
		}
		
		final ConfigurationSection valuables = this.config.getConfigurationSection("Valuables");
		
		for(String key : valuables.getKeys(false)) {
			if(valuables.isConfigurationSection(key)) {
				final ConfigurationSection valuableSection = valuables.getConfigurationSection(key);
				ValuableManager.processValuableSection(valuableSection);
			}
		}
	}
	
	public String getTopSixteenMenuName() {
		return this.top16MenuName;
	}
	
	public String getIslandMenuName() {
		return this.islandMenuName;
	}
	
	public String getIslandMemberEntry() {
		return this.islandMemberEntry;
	}
	
	public String getCalculating() {
		return this.calculating;
	}
	
	public String getValuable() {
		return this.valuable;
	}
	
	public String getQuantity() {
		return this.quantity;
	}
	
	public String getAttribute() {
		return this.attribute;
	}
	
	public String getTotalWorth() {
		return this.totalWorth;
	}
	
	public String getWorth() {
		return this.worth;
	}
	
	public String getPlayerNotFound() {
	    return this.playerNotFound;
	}
	
	public String getDefault() {
		return this.defaultTxt;
	}
	
	public List<String> getCalculatingBroadcast() {
		return this.calculatingBroadcast;
	}
	
	public List<String> getCalculatingDoneBroadcast() {
		return this.calculatingDoneBroadcast;
	}
	
	public List<Integer> getTopIslandSlots() {
		return this.topIslandSlots;
	}
	
	public int getDisplayTopIslands() {
		return this.displayTopIslands;
	}
	
	public boolean shouldFillEmptySlots() {
		return this.fillEmptySlotsWithGlass;
	}
	
	public long getCalculationCooldown() {
		return this.calculateEvery;
	}

}
