package mc.decodedlogic.skybattlesuspawners;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mc.decodedlogic.skybattlesuspawners.USpawnersLogger.LogLevel;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton.Data;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;

public class Settings {
	
	final USpawners main;
	final File file;
	final YamlConfiguration config;
	final USpawnersLogger LOGGER;
	
	private String spawnerName;
	private String entityDisplayName;
	private String addRemovePageName;
	private String upgradePageName;
	private String spawnerItemName;
	private List<String> spawnerItemDesc;
	private String upgradeMade;
	private String depositMade;
	private String withdrawMade;
	private String noPermission;
	private String gaveSpawners;
	private String playerNotFound;
	private String unknownData;
	private String withdrawCorrectly;
	private String noMoney;
	private String downgrade;
	private String noneToDeposit;
	private String clearedEntities;
	private String spawnerReset;
	private String clrCmdFdbk;
	private String inspOn;
	private String inspOff;
	private String noData;
	private String noNearData;
	private String radError;
	private String spawnerMerged;
	private List<String> resetConfirmation;
	private List<String> alreadyPurchased;
	private boolean useStackedHealth;
	private boolean passiveMobs;
	private boolean defaultCmdHelp;
	private boolean clearLegacyEntities;
	private int spawnerStackLimit;
	private int entityStackLimit;
	private int spawnerMergeRadius;
	private double stackRadius;
	private double firstSpawnDelaySecs;
    private List<DamageCause> killOptions;
	
	public Settings(final USpawners mainInstance) {
		this.main = mainInstance;
		this.file = new File(main.getDataFolder() + "/config.yml");
		this.main.saveDefaultConfig();
		
		this.LOGGER = new USpawnersLogger("Settings");
		
		this.config = YamlConfiguration.loadConfiguration(this.file);
		
		this.spawnerName = "";
		this.entityDisplayName = "";
		this.addRemovePageName = "";
		this.upgradePageName = "";
		this.spawnerItemName = "";
		this.spawnerItemDesc = null;
		this.upgradeMade = "";
		this.depositMade = "";
		this.withdrawMade = "";
		this.noPermission = "";
		this.gaveSpawners = "";
		this.playerNotFound = "";
		this.unknownData = "";
		this.withdrawCorrectly = "";
		this.noMoney = "";
		this.downgrade = "";
		this.noneToDeposit = "";
		this.clearedEntities = "";
		this.spawnerReset = "";
		this.clrCmdFdbk = "";
		this.inspOn = "";
		this.inspOff = "";
		this.noNearData = "";
		this.radError = "";
		this.spawnerMerged = "";
		this.resetConfirmation = null;
		this.useStackedHealth = true;
		this.passiveMobs = true;
		this.defaultCmdHelp = true;
		this.clearLegacyEntities = true;
		this.spawnerStackLimit = 999;
		this.entityStackLimit = 500;
		this.stackRadius = 5.0;
		this.spawnerMergeRadius = 40;
		this.firstSpawnDelaySecs = 20.0;
        this.killOptions = new ArrayList<DamageCause>();
        this.alreadyPurchased = null;
		this.load();
	}
	
