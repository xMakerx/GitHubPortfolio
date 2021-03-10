package mc.decodedlogic.skybattlesuspawners.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public class MenuManager {
	
	private static Map<UUID, Menu> menus;
	
	static {
		menus = new HashMap<UUID, Menu>(); 
	}
	
	public static void openMenu(final Player player, final MobSpawner spawner) {
	    openMenu(player.getUniqueId(), spawner);
	}
	
	public static void openMenu(final UUID uuid, final MobSpawner spawner) {
		if(spawner == null) throw new IllegalArgumentException("Spawner cannot be null!");
		final SpawnerMenu menu = new SpawnerMenu(Bukkit.getPlayer(uuid), spawner);
		menu.open(true);
		
		menus.put(uuid, menu);
	}
	
	public static void openLogMenu(final Player player, Location l) {
	    LogMenu menu = new LogMenu(player, l);
	    menu.open(true);
	    menus.put(player.getUniqueId(), menu);
	}
	
	public static boolean removeMenu(final UUID uuid) {
		if(menus.containsKey(uuid)) {
			menus.remove(uuid);
			
			final Player player = USpawners.get().getServer().getPlayer(uuid);
			if(player != null) player.closeInventory();
			return true;
		}
		
		return false;
	}
	
	public static void setMenu(final UUID uuid, final Menu menu) {
		if(menu == null && menus.containsKey(uuid)) {
			removeMenu(uuid);
		}else {
			menus.put(uuid, menu);
		}
	}
	
	public static Menu getMenu(final UUID uuid) {
	    return menus.get(uuid);
	}
	
	public static void closeAllMenus() {
	    
	    menus.keySet().stream().forEach(uuid -> {
	        Player p = Bukkit.getPlayer(uuid);
	        
	        if(p != null) p.closeInventory();
	    });
	    
	    menus.clear();
	}
	
}
