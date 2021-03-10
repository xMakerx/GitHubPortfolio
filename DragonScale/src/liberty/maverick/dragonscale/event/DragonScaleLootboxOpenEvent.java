package liberty.maverick.dragonscale.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import liberty.maverick.dragonscale.lootbox.LootDrop;

public class DragonScaleLootboxOpenEvent extends DragonScaleEvent {
	
	final Player player;
	final LootDrop lootDropInst;
	final ItemStack loot;
	
	public DragonScaleLootboxOpenEvent(final Player player, final LootDrop lootInst, final ItemStack loot) {
		this.player = player;
		this.lootDropInst = lootInst;
		this.loot = loot;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public LootDrop getLoopData() {
		return this.lootDropInst;
	}
	
	public ItemStack getLoot() {
		return this.loot;
	}

}
