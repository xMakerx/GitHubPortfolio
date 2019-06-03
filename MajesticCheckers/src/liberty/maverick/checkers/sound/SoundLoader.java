package liberty.maverick.checkers.sound;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundLoader {
	private static ArrayList<Sound> sounds = new ArrayList<Sound>();
	public boolean soundsLoaded = false;
	
	public SoundLoader() {
		sounds.add(new Sound("BGM", "resources/sound/bgm.wav"));
		sounds.add(new Sound("Drop", "resources/sound/drop.wav"));
		sounds.add(new Sound("MouseOver", "resources/sound/mouseOver.wav"));
		sounds.add(new Sound("MouseClick", "resources/sound/mouseClick.wav"));
		sounds.add(new Sound("Queened", "resources/sound/queened.wav"));
		sounds.add(new Sound("Tick", "resources/sound/tick.wav"));
		sounds.add(new Sound("TimeUp", "resources/sound/timeUp.wav"));
		sounds.add(new Sound("Wednesday", "resources/sound/Wednesday.wav"));
		sounds.add(new Sound("Fight", "resources/sound/fight.wav"));
		loadSounds();
	}
	
	public void addSound(Sound sound) {
		SoundLoader.sounds.add(sound);
	}
	
	public static ArrayList<Sound> getSounds() {
		return SoundLoader.sounds;
	}
	
	protected void loadSounds() {
		for(int i = 0; i < SoundLoader.sounds.size(); i++) {
			Sound sound = SoundLoader.sounds.get(i);
			try {
				AudioInputStream loadAttempt = AudioSystem.getAudioInputStream(ClassLoader.getSystemResource(sound.getSource()));
				if(loadAttempt == null) return;
				sound.setSound(loadAttempt);
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			}
		}
		this.soundsLoaded = true;
	}
	
	public static Sound getSoundByName(String name) {
		for(int i = 0; i < SoundLoader.sounds.size(); i++) {
			Sound sound = SoundLoader.sounds.get(i);
			if(sound.getName().equalsIgnoreCase(name)) {
				return sound;
			}
		}
		return null;
	}
	
	public static void playSoundByName(String name) {
		for(int i = 0; i < SoundLoader.sounds.size(); i++) {
			Sound sound = SoundLoader.sounds.get(i);
			if(sound.getName().equalsIgnoreCase(name)) {
				sound.play();
			}
		}
	}
}