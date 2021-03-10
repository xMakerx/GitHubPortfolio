package liberty.maverick.dragonscale;

import org.bukkit.Location;
import org.bukkit.Sound;

public class DragonScaleSound {
	
	final Sound mcSound;
	final float volume;
	final float pitch;
	
	/**
	 * This is a container that holds information relating to Sound
	 * information that was loaded from the config file.
	 * @param sound - A Minecraft Sound enum.
	 * @param volume - The volume of the sound. (0.0-1.0)
	 * @param pitch - The pitch of the sound.
	 */
	
	public DragonScaleSound(Sound sound, float volume, float pitch) {
		this.mcSound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	/**
	 * Plays this sound at the specified {@link Location}
	 * @param location - The Location to play this sound at.
	 */
	
	public void play(final Location location) {
		if(location == null) throw new NullPointerException("Tried to play a sound at null Location!");
		
		location.getWorld().playSound(location, mcSound, volume, pitch);
	}
	
	/**
	 * Fetches the MC {@link Sound} object.
	 * @return
	 */
	
	public Sound getMCSound() {
		return this.mcSound;
	}
	
	/**
	 * Fetches the volume of this sound.
	 * @return
	 */
	
	public float getVolume() {
		return this.volume;
	}
	
	/**
	 * Fetches the pitch of this sound.
	 * @return
	 */
	
	public float getPitch() {
		return this.pitch;
	}

}
