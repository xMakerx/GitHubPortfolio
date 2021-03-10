package online.coginvasion.spider.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import online.coginvasion.spider.SpiderAssetManager;
import online.coginvasion.spider.SpiderSolitaire;
import online.coginvasion.spider.scene.card.TableInventory;

public class PlayScene extends Scene {
	
	final SpiderAssetManager assetMgr;
	private final TableInventory tableInv;
	
	private boolean inputEnabled;
	
	public PlayScene(SpiderSolitaire main, SceneManager sceneMgr) {
		super(main, sceneMgr);
		this.assetMgr = main.getAssetManager();
		this.tableInv = new TableInventory(main);
		this.inputEnabled = true;
	}
	
	public TableInventory getTableInventory() {
		return this.tableInv;
	}
	
	public void setInputEnabled(boolean flag) {
		this.inputEnabled = flag;
	}
	
	public boolean isInputEnabled() {
		return this.inputEnabled;
	}

	@Override
	public void enter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(SpriteBatch batch) {
		this.tableInv.render(batch);
	}

	@Override
	public void update(float dt) {
		this.tableInv.update(dt);
	}

	@Override
	public void dispose() {
		this.tableInv.dispose();
	}

}
