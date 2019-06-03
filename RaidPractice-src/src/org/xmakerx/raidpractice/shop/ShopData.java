package org.xmakerx.raidpractice.shop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.Settings;

public class ShopData {
	
	final RaidPractice instance;
	final Localizer localizer;
	final Settings settings;
	final File shopFile;
	
	// Codename : ShopItem
	final LinkedHashMap<String, ShopItem> shopItems;
	
	YamlConfiguration config;
	
	public ShopData(final RaidPractice main) {
		this.instance = main;
		this.localizer = main.getLocalizer();
		this.settings = main.getSettings();
		this.shopFile = new File(main.getDataFolder() + "/shop.yml");
		this.shopItems = new LinkedHashMap<String, ShopItem>();
		
		final DoubleHealth dblHealth = new DoubleHealth();
		this.shopItems.put(dblHealth.getCodeName(), dblHealth);
		
		final EmptySlotItem emptySlot = new EmptySlotItem();
		this.shopItems.put(emptySlot.getCodeName(), emptySlot);
		
		final BalanceItem balance = new BalanceItem();
		this.shopItems.put(balance.getCodeName(), balance);
		
		final RespawnItem respawn = new RespawnItem();
		this.shopItems.put(respawn.getCodeName(), respawn);
		
		boolean needsDataWrite = false;
		
		// Let's make sure the shop file exists.
		if(!shopFile.exists()) {
			try {
				shopFile.createNewFile();
				needsDataWrite = true;
			} catch (IOException e) {
				main.getLogger().severe(String.format("Failed to generate shop.yml. Error: %s", e.getMessage()));
				return;
			}
		}
		
		this.config = YamlConfiguration.loadConfiguration(shopFile);
		if(needsDataWrite) {
			writeData();
		}else {
			loadData();
		}
	}
	
	public void save() {
		try {
			this.config.save(shopFile);
		} catch (IOException e) {
			instance.getLogger().severe(String.format("Failed to save shop.yml. Error: %s", e.getMessage()));
		}
	}
	
	public void writeItemData(final ConfigurationSection shopSection, final ShopItem item) {
		final ConfigurationSection itemSection = shopSection.createSection(item.getCodeName());
		
		// The default values for all shop items.
		itemSection.set("name", item.getName());
		itemSection.set("description", item.getDescription());
		if(item.getCost() > 0) itemSection.set("cost", item.getCost());
		itemSection.set("icon", item.getIcon().name());
		if(item.canBeDisabled()) itemSection.set("enabled", item.isEnabled());
		
		// Now, let's save the item's metadata.
		for(Map.Entry<String, Object> dataEntry : item.getMetadata().entrySet()) {
			// Save each metadata value one-by-one.
			final String key = dataEntry.getKey();
			final Object value = dataEntry.getValue();
			itemSection.set(key, value);
		}
	}
	
	public void writeData() {
		if(config.getConfigurationSection("shop") != null) {
			config.set("shop", null);
		}
		
		final ConfigurationSection shopSection = config.createSection("shop");
		
		for(Map.Entry<String, ShopItem> entry : shopItems.entrySet()) {
			final ShopItem item = entry.getValue();
			writeItemData(shopSection, item);
		}
		
		save();
	}
	
	public void loadData() {
		final ConfigurationSection shopSection = config.getConfigurationSection("shop");
		boolean needsSave = false;
		
		for(Map.Entry<String, ShopItem> entry : shopItems.entrySet()) {
			final ConfigurationSection itemSection = shopSection.getConfigurationSection(entry.getKey());
			final ShopItem item = entry.getValue();
			
			if(itemSection == null) {
				writeItemData(shopSection, item);
				needsSave = true;
				continue;
			}
			
			// These are all default values for all shop items.
			item.setName(itemSection.getString("name"));
			item.setDescription(itemSection.getStringList("description"));
			if(item.getCost() > 0) item.setCost(itemSection.getInt("cost"));
			item.setIcon(Material.getMaterial(itemSection.getString("icon")));
			if(item.canBeDisabled()) item.setEnabled(itemSection.getBoolean("enabled"));
			
			// Remove any keys that aren't utilized with this item.
			for(String key : itemSection.getKeys(false)) {
				if(!item.getMetadata().keySet().contains(key) && !Arrays.asList("name", "description", "cost", "icon", "enabled").contains(key)) {
					itemSection.set(key, null);
					needsSave = true;
				}
			}
			
			for(Map.Entry<String, Object> dataEntry : item.getMetadata().entrySet()) {
				final String dataKey = dataEntry.getKey();
				
				if(itemSection.contains(dataKey)) {
					// Update the value if necessary.
					item.getMetadata().put(dataKey, itemSection.get(dataKey));
				}else {
					// Make sure to add any missing metadata values.
					itemSection.set(dataKey, dataEntry.getValue());
					needsSave = true;
				}
			}
			
			if(needsSave) save();
			
			shopItems.put(entry.getKey(), item);
		}
	}
	
	public HashMap<String, ShopItem> getItems() {
		return this.shopItems;
	}
}