	public void load() {
		final String llName = this.config.getString("NotifyLevel");
		final LogLevel userLogLevel = USpawnersLogger.LogLevel.getByName(llName);
		if(userLogLevel != null) {
			main.setLogLevel(userLogLevel);
		}else {
			throw new IllegalArgumentException(String.format("Invalid log level type: \"%s\"!", llName));
		}
		
		this.useStackedHealth = this.config.getBoolean("UseStackedHealth", true);
		this.passiveMobs = this.config.getBoolean("SetSpawnerMobsPassive", true);
		this.defaultCmdHelp = this.config.getBoolean("ShowDefaultCmdHelp", true);
		this.clearLegacyEntities = this.config.getBoolean("ClearLegacyEntities", true);
		this.stackRadius = this.config.getDouble("StackRadius", 5.0);
        this.firstSpawnDelaySecs = this.config.getDouble("FirstSpawnDelaySeconds", 20.0);
		this.spawnerStackLimit = this.config.getInt("SpawnerStackLimit", 999);
		this.entityStackLimit = this.config.getInt("EntityStackLimit", 500);
		this.spawnerMergeRadius = this.config.getInt("SpawnerMergeRadius", 40);
		
		for(String causeStr : this.config.getStringList("KillOptions")) {
		    try {
		        killOptions.add(DamageCause.valueOf(causeStr));
		    } catch (IllegalArgumentException | NullPointerException e) {
		        LOGGER.error(String.format("Error parsing kill option \"%s\"! Invalid DamageCause! Message: %s.", causeStr, e.getMessage()));
		    }
		}
		
        final ConfigurationSection btnDataSection = this.config.getConfigurationSection("ButtonData");
        for(Data btnData : MenuButton.Data.values()) {
            final ConfigurationSection btnSection = btnDataSection.getConfigurationSection(btnData.getConfigName());
            
            if(btnSection != null) {
                btnData.loadDataFrom(btnSection);
            }else {
                throw new NullPointerException(String.format("Error parsing button \"%s\"'s data. No section found!", btnData.name()));
            }
        }
		
		final ConfigurationSection spawnerData = this.config.getConfigurationSection("SpawnerData");
		for(String key : spawnerData.getKeys(false)) {
			try {
				final SpawnerType type = SpawnerType.getTypeFromEntityType(EntityType.valueOf(key));
				
				if(type != null) {
					type.loadData(spawnerData.getConfigurationSection(key));
				}
				
			} catch (IllegalArgumentException e) {}
		}
		
		String missing = "&cERRNO: Make sure your config is up to date! Could not find string!";
		this.spawnerName = this.config.getString("Strings.MobSpawnerName", missing);
		this.entityDisplayName = this.config.getString("Strings.EntityDisplayName", missing);
		this.addRemovePageName = this.config.getString("Strings.AddRemovePageName", missing);
		this.upgradePageName = this.config.getString("Strings.UpgradePageName", missing);
		this.upgradeMade = this.config.getString("Strings.UpgradeMade", missing);
		this.depositMade = this.config.getString("Strings.DepositMade", missing);
		this.withdrawMade = this.config.getString("Strings.WithdrawMade", missing);
		this.noPermission = this.config.getString("Strings.NoPermission", missing);
		this.gaveSpawners = this.config.getString("Strings.GaveSpawners", missing);
		this.playerNotFound = this.config.getString("Strings.PlayerNotFound", missing);
		this.unknownData = this.config.getString("Strings.UnknownData", missing);
		this.withdrawCorrectly = this.config.getString("Strings.WithdrawCorrectly", missing);
		this.noMoney = this.config.getString("Strings.NoMoney", missing);
		this.noneToDeposit = this.config.getString("Strings.NothingToDeposit", missing);
		this.downgrade = this.config.getString("Strings.Downgrade", missing);
		this.clearedEntities = this.config.getString("Strings.ClearedEntities", missing);
		this.spawnerReset = this.config.getString("Strings.SpawnerReset", missing);
		this.clrCmdFdbk = this.config.getString("Strings.ClearCommandFeedback", missing);
		this.inspOn = this.config.getString("Strings.InspectorEnabled", missing);
        this.inspOff = this.config.getString("Strings.InspectorDisabled", missing);
        this.noData = this.config.getString("Strings.NoBlockData", missing);
        this.noNearData = this.config.getString("Strings.NoNearData", missing);
        this.radError = this.config.getString("Strings.RadiusError", missing);
        this.spawnerMerged = this.config.getString("Strings.SpawnerMerged", missing);
        this.alreadyPurchased = this.config.getStringList("Strings.AlreadyPurchased");
        
        List<String> missingList = Arrays.asList(missing);
        
		this.resetConfirmation = this.config.getStringList("ButtonData.Reset.Confirmation");
		this.spawnerItemName = this.config.getString("SpawnerItemData.Name", missing);
		this.spawnerItemDesc = this.config.getStringList("SpawnerItemData.Description");
		
		if(resetConfirmation == null) {
		    resetConfirmation = missingList;
		}
		
		if(spawnerItemDesc == null) {
		    spawnerItemDesc = missingList;
		}
		
		if(alreadyPurchased == null) {
		    alreadyPurchased = missingList;
		}
	}
	
