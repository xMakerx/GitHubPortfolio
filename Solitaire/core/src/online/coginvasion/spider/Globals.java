package online.coginvasion.spider;

import static online.coginvasion.spider.SpiderSolitaire.core;

import online.coginvasion.spider.SpiderAssetManager.SFX;

public class Globals {
	
	// Options
	public static boolean PLAY_BGM = false;
	public static float BGM_VOLUME = 0.6f;
	
	public static boolean PLAY_SFX = true;
	public static float SFX_VOLUME = 0.8f;
	
	public static int DECK_SIZE = 104;
	public static int SUIT_SIZE = 13;
	public static int NUM_COLUMNS = 8;
	public static int STARTING_CARD_AMOUNT = 40;
	
	public static Ratio CARD_ROW_OFFSET = new Ratio(0.01875f, true);
	public static Ratio ROW_START_OFFSET = new Ratio(0.0375f, true);
	
	public static Ratio CARD_WIDTH_RATIO = new Ratio(0.0984375f, true);
	public static Ratio CARD_HEIGHT_RATIO = new Ratio(0.179166667f, false);
	public static Ratio VBAR_OFFSET_RATIO = new Ratio(0.0468f, false);
	public static Ratio CARD_STACK_OFFSET_RATIO = new Ratio(0.03125f, false);
	
	public static int TEXTURE_CARD_WIDTH = 140;
	public static int TEXTURE_CARD_HEIGHT = 190;
	public static float CARD_DEAL_TIME = 0.5f;
	
	public static String[] CARD_NAMES = {
		"ACE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE",
		"TEN", "JACK", "QUEEN", "KING"
	};
	
	public static void playSfx(SFX sfx) {
		if(PLAY_SFX) core.getAssetManager().getSound(sfx.getPath()).play(SFX_VOLUME);
	}
	
}
