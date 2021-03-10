package liberty.maverick.dragonscale.pickaxe;

import org.bukkit.block.Block;

public class VeinMinerEnchantment extends PickaxeEnchantment {
	
	final int DEFAULT_RADIUS = 3;

	public VeinMinerEnchantment(final DragonScalePickaxe parent, int level, double occurChance) {
		super(parent, "Vein Miner", level, occurChance * level);
	}
	
	public void activate(Block block) {
		affectBlocksInRadius(block, level * DEFAULT_RADIUS);
	}

}
