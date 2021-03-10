package liberty.maverick.dragonscale.pickaxe;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class ExplodeEnchantment extends PickaxeEnchantment {

	final int DEFAULT_RADIUS = 3;

	public ExplodeEnchantment(final DragonScalePickaxe parent, int level, double occurChance) {
		super(parent, "Explode", level, occurChance * level);
	}
	
	public void activate(Block block) {
		final Location loc = block.getLocation();
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), level * DEFAULT_RADIUS, false, false);
		
		affectBlocksInRadius(block, level * DEFAULT_RADIUS);
	}

}
