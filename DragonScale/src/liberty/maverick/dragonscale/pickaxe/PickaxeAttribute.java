package liberty.maverick.dragonscale.pickaxe;

import org.bukkit.block.Block;

public abstract class PickaxeAttribute {
	
	public enum AttributeType {
		POTION_EFFECT, ENCHANTMENT;
	}
	
	protected DragonScalePickaxe pickaxe;
	protected final String name;
	protected final AttributeType type;
	
	public PickaxeAttribute(DragonScalePickaxe parent, String name, AttributeType type) {
		this.pickaxe = parent;
		this.name = name;
		this.type = type;
	}
	
	/**
	 * This method should be called whenever the player equips
	 * a pickaxe with this attribute on it.
	 */
	
	public abstract void onEquip();
	
	/**
	 * This method should be called whenever the player dequips
	 * a pickaxe with this attribute on it.
	 */
	
	public abstract void onDequip();
	
	/**
	 * This method is called whenever the player mines with a pickaxe
	 * with this attribute on it.
	 * @param {@link Block} The block that was mined.
	 */
	
	public abstract void onMine(Block block);
	
	/**
	 * This method is called whenever this attribute is to take effect.
	 */
	
	public abstract void activate(Block block);
	
	public String getName() {
		return this.name;
	}
	
	public AttributeType getType() {
		return this.type;
	}
	
	public DragonScalePickaxe getPickaxe() {
		return this.pickaxe;
	}

}
