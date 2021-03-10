package online.coginvasion.spider.scene;

import java.util.EmptyStackException;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import online.coginvasion.spider.Globals;
import online.coginvasion.spider.SpiderSolitaire;

public class SceneManager {
	
	final SpiderSolitaire game;
	private final Stack<Scene> scenes;
	
	// The main app BGM
	private Music bgm;
	
	public SceneManager(SpiderSolitaire main) {
		this.game = main;
		this.scenes = new Stack<Scene>();
		this.bgm = null;
		setScene(new LoadScene(main, this));
	}
	
	/**
	 * The initial LoadScene should call the following method.
	 */
	
	public void finishedInitialLoad() {
		this.bgm = game.getAssetManager().get("audio/Samba Isobel.mp3", Music.class);
		this.bgm.setLooping(true);
		this.bgm.setVolume(Globals.BGM_VOLUME);
		if(Globals.PLAY_BGM) this.bgm.play();
		
		setScene(new PlayScene(game, this));
	}
	
	/**
	 * Renders the current scene.
	 * @param SpriteBatch
	 */
	
	public void render(SpriteBatch batch) {
		
		batch.end();
		ShapeRenderer sr = new ShapeRenderer();
		sr.begin(ShapeType.Filled);
		sr.setColor(0, 102f/255, 0, 1);
		sr.box(0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 4);
		sr.end();
		
		batch.begin();
		
		getCurrentScene().render(batch);
	}
	
	/**
	 * Updates the current scene.
	 * @param delta time.
	 */
	
	public void update(float dt) {
		getCurrentScene().update(dt);
	}
	
	/**
	 * Sets the current {@link Scene}.
	 * @param The scene that should start.
	 * @return The last scene at the top of the stack.
	 */
	
	public Scene setScene(Scene scene) {
		Scene curScene = getCurrentScene();
		
		// Let's push this scene to the top of the stack
		// and call #enter().
		this.scenes.push(scene);
		scene.enter();

		return curScene;
	}
	
	public Scene getCurrentScene() {
		try {
			return this.scenes.peek();
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	public Stack<Scene> getScenes() {
		return this.scenes;
	}
	
	public void dispose() {
		for(Scene s : scenes) {
			s.dispose();
		}
		
		bgm = null;
		scenes.clear();
	}
	
}
