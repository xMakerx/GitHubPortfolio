package online.coginvasion.spider;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpiderAssetManager extends AssetManager {
	
	public enum SFX {
		ERROR("audio/error.ogg"),
		CLICK("audio/click.ogg"),
		CARD_FLIP("audio/cardflip.ogg"),
		CARD_PLACE("audio/cardplace.ogg"),
		DEAL_CARD("audio/dealcard.ogg");
		
		private String path;
		
		private SFX(String sfxPath) {
			this.path = sfxPath;
		}
		
		public String getPath() {
			return this.path;
		}
	}
	
	private TextureRegion cardBack;
	
	public SpiderAssetManager() {
		super();
		this.cardBack = null;
	}
	
	/**
	 * Queues the following assets for preloading.
	 * These assets must be loaded so we can do a loading bar.
	 */
	
	public void queueRequiredAssets() {
		
		// Load sfxs
		for(SFX sfx : SFX.values()) load(sfx.getPath(), Sound.class);
		
		this.load("audio/Samba Isobel.mp3", Music.class);
		this.load("audio/yeahboi.mp3", Sound.class);
		this.load("cardbg.png", Texture.class);
		this.load("playingCards.png", Texture.class);
		this.load("playingCardBacks.png", Texture.class);
	}
	
	public Texture getTexture(String fileName) {
		return this.get(fileName, Texture.class);
	}
	
	public Sound getSound(String fileName) {
		return this.get(fileName, Sound.class);
	}
	
	public TextureRegion getCardBack() {
		if(cardBack == null) {
			cardBack = new TextureRegion(this.get("playingCardBacks.png", Texture.class),
					Globals.TEXTURE_CARD_WIDTH * 2,
					Globals.TEXTURE_CARD_HEIGHT * 1, 
					Globals.TEXTURE_CARD_WIDTH, 
			Globals.TEXTURE_CARD_HEIGHT);
		}
		
		return cardBack;
	}

}
