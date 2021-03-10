package online.coginvasion.spider.scene.card;

import static online.coginvasion.spider.SpiderSolitaire.core;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.scene.PlayScene;
import online.coginvasion.spider.scene.SceneManager;

public class EmptySlot extends Actor {
	
	final TableColumn column;
	private boolean active;
	
	public EmptySlot(final TableColumn column) {
		this.column = column;
		this.active = false;
		
		int y = (int) (core.getHeight() - (Globals.CARD_HEIGHT_RATIO.get() + 
				Globals.VBAR_OFFSET_RATIO.get()));
		this.setBounds(column.getX(), y, Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
		
		this.addListener(new InputListener() {
			public boolean touchDown(InputEvent evt, float x, float y, int pointer, int button) {
				SceneManager sm = column.getGame().getSceneManager();
				if(((PlayScene) sm.getCurrentScene()).isInputEnabled()) {
					ArrayList<Card> activeCards = column.getInventory().getActiveCards();
					
					if(activeCards.size() > 0) {
						for(Card c : activeCards) {
							c.getTableColumn().moveCardTo(c, column);
						}
						
						column.getInventory().resetActiveCards(false);
						EmptySlot me = ((EmptySlot) evt.getTarget());
						me.setActive(false);
					}
					
				}
				
				return true;
			}
		});
	}
	
	public void draw(Batch batch, float alpha) {
		if(active) {
			TextureRegion image = column.getGame().getAssetManager().getCardBack();
			Color c = batch.getColor();
			batch.setColor(1.0f, 1.0f, 1.0f, 0.1f);

			int y = (int) (core.getHeight() - (Globals.CARD_HEIGHT_RATIO.get() + 
					Globals.VBAR_OFFSET_RATIO.get()));
			batch.draw(image, getX(), y, Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
			batch.setColor(c);
		}
	}
	
	public void setActive(boolean flag) {
		this.active = flag;
		
		if(active) {
			setTouchable(Touchable.enabled);
			column.getGame().getStage().addActor(this);
		}else {
			setTouchable(Touchable.disabled);
			remove();
		}
		
	}
	
	public boolean isActive() {
		return this.active;
	}

}
