package online.coginvasion.spider.scene.card;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderAssetManager;

public enum Cards {
	QUEEN_OF_SPADES(Suit.SPADES, 12, 0, 0),
	FOUR_OF_SPADES(Suit.SPADES, 4, 1, 0),
	EIGHT_OF_HEARTS(Suit.HEARTS, 8, 2, 0),
	ACE_OF_DIAMONDS(Suit.DIAMONDS, 1, 3, 0),
	QUEEN_OF_CLUBS(Suit.CLUBS, 12, 4, 0),
	FOUR_OF_CLUBS(Suit.CLUBS, 4, 5, 0),
	KING_OF_SPADES(Suit.SPADES, 13, 0, 1),
	THREE_OF_SPADES(Suit.SPADES, 3, 1, 1),
	SEVEN_OF_HEARTS(Suit.HEARTS, 7, 2, 1),
	TEN_OF_DIAMONDS(Suit.DIAMONDS, 10, 3, 1),
	KING_OF_CLUBS(Suit.CLUBS, 13, 4, 1),
	THREE_OF_CLUBS(Suit.CLUBS, 3, 5, 1),
	JACK_OF_SPADES(Suit.SPADES, 11, 0, 2),
	TWO_OF_SPADES(Suit.SPADES, 2, 1, 2),
	SIX_OF_HEARTS(Suit.HEARTS, 6, 2, 2),
	NINE_OF_DIAMONDS(Suit.DIAMONDS, 9, 3, 2),
	JACK_OF_CLUBS(Suit.CLUBS, 11, 4, 2),
	TWO_OF_HEARTS(Suit.HEARTS, 2, 5, 2),
	ACE_OF_SPADES(Suit.SPADES, 1, 0, 3),
	FIVE_OF_HEARTS(Suit.HEARTS, 5, 2, 3),
	EIGHT_OF_DIAMONDS(Suit.DIAMONDS, 8, 3, 3),
	ACE_OF_CLUBS(Suit.CLUBS, 1, 4, 3),
	TEN_OF_SPADES(Suit.SPADES, 10, 0, 4),
	QUEEN_OF_HEARTS(Suit.HEARTS, 12, 1, 4),
	FOUR_OF_HEARTS(Suit.HEARTS, 4, 2, 4),
	SEVEN_OF_DIAMONDS(Suit.DIAMONDS, 7, 3, 4),
	TEN_OF_CLUBS(Suit.CLUBS, 10, 4, 4),
	NINE_OF_SPADES(Suit.SPADES, 9, 0, 5),
	KING_OF_HEARTS(Suit.HEARTS, 13, 1, 5),
	THREE_OF_HEARTS(Suit.HEARTS, 3, 2, 5),
	SIX_OF_DIAMONDS(Suit.DIAMONDS, 6, 3, 5),
	NINE_OF_CLUBS(Suit.CLUBS, 9, 4, 5),
	EIGHT_OF_SPADES(Suit.SPADES, 8, 0, 6),
	JACK_OF_HEARTS(Suit.HEARTS, 11, 1, 6),
	TWO_OF_CLUBS(Suit.CLUBS, 2, 2, 6),
	FIVE_OF_DIAMONDS(Suit.DIAMONDS, 5, 3, 6),
	EIGHT_OF_CLUBS(Suit.CLUBS, 8, 4, 6),
	SEVEN_OF_SPADES(Suit.SPADES, 7, 0, 7),
	ACE_OF_HEARTS(Suit.HEARTS, 1, 1, 7),
	QUEEN_OF_DIAMONDS(Suit.DIAMONDS, 12, 2, 7),
	FOUR_OF_DIAMONDS(Suit.DIAMONDS, 4, 3, 7),
	SEVEN_OF_CLUBS(Suit.CLUBS, 7, 4, 7),
	SIX_OF_SPADES(Suit.SPADES, 6, 0, 8),
	TEN_OF_HEARTS(Suit.HEARTS, 10, 1, 8),
	KING_OF_DIAMONDS(Suit.DIAMONDS, 13, 2, 8),
	THREE_OF_DIAMONDS(Suit.DIAMONDS, 3, 3, 8),
	SIX_OF_CLUBS(Suit.CLUBS, 6, 4, 8),
	FIVE_OF_SPADES(Suit.SPADES, 5, 0, 9),
	NINE_OF_HEARTS(Suit.HEARTS, 9, 1, 9),
	JACK_OF_DIAMONDS(Suit.DIAMONDS, 11, 2, 9),
	TWO_OF_DIAMONDS(Suit.DIAMONDS, 2, 3, 9),
	FIVE_OF_CLUBS(Suit.CLUBS, 5, 4, 9);
	
	public static boolean loadCards(SpiderAssetManager assetMgr) {
		Texture sheet = assetMgr.getTexture("playingCards.png");
		
		for(Cards card : values()) {
			int x = card.getSheetX();
			int y = card.getSheetY();
			
			card.setImage(new TextureRegion(sheet, x * Globals.TEXTURE_CARD_WIDTH, 
					y * Globals.TEXTURE_CARD_HEIGHT, 
			Globals.TEXTURE_CARD_WIDTH, Globals.TEXTURE_CARD_HEIGHT));
			card.setBackImage(assetMgr.getCardBack());
		}
		
		sheet = null;
		return true;
	}
	
	private final Suit suit;
	private final int value;
	
	private TextureRegion image;
	private TextureRegion backImage;
	
	// The coordinates for the TextureRegion.
	private int sheetX, sheetY;
	
	private Cards(Suit suit, int numericValue, int xPos, int yPos) {
		this.suit = suit;
		this.value = numericValue;
		this.image = null;
		this.sheetX = xPos;
		this.sheetY = yPos;
	}
	
	/**
	 * Fetches the {@link Suit} of the card.
	 * @return The suit enum.
	 */
	
	public Suit getSuit() {
		return this.suit;
	}
	
	/**
	 * Fetches the numeric value of a card.
	 * @return An int in the range 1-13.
	 */
	
	public int getNumericValue() {
		return this.value;
	}
	
	/**
	 * Sets the {@link TextureRegion} that represents this card.
	 * NOTE: Called from {@link Card.#loadCards(SpiderAssetManager)}.
	 * @param TextureRegion instance.
	 */
	
	public void setImage(TextureRegion region) {
		this.image = region;
	}
	
	/**
	 * Fetches the {@link TextureRegion} for this card.
	 * @return A valid TextureRegion instance or null.
	 */
	
	public TextureRegion getImage() {
		return this.image;
	}
	
	/**
	 * Sets the {@link TextureRegion} that represents the back of this card.
	 * NOTE: Called from {@link Card.#loadCards(SpiderAssetManager)}.
	 * @param TextureRegion instance.
	 */
	
	public void setBackImage(TextureRegion region) {
		this.backImage = region;
	}
	
	/**
	 * Fetches the {@link TextureRegion} for the back of the card.
	 * @return A valid TextureRegion instance or null.
	 */
	
	public TextureRegion getBackImage() {
		return this.backImage;
	}
	
	/**
	 * Fetches the X-coordinate on the texture sheet.
	 * @return
	 */
	
	public int getSheetX() {
		return this.sheetX;
	}
	
	/**
	 * Fetches the Y-coordinate on the texture sheet.
	 * @return
	 */
	
	public int getSheetY() {
		return this.sheetY;
	}
	
}
