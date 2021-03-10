package online.coginvasion.spider.scene.card;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderSolitaire;
import static online.coginvasion.spider.SpiderSolitaire.core;

public class TableInventory {
	
	final SpiderSolitaire game;
	private final ArrayList<TableColumn> columns;
	private final ArrayList<DrawCard> drawCards;
	private final Deck deck;
	
	private volatile ArrayList<Card> activeCards;
	
	public TableInventory(SpiderSolitaire main) {
		this.game = main;
		this.columns = new ArrayList<TableColumn>();
		this.deck = new Deck(1);
		this.activeCards = new ArrayList<Card>();
		this.drawCards = new ArrayList<DrawCard>();
		this.setup();
	}
	
	public void setActiveCards(ArrayList<Card> cards) {
		this.activeCards = cards;
		HashMap<Integer, Card> organizer = new HashMap<Integer, Card>();
		
		for(Card c : cards) {
			c.remove();
			c.startCooldown();
			c.setTouchable(Touchable.disabled);
			organizer.put(c.getData().getNumericValue(), c);
		}
		
		SortedSet<Integer> keys = new TreeSet<Integer>(organizer.keySet());
		
		for(int i = keys.size() - 1; i >= 0; i--) {
			game.getStage().addActor(organizer.get(keys.toArray()[i]));
		}
	}
	
	public void setActiveCard(Card card) {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(card);
		this.setActiveCards(cards);
	}
	
	public void resetActiveCards(boolean playErrorSfx) {
		TableColumn column = null;
		if(activeCards.size() > 0) {
			column = activeCards.get(0).getTableColumn();
			
			if(playErrorSfx) {
				game.getAssetManager().get("audio/error.ogg", Sound.class).play(Globals.SFX_VOLUME);
			}
		}
		
		for(Card c : activeCards) {
			c.setTouchable(Touchable.enabled);
			
			if(c.getX() != c.getTableColumn().getX()) {
				c.getTableColumn().correctCardPositioning(c);
			}
		}
		
		this.activeCards.clear();
		if(column != null) column.update();
	}
	
	public ArrayList<Card> getActiveCards() {
		return this.activeCards;
	}
	
	public void removeDrawCard(DrawCard dc) {
		drawCards.remove(dc);
		dc.remove();
	}
	
	public ArrayList<TableColumn> getTableColumns() {
		return this.columns;
	}
	
	private void setup() {
		int x = (int) Globals.ROW_START_OFFSET.get();
		for(int i = 0; i < Globals.NUM_COLUMNS; i++) {
			columns.add(new TableColumn(game, this, x));
			x += Globals.CARD_WIDTH_RATIO.get() + Globals.CARD_ROW_OFFSET.get();
		}

		int columnIndex = 0;
		for(int i = 0; i < Globals.STARTING_CARD_AMOUNT; i++) {
			boolean faceUp = (i > (Globals.STARTING_CARD_AMOUNT - Globals.NUM_COLUMNS) - 1);
			Card card = deck.takeCard();
			card.setFaceUp(faceUp);
			columns.get(columnIndex).addCard(card);
			
			if(columnIndex == (Globals.NUM_COLUMNS - 1)) {
				columnIndex = 0;
			}else {
				columnIndex++;
			}
		}
		
		for(int i = 0; i < (deck.getCards().size() / Globals.NUM_COLUMNS); i++) {
			ArrayList<Card> cards = new ArrayList<Card>();
			for(int cI = (i * 8); cI < (i * 8) + 8; cI++) {
				cards.add(deck.getCards().get(cI));
			}
			
			DrawCard dc = new DrawCard(this, cards);
			dc.setPosition(core.getWidth() - ((Globals.CARD_WIDTH_RATIO.get()*2) + Globals.CARD_STACK_OFFSET_RATIO.get() * drawCards.size()), 
					(Globals.CARD_HEIGHT_RATIO.get() / 2));
			game.getStage().addActor(dc);
			drawCards.add(dc);
		}
	}
	
	public void render(SpriteBatch batch) {
		for(TableColumn tc : columns) {
			tc.render(batch);
		}
	}
	
	public void resize() {
		int x = (int) Globals.ROW_START_OFFSET.get();
		for(int i = 0; i < columns.size(); i++) {
			// Adjust the column positioning
			TableColumn tc = columns.get(i);
			tc.setX(x);
			tc.update();
			
			x += Globals.CARD_WIDTH_RATIO.get() + Globals.CARD_ROW_OFFSET.get();
		}
		
		int i = 0;
		for(DrawCard dc : drawCards) {
			dc.setPosition(core.getWidth() - ((Globals.CARD_WIDTH_RATIO.get()*2) + Globals.CARD_STACK_OFFSET_RATIO.get() * i), 
					(Globals.CARD_HEIGHT_RATIO.get() / 2));
			dc.setBounds(dc.getX(), dc.getY(), Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
			i++;
		}
	}
	
	public void update(float dt) {
		for(TableColumn tc : columns) {
			tc.update(dt);
		}
	}
	
	public void dispose() {
		for(TableColumn tc : columns) {
			tc.dispose();
		}
	}
	
}
