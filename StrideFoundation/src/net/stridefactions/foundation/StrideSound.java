package net.stridefactions.foundation;

import org.bukkit.Sound;

public enum StrideSound {
	GUI_OPEN,
	MONEY_SENT,
    POT_FULL;
	
	private Sound sound;
	
	public void setSound(final String soundName) {
		final StrideFoundation instance = StrideFoundation.getInstance();
		
		try {
			this.sound = Sound.valueOf(soundName);
		}catch (NullPointerException | IllegalArgumentException e) {
			instance.getLogger().info(String.format("Sound %s does not exist.", soundName));
		}
	}
	
	public Sound getSound() {
		return this.sound;
	}
	
	public static StrideSound getSoundByName(final String name) {
		final StrideFoundation instance = StrideFoundation.getInstance();
		
		try {
			return StrideSound.valueOf(name);
		}catch (NullPointerException | IllegalArgumentException e) {
			instance.getLogger().severe(String.format("Failed to obtain StrideSound %s.", name));
			return null;
		}
	}
}
