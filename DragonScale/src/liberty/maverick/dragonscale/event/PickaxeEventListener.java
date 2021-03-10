package liberty.maverick.dragonscale.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleSettings;
import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;

public class PickaxeEventListener implements Listener {
	
	final DragonScale main;
	final DragonScaleSettings settings;
	
	public PickaxeEventListener(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.settings = mainInstance.getSettings();
	}
	
	@EventHandler
	public void onPickaxeExpChangeEvent(final DragonScalePickaxeExpChangeEvent evt) {
		final DragonScalePickaxe pickaxe = evt.getPickaxe();
		final Player player = pickaxe.getWielder();
		
		if(player != null) {
			settings.getSoundByName("EXP_PICKUP").play(player.getLocation());
		}
	}
	
	@EventHandler
	public void onPickaxeLevelChangeEvent(final DragonScalePickaxeLevelChangeEvent evt) {
		final DragonScalePickaxe pickaxe = evt.getPickaxe();
		final Player player = pickaxe.getWielder();
		
		if(player != null) {
			settings.getSoundByName("LEVEL_UP").play(player.getLocation());
			
			if(evt.getNewLevel() == 5 || evt.getNewLevel() % 10 == 0) {
				String broadcastMsg = settings.getLevelUpBroadcastMessage();
				broadcastMsg = broadcastMsg.replaceAll("\\{PLAYER\\}", player.getDisplayName());
				broadcastMsg = broadcastMsg.replaceAll("\\{LEVEL\\}", String.valueOf(evt.getNewLevel()));
				main.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMsg));
			}
		}
	}
}
