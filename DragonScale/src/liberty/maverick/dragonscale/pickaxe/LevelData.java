package liberty.maverick.dragonscale.pickaxe;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleSettings;

public class LevelData {
	
	// The numeric level we're representing.
	private int numericLevel = 0;
	
	// The experience needed to reach this level.
	private int neededExp;
	
	// The attributes associated with this level.
	private HashSet<PickaxeAttribute> attributes;
	
	// The type of pickaxe associated with this level.
	private Material type;
	
	/**
	 * Container for data pertaining to level 1.
	 */
	
	public LevelData() {
		this(1);
	}
	
	/**
	 * Container for data pertaining to the specified level
	 * @param level - The numeric level to fetch the data of.
	 */
	
    public LevelData(int level) {
		this.neededExp = 0;
		this.attributes = new HashSet<PickaxeAttribute>();
		this.type = Material.WOODEN_PICKAXE;
		this.setLevel(level);
	}
	
	/**
	 * This method fetches all the data pertaining to the level
	 * this set is to represent.
	 */
	
    @SuppressWarnings("deprecation")
    public void setup() {
		final DragonScaleSettings settings = DragonScale.singleton.getSettings();
		
		this.attributes.clear();
		this.neededExp = settings.getNeededExp(numericLevel);
		this.type = Material.WOODEN_PICKAXE;
		
		final ConfigurationSection upgradesSection = DragonScale.singleton.getSettings().getLevelData(numericLevel);
		
		if(upgradesSection != null) {
			try {
				type = Material.valueOf(upgradesSection.getString("pickaxe"));
			} catch (IllegalArgumentException | NullPointerException e) {
				DragonScale.singleton.getSystemLogger().error(String.format("%s is not a valid Material type in upgrade data for Level %d."
						, upgradesSection.getString("pickaxe"), numericLevel));
			}
			
			final ConfigurationSection enchantmentsSection = upgradesSection.getConfigurationSection("enchantments");
			
			if(enchantmentsSection != null) {
				for(String key : enchantmentsSection.getKeys(false)) {
					final int level = enchantmentsSection.getInt(key, 1);
					
					if(Enchantment.getByName(key.toUpperCase()) != null) {
						attributes.add(new PickaxeEnchantment(null, key.toUpperCase(), level));
					}else if(key.equalsIgnoreCase("vein_miner")) {
						final double chance = settings.getConfig().getDouble("VeinminerChance");
						attributes.add(new VeinMinerEnchantment(null, level, chance));
					}else if(key.equalsIgnoreCase("explode")) {
						final double chance = settings.getConfig().getDouble("ExplodeChance");
						attributes.add(new ExplodeEnchantment(null, level, chance));
					}else {
						DragonScale.singleton.getSystemLogger().error(String.format("Enchantment \"%s\" could not be found!", key));
					}
				}
			}
			
			final ConfigurationSection effectsSection = upgradesSection.getConfigurationSection("effects");
			
			if(effectsSection != null) {
				for(String key : effectsSection.getKeys(false)) {
					final int level = effectsSection.getInt(key, 1);
					
					if(PotionEffectType.getByName(key.toUpperCase()) != null) {
						attributes.add(new PickaxePotionEffect(null, key.toUpperCase(), level));
					}else {
						DragonScale.singleton.getSystemLogger().error(String.format("PotionEffect \"%s\" could not be found!", key));
					}
				}
			}
		}else if(numericLevel != 1) {
			DragonScale.singleton.getSystemLogger().info(String.format("Could not fetch data for Level %d.", numericLevel));
		}
	}
	
	/**
	 * Sets the numeric level that this data set represents.
	 * @param level - An integer 1 <= n <= Integer.MAX_VALUE
	 * 
	 * <p>
	 * This will reset all members and reassign them as well.
	 * Essentially, changing the level via this method will
	 * call {@link #setup()}.
	 * </p>
	 */
	
	public void setLevel(int level) {
		final int maxLevel = DragonScale.singleton.getSettings().getMaxLevel();
		final int prevLevel = numericLevel;
		this.numericLevel = (level <= maxLevel) ? level : maxLevel;
		
		// Let's setup the data for the new level we're representing.
		if(prevLevel != level) setup();
	}
	
	/**
	 * Fetches the numeric level this data set represents.
	 * @return int 1 <= n <= Integer.MAX_VALUE
	 */
	
	public int getLevel() {
		return this.numericLevel;
	}
	
	/**
	 * Fetches the experience needed to level up from
	 * this level.
	 * @return int
	 */
	
	public int getLevelUpExp() {
		return this.neededExp;
	}
	
	/**
	 * Assigns all the {@link PickaxeAttribute} instances to the specified
	 * {@link DragonScalePickaxe} object.
	 * @param pickaxe - The non-null DragonScalePickaxe object that should take on the
	 * attributes.
	 */
	
	public void assignAttributesToPickaxe(final DragonScalePickaxe pickaxe) {
		if(pickaxe == null) throw new IllegalArgumentException("You must specify a valid DragonScalePickaxe instance!");
		
		for(final PickaxeAttribute attr : attributes) {
			attr.pickaxe = pickaxe;
		}
	}
	
	/**
	 * Fetches the {@link PickaxeAttribute}s associated with this
	 * level.
	 * @return HashSet of PickaxeAttributes.
	 */
	
	public HashSet<PickaxeAttribute> getPickaxeAttributes() {
		return this.attributes;
	}
	
	/**
	 * Fetches the {@link Material} of a pickaxe with this level.
	 * @return A pickaxe material. (Ex. WOODEN_PICKAXE)
	 */
	
	public Material getPickaxeType() {
		return this.type;
	}

}
