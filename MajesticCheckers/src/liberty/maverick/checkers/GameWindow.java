package liberty.maverick.checkers;
/**
 * Checkers!
 * GameWindow.java
 * 
 * @author (Maverick Liberty)
 * @version (September 12, 2016)
 */

import java.awt.Canvas;

import javax.swing.JFrame;

public class GameWindow extends Canvas {

	final Checkers instance;
	private final JFrame frame;
	private static final long serialVersionUID = -8691452716947648701L;
    
    public GameWindow(Checkers instance) {
    	this.instance = instance;
    	this.frame = new JFrame();
        this.frame.setTitle(Globals.GAME_TITLE);
        this.frame.setSize(Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        this.frame.setVisible(true);
        this.frame.add(instance);
        this.frame.setIgnoreRepaint(true);
        this.instance.start();
    }
}
