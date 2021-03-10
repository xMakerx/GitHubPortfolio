package online.coginvasion.spider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderAssetManager;
import online.coginvasion.spider.SpiderSolitaire;
import online.coginvasion.spider.scene.card.Cards;

public class LoadScene extends Scene {
	
	private final SpiderAssetManager assetMgr;
	
	public LoadScene(SpiderSolitaire main, SceneManager sceneMgr) {
		super(main, sceneMgr);
		this.assetMgr = main.getAssetManager();
	}
	
	public void enter() {
		this.assetMgr.queueRequiredAssets();
	}
	
	public void drawCardRow(SpriteBatch batch, TextureRegion image, int y) {
		int x = (int) Globals.ROW_START_OFFSET.get();
		for(int i = 0; i < 8; i++) {
			batch.draw(image, x, y, 
					Globals.CARD_WIDTH_RATIO.get(), 
			Globals.CARD_HEIGHT_RATIO.get());
			
			x += Globals.CARD_WIDTH_RATIO.get() + Globals.CARD_ROW_OFFSET.get();
		}
	}

	@Override
	public void render(SpriteBatch batch) {
		BitmapFont font = new BitmapFont();
		if(assetMgr.update()) {
			Cards.loadCards(assetMgr);
			font.draw(batch, "Done loading!", 
					(Gdx.graphics.getWidth() / 2), (Gdx.graphics.getHeight() / 2));
			sceneMgr.finishedInitialLoad();
		}else {
			float progress = assetMgr.getProgress();
			font.draw(batch, String.format("Loading %2.1f...", progress), 
					(Gdx.graphics.getWidth() / 2), (Gdx.graphics.getHeight() / 2));
		}

	}
	
	public void update(float dt) {}

	public void dispose() {}

}
