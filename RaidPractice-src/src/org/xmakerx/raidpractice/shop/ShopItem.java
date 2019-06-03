package org.xmakerx.raidpractice.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;

public abstract class ShopItem {
	
	protected String name;
	protected ArrayList<String> description;
	protected int cost;
	protected Material icon;
	protected boolean enabled;
	
	// The name this item will be stored by.
	protected final String codeName;
	
	// If this is some sort of required item, we might want to have this item always available.
	protected final boolean allowDisable;
	
	// Data that should be stored in shop.yml.
	protected final HashMap<String, Object> metadata;
	
	public ShopItem(final String name, final ArrayList<String> desc, final int cost, final Material icon, final boolean allowDisable) {
		this.name = name;
		this.codeName = getCamelName();
		this.description = desc;
		this.cost = cost;
		this.icon = icon;
		this.allowDisable = allowDisable;
		this.enabled = true;
		this.metadata = new HashMap<String, Object>();
	}
	
	public void setName(final String newName) {
		this.name = newName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCamelName() {
		String curName = name;
		curName = curName.toLowerCase();
		curName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', curName));
		
		if(curName.indexOf(" ") == -1) return curName;
		
		String firstSection = curName.substring(0, curName.indexOf(" "));
		
		final String[] split = curName.split("\\s+");
		
		for(int i = 1; i < split.length; i++) {
			final String line = split[i];
			firstSection = firstSection.concat(String.valueOf(line.toUpperCase().charAt(0))
					.concat(line.substring(1, line.length())));
		}
		
		return firstSection;
	}
	
	public String getCodeName() {
		return this.codeName;
	}
	
	public void setDescription(final List<String> list) {
		this.description.clear();
		for(final String str : list) {
			this.description.add(str);
		}
	}
	
	public ArrayList<String> getDescription() {
		return this.description;
	}
	
	public void setCost(final int newCost) {
		this.cost = newCost;
	}
	
	public int getCost() {
		return this.cost;
	}
	
	public void setIcon(final Material newIcon) {
		this.icon = newIcon;
	}
	
	public Material getIcon() {
		return this.icon;
	}
	
	public void setEnabled(final boolean flag) {
		this.enabled = flag;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public boolean canBeDisabled() {
		return this.allowDisable;
	}
	
	public HashMap<String, Object> getMetadata() {
		return this.metadata;
	}
}
