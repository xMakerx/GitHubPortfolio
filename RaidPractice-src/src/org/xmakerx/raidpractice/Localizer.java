package org.xmakerx.raidpractice;

import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.xmakerx.raidpractice.arena.Game.GameState;
import org.xmakerx.raidpractice.util.ConfigUtils;

public class Localizer {
	
	final RaidPractice instance;
	final File configFile;
	final YamlConfiguration config;
	
	public Localizer(final RaidPractice main) {
		this.instance = main;
		
		// Let's load up the language file.
		this.configFile = new File(main.getDataFolder() + "/lang.yml");

		// Let's check if the file already exists and generate if it doesn't.
		if(!configFile.exists()) {
			main.saveResource("lang.yml", true);
		}
		
		this.config = YamlConfiguration.loadConfiguration(configFile);
		ConfigUtils.update(instance, "lang.yml", config, configFile);
		
		// We need to set the text for our game states.
		GameState.WAITING.setText(getColoredString("waiting"));
		GameState.IN_GAME.setText(getColoredString("in_game"));
		GameState.DISABLED.setText(getColoredString("disabled"));
		GameState.SETTING_UP.setText(getColoredString("setting_up"));
	}
	
	private boolean isKey(final String str) {
		return config.getKeys(false).contains(str);
	}
	
	public String getMessage(String message) {
		String prefix = getColoredString("messagePrefix");
		if(isKey(message)) message = getColoredString(message);
		
		if(!prefix.isEmpty()) {
			return String.format("%s %s", prefix, message);
		}else {
			return message;
		}
	}
	
	public String getString(final String name) {
		String msg = config.getString(name);
		if(msg == null) {
			msg = String.format("%s could not be found.", name);
		}
		return msg;
	}
	
	public List<String> getStringList(final String listName) {
		return config.getStringList(listName);
	}
	
	public String getColoredString(final String name) {
		return ChatColor.translateAlternateColorCodes('&', getString(name));
	}
}
