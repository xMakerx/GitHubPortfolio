package liberty.maverick.checkers.sound;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound {
	private AudioInputStream sound;
	private String name;
	private String src;
	private Clip clip;
	private boolean setup = false;
	Clip oldClip;
	
	public Sound(String name, String src) {
		this.name = name;
		this.src = src;
		this.oldClip = null;
		this.setup = false;
	}
	
	public void setupSound() {
		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		try {
			clip.open(getSound());
			setup = true;
		} catch (LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void play() {
		if(setup && clip.isOpen()) {
			clip.start();
			oldClip = clip;
			reset();
		}else {
			return;
		}
	}
	
	public void loop() {
		if(setup && clip.isOpen()) {
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			oldClip = clip;
			reset();
		}else {
			return;
		}
	}
	
	public void stop() {
		oldClip.stop();
		oldClip = null;
	}
	
	public void reset() {
		sound = null;
		try {
			AudioInputStream loadAttempt = AudioSystem.getAudioInputStream(ClassLoader.getSystemResource(getSource()));
			if(loadAttempt == null) return;
			setSound(loadAttempt);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setSource(String src) {
		this.src = src;
	}
	
	public String getSource() {
		return this.src;
	}
	
	public void setSound(AudioInputStream sound) {
		this.sound = sound;
		setupSound();
	}
	
	public AudioInputStream getSound() {
		return this.sound;
	}
}
