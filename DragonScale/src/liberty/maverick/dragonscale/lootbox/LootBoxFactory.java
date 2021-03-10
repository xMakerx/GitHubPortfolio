package liberty.maverick.dragonscale.lootbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleLogger;
import liberty.maverick.dragonscale.event.DragonScaleLootboxOpenEvent;
import liberty.maverick.dragonscale.util.DragonUtils;
import liberty.maverick.dragonscale.util.GlowEnchantment;

@SuppressWarnings("deprecation")
public class LootBoxFactory implements ContingentObject {
	
	final DragonScale main;
	final DragonScaleLogger logger;
	
	private String displayName;
	private List<String> lore;
	private double dropChance;
	
	// The drops that have a chance to drop.
	private Set<LootDrop> possibleDrops;
	
	public LootBoxFactory(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.logger = new DragonScaleLogger(mainInstance, "LootBoxFactory");
		this.displayName = "";
		this.lore = null;
		this.dropChance = 0.0;
		this.possibleDrops = new HashSet<LootDrop>();
	}
	
	public void readConfigData(final ConfigurationSection section) {
		this.possibleDrops.clear();
		this.displayName = section.getString("Name");
		this.lore = section.getStringList("Lore");
		this.dropChance = section.getDouble("Chance");
		
		// Correct drop chances that are greater than 1
		this.dropChance = (dropChance >= 1) ? dropChance / 100 : dropChance;
		
		final ConfigurationSection lootSection = section.getConfigurationSection("Loot");
		
		for(String sectionName : lootSection.getKeys(false)) {
			final ConfigurationSection dropSection = lootSection.getConfigurationSection(sectionName);
			Material material = null;
			String displayName = dropSection.getString("Name", null);
			double dropChance = dropSection.getDouble("Chance", -1.0);
			byte data = (byte) dropSection.getInt("Data", 0);
			List<String> lore = dropSection.getStringList("Lore");
			boolean glow = dropSection.getBoolean("Glow", false);
			int minAmount = 1;
			int maxAmount = 1;
			
			final String typeName = dropSection.getString("Type", null);
			final String matName = (typeName == null) ? sectionName.toUpperCase() : typeName.toUpperCase();
			
			// Let's verify that we fetched a drop chance.
			if(dropChance == -1.0) {
				logger.error(String.format("Could not fetch the drop chance for \"%s\"!", sectionName));
				main.disable();
				break;
			}
			
			// Let's fetch the material.
			try {
				material = Material.valueOf(matName);
				
			} catch (IllegalArgumentException | NullPointerException e) {
				logger.error(String.format("Material \"%s\" is invalid!", matName));
				main.disable();
				break;
			}
			
			// Let's fetch the minimum and maximum amount.
			// We allow a range to be specified by doing X-Y
			// X being the minimum amount and Y being the maximum.
			String rawData = dropSection.getString("Amount");
			String[] split = rawData.split("-");
			
			if(split.length == 1) {
				minAmount = Integer.valueOf(rawData);
				maxAmount = minAmount;
			}else {
				minAmount = Integer.valueOf(split[0]);
				maxAmount = Integer.valueOf(split[1]);
			}
			
			// Let's fetch the enchantments.
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			
			final ConfigurationSection enchSection = dropSection.getConfigurationSection("Enchantments");
			
			if(enchSection != null) {
				for(String enchName : enchSection.getKeys(false)) {
					Enchantment ench = Enchantment.getByName(enchName.toUpperCase());
					int level = enchSection.getInt(enchName);
					
					if(ench == null) {
						logger.error(String.format("Enchantment \"%s\" does not exist! Make sure you typed it correctly!", enchName));
						main.disable();
						break;
					}
					
					enchantments.put(ench, level);
				}
			}
			
			final MaterialData matData = new MaterialData(material, data);
			final LootDrop drop = new LootDrop(matData, dropChance, displayName, 
					lore, minAmount, maxAmount, enchantments, glow);
			possibleDrops.add(drop);
		}
	}
	
	/**
	 * Handles whenever a {@link Player} "opens" a loot box by placing one on the ground.
	 * @param player - The player that "opened" the loot box.
	 * @param boxLocation - The {@link Location} where the loot box was placed.
	 */
	
	public void processPlayerOpen(final Player player, final Location boxLocation) {
		final double theta = Math.random();
		Optional<LootDrop> drop = possibleDrops.stream().sorted(new Comparator<LootDrop>() {
			
			@Override
			public int compare(LootDrop drop1, LootDrop drop2) {
				int dropChance1 = (int) drop1.getDropChance() * 100;
				int dropChance2 = (int) drop2.getDropChance() * 100;
				
				return dropChance1 - dropChance2;
			}
			
		}).filter(x -> x.getDropChance() > theta).findAny();
		
		if(drop.isPresent()) {
			final ItemStack item = drop.get().generate();
			DragonUtils.givePlayerItems(player, boxLocation, item);
			
			String name = DragonUtils.fromCodeNameToServerName(item.getType().name());
			
			if(item.hasItemMeta() && item.getItemMeta().getDisplayName() != null && !item.getItemMeta().getDisplayName().isEmpty()) {
				name = item.getItemMeta().getDisplayName();
			}
			
			String msg = main.getSettings().getLootBoxOpenMessage();
			msg = msg.replaceAll("\\{AMOUNT\\}", (item.getAmount() == 1) ? "a" : String.valueOf(item.getAmount()));
			msg = msg.replaceAll("\\{ITEM\\}", name);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			main.getSettings().getSoundByName("LOOTBOX_OPEN").play(player.getLocation());
			
			// Let's call the lootbox open event.
			final DragonScaleLootboxOpenEvent evt = new DragonScaleLootboxOpenEvent(player, drop.get(), item);
			main.getServer().getPluginManager().callEvent(evt);
			
			logger.debug(String.format("Giving %s %s as loot!", player.getDisplayName(), item.getType().name()));
		}else {
			logger.error("Couldn't determine which an item to drop!");
		}
	}
	
	public ItemStack generate() {
		ItemStack lootBox = new ItemStack(Material.NOTE_BLOCK, 1);
		ItemMeta meta = lootBox.getItemMeta();
		
		if(displayName != null) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		}
		
		if(lore != null) {
			List<String> itemLore = new ArrayList<String>();
			itemLore.addAll(lore);
			
			for(int i = 0; i < itemLore.size(); i++) {
				String line = itemLore.get(i);
				itemLore.set(i, ChatColor.translateAlternateColorCodes('&', line));
			}
			
			meta.setLore(itemLore);
		}
		
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(new GlowEnchantment(), 1, true);
		lootBox.setItemMeta(meta);
		
		return lootBox;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public List<String> getLore() {
		return this.lore;
	}
	
	public double getDropChance() {
		return this.dropChance;
	}
	
	/**
	 * Fetches a Set of {@link LootDrop} which have a
	 * chance of being dropped from a loot box.
	 * @return Set of LootDrops
	 */
	
	public Set<LootDrop> getPossibleDrops() {
		return this.possibleDrops;
	}
	
}
