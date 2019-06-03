package org.xmakerx.raidpractice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.xmakerx.raidpractice.util.ConfigUtils;

/**
 * This handles data such as points, games played, games won, games lost, etc.
 * All data is saved with UUIDs.
 */

public class Database {
	
	final RaidPractice instance;
	final File dbFile;
	final YamlConfiguration database;
	final HashMap<String, Object> defaultValues = new HashMap<String, Object>();
	
	public Database(final RaidPractice main) {
		this.instance = main;
		this.dbFile = new File(main.getDataFolder() + "/database.yml");
		
		// Let's set up the default values.
		this.defaultValues.put("gamesPlayed", 0);
		this.defaultValues.put("gamesWon", 0);
		this.defaultValues.put("gamesLost", 0);
		this.defaultValues.put("points", 0);
		this.defaultValues.put("keys", 0);
		
		// Let's generate the database file if it doesn't exist.
		if(!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				instance.getLogger().severe("Could not generate database.yml. Aborting...");
				instance.disable();
			}
		}
		
		database = YamlConfiguration.loadConfiguration(dbFile);
		update();
	}
	
	/**
	 * Let's update the database entries if there's new defaults.
	 */
	
	private void update() {
		boolean upToDate = true;
		for(String uuid : database.getKeys(false)) {
			final ConfigurationSection playerSection = database.getConfigurationSection(uuid);
			for(Map.Entry<String, Object> entry : defaultValues.entrySet()) {
				if(playerSection.get(entry.getKey()) == null) {
					upToDate = false;
					playerSection.set(entry.getKey(), entry.getValue());
				}
			}
		}
		
		// Let's save now to be performance effective.
		save();
		if(upToDate) {
			instance.getLogger().info("Database is up to date.");
		}else {
			instance.getLogger().info("Brought database up to date.");
		}
	}
	
	public void save() {
		try {
			ConfigUtils.save(dbFile, database);
		} catch (IOException e) {
			instance.getLogger().severe("Failed to save database.yml.");
		}
	}
	
	public HashMap<OfflinePlayer, Integer> getTopPoints() {
		final HashMap<OfflinePlayer, Integer> map = new HashMap<OfflinePlayer, Integer>();
		for(final String uuid : database.getKeys(false)) {
			final ConfigurationSection playerSection = database.getConfigurationSection(uuid);
			final int points = playerSection.getInt("points");
			map.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), points);
		}
		
		final List<Map.Entry<OfflinePlayer, Integer>> sortedEntries = new ArrayList<Map.Entry<OfflinePlayer, Integer>>(map.entrySet());
		Collections.sort(sortedEntries, new Comparator<Map.Entry<OfflinePlayer, Integer>>() {
			public int compare(Entry<OfflinePlayer, Integer> entry1, Entry<OfflinePlayer, Integer> entry2) {
				return entry2.getValue().compareTo(entry1.getValue());
			}

			@Override
			public Comparator<Entry<OfflinePlayer, Integer>> reversed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<Entry<OfflinePlayer, Integer>> thenComparing(
					Comparator<? super Entry<OfflinePlayer, Integer>> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U extends Comparable<? super U>> Comparator<Entry<OfflinePlayer, Integer>> thenComparing(
					Function<? super Entry<OfflinePlayer, Integer>, ? extends U> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> Comparator<Entry<OfflinePlayer, Integer>> thenComparing(
					Function<? super Entry<OfflinePlayer, Integer>, ? extends U> arg0,
					Comparator<? super U> arg1) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<Entry<OfflinePlayer, Integer>> thenComparingDouble(
					ToDoubleFunction<? super Entry<OfflinePlayer, Integer>> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<Entry<OfflinePlayer, Integer>> thenComparingInt(
					ToIntFunction<? super Entry<OfflinePlayer, Integer>> arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<Entry<OfflinePlayer, Integer>> thenComparingLong(
					ToLongFunction<? super Entry<OfflinePlayer, Integer>> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		final LinkedHashMap<OfflinePlayer, Integer> topPlayers = new LinkedHashMap<OfflinePlayer, Integer>();
		
		for(Map.Entry<OfflinePlayer, Integer> entry : sortedEntries) {
			topPlayers.put(entry.getKey(), entry.getValue());
		}
		return topPlayers;
	}
	
	public void setPoints(final Player player, final int points) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			section.set("points", points);
		}
	}
	
	public int getPoints(final Player player) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			return section.getInt("points");
		}else {
			return -1;
		}
	}
	
	public void setKeys(final Player player, final int keys) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			section.set("keys", keys);
		}
	}
	
	public int getKeys(final Player player) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			return section.getInt("keys");
		}else {
			return -1;
		}
	}
	
	public void setGamesPlayed(final Player player, final int gamesPlayed) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			section.set("gamesPlayed", gamesPlayed);
		}
	}
	
	public int getGamesPlayed(final Player player) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			return section.getInt("gamesPlayed");
		}else {
			return -1;
		}
	}
	
	public void setGamesWon(final Player player, final int gamesWon) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			section.set("gamesWon", gamesWon);
		}
	}
	
	public int getGamesWon(final Player player) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			return section.getInt("gamesWon");
		}else {
			return -1;
		}
	}
	
	public void setGamesLost(final Player player, final int gamesLost) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			section.set("gamesLost", gamesLost);
		}
	}
	
	public int getGamesLost(final Player player) {
		if(hasData(player)) {
			final ConfigurationSection section = getData(player);
			return section.getInt("gamesLost");
		}else {
			return -1;
		}
	}
	
	public ConfigurationSection getData(final Player player) {
		return database.getConfigurationSection(player.getUniqueId().toString());
	}
	
	public void createEntry(final Player player) {
		final UUID uuid = player.getUniqueId();
		final ConfigurationSection playerSection = database.createSection(uuid.toString());
		for(Map.Entry<String, Object> entry : defaultValues.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			playerSection.set(key, value);
		}
	}
	
	/**
	 * This checks if we are storing data for this player.
	 * @param Player player
	 * @return true/false flag.
	 */
	
	public boolean hasData(final Player player) {
		if(database.getConfigurationSection(player.getUniqueId().toString()) != null) {
			return true;
		}
		return false;
	}
}