	public String getSpawnerName() {
		return this.spawnerName;
	}
	
	public String getEntityDisplayName() {
		return this.entityDisplayName;
	}
	
	public String correctLongNames(String name) {
		if(name.length() > 32) {
			name = name.substring(0, 28);
			name = name.concat("...");
			return name;
		}
		
		return name;
	}
	
	public String getAddRemovePageName() {
		return this.addRemovePageName;
	}
	
	public String getUpgradePageName() {
		return this.upgradePageName;
	}
	
	public String getSpawnerItemName() {
		return this.spawnerItemName;
	}
	
	public List<String> getSpawnerItemDescription() {
		return this.spawnerItemDesc;
	}
	
	public boolean doesUseStackedHealth() {
		return this.useStackedHealth;
	}
	
	public boolean areSpawnerMobsPassive() {
		return this.passiveMobs;
	}
	
	public boolean clearLegacyEntities() {
	    return this.clearLegacyEntities;
	}
	
	public String getUpgradeMade() {
		return this.upgradeMade;
	}
	
	public String getDepositMade() {
		return this.depositMade;
	}
	
	public String getWithdrawMade() {
		return this.withdrawMade;
	}
	
	public String getNoPermission() {
		return this.noPermission;
	}
	
	public String getGaveSpawners() {
		return this.gaveSpawners;
	}
	
	public String getPlayerNotFound() {
		return this.playerNotFound;
	}
	
	public String getUnknownData() {
		return this.unknownData;
	}
	
	public String getWithdrawCorrectly() {
		return this.withdrawCorrectly;
	}
	
	public String getNoMoney() {
		return this.noMoney;
	}
	
	public String getNoneToDeposit() {
		return this.noneToDeposit;
	}
	
	public String getDowngrade() {
		return this.downgrade;
	}
	
	public String getClearedEntities() {
	    return this.clearedEntities;
	}
	
	public String getSpawnerReset() {
	    return this.spawnerReset;
	}
	
	public String getClearCommandFeedback() {
	    return this.clrCmdFdbk;
	}
	
	public String getInspectorEnabled() {
	    return this.inspOn;
	}
	
	public String getInspectorDisabled() {
	    return this.inspOff;
	}
	
	public String getNoBlockData() {
	    return this.noData;
	}
	
	public String getNoNearData() {
	    return this.noNearData;
	}
	
	public String getRadiusError() {
	    return this.radError;
	}
	
	public String getSpawnerMerged() {
	    return this.spawnerMerged;
	}
	
	public List<String> getResetConfirmation() {
	    return new ArrayList<String>(this.resetConfirmation);
	}
	
	public List<String> getAlreadyPurchased() {
	    return new ArrayList<String>(this.alreadyPurchased);
	}
	
	public int getSpawnerStackLimit() {
		return this.spawnerStackLimit;
	}
	
	public int getEntityStackLimit() {
		return this.entityStackLimit;
	}
	
	public double getStackRadius() {
		return this.stackRadius;
	}
	
	public double getFirstSpawnDelaySeconds() {
	    return this.firstSpawnDelaySecs;
	}
	
	public int getSpawnerMergeRadius() {
	    return this.spawnerMergeRadius;
	}
	
	public List<DamageCause> getKillOptions() {
	    return this.killOptions;
	}
	
	public boolean showDefaultCommandHelp() {
	    return this.defaultCmdHelp;
	}

}
