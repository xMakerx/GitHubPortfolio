package online.coginvasion.spider.scene.card;

import java.util.ArrayList;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.scene.PlayScene;
import online.coginvasion.spider.scene.SceneManager;

public class DrawCard extends Actor {
	
	private final ArrayList<Card> cards;
	final TableInventory inventory;
	
	public DrawCard(TableInventory inv, ArrayList<Card> drawCards) {
		this.setBounds(0, 0, Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
		this.inventory = inv;
		this.cards = drawCards;

		this.addListener(new InputListener() {
			public boolean touchDown(InputEvent evt, float x, float y, int pointer, int button) {
				SceneManager sm = inventory.game.getSceneManager();
				final DrawCard me = ((DrawCard) evt.getTarget());
				final PlayScene scene = ((PlayScene) sm.getCurrentScene());
				if(scene.isInputEnabled() && inventory.getActiveCards().size() == 0) {
					int columnIndex = 0;
					scene.setInputEnabled(false);
					for(int i = 0; i < cards.size(); i++) {
						final Card c = cards.get(i);
						c.setPosition(me.getX(), me.getY());

						TableColumn tc = inventory.getTableColumns().get(columnIndex);
						tc.acceptDrawCard(c, (i * Globals.CARD_DEAL_TIME));
						
						if((columnIndex + 1) < inventory.getTableColumns().size()) {
							columnIndex++;
						}else {
							columnIndex = 0;
						}
					}
					
					Timer.schedule(new Task() {
						
						public void run() {
							scene.setInputEnabled(true);
						}
						
					}, cards.size() * Globals.CARD_DEAL_TIME);
					
					inventory.removeDrawCard(me);
				}else {
					inventory.game.getAssetManager().get("audio/error.ogg", Sound.class).play(Globals.SFX_VOLUME);
				}
				
				return true;
			}
		});
	}
	
	public void draw(Batch batch, float alpha) {
		TextureRegion image = inventory.game.getAssetManager().getCardBack();
		batch.draw(image, this.getX(), this.getY(), Globals.CARD_WIDTH_RATIO.get(), Globals.CARD_HEIGHT_RATIO.get());
	}
	
	public ArrayList<Card> getCards() {
		return this.cards;
	}

}
