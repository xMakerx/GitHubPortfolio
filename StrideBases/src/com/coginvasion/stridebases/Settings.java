package com.coginvasion.stridebases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.coginvasion.stridebases.schematic.Schematic;
import com.coginvasion.stridebases.schematic.SchematicUtils;

public class Settings {
	
	final StrideBases instance;
	final File configFile;
	final File dataFile;
	final File basesDir;
	final YamlConfiguration config;
	final YamlConfiguration dataConfig;
	
	final HashSet<Base> bases;
	final HashMap<UUID, HashMap<Base, Boolean>> playerBases;
	final HashMap<UUID, BaseBuildSession> buildSessions;
	final HashMap<UUID, CheckedOutBase> checkedOutBases;
	
	private String skullName, skullOwner;
	private List<String> skullLore;
	
	// Load materials
	private ItemStack buildBase, builtBase, emptySlot, donateItem, nextPageItem, backPageItem;
	private Sound buildCompleteSound;
	
	ConfigurationSection msgs;
	
	public Settings(final StrideBases main) {
		this.instance = main;
		this.configFile = new File(main.getDataFolder() + "/config.yml");
		this.bases = new HashSet<Base>();
		this.playerBases = new HashMap<UUID, HashMap<Base, Boolean>>();
		this.buildSessions = new HashMap<UUID, BaseBuildSession>();
		this.checkedOutBases = new HashMap<UUID, CheckedOutBase>();
		
		// Let's make sure the bases directory exists.
		this.basesDir = new File(main.getDataFolder() + "/bases");
		if(!basesDir.exists()) basesDir.mkdirs();
		
		// Let's make sure our data file exists.
		this.dataFile = new File(main.getDataFolder() + "/data.yml");
		if(!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				main.getLogger().severe(String.format("Failed to create data.yml. Error: %s.", e.getMessage()));
			}
		}
		
