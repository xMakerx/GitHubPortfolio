package mc.decodedlogic.gucciislandtop.menu;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import mc.decodedlogic.skybattlesuspawners.Utils;

public enum ElementData {
	ISLAND("IslandElementData"),
	UNKNOWN_PLAYER("UnknownPlayerElementData"),
	EMPTY_SLOT("EmptySlotElementData"),
	SPAWNERS_IDENTIFIER("SpawnerElementData"),
	ITEMS_IDENTIFIER("ItemElementData");
	
	private final String CONFIG_NAME;
	private MaterialData iconData;
	private String name;
	private String headName;
	private List<String> description;
	private int slot;
	
	private ElementData(String cfgKey) {
		this.CONFIG_NAME = cfgKey;
		this.iconData = null;
		this.name = null;
		this.headName = null;
		this.description = null;
		this.slot = -1;
	}
	
	public void loadDataFrom(final ConfigurationSection section) {
		if(section == null) throw new NullPointerException("Section cannot be null!");
		
		// Attempt to fetch the slot from the section, if not found, continue using
		// the default slot.
		this.slot = section.getInt("Slot", this.slot);
		
		this.name = section.getString("Name");
		this.description = section.getStringList("Description");
		this.headName = section.getString("HeadName");
		
		String itemData = section.getString("Icon", "");
		
		if(itemData.isEmpty()) throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
				+ "You must specify an icon with its name and optional data. Ex: STAINED_GLASS_PANE:7", section.getName()));
		
		String[] split = itemData.split(":");
		String matName = null;
		byte data = 0;
		
		if(split.length > 0 && split.length <= 2) {
			matName = split[0];
			
			if(split.length == 2) {
				try {
					data = Byte.valueOf(split[1]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
					+ "Incorrect data argument for icon. Expected byte, got \"%s\".", section.getName(), split[1]));
				}
			}
			
			this.iconData = Utils.buildMaterialData(matName, data);
		}else if(split.length > 2) {
			throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
			+ "Too many arguments! Expected 2 at most!", section.getName()));
		}
	}
	
	public String getConfigName() {
		return this.CONFIG_NAME;
	}
	
	public MaterialData getIconData() {
		return this.iconData;
	}
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * If this button is to be a skull, this will return the specified head name for it.
	 * @return
	 */
	
	public String getHeadName() {
		return this.headName;
	}
	
	/**
	 * Returns the raw description without formatting and variable replacements.
	 * @return
	 */
	
	public List<String> getDescription() {
		return this.description;
	}
	
	public int getSlot() {
		return this.slot;
	}
}
