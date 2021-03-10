package online.coginvasion.spider.scene.card;

import static online.coginvasion.spider.SpiderSolitaire.core;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderAssetManager.SFX;
import online.coginvasion.spider.SpiderSolitaire;

public class TableColumn {
	
	final Stage stage;
	private final SpiderSolitaire game;
	private final TableInventory inventory;
	private int xPosition;
	private final ArrayList<Card> cards;
	private final EmptySlot emptySlot;
	
	public TableColumn(SpiderSolitaire game, TableInventory inventory, int xPosition) {
		this.stage = game.getStage();
		this.game = game;
		this.inventory = inventory;
		this.xPosition = xPosition;
		this.cards = new ArrayList<Card>();
		this.emptySlot = new EmptySlot(this);
	}
	
	public void addCard(Card card) {
		int i = cards.size();
		int y = (int) (core.getHeight() - (Globals.CARD_HEIGHT_RATIO.get() + 
				Globals.VBAR_OFFSET_RATIO.get() + (Globals.CARD_STACK_OFFSET_RATIO.get() * i)));
		
		card.setPosition(xPosition, y);
		card.setTableColumn(this);
		cards.add(card);
		update();
	}
	
	public void correctCardPositioning(Card card) {
		int i = cards.indexOf(card);
		card.setPosition(xPosition, getY(i));
	}
	
	public void removeCard(Card card) {
		cards.remove(card);
		update();
	}
	
	public int getY(int i) {
		return (int) (core.getHeight() - (Globals.CARD_HEIGHT_RATIO.get() + 
				Globals.VBAR_OFFSET_RATIO.get() + (Globals.CARD_STACK_OFFSET_RATIO.get() * i)));
	}
	
	public void acceptDrawCard(final Card card, float animBeginDelay) {
		card.setTableColumn(this);
		card.setFaceUp(false);
		stage.addActor(card);
		Timer.schedule(new Task() {
			
			public void run() {
				int i = cards.size();
				int y = getY(i);
				
				Globals.playSfx(SFX.DEAL_CARD);
				card.addAction(Actions.moveTo(xPosition, y, Globals.CARD_DEAL_TIME));
				
				Timer.schedule(new Task() {
					
					public void run() {
						card.setFaceUp(true);
					}
					
				}, (Globals.CARD_DEAL_TIME * 0.4f));
				
				Timer.schedule(new Task() {
					
					public void run() {
						addCard(card);
					}
					
				}, (Globals.CARD_DEAL_TIME * 2f));
			}
			
		}, animBeginDelay);
	}
	
	public void moveCardTo(Card card, TableColumn column) {
		this.removeCard(card);
		
		// Let's move this card to the new column.
		column.addCard(card);
		
		if(cards.size() > 0) {
			// Let's flip the new lowest card in this column.
			Card lowestCard = cards.get(cards.size() - 1);
			if(!lowestCard.isFaceUp()) lowestCard.setFaceUp(true);
		}
		
	}
	
	public void update() {
		for(Card c : cards) {
			c.remove();
			correctCardPositioning(c);
			stage.addActor(c);
		}
		
		if(cards.size() == 0) {
			emptySlot.setActive(true);
		}
	}
	
	public ArrayList<Card> getCards() {
		return this.cards;
	}
	
	public SpiderSolitaire getGame() {
		return this.game;
	}
	
	public TableInventory getInventory() {
		return this.inventory;
	}
	
	public void setX(int newX) {
		this.xPosition = newX;
	}
	
	public int getX() {
		return this.xPosition;
	}
	
	public void render(SpriteBatch batch) {
		
	}
	
	public void update(float dt) {
		
	}
	
	public void dispose() {
		cards.clear();
	}

}
