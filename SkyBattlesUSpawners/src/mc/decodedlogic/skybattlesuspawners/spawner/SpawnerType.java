package mc.decodedlogic.skybattlesuspawners.spawner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mc.decodedlogic.skybattlesuspawners.USpawners;

public enum SpawnerType {
	COW(EntityType.COW, (short) 92),
	MUSHROOM_COW(EntityType.MUSHROOM_COW, (short) 96, "MOOSHROOM"),
	PIG(EntityType.PIG, (short) 90),
	CHICKEN(EntityType.CHICKEN, (short) 93),
	SHEEP(EntityType.SHEEP, (short) 91),
	WOLF(EntityType.WOLF, (short) 95),
	OCELOT(EntityType.OCELOT, (short) 98),
	SQUID(EntityType.SQUID, (short) 94),
	WITCH(EntityType.WITCH, (short) 66),
	SKELETON(EntityType.SKELETON, (short) 51),
	ZOMBIE(EntityType.ZOMBIE, (short) 54),
	GIANT(EntityType.GIANT, (short) 53),
	HORSE(EntityType.HORSE, (short) 100),
	IRON_GOLEM(EntityType.IRON_GOLEM, (short) 99),
	SILVERFISH(EntityType.SILVERFISH, (short) 60),
	CREEPER(EntityType.CREEPER, (short) 50),
	SPIDER(EntityType.SPIDER, (short) 52),
	CAVE_SPIDER(EntityType.CAVE_SPIDER, (short) 59),
	SLIME(EntityType.SLIME, (short) 55),
	WITHER(EntityType.WITHER, (short) 64),
	ENDERMAN(EntityType.ENDERMAN, (short) 58),
	ENDER_DRAGON(EntityType.ENDER_DRAGON, (short) 63),
	BAT(EntityType.BAT, (short) 65),
	BLAZE(EntityType.BLAZE, (short) 61),
	GHAST(EntityType.GHAST, (short) 56),
	PIG_ZOMBIE(EntityType.PIG_ZOMBIE, (short) 57, "ZOMBIE_PIGMAN"),
	MAGMA_CUBE(EntityType.MAGMA_CUBE, (short) 62),
	VILLAGER(EntityType.VILLAGER, (short) 120),
	GUARDIAN(EntityType.GUARDIAN, (short) 68),
	ENDERMITE(EntityType.ENDERMITE, (short) 67),
	SNOW_GOLEM(EntityType.SNOWMAN, (short) 97, "SNOWMAN"),
	RABBIT(EntityType.RABBIT, (short) 101);
	
	private final EntityType TYPE;
	private final short DATA;
	private final List<SpawnerUpgrade> UPGRADES;
	private final String NAME;
	
	private Material defaultDrop;
	
	// Whether or not killing a mob will only remove one from the stack.
	private boolean oneByOne;
	
	// Blacklisted death causes
	private final List<DamageCause> BLACKLIST_DEATH_CAUSES;
	
	private SpawnerType(EntityType type, short data) {
		this.TYPE = type;
		this.DATA = data;
		this.UPGRADES = new ArrayList<SpawnerUpgrade>();
		this.NAME = type.name();
		this.defaultDrop = null;
		this.oneByOne = false;
		this.BLACKLIST_DEATH_CAUSES = new ArrayList<DamageCause>();
	}
	
	private SpawnerType(EntityType type, short data, String name) {
		this.TYPE = type;
		this.DATA = data;
		this.UPGRADES = new ArrayList<SpawnerUpgrade>();
		this.NAME = name;
		this.defaultDrop = null;
		this.oneByOne = false;
		this.BLACKLIST_DEATH_CAUSES = new ArrayList<DamageCause>();
	}
	
	public void loadData(final ConfigurationSection section) {
		// Let's load up the upgrades and the default drop.
		String dropMaterial = section.getString("defaultDrop", null);
		
		if(dropMaterial != null) {
			try {
				this.defaultDrop = Material.valueOf(dropMaterial);
			} catch (IllegalArgumentException e) {
				USpawners.get().getNotify().error(String.format("Material %s is invalid in ConfigurationSection \"%s\".", 
						dropMaterial, section.getName()));
				USpawners.get().disable();
				return;
			}
		}
		
		if(section.getStringList("blacklistDeathCauses") != null) {
		    for(String causeStr : section.getStringList("blacklistDeathCauses")) {
		        try {
		            this.BLACKLIST_DEATH_CAUSES.add(DamageCause.valueOf(causeStr));
		        } catch (IllegalArgumentException | NullPointerException e) {
                    USpawners.get().getNotify().error(String.format("Error parsing kill option \"%s\"! Invalid DamageCause! Message: %s.", causeStr, e.getMessage()));
                }
		    }
		}
		
		this.oneByOne = section.getBoolean("oneByOne", false);
		
		for(String upgradeKey : section.getKeys(false)) {
			if(upgradeKey.equalsIgnoreCase("defaultDrop") || upgradeKey.equalsIgnoreCase("oneByOne") || upgradeKey.equalsIgnoreCase("blacklistDeathCauses")) continue;
			final ConfigurationSection uSection = section.getConfigurationSection(upgradeKey);
			
			final String icon = uSection.getString("icon");
			String drop = (String) uSection.get("drop", icon);
			final int rate = (int) uSection.get("rate", 0);
			final double cost = (double) uSection.get("cost", 0.0);
			
			final Material iconMat = Material.valueOf(icon);
			final Material dropMat = Material.valueOf(drop);
			
			SpawnerUpgrade upgrade = new SpawnerUpgrade(iconMat, dropMat, cost, rate);
			
			String iconName = uSection.getString("name", null);
			if(iconName != null) {
			    upgrade.setIconName(iconName);
			}
			
			List<String> iconDesc = (uSection.get("description", null) != null) ? uSection.getStringList("description") : null;
			if(iconDesc != null) {
			    upgrade.setIconDesc(iconDesc);
			}
			
			int iconSlot = uSection.getInt("slot", -1);
			if(iconSlot >= 0) {
			    upgrade.setIconSlot(iconSlot);
			}
			
			upgrade.index = UPGRADES.size();
			upgrade.type = this;
			this.UPGRADES.add(upgrade);
		}
	}
	
	public EntityType getEntityType() {
		return this.TYPE;
	}
	
	public short getData() {
		return this.DATA;
	}
	
	public List<SpawnerUpgrade> getUpgrades() {
		return this.UPGRADES;
	}
	
	public String getName() {
		return this.NAME;
	}
	
	public Material getDefaultDrop() {
		return this.defaultDrop;
	}
	
	public boolean isOneByOne() {
	    return this.oneByOne;
	}
	
	public List<DamageCause> getBlacklistDeathCauses() {
	    return this.BLACKLIST_DEATH_CAUSES;
	}
	
	public static SpawnerType getTypeFromEntityType(EntityType type) {
		if(type == null) throw new NullPointerException("Cannot get SpawnerType from null!");
		
		for(SpawnerType sType : values()) {
			if(sType.getEntityType() == type) {
				return sType;
			}
		}
		
		return null;
	}

}
