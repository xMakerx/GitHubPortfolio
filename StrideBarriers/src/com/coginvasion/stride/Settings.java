package com.coginvasion.stride;

import java.io.File;
import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.coginvasion.stride.barrier.BarrierType;

public class Settings {
	
	final StrideBarriers instance;
	final YamlConfiguration config;
	private final ArrayList<BarrierType> barrierTypes;
	private ItemStack barrierWand, infoHead, emptySlot, remove;
	private Sound barrierBuilt, barrierDeath;
	
	public Settings(final StrideBarriers main) {
		this.instance = main;
		this.barrierTypes = new ArrayList<BarrierType>();
		
		// Let's make sure we have a config.
		final File configFile = new File(main.getDataFolder() + "/config.yml");
		if(!configFile.exists()) main.saveDefaultConfig();
		this.config = YamlConfiguration.loadConfiguration(configFile);
		loadData();
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
	
	public void loadData() {
		final ConfigurationSection barrierSection = config.getConfigurationSection("barriers");
		for(final String key : barrierSection.getKeys(false)) {
			final ConfigurationSection barSection = barrierSection.getConfigurationSection(key);
			final DyeColor color = DyeColor.valueOf(barSection.getString("color"));
			final double cost = barSection.getDouble("cost");
			final int durability = barSection.getInt("durability");
			
			if(color == null) {
				instance.getLogger().severe(String.format("DyeColor %s does not exist!", barSection.getString("color")));
				continue;
			}
			
			final BarrierType type = new BarrierType(color, cost, durability);
			barrierTypes.add(type);
			instance.getLogger().info(String.format("Loaded BarrierType %s!", color.name()));
		}
		
		final ConfigurationSection headSection = config.getConfigurationSection("infoHead");
		final SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		meta.setOwner(headSection.getString("owner"));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', headSection.getString("name")));
		
		// Let's add the lore!
		final ArrayList<String> lore = new ArrayList<String>();
		
		for(final String line : headSection.getStringList("lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		meta.setLore(lore);
		this.infoHead = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		this.infoHead.setItemMeta(meta);
		
		final Material toolMat = Material.getMaterial(config.getString("toolMat"));
		final String name = ChatColor.translateAlternateColorCodes('&', config.getString("toolName"));
		this.barrierWand = new ItemStack(toolMat, 1);
		final ItemMeta toolMeta = barrierWand.getItemMeta();
		toolMeta.setDisplayName(name);
		this.barrierWand.setItemMeta(toolMeta);
		
		this.emptySlot = handleItemData(config.getString("emptySlotMat"));
		final ItemMeta slotMeta = emptySlot.getItemMeta();
		slotMeta.setDisplayName(getMessage("emptySlot"));
		emptySlot.setItemMeta(slotMeta);
		
		this.remove = handleItemData(config.getString("removeBarrierMat"));
		final ItemMeta rmvMeta = remove.getItemMeta();
		rmvMeta.setDisplayName(getMessage("barrierRemove"));
		remove.setItemMeta(rmvMeta);
		
		this.barrierBuilt = Sound.valueOf(config.getString("barrierBuiltSound"));
		this.barrierDeath = Sound.valueOf(config.getString("barrierDestroyedSound"));
	}
	
	public String getString(final String msgName) {
		String msg = config.getConfigurationSection("messages").getString(msgName);
		if(msg == null) msg = "MESSAGE NOT FOUND";
		return msg;
	}
	
	public String color(final String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String getMessage(final String msgName) {
		return color(getString(msgName));
	}
	
	public ArrayList<BarrierType> getBarrierTypes() {
		return this.barrierTypes;
	}
	
	public ItemStack getBarrierWand() {
		return this.barrierWand;
	}
	
	public ItemStack getInfoHeadItem() {
		return this.infoHead;
	}
	
	public ItemStack getEmptySlotItem() {
		return this.emptySlot;
	}
	
	public ItemStack getRemoveItem() {
		return this.remove;
	}
	
	public Sound getBarrierBuiltSound() {
		return this.barrierBuilt;
	}
	
	public Sound getBarrierDeathSound() {
		return this.barrierDeath;
	}
}
