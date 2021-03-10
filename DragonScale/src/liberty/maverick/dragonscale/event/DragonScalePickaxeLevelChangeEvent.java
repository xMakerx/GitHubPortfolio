package liberty.maverick.dragonscale.event;

import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;

public class DragonScalePickaxeLevelChangeEvent extends DragonScaleEvent {
	
	final DragonScalePickaxe pickaxe;
	final int lastLevel;
	final int newLevel;
	
	/**
	 * Called when the level on a {@link DragonScalePickaxe} changes.
	 * @param pickaxe - The {@link DragonScalePickaxe} instance.
	 * @param lastExp - The level before the event was called.
	 * @param newExp - The level after the event was called.
	 */
	
	public DragonScalePickaxeLevelChangeEvent(final DragonScalePickaxe pickaxe,
			final int lastLevel, final int newLevel) {
		this.pickaxe = pickaxe;
		this.lastLevel = lastLevel;
		this.newLevel = newLevel;
	}
	
	/**
	 * Fetches the {@link DragonScalePickaxe} instance associated with this
	 * event.
	 * @return DragonScalePickaxe instance
	 */
	
	public DragonScalePickaxe getPickaxe() {
		return this.pickaxe;
	}
	
	/**
	 * Fetches the previous level of the pickaxe before this event.
	 * @return int
	 */
	
	public int getLastLevel() {
		return this.lastLevel;
	}
	
	/**
	 * Fetches the new/current level that the pickaxe has changed to.
	 * @return int
	 */
	
	public int getNewLevel() {
		return this.newLevel;
	}

}