		if(!configFile.exists()) main.saveDefaultConfig();
		this.config = YamlConfiguration.loadConfiguration(configFile);
		this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
		this.loadData();
	}
	
	public ItemStack handleItemData(final String itemData) {
		if(itemData.indexOf(":") != -1) {
			final String[] bbSplit = itemData.split(":");
			final Material bbMat = Material.getMaterial(bbSplit[0]);
			int data = Integer.valueOf(bbSplit[1]);
			final ItemStack item = new ItemStack(bbMat, 1);
			item.setDurability((short) data);
			return item;
		}else {
			return new ItemStack(Material.getMaterial(itemData), 1);
		}
	}
	
	public ItemStack handleSpecialItem(final String sectionName) {
		final ConfigurationSection section = config.getConfigurationSection("gui").getConfigurationSection(sectionName);
		final ItemStack item = handleItemData(section.getString("material"));
		final ItemMeta meta = item.getItemMeta();
		final ArrayList<String> lore = new ArrayList<String>();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
		
		// Set up the lore
		for(final String line : section.getStringList("lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		
		if(lore.size() > 0) meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public void loadData() {
		final ConfigurationSection gui = config.getConfigurationSection("gui");
		this.buildBase = handleItemData(gui.getString("buildBaseMaterial"));
		this.builtBase = handleItemData(gui.getString("builtBaseMaterial"));
		this.emptySlot = handleItemData(gui.getString("emptySlotMaterial"));
		this.donateItem = handleSpecialItem("donateItem");
		this.nextPageItem = handleSpecialItem("nextPage");
		this.backPageItem = handleSpecialItem("backPage");
		this.buildCompleteSound = Sound.valueOf(config.getString("baseCompleteSound"));
		
		// Let's load the bases.
		final ConfigurationSection bases = config.getConfigurationSection("bases");
		
		for(final String key : bases.getKeys(false)) {
			final ConfigurationSection baseSection = bases.getConfigurationSection(key);
			final String name = baseSection.getString("name");
			final List<String> lore;
			
			if(baseSection.getStringList("lore") == null) {
				lore = new ArrayList<String>();
			}else {
				lore = baseSection.getStringList("lore");
			}
			
			String schematicFile = baseSection.getString("schematic");
			if(!schematicFile.contains(".schematic")) {
				schematicFile = schematicFile.concat(".schematic");
			}
			
			final File schematicPath = new File(basesDir.getAbsolutePath() + "/" + schematicFile);
			
			if(!schematicPath.exists()) {
				instance.getLogger().severe(String.format(
					"Cannot load Base %s because schematic file does not exist in /bases.", 
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name))));
				continue;
			}
			
			final Schematic schematic = SchematicUtils.loadSchematic(schematicPath);
			final Base base = new Base(instance, name, lore, schematic);
			
			this.bases.add(base);
			instance.getLogger().info(String.format("Successfully loaded Base %s!", base.getCodeName()));
		}
		
		this.msgs = config.getConfigurationSection("messages");
		final ItemMeta eMeta = emptySlot.getItemMeta();
		eMeta.setDisplayName(getMessage("emptySlot"));
		emptySlot.setItemMeta(eMeta);
		
		// Let's load up the skull name and lore.
		final ConfigurationSection skullSection = config.getConfigurationSection("skullItem");
		this.skullName = ChatColor.translateAlternateColorCodes('&', skullSection.getString("name"));
		this.skullOwner = skullSection.getString("owner");
		final List<String> rawLore = skullSection.getStringList("lore");
		
		for(int i = 0; i < rawLore.size(); i++) {
			String line = rawLore.get(i);
			line = ChatColor.translateAlternateColorCodes('&', line);
			rawLore.set(i, line);
		}
		
		this.skullLore = rawLore;
		
		// Let's load the bases players have.
		for(final String key : dataConfig.getKeys(false)) {
			final UUID uuid = UUID.fromString(key);
			if(uuid != null) {
				final HashMap<Base, Boolean> ownedBases = new HashMap<Base, Boolean>();
				for(final String line : dataConfig.getStringList(key)) {
					for(final Base base : this.bases) {
						final String[] split = line.split(":");
						String name = split[0];
						
						if(!name.equalsIgnoreCase("activeBase")) {
							boolean used = Boolean.valueOf(split[1]);
							if(base.getCodeName().equalsIgnoreCase(name) && !used) {
								ownedBases.put(base, used);
							}
						}else {
							final String baseName = split[1];
							if(base.getCodeName().equalsIgnoreCase(baseName)) {
								// TODO: Add active base item here.
							}
						}
					}
				}
				
				this.playerBases.put(uuid, ownedBases);
			}
		}
	}
	
	public void saveData() {
		for(final String key : dataConfig.getKeys(false)) {
			dataConfig.set(key, null);
		}
		
		for(Map.Entry<UUID, HashMap<Base, Boolean>> entry : playerBases.entrySet()) {
			final UUID uuid = entry.getKey();
			final HashMap<Base, Boolean> bases = entry.getValue();
			final ArrayList<String> codeNames = new ArrayList<String>();
			
			for(Map.Entry<Base, Boolean> entry2 : bases.entrySet()) {
				final String codeName = entry2.getKey().getCodeName().concat(":").concat(entry2.getValue().toString());
				codeNames.add(codeName);
			}
			
			dataConfig.set(uuid.toString(), codeNames);
		}
		
		save();
	}
	
	public String getString(final String msgName) {
		String msg = msgs.getString(msgName);
		if(msg == null) msg = "MESSAGE NOT FOUND";
		return msg;
	}
	
	public String getMessage(final String msgName) {
		String msg = getString(msgName);
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public void save() {
		try {
			this.dataConfig.save(dataFile);
		} catch (IOException e) {
			instance.getLogger().severe(String.format("Failed to save data.yml. Error: %s.", e.getMessage()));
		}
	}
	
	
	public void addOwnedBase(final UUID uuid, final Base base) {
		HashMap<Base, Boolean> ownedBases = new HashMap<Base, Boolean>();
		if(playerBases.containsKey(uuid)) {
			ownedBases = playerBases.get(uuid);
		}
		
		ownedBases.put(base, false);
		playerBases.put(uuid, ownedBases);
	}
	
	public void setBaseUsed(final UUID uuid, final Base base, final boolean flag) {
		if(playerBases.containsKey(uuid)) {
			final HashMap<Base, Boolean> ownedBases = playerBases.get(uuid);
			ownedBases.put(base, flag);
		}
	}
	
	public void removeOwnedBase(final Player player, final Base base) {
		if(playerBases.containsKey(player.getUniqueId())) {
			final HashMap<Base, Boolean> ownedBases = playerBases.get(player.getUniqueId());
			if(ownedBases.keySet().contains(base)) ownedBases.remove(base);
			playerBases.put(player.getUniqueId(), ownedBases);
		}
	}
	
	public HashMap<Base, Boolean> getOwnedBases(final Player player) {
		if(playerBases.containsKey(player.getUniqueId())) {
			return playerBases.get(player.getUniqueId());
		}
		
		return null;
	}
	
	public CheckedOutBase getCheckedOutBase(final Player player) {
		if(checkedOutBases.containsKey(player.getUniqueId())) {
			return checkedOutBases.get(player.getUniqueId());
		}
		
		return null;
	}
	
	public BaseBuildSession startBuildSession(final Player player, final Base base) {
		final BaseBuildSession session = new BaseBuildSession(player, base);
		this.buildSessions.put(player.getUniqueId(), session);
		return session;
	}
	
	public void removeBuildSession(final UUID uuid) {
		if(buildSessions.containsKey(uuid)) {
			this.buildSessions.remove(uuid);
		}
	}
	
	public BaseBuildSession getBaseBuildSession(final UUID uuid) {
		return buildSessions.get(uuid);
	}
	
	public Collection<BaseBuildSession> getBuildSessions() {
		return buildSessions.values();
	}
	
	public HashSet<Base> getBases() {
		return this.bases;
	}
	
	public ItemStack getBuildBaseItem() {
		return this.buildBase;
	}
	
	public ItemStack getBuiltBaseItem() {
		return this.builtBase;
	}
	
	public ItemStack getEmptySlotItem() {
		return this.emptySlot;
	}
	
	public ItemStack getDonateItem() {
		return this.donateItem;
	}
	
	public ItemStack getNextPageItem() {
		return this.nextPageItem;
	}
	
	public ItemStack getBackPageItem() {
		return this.backPageItem;
	}
	
	public String getSkullName() {
		return this.skullName;
	}
	
	public String getSkullOwner() {
		return this.skullOwner;
	}
	
	public List<String> getSkullLore() {
		return this.skullLore;
	}
	
	public Sound getBaseCompleteSound() {
		return this.buildCompleteSound;
	}
}
