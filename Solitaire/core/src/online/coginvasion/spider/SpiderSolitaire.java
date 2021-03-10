package online.coginvasion.spider;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import online.coginvasion.spider.scene.PlayScene;
import online.coginvasion.spider.scene.Scene;
import online.coginvasion.spider.scene.SceneManager;

public class SpiderSolitaire extends ApplicationAdapter {
	
	public static SpiderSolitaire core;
	
	static {
		core = null;
	}
	
	private SpiderAssetManager assetMgr;
	private SceneManager sceneMgr;
	private SpriteBatch batch;
	private Stage stage;
	
	public void create() {
		if(SpiderSolitaire.core != null) return;
		
		SpiderSolitaire.core = this;
		this.assetMgr = new SpiderAssetManager();
		this.sceneMgr = new SceneManager(this);
		this.batch = new SpriteBatch();
		this.stage = new Stage(new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), 
				Gdx.graphics.getHeight()), batch);
		Gdx.input.setInputProcessor(stage);
	}
	
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		
		Scene curScene = sceneMgr.getCurrentScene();
		if(curScene instanceof PlayScene) {
			((PlayScene) curScene).getTableInventory().resize();
		}
	}
	
	public void render() {
		// Let's call the update method with our delta time.
		update(Gdx.graphics.getDeltaTime());
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		sceneMgr.render(batch);
		batch.end();
		
		stage.draw();
	}
	
	public void update(float dt) {
		sceneMgr.update(dt);
		stage.act(dt);
	}
	
	public int getWidth() {
		return (int) this.stage.getViewport().getWorldWidth();
	}
	
	public int getHeight() {
		return (int) this.stage.getViewport().getWorldHeight();
	}
	
	public SpiderAssetManager getAssetManager() {
		return this.assetMgr;
	}
	
	public SceneManager getSceneManager() {
		return this.sceneMgr;
	}
	
	public Stage getStage() {
		return this.stage;
	}
	
	public void dispose() {
		assetMgr.dispose();
		sceneMgr.dispose();
		stage.dispose();
		batch.dispose();
	}
	
}
