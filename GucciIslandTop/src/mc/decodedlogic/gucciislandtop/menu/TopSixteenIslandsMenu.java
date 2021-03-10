package mc.decodedlogic.gucciislandtop.menu;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.IslandTop;
import mc.decodedlogic.gucciislandtop.Settings;
import mc.decodedlogic.gucciislandtop.Utils;

public class TopSixteenIslandsMenu extends Menu {
	
	final Settings settings;
	
	public TopSixteenIslandsMenu(final Player viewer) {
		super(viewer);
		this.settings = IslandTop.get().getSettings();
	}

	@Override
	public void generate() {
		this.elements.clear();
		this.topIslands = Utils.getLimitedTopIslands();

		String menuName = Utils.limitLengthTo(Utils.color(settings.getTopSixteenMenuName()), 32);
		this.gui = Bukkit.createInventory(null, 45, menuName);
		
		List<Integer> slots = settings.getTopIslandSlots();
		UUID topRankedOwner = null;
		
		for(int i = 0; i < slots.size(); i++) {
			int slot = slots.get(i);
			UUID uuid = null;
			
			if(i < topIslands.keySet().size()) {
				uuid = (UUID) topIslands.keySet().toArray()[i];
			}
			
			if(i == 0) {
				topRankedOwner = uuid;
			}
			
			createIslandButton(uuid, slot);
		}
		
		final Island islandOn = ASkyBlockAPI.getInstance().getIslandAt(VIEWER.getLocation());
		
		if(topRankedOwner != null && topRankedOwner != VIEWER.getUniqueId() && islandOn != null) {
			createIslandButton(islandOn.getOwner(), ElementData.ISLAND.getSlot());
		}else {
			createIslandButton(VIEWER.getUniqueId(), ElementData.ISLAND.getSlot());
		}
		
		if(!Utils.getTopIslands().containsKey(VIEWER.getUniqueId())) {
			this.startRegenerateTimer(ASkyBlockAPI.getInstance().getIslandOwnedBy(VIEWER.getUniqueId()));
		}
		
		if(settings.shouldFillEmptySlots()) {
			for(int i = 0; i < gui.getContents().length; i++) {
				final ItemStack item = gui.getContents()[i];
				
				if(item == null) {
					final Element element = new Element(this, i);
					element.setData(ElementData.EMPTY_SLOT);
					element.generate(null);
					addElement(element);
				}
			}
		}
	}
	
	public void createIslandButton(final UUID uuid, int slot) {
		final Island island = ASkyBlockAPI.getInstance().getIslandOwnedBy(uuid);
		Element element = null;
		
		if(uuid != null && island != null) {
			element = new ButtonElement(this, slot);
			element.setData(ElementData.ISLAND);
			
			((ButtonElement) element).setClickCommand(new Command() {
				public void execute() {
					final IslandMenu islandMenu = new IslandMenu(island, VIEWER);
					islandMenu.generate();
					islandMenu.open();
				}
			});

		}else {
			element = new Element(this, slot);
			element.setData(ElementData.UNKNOWN_PLAYER);
		}
		
		element.generate(uuid);
		addElement(element);
	}
	
	public String replaceVariables(String message) {
		return message;
	}

}
