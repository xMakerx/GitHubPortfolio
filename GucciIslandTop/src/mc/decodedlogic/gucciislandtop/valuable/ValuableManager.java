package mc.decodedlogic.gucciislandtop.valuable;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import mc.decodedlogic.gucciislandtop.Utils;

public class ValuableManager {
	
	private static Set<Valuable> valuables;
	
	static {
		valuables = new HashSet<Valuable>();
	}
	
	public static boolean processValuableSection(ConfigurationSection section) {
		if(section == null) throw new NullPointerException("Valuable section cannot be null!");
		
		String displayName = section.getString("Name", "N/A");
		double worth =  section.getDouble("Worth", 0.0);
		
		String rawBlockData = section.getString("Block");
		MaterialData blockData = Utils.processRawMaterialData(rawBlockData, section.getName());
		
		String rawIconData = section.getString("Icon", null);
		MaterialData iconData = (rawIconData == null) ? blockData : Utils.processRawMaterialData(rawIconData, section.getName());
		
		String headName = section.getString("HeadName", null);
		int slot = section.getInt("Slot", -1);
		
		final Valuable valuable = new Valuable(blockData);
		valuable.setName(displayName);
		valuable.setWorth(worth);
		valuable.setIconData(iconData);
		valuable.setHeadName(headName);
		valuable.setSlot(slot);
		
		if(section.isConfigurationSection("Attributes")) {
			final ConfigurationSection attrs = section.getConfigurationSection("Attributes");
			
			for(String key : attrs.getKeys(false)) {
				if(attrs.isConfigurationSection(key)) {
					final ConfigurationSection attrSection = attrs.getConfigurationSection(key);
					final String name = attrSection.getString("Name", "N/A");
					final double attrWorth = attrSection.getDouble("Worth", 0.0);
					
					final Attribute attr = new Attribute(name, key);
					attr.setWorth(attrWorth);
					valuable.addAttribute(attr);
				}
			}
		}
		
		return valuables.add(valuable);
	}
	
	public static Set<Valuable> getRegisteredValuables() {
		return valuables;
	}
}
