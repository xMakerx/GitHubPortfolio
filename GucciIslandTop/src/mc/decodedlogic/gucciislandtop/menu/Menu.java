package mc.decodedlogic.gucciislandtop.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.IslandTop;
import mc.decodedlogic.gucciislandtop.IslandTopLogger;
import mc.decodedlogic.gucciislandtop.Utils;
import net.md_5.bungee.api.ChatColor;

public abstract class Menu {
	
	protected final Player VIEWER;
	protected final IslandTopLogger logger;
	protected Inventory gui;
	
	protected List<Element> elements;
	protected Map<UUID, Double> topIslands;
	protected BukkitTask regenerateTask;
	
	public Menu(final @Nonnull Player viewer) {
		this.logger = new IslandTopLogger("Menu");
		this.VIEWER = viewer;
		this.gui = null;
		this.elements = new ArrayList<Element>();
		this.topIslands = Utils.getTopIslands();
		this.regenerateTask = null;
	}
	
	public void startRegenerateTimer(final Island island) {
		final IslandTop main = IslandTop.get();
		
		if(island == null) {
			logger.warning("Attempted to start regeneration timer of a NULL island's menu.");
			return;
		}
		
		regenerateTask = new BukkitRunnable() {
			
			public void run() {
				final Map<UUID, Double> topIslands = Utils.getTopIslands();
				final UUID owner = island.getOwner();
				
				if((topIslands != null && owner != null) && topIslands.containsKey(owner)) {
					
					new BukkitRunnable() {
						
						public void run() {
							generate();
							open();
						}
						
					}.runTask(main);

				}
				
				this.cancel();
				regenerateTask = null;
				return;
			}
			
		}.runTaskTimer(main, 200L, 200L);
	}
	
	public void open() {
		if(gui == null) throw new NullPointerException("Cannot open a NULL inventory!");
		
		VIEWER.openInventory(gui);
		VIEWER.updateInventory();
		
		// Let's make sure that the MenuManager is aware of the viewer accessing this menu.
		if(MenuManager.getMenu(VIEWER) != this) MenuManager.setMenu(VIEWER, this);
	}
	
	public void close() {
		if(regenerateTask != null) {
			regenerateTask.cancel();
			regenerateTask = null;
		}
	}
	
	public abstract void generate();
	
	public void update() {
		for(Element element : elements) {
			element.update();
		}
	}
	
	public Player getViewer() {
		return this.VIEWER;
	}
	
	public Inventory getGUI() {
		return this.gui;
	}
	
	public boolean addElement(Element element) {
		if(element == null) throw new NullPointerException("Cannot add a NULL element!");
		return this.elements.add(element);
	}
	
	public boolean removeElement(Element element) {
		if(element == null) throw new NullPointerException("Cannot remove a NULL element!");
		return this.elements.remove(element);
	}
	
	public Element getElementAt(int slot) {
		for(Element element : elements) {
			if(element.getSlot() == slot) {
				return element;
			}
		}
		
		return null;
	}
	
	public String replaceIslandElementVariables(UUID islandOwnerUUID, String message) {
		if(islandOwnerUUID != null) {
			final Island island = ASkyBlockAPI.getInstance().getIslandOwnedBy(islandOwnerUUID);
			if(island != null) {
				if(topIslands.containsKey(islandOwnerUUID)) {
					int rank = calculateIslandRank(islandOwnerUUID) + 1;
					int level = (Utils.getIslandLevels().containsKey(islandOwnerUUID)) ? Utils.getIslandLevel(islandOwnerUUID) : 0;
					double worth = (topIslands.containsKey(islandOwnerUUID)) ? topIslands.get(islandOwnerUUID) : 0.0;
					message = Utils.replaceVariableWith(message, "player", 
							ChatColor.stripColor(Bukkit.getOfflinePlayer(islandOwnerUUID).getName()));
					message = Utils.replaceVariableWith(message, "rank", rank);
					message = Utils.replaceVariableWith(message, "level", level);
					message = Utils.replaceVariableWith(message, "worth", worth);
				}else {
					String calculating = ChatColor.stripColor(Utils.color(IslandTop.get().getSettings().getCalculating()));
					message = Utils.replaceVariableWith(message, "player", 
							ChatColor.stripColor(Bukkit.getOfflinePlayer(islandOwnerUUID).getName()));
					message = Utils.replaceVariableWith(message, "rank", calculating);
					message = Utils.replaceVariableWith(message, "level", calculating);
					message = Utils.replaceVariableWith(message, "worth", calculating);
				}
			}
		}
		
		return Utils.color(message);
	}
	
	public List<String> getMemberList(final UUID islandOwnerUUID) {
		final List<String> members = new ArrayList<String>();
		final Island island = ASkyBlockAPI.getInstance().getIslandOwnedBy(islandOwnerUUID);
		
		if(island == null) throw new NullPointerException("Could not find the island owned by the specified UUID.");
		
		for(UUID uuid : island.getMembers()) {
			if(uuid.equals(island.getOwner())) continue;
			
			final OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
			
			String line = IslandTop.get().getSettings().getIslandMemberEntry();
			line = Utils.replaceVariableWith(line, "player", ChatColor.stripColor(member.getName()));
			members.add(Utils.color(line));
		}
		
		if(members.size() == 0) {
			String line = IslandTop.get().getSettings().getIslandMemberEntry();
			line = Utils.replaceVariableWith(line, "player", "None");
			members.add(Utils.color(line));
		}
		
		return members;
	}
	
	public int calculateIslandRank(UUID islandOwnerUUID) {
		for(int i = 0; i < topIslands.keySet().size(); i++) {
			final UUID uuid = (UUID) topIslands.keySet().toArray()[i];
			
			if(uuid.equals(islandOwnerUUID)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public List<Element> getElements() {
		return this.elements;
	}
	
}
