package org.xmakerx.raidpractice;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xmakerx.raidpractice.util.ConfigUtils;

public class Settings {
	
	final RaidPractice instance;
	final File configFile;
	final YamlConfiguration config;
	final ScoreboardBank sbBank;
	private Material shopItem;
	private Material exitItem;
	private Material teleportItem;
	private Sound respawnSound;
	private Sound winSound;
	private Sound loseSound;
	private Sound tickSound;
	private Sound countdownSound;
	private int winPoints;
	private int deathWinPoints;
	private int brokeEggPoints;
	private int prepareTime;
	private int gameFullPrepareTime;
	private boolean autoEquipArmor;
	private boolean useWorldSpawn;
	
	public Settings(final RaidPractice main) {
		this.instance = main;
		this.configFile = new File(main.getDataFolder() + "/config.yml");
		
		if(!configFile.exists()) {
			instance.saveDefaultConfig();
		}
		
		// Let's start loading our values.
		this.config = YamlConfiguration.loadConfiguration(configFile);
		this.sbBank = new ScoreboardBank(instance, config.getConfigurationSection("scoreboards"));
		this.sbBank.buildBoards();
		
		// Let's make sure our config is up to date.
		ConfigUtils.update(instance, "config.yml", config, configFile);
		
		this.shopItem = Material.getMaterial(config.getString("shopItem"));
		this.exitItem = Material.getMaterial(config.getString("exitItem"));
		this.teleportItem = Material.getMaterial(config.getString("teleportItem"));
		this.respawnSound = Sound.valueOf(config.getString("respawnSound"));
		this.winSound = Sound.valueOf(config.getString("winSound"));
		this.loseSound = Sound.valueOf(config.getString("loseSound"));
		this.tickSound = Sound.valueOf(config.getString("tickSound"));
		this.countdownSound = Sound.valueOf(config.getString("countdownSound"));
		this.winPoints = config.getInt("aliveWinPoints");
		this.deathWinPoints = config.getInt("diedWinPoints");
		this.brokeEggPoints = config.getInt("brokeEggPoints");
		this.prepareTime = config.getInt("prepareTime");
		this.gameFullPrepareTime = config.getInt("gameFullPrepareTime");
		this.autoEquipArmor = config.getBoolean("autoEquipArmor");
		this.useWorldSpawn = config.getBoolean("useWorldSpawn");
		
		if(shopItem == null) {
			instance.getLogger().severe(String.format("Invalid Material %s does not exist.", config.getString("respawnItem")));
			instance.disable();
		}else if(exitItem == null) {
			instance.getLogger().severe(String.format("Invalid Material %s does not exist.", config.getString("exitItem")));
			instance.disable();
		}else if(teleportItem == null) {
			instance.getLogger().severe(String.format("Invalid Material %s does not exist.", config.getString("teleportItem")));
			instance.disable();
		}
	}
	
	public ScoreboardBank getScoreboardBank() {
		return this.sbBank;
	}
	
	public Material getShopItem() {
		return this.shopItem;
	}
	
	public Material getExitItem() {
		return this.exitItem;
	}
	
	public Material getTeleportItem() {
		return this.teleportItem;
	}
	
	public Sound getRespawnSound() {
		return this.respawnSound;
	}
	
	public Sound getWinSound() {
		return this.winSound;
	}
	
	public Sound getLoseSound() {
		return this.loseSound;
	}
	
	public Sound getTickSound() {
		return this.tickSound;
	}
	
	public Sound getCountdownSound() {
		return this.countdownSound;
	}
	
	public int getAliveWinPoints() {
		return this.winPoints;
	}
	
	public int getDeathWinPoints() {
		return this.deathWinPoints;
	}
	
	public int getEggBrokenPoints() {
		return this.brokeEggPoints;
	}
	
	public int getPrepareTime() {
		return this.prepareTime;
	}
	
	public int getGameFullPrepareTime() {
		return this.gameFullPrepareTime;
	}
	
	public boolean doesAutoEquipArmor() {
		return this.autoEquipArmor;
	}
	
	public boolean doesUseWorldSpawn() {
		return this.useWorldSpawn;
	}
}
