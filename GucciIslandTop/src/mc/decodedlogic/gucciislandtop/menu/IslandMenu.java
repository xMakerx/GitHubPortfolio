package mc.decodedlogic.gucciislandtop.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.IslandTop;
import mc.decodedlogic.gucciislandtop.Settings;
import mc.decodedlogic.gucciislandtop.Utils;
import mc.decodedlogic.gucciislandtop.valuable.Attribute;
import mc.decodedlogic.gucciislandtop.valuable.Valuable;
import mc.decodedlogic.gucciislandtop.valuable.ValuableManager;

public class IslandMenu extends Menu {
	
	final Settings settings;
	final Island island;
	
	private boolean isCalculating;
	
	public IslandMenu(final Island island, final Player viewer) {
		super(viewer);
		this.settings = IslandTop.get().getSettings();
		this.island = island;
		this.isCalculating = !Utils.getTopIslands().containsKey(island.getOwner());
		
		if(Utils.getIslandsLeftToCalculate() == 0 && isCalculating) {
			Utils.calculateIslandWorth(island);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void generate() {
		this.elements.clear();

		String menuName = Utils.limitLengthTo(Utils.color(replaceIslandElementVariables(island.getOwner(), settings.getIslandMenuName())), 32);
		this.gui = Bukkit.createInventory(null, 45, menuName);
		
		Collection<Valuable> valuables = null;
		isCalculating = !Utils.getTopIslands().containsKey(island.getOwner());
		
		if(!isCalculating) {
			valuables = Utils.getIslandValuables(island.getOwner());
		}else {
			valuables = ValuableManager.getRegisteredValuables();
			startRegenerateTimer(island);
		}
		
		for(Valuable valuable : valuables) {
			if(valuable.getSlot() != -1) {
				final MaterialData data = (valuable.getIconData() == null) ? valuable.getMaterialData() : valuable.getIconData();
				ItemStack icon = null;
				
				if(data.getItemType() == Material.SKULL_ITEM) {
					icon = Utils.generatePlayerHead(valuable.getHeadName());
				}else {
					icon = new ItemStack(data.getItemType(), 1, data.getData());
				}
				
				final ItemMeta meta = icon.getItemMeta();
				
				if(isCalculating) {
					meta.setDisplayName(Utils.color(settings.getCalculating()));
				}else {
					meta.setDisplayName(replaceVariables(settings.getValuable(), valuable.getName(), valuable.getQuantity()));
					List<String> description = new ArrayList<String>();
					
					if(valuable.getMaterialData().getItemType() == Material.MOB_SPAWNER) {
						addAttributeSection(description, settings.getDefault(), valuable.getQuantity(), valuable.getWorth());
						
						for(Attribute attr : valuable.getAttributes()) {
							addAttributeSection(description, attr.getName(), attr.getQuantity(), attr.getWorth());
						}
						
						description.add(" ");
						String worthLine = settings.getTotalWorth();
						worthLine = Utils.replaceVariableWith(worthLine, "worth", valuable.calculateOverallWorth());
						description.add(Utils.color(worthLine));
					}else {
						description.add(replaceVariables(settings.getQuantity(), "", valuable.getQuantity()));
						String worthLine = settings.getWorth();
						worthLine = Utils.replaceVariableWith(worthLine, "worth", valuable.calculateOverallWorth());
						worthLine = Utils.replaceVariableWith(worthLine, "each", valuable.getWorth());
						description.add(Utils.color(worthLine));
					}
					
					meta.setLore(description);
				}
				
				icon.setItemMeta(meta);
				icon.setAmount((valuable.getQuantity() >= 64) ? 64 : valuable.getQuantity());
				gui.setItem(valuable.getSlot(), icon);
			}
		}
		
		final Element spwnID = new Element(this, ElementData.SPAWNERS_IDENTIFIER.getSlot());
		spwnID.setData(ElementData.SPAWNERS_IDENTIFIER);
		spwnID.generate(island.getOwner());
		addElement(spwnID);
		
		final Element itemsID = new Element(this, ElementData.ITEMS_IDENTIFIER.getSlot());
		itemsID.setData(ElementData.ITEMS_IDENTIFIER);
		itemsID.generate(island.getOwner());
		addElement(itemsID);
		
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
	
	public void close() {
		super.close();
	}
	
	public void addAttributeSection(List<String> description, String attrName, int quantity, double worth) {
		description.add(" ");
		description.add(Utils.color(replaceVariables(settings.getAttribute(), attrName, quantity)));
		
		String worthLine = settings.getWorth();
		worthLine = Utils.replaceVariableWith(worthLine, "worth", worth * quantity);
		worthLine = Utils.replaceVariableWith(worthLine, "each", worth);
		description.add(Utils.color(worthLine));
	}
	
	private String replaceVariables(String message, String name, int quantity) {
		message = Utils.replaceVariableWith(message, "name", Utils.color(name));
		message = Utils.replaceVariableWith(message, "quantity", quantity);
		return Utils.color(message);
	}
	
	public Island getIsland() {
		return this.island;
	}
	
	public boolean isCalculating() {
		return this.isCalculating;
	}

}
