package liberty.maverick.dragonscale.pickaxe;

import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.util.DragonUtils;

public class PickaxePotionEffect extends PickaxeAttribute {
	
	private PotionEffectType potionEffect;
	private int amplifier;
	
	public PickaxePotionEffect(final DragonScalePickaxe parent, String potionEffectName, int amplifier) {
		super(parent, DragonUtils.getPickaxeAttributeTitle(potionEffectName, amplifier), 
				PickaxeAttribute.AttributeType.POTION_EFFECT);
		this.potionEffect = null;
		this.amplifier = amplifier;
		
		try {
			potionEffect = PotionEffectType.getByName(potionEffectName.toUpperCase());
		} catch (IllegalArgumentException e) {
			DragonScale.singleton.getSystemLogger().error(String.format("Failed to fetch PotionEffectType \"%s\"!", potionEffectName));
			DragonScale.singleton.disable();
		}
	}
	
	/**
	 * Let's add our potion effect whenever the player equips the pickaxe.
	 */

	public void onEquip() {
		pickaxe.wielder.addPotionEffect(potionEffect.createEffect(Integer.MAX_VALUE, amplifier));
	}
	
	/**
	 * Let's remove our potion effect whenever the player dequips the pickaxe.
	 */
	
	public void onDequip() {
		Iterator<PotionEffect> iterator = pickaxe.wielder.getActivePotionEffects().iterator();
		
		while(iterator.hasNext()) {
			PotionEffect effect = iterator.next();
			if(effect.getType() == potionEffect && effect.getAmplifier() == amplifier) {
				pickaxe.wielder.removePotionEffect(effect.getType());
			}
		}
	}

	public void onMine(Block block) {
		return;
	}

	@Override
	public void activate(Block block) {
		return;
	}
	
	/**
	 * Fetches the {@link PotionEffectType} of this potion effect.
	 * @return Returns a valid PotionEffectType or null if the specified one wasn't found.
	 */
	
	public PotionEffectType getPotionEffect() {
		return this.potionEffect;
	}
	
	/**
	 * Fetches the amplifier of this potion effect.
	 * @return int
	 */
	
	public int getAmplifier() {
		return this.amplifier;
	}

}
