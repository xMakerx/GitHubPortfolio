package online.coginvasion.spider.scene.card;

import static online.coginvasion.spider.SpiderSolitaire.core;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderAssetManager.SFX;
import online.coginvasion.spider.scene.PlayScene;
import online.coginvasion.spider.scene.SceneManager;

public class Card extends Actor {
	
	private final Cards type;
	private TableColumn column;
	private boolean faceUp;
	private long pickupCooldown;
	
	public Card(Cards cardType) {
		super();
		this.setBounds(0, 0, Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
		this.type = cardType;
		this.column = null;
		this.faceUp = true;
		this.pickupCooldown = System.currentTimeMillis();
	}
	
	public Card(Cards cardType, boolean faceUp) {
		super();
		this.setBounds(0, 0, Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
		this.type = cardType;
		this.faceUp = faceUp;
		this.column = null;
		this.pickupCooldown = System.currentTimeMillis();
	}
	
	public void setTableColumn(TableColumn column) {
		this.column = column;
		this.setupInput();
	}
	
	public TableColumn getTableColumn() {
		return this.column;
	}
	
	private void setupInput() {
		this.addListener(new InputListener() {
			public boolean touchDown(InputEvent evt, float x, float y, int pointer, int button) {
				SceneManager sm = column.getGame().getSceneManager();
				if(faceUp && ((PlayScene) sm.getCurrentScene()).isInputEnabled()) {
					Card me = ((Card) evt.getTarget());
					
					if(System.currentTimeMillis() - me.pickupCooldown <= 1000) {
						return true;
					}
					
					ArrayList<Card> activeCards = column.getInventory().getActiveCards();
					
					if(activeCards.size() == 0) {
						ArrayList<Card> cards = column.getCards();
						int myIndex = cards.indexOf(me);
						
						// Handle if we're trying to pickup the last card in the column.
						if(myIndex == (cards.size() - 1)) {
							// Pickup this card
							me.setTouchable(Touchable.disabled);
							column.getInventory().setActiveCard(me);
						}else {
							// We're trying to move a card that's in a different position in the column.
							int checkValue = me.getData().getNumericValue();
							
							ArrayList<Card> moveCards = new ArrayList<Card>();
							moveCards.add(me);
							
							for(int i = myIndex+1; i < cards.size(); i++) {
								// Let's iterate through the cards below us to see if this card is moveable.
								Card checkCard = cards.get(i);
								int value = checkCard.getData().getNumericValue();
								Suit s = checkCard.getData().getSuit();
								
								// If the suit of the card we're checking doesn't match the suit of us
								// and the numeric value isn't one less than the check value, then
								// this movement cannot be complete.
								if(value != (checkValue - 1) || s != me.getData().getSuit()) {
									Globals.playSfx(SFX.ERROR);
									return true;
								}
								
								// We're able to move this card!
								// Add it to move cards and decrease the check value.
								checkValue--;
								moveCards.add(checkCard);
							}
							
							// Set the cards that are active.
							column.getInventory().setActiveCards(moveCards);
						}
					}else {
						TableColumn tc = me.getTableColumn();
						
						if(tc.getCards().size() > 0) {
							Card lowestCard = tc.getCards().get(tc.getCards().size()-1);
							Card highestCard = column.getInventory().getActiveCards().get(0);
							
							Cards highestCardData = highestCard.getData();
							int numericValue = highestCardData.getNumericValue();
							
							boolean sameSuits = highestCard.getData().getSuit() == lowestCard.getData().getSuit();
	
							if(lowestCard.getData().getNumericValue() == (numericValue + 1) && sameSuits) {
								for(int i = 0; i < activeCards.size(); i++) {
									Card c = activeCards.get(i);
									c.getTableColumn().moveCardTo(c, tc);
									
									ArrayList<Card> setCards = new ArrayList<Card>();
									
									if(i == (activeCards.size() - 1)) {
										int checkValue = 0;
										int outsideSetCard = -1;
										
										for(int t = tc.getCards().size() - 1; t >= 0; t--) {
											Card testCard = tc.getCards().get(t);
											if(!(testCard.getData().getNumericValue() == (checkValue + 1))) {
												outsideSetCard = t;
												break;
											}
											
											checkValue++;
											setCards.add(testCard);
										}
										
										if(setCards.size() == 13) {
											for(Card sc : setCards) {
												sc.getTableColumn().removeCard(sc);
												sc.remove();
											}
											
											if(outsideSetCard != -1) {
												Card osCard = tc.getCards().get(outsideSetCard);
												
												if(!osCard.isFaceUp()) osCard.setFaceUp(true);
											}
	
											//column.getGame().getAssetManager().get("audio/yeahboi.mp3", Sound.class).play(Globals.SFX_VOLUME);
											
										}
									}
								}
								
								Globals.playSfx(SFX.CARD_PLACE);
								column.getInventory().resetActiveCards(false);
							}else {
								column.getInventory().resetActiveCards(!(highestCard == me));
							}
						}
					}
					
					startCooldown();
					
				}
				
				return true;
			}
		});
		
		if(faceUp) this.setTouchable(Touchable.enabled);
	}
	
	public void startCooldown() {
		pickupCooldown = System.currentTimeMillis();
	}
	
	public void draw(Batch batch, float alpha) {
		TextureRegion image = type.getImage();
		if(!faceUp) {
			image = type.getBackImage();
		}
		
		// Correct our width & height if necessary
		if(this.getWidth() != Globals.CARD_WIDTH_RATIO.get()) {
			this.setSize(Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
		}
		
		batch.draw(image, this.getX(), this.getY(), getWidth(), getHeight());
	}
	
	public void act(float delta) {
		ArrayList<Card> activeCards = column.getInventory().getActiveCards();
		if(activeCards.contains(this)) {
			int myIndex = activeCards.indexOf(this);
			if(myIndex == 0) {
				Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
				core.getStage().getViewport().unproject(mousePos);
				this.setPosition(mousePos.x, mousePos.y - Globals.CARD_HEIGHT_RATIO.get());
			}else if(activeCards.size() > 0) {
				Card highestCard = activeCards.get(0);
				float x = highestCard.getX();
				float y = highestCard.getY();
				this.setPosition(x, y - (Globals.CARD_STACK_OFFSET_RATIO.get() * myIndex));
			}
		}
		
		super.act(delta);
	}
	
	public Cards getData() {
		return this.type;
	}
	
	public void setFaceUp(boolean faceUp) {
		this.faceUp = faceUp;
		if(faceUp) {
			this.setTouchable(Touchable.enabled);
			if(column != null) Globals.playSfx(SFX.CARD_FLIP);
		}
	}
	
	public boolean isFaceUp() {
		return this.faceUp;
	}
	
}
