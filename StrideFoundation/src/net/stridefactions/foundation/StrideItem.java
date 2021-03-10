package net.stridefactions.foundation;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A simple sort of ItemBuilder system to easily create items.
 */

public enum StrideItem {
	PROGRESS, 
	ADD_1, 
	ADD_5, 
	ADD_25,
	ADD_1000,
	EFFECT_01,
	EFFECT_02;
	
	private ItemStack item;
	private String displayName;
	private List<String> lore;
	private Material material;
	private int amount;
	private short durability;
	private boolean enchanted;
	
	private StrideItem() {
		this.item = null;
		this.displayName = "";
		this.lore = new ArrayList<String>();
		this.material = Material.AIR;
		this.amount = 1;
		this.durability = (short) 0;
		this.enchanted = false;
	}
	
	public ItemStack buildItem() {
		this.item = new ItemStack(material, amount);
		this.item.setDurability(this.durability);
		if(enchanted) item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
		
		final ArrayList<String> itemLore = new ArrayList<String>(lore);
		
		for(int i = 0; i < itemLore.size(); i++) {
			String line = itemLore.get(i);
			line = line.replaceAll("\\{progress\\}", MoneyPot.getProgressString());
			line = line.replaceAll("\\{progressBar\\}", MoneyPot.getProgressBar());
			line = line.replaceAll("\\{goal\\}", String.valueOf(MoneyPot.getMaxBalance()));
			itemLore.set(i, ChatColor.translateAlternateColorCodes('&', line));
		}
		
		// Let's set up the item meta.
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		meta.setLore(itemLore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		this.item.setItemMeta(meta);
		
		return this.item;
	}
	
	public StrideItem setDisplayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public StrideItem setLore(final List<String> lines) {
		this.lore = lines;
		return this;
	}
	
	public List<String> getLore() {
		return this.lore;
	}
	
	public StrideItem setMaterial(final String materialName) {
		try {
			this.material = Material.valueOf(materialName);
		}catch (NullPointerException | IllegalArgumentException e) {
			final String mat = (materialName != null) ? materialName : "NONE";
			StrideFoundation.getInstance().getLogger().severe(String.format("Material %s does not exist.", mat));
		}
		
		return this;
	}
	
	public Material getMaterial() {
		return this.material;
	}
	
	public StrideItem setDurability(final short durability) {
		this.durability = durability;
		return this;
	}
	
	public short getDurability() {
		return this.durability;
	}
	
	public StrideItem setAmount(final int amount) {
		this.amount = amount;
		return this;
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	public StrideItem setEnchanted(final boolean enchanted) {
		this.enchanted = enchanted;
		return this;
	}
	
	public boolean isEnchanted() {
		return this.enchanted;
	}
	
	public static StrideItem getItemByName(final String itemName) {
		try {
			final StrideItem item = StrideItem.valueOf(itemName);
			return item;
		}catch (IllegalArgumentException | NullPointerException e) {
			final String name = (itemName != null) ? itemName : "NONE";
			StrideFoundation.getInstance().getLogger().severe(String.format("StrideItem %s does not exist.", name));
			return null;
		}
	}
}
