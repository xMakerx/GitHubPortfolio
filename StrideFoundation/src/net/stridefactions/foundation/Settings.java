package net.stridefactions.foundation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;

public class Settings {
	
	final StrideFoundation instance;
	final YamlConfiguration config;
	final File configFile;
	
	private int hologramHeight, effectTime, resetTime;
	private List<String> hologramLines;
	private HashMap<Integer, List<String>> commands;
	
	ConfigurationSection msgs;
	
	public Settings() {
		this.instance = StrideFoundation.getInstance();
		this.configFile = new File(instance.getDataFolder() + "/config.yml");
		this.msgs = null;
		this.hologramLines = new ArrayList<String>();
		this.commands = new HashMap<Integer, List<String>>();
		
		// Generate the default config.
		if(!configFile.exists()) {
			instance.saveDefaultConfig();
			instance.getLogger().info(String.format("Successfully saved default configuration."));
		}
		
		this.config = YamlConfiguration.loadConfiguration(configFile);
		this.msgs = config.getConfigurationSection("messages");
	}
	
	public void sendMessage(final CommandSender sender, final String msg) {
		sender.sendMessage(color(msg));
	}
	
	public String getString(final String msgName) {
		String msg = msgs.getString(msgName);
		if(msg == null) msg = "MESSAGE NOT FOUND";
		return msg;
	}
	
	public String getMessage(final String msgName) {
		return color(getString(msgName));
	}
	
	public String color(final String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public void loadData() {
		// Let's load up the sounds.
		final ConfigurationSection soundSection = config.getConfigurationSection("sounds");
		for(final String soundKey : soundSection.getKeys(false)) {
			final StrideSound sound = StrideSound.getSoundByName(soundKey.toUpperCase());
			if(sound != null) {
				sound.setSound(soundSection.getString(soundKey));
			}
		}
		
		final ConfigurationSection itemsSection = config.getConfigurationSection("items");
		for(final String itemKey : itemsSection.getKeys(false)) {
			final StrideItem item = StrideItem.getItemByName(itemKey.toUpperCase());
			if(item != null) {
				final ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
				for(final String value : itemSection.getKeys(false)) {
					if(value.equalsIgnoreCase("lore")) {
						item.setLore(itemSection.getStringList(value));
					}else if(value.equalsIgnoreCase("durability")) {
						item.setDurability((short) itemSection.getInt(value));
					}else if(value.equalsIgnoreCase("displayName")) {
						item.setDisplayName(itemSection.getString(value));
					}else if(value.equalsIgnoreCase("enchanted")) {
						item.setEnchanted(itemSection.getBoolean(value));
					}else if(value.equalsIgnoreCase("amount")) {
						item.setAmount(itemSection.getInt(value));
					}else if(value.equalsIgnoreCase("material")) {
						item.setMaterial(itemSection.getString(value));
					}
				}
			}
		}
		
		final ConfigurationSection cmdsSection = config.getConfigurationSection("commands");
		for(final String progress : cmdsSection.getKeys(false)) {
			commands.put(Integer.valueOf(progress), cmdsSection.getStringList(progress));
		}
		
		this.hologramHeight = config.getInt("hologramHeight");
		this.effectTime = config.getInt("effectTime");
		this.resetTime = config.getInt("resetTime");
		
		final ConfigurationSection hologramSection = config.getConfigurationSection("hologram");
		this.hologramLines = hologramSection.getStringList("lore");
		this.hologramLines.add(0, hologramSection.getString("title"));
		MoneyPot.setMaxBalance(config.getDouble("moneyPotSize"));
	}
	
	public int getHologramHeight() {
		return this.hologramHeight;
	}
	
	public int getEffectTime() {
		return this.effectTime;
	}
	
	public int getPotResetTime() {
		return this.resetTime;
	}
	
	public HashMap<Integer, List<String>> getCommands() {
		return this.commands;
	}
	
	public List<String> getHologramLines() {
		return this.hologramLines;
	}
}
