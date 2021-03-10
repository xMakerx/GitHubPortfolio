package mc.decodedlogic.gucciislandtop.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mc.decodedlogic.gucciislandtop.Utils;

public class Element {
	
	protected Menu gui;
	protected int slot;
	protected State state;
	
	protected ElementData data;
	
	public Element(Menu gui, int slot) {
		this.gui = gui;
		this.slot = slot;
		this.state = State.ENABLED;
		this.data = null;
	}
	
	public void onStateChange(State previousState, State newState) {}
	
	@SuppressWarnings("deprecation")
	public void generate(final UUID islandOwnerUUID) {
		if(data == null) throw new NullPointerException("You must set the element data before generating an element!");
		
		ItemStack icon = null;

		if(data.getIconData().getItemType() == Material.SKULL_ITEM) {
			String headName = data.getHeadName();
			
			if(islandOwnerUUID != null) {
				headName = ChatColor.stripColor(Bukkit.getOfflinePlayer(islandOwnerUUID).getName());
			}
			
			icon = Utils.generatePlayerHead(headName);
		}else {
			icon = new ItemStack(data.getIconData().getItemType());
			icon.setDurability((short) data.getIconData().getData());
		}
		
		final ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(Utils.color(gui.replaceIslandElementVariables(islandOwnerUUID, data.getName())));
		
		final List<String> description = new ArrayList<String>();
		
		for(int i = 0; i < data.getDescription().size(); i++) {
			String line = data.getDescription().get(i);
			
			if(line.equalsIgnoreCase("%memberList%")) {
				final List<String> memberList = gui.getMemberList(islandOwnerUUID);
				for(String member : memberList) {
					description.add(member);
				}
			}else {
				description.add(Utils.color(gui.replaceIslandElementVariables(islandOwnerUUID, line)));
			}
		}
		
		meta.setLore(description);
		icon.setItemMeta(meta);
		
		gui.gui.setItem(slot, icon);
	}

	
	public void update() {
		
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public void setState(State newState) {
		final State prevState = state;
		this.state = newState;
		this.onStateChange(prevState, newState);
	}
	
	public State getState() {
		return this.state;
	}
	
	public void setData(ElementData data) {
		this.data = data;
	}
	
	public ElementData getData() {
		return this.data;
	}
	
}
