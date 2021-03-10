package mc.decodedlogic.gucciislandtop.menu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MenuManager {
	
	private static final Map<UUID, Menu> dictionary;
	
	static {
		dictionary = new HashMap<UUID, Menu>();
	}
	
	public static void setMenu(UUID uuid, Menu menu) {
		final Menu activeMenu = dictionary.get(uuid);
		if(activeMenu != null) activeMenu.close();
		if(menu == null) dictionary.remove(uuid);
		
		dictionary.put(uuid, menu);
	}
	
	public static void setMenu(Player player, Menu menu) {
		setMenu(player.getUniqueId(), menu);
	}
	
	public static Menu getMenu(UUID uuid) {
		return dictionary.get(uuid);
	}
	
	public static Menu getMenu(Player player) {
		return getMenu(player.getUniqueId());
	}
	
	public static void closeAll() {
		Iterator<UUID> it = dictionary.keySet().iterator();
		
		while(it.hasNext()) {
			final UUID uuid = it.next();
			final Menu activeMenu = dictionary.get(uuid);
			
			if(activeMenu != null) activeMenu.close();
			it.remove();
			
			final Player player = Bukkit.getServer().getPlayer(uuid);
			
			if(player != null) {
				player.closeInventory();
			}
		}
		
		dictionary.clear();
	}

}
