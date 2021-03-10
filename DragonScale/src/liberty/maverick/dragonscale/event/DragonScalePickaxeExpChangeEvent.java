package liberty.maverick.dragonscale.event;

import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;

public class DragonScalePickaxeExpChangeEvent extends DragonScaleEvent {
	
	final DragonScalePickaxe pickaxe;
	final int lastExp;
	final int newExp;
	
	/**
	 * Called when the exp on a {@link DragonScalePickaxe} changes.
	 * @param pickaxe - The {@link DragonScalePickaxe} instance.
	 * @param lastExp - The amount of experience before the event was called.
	 * @param newExp - The amount of experience after the event was called.
	 */
	
	public DragonScalePickaxeExpChangeEvent(final DragonScalePickaxe pickaxe, 
			final int lastExp, final int newExp) {
		this.pickaxe = pickaxe;
		this.lastExp = lastExp;
		this.newExp = newExp;
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
	 * Fetches the previous experience of the pickaxe before this event.
	 * @return int
	 */
	
	public int getLastExp() {
		return this.lastExp;
	}
	
	/**
	 * Fetches the new/current experience that the pickaxe has changed to.
	 * @return int
	 */
	
	public int getNewExp() {
		return this.newExp;
	}

}
