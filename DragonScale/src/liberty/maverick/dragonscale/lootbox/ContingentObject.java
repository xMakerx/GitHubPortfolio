package liberty.maverick.dragonscale.lootbox;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface ContingentObject {
	
	/**
	 * The display name of this object.
	 * @return String
	 */
	
	public String getDisplayName();
	
	/**
	 * The unformatted lore of this object.
	 * @return List<String>
	 */
	
	public List<String> getLore();
	
	/**
	 * The chance of this object dropping.
	 * @return double (0.0-1.0)
	 */
	
	public double getDropChance();
	
	/**
	 * Returns a physical {@link ItemStack} representation of this object.
	 * @return ItemStack
	 */
	
	public ItemStack generate();
}
