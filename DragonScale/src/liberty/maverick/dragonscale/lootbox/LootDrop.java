package liberty.maverick.dragonscale.lootbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import liberty.maverick.dragonscale.util.GlowEnchantment;
import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("deprecation")
public class LootDrop implements ContingentObject {
	
    private final MaterialData matData;
	private final double dropChance;
	
	private String displayName;
	private List<String> lore;
	private int minAmount;
	private int maxAmount;
	
	// Enchantment map containing the enchantment type
	// as the key and the level as the value.
	private Map<Enchantment, Integer> enchantments;
	
	private boolean glowing;
	
	public LootDrop(final MaterialData data, double chance, String displayName, 
			List<String> lore, int minAmount, int maxAmount, 
			HashMap<Enchantment, Integer> enchantments, boolean shouldGlow) {
		this.matData = data;
		this.dropChance = (chance >= 1) ? chance / 100 : chance;
		this.displayName = displayName;
		this.lore = (lore == null) ? new ArrayList<String>() : lore;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.enchantments = enchantments;
		this.glowing = shouldGlow;
	}
	
	/**
	 * Generates a {@link ItemStack} based on the data stored within this class.
	 * @return An ItemStack instance
	 */
	
	public ItemStack generate() {
		final Random rand = new Random();
		final int amount = rand.nextInt((maxAmount - minAmount) + 1) + minAmount;
		
		ItemStack drop = new ItemStack(matData.getItemType(), amount);
		drop.setData(matData);
		
		ItemMeta meta = drop.getItemMeta();
		
		// Let's set the display name of the item.
		if(displayName != null) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		}
		
		// Let's setup our lore for the item.
		final List<String> dropLore = new ArrayList<String>();
		dropLore.addAll(lore);
		
		for(int i = 0; i < dropLore.size(); i++) {
			String line = dropLore.get(i);
			dropLore.set(i, ChatColor.translateAlternateColorCodes('&', line));
		}
		
		meta.setLore(dropLore);
		
		if(glowing && enchantments.size() == 0) {
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(new GlowEnchantment(), 1, true);
		}
		
		// Let's add the enchantments to the item.
		for(Enchantment ench : enchantments.keySet()) {
			int level = enchantments.get(ench);
			meta.addEnchant(ench, level, true);
		}
		
		drop.setItemMeta(meta);
		
		return drop;
	}
	
	/**
	 * Fetches the {@link MaterialData} associated
	 * with this drop.
	 * @return MaterialData object.
	 */
	
	public MaterialData getMaterialData() {
		return this.matData;
	}
	
	public double getDropChance() {
		return this.dropChance;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public List<String> getLore() {
		return this.lore;
	}
	
	/**
	 * Fetches the minimum amount that can drop at a time.
	 * @return int
	 */
	
	public int getMinimumDropAmount() {
		return this.minAmount;
	}
	
	/**
	 * Fetches the maximum amount that can drop at a time.
	 * @return int
	 */
	
	public int getMaximumDropAmount() {
		return this.maxAmount;
	}
	
	/**
	 * Fetches the map of {@link Enchantment} mapped to their level
	 * @return Map<Enchantment, Integer (representing level)>
	 */
	
	public Map<Enchantment, Integer> getEnchantments() {
		return this.enchantments;
	}
	
}
