package liberty.maverick.dragonscale.pickaxe;

import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.util.DragonUtils;

public class PickaxeEnchantment extends PickaxeAttribute {
	
	protected String enchantmentName;
	protected int level;
	
	// This is the chance for the enchantment to occur.
	protected double occurChance;
	
	/**
	 * This constructor is to represent a vanilla Minecraft enchantment.
	 * @param The name of the enchantment in the code.
	 * @param level
	 */
	
	@SuppressWarnings("deprecation")
    public PickaxeEnchantment(final DragonScalePickaxe parent, String enchantmentName, int level) {
		super(parent, DragonUtils.getPickaxeAttributeTitle(enchantmentName, level), 
				PickaxeAttribute.AttributeType.ENCHANTMENT);
		this.enchantmentName = enchantmentName;
		this.level = level;
		this.occurChance = 1.0;
		
		if(Enchantment.getByName(enchantmentName.toUpperCase()) == null) {
			DragonScale.singleton.getSystemLogger().error(String.format("Failed to fetch Vanilla "
					+ "Minecraft Enchantment \"%s\"!", enchantmentName));
			DragonScale.singleton.disable();
		}
	}
	
	/**
	 * The following constructor is for custom enchantments with a chance to occur.
	 * @param The name of the enchantment.
	 * @param The level of the enchantment.
	 * @param The chance of the enchantment to occur.
	 */
	
	public PickaxeEnchantment(final DragonScalePickaxe parent, String enchantmentName, int level, double occurChance) {
		super(parent, DragonUtils.getPickaxeAttributeTitle(enchantmentName, level), 
				PickaxeAttribute.AttributeType.ENCHANTMENT);
		this.enchantmentName = enchantmentName;
		this.level = level;
		this.occurChance = (occurChance > 1) ? occurChance / 100 : occurChance;
	}
	
	public void affectBlocksInRadius(final Block block, final int radius) {
		// This is the maxiumum radius 
		final int delta = (int) Math.ceil((radius - 1) / 2.0);
		
		final int x1 = block.getLocation().getBlockX() - delta;
		final int x2 = block.getLocation().getBlockX() + delta;
		final int minX = Math.min(x1, x2);
		final int maxX = Math.max(x1, x2);
		
		final int y1 = block.getLocation().getBlockY() - delta;
		final int y2 = block.getLocation().getBlockY() + delta;
		final int minY = Math.min(y1, y2);
		final int maxY = Math.max(y1, y2);
		
		final int z1 = block.getLocation().getBlockZ() - delta;
		final int z2 = block.getLocation().getBlockZ() + delta;
		final int minZ = Math.min(z1, z2);
		final int maxZ = Math.max(z1, z2);
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					Block suppBlock = pickaxe.wielder.getWorld().getBlockAt(x, y, z);
					
					final boolean canBuildHere = DragonUtils.canBuildHere(pickaxe.wielder, suppBlock.getLocation());
					
					if(suppBlock.getLocation() != block.getLocation() && DragonUtils.isSameType(block, suppBlock) && canBuildHere) {
						pickaxe.minedThisPeriod.add(suppBlock);
						
						// Let's do some effects to spice things up.
						suppBlock.getWorld().spawnParticle(Particle.BLOCK_DUST, suppBlock.getLocation(), 10, suppBlock.getType().createBlockData());
						suppBlock.getWorld().playEffect(suppBlock.getLocation(), Effect.SMOKE, 5);
					}
				}
			}
		}
	}
	
	/**
	 * Checks whether or not this enchantment is representing a vanilla one.
	 * @return true/false flag.
	 */
	
	@SuppressWarnings("deprecation")
    public boolean isVanillaEnchantment() {
		return (Enchantment.getByName(enchantmentName.toUpperCase()) != null);
	}
	
	/**
	 * These methods are to be re-implemented in custom enchantment classes.
	 */
	
	public void onEquip() {
		return;
	}
	
	public void onDequip() {
		return;
	}

	public void onMine(Block block) {
		// If we're working with a non-vanilla enchantment, let's
		// "roll-the-dice."
		if(!isVanillaEnchantment() && Math.random() < occurChance) {
			// Great! This enchantment is going off!
			activate(block);
		}
	}

	public void activate(Block block) {
		return;
	}
	
	public String getEnchantmentName() {
		return this.enchantmentName;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public double getOccurChance() {
		return this.occurChance;
	}

}
