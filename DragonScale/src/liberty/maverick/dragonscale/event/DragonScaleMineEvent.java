package liberty.maverick.dragonscale.event;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;

public class DragonScaleMineEvent extends DragonScaleEvent {
	
	private final Player player;
	private final Block mainBlock;
	
	private final DragonScalePickaxe pickaxe;

	// These are the supplemental blocks mined maybe from
	// some sort of special enchantment, explosion, etc.
	private final HashSet<Block> extraMinedBlocks;
	
	/**
	 * This event occurs upon a successful mine event.
	 * @param {@link Block} The block that the player actually destroyed.
	 * @param {@link HashSet<Block>} The supplemental blocks that were also destroyed possibly from
	 * a pickaxe effect or enchantment.
	 */
	
	public DragonScaleMineEvent(final DragonScalePickaxe pickaxe, Block mainBlock, HashSet<Block> extraBlocks, Player player) {
		this.player = player;
		this.mainBlock = mainBlock;
		this.pickaxe = pickaxe;
		this.extraMinedBlocks = extraBlocks;
	}
	
	/**
	 * Fetches the {@link DragonScalePickaxe} instance that produced this event.
	 * @return A DragonScalePickaxe instance associated with the player provided to this event.
	 */
	
	public DragonScalePickaxe getPickaxe() {
		return this.pickaxe;
	}
	
	/**
	 * Fetches all the blocks involved in this event.
	 * Includes the block the player actually mined. (The initial block passed by the {@link BlockBreakEvent}).
	 * @return {@link HashSet<Block>}
	 */
	
	public HashSet<Block> getMinedBlocks() {
		HashSet<Block> totalDestroyed = new HashSet<Block>();
		totalDestroyed.add(mainBlock);
		totalDestroyed.addAll(extraMinedBlocks);
		
		return totalDestroyed;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Block getBlock() {
		return this.mainBlock;
	}
	
}
