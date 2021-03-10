package online.coginvasion.spider.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import online.coginvasion.spider.SpiderSolitaire;

public abstract class Scene {
	
	protected final SpiderSolitaire game;
	protected final SceneManager sceneMgr;
	
	public Scene(SpiderSolitaire main, SceneManager sceneMgr) {
		this.game = main;
		this.sceneMgr = sceneMgr;
	}
	
	public abstract void enter();
	
	public abstract void render(SpriteBatch batch);
	public abstract void update(float dt);
	public abstract void dispose();

}
