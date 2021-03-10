package liberty.maverick.dragonscale.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DragonScaleLootboxPickupEvent extends DragonScaleEvent {
	
	final Player player;
	final ItemStack lootBoxItem;
	
	public DragonScaleLootboxPickupEvent(final Player player, final ItemStack lootBoxItem) {
		this.player = player;
		this.lootBoxItem = lootBoxItem;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public ItemStack getLootBoxItem() {
		return this.lootBoxItem;
	}

}
