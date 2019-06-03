package liberty.maverick.checkers;
/**
 * Checkers!
 * Globals.java
 * 
 * @author (Maverick Liberty)
 * @version (September 12, 2016)
 */

import java.awt.Color;

public class Globals {

    public final static Color BOARD_PRIMARY = new Color(255, 178, 102);
    public final static Color BOARD_SECONDARY = Color.WHITE;
    public final static Color SELECTION_COLOR = Color.YELLOW;
    public final static Color GHOST_PRIMARY = new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), Color.WHITE.getAlpha() - 100);
    public final static Color GHOST_SECONDARY = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), Color.GRAY.getAlpha() - 100);
    public final static String GAME_TITLE = "Majestic Checkers";
    public final static int BOARD_WIDTH = 400;
    public final static int BOARD_HEIGHT = 400;
    public final static int GAME_WIDTH = 600;
    public final static int GAME_HEIGHT = 600;
    public final static int BOARD_OFFSETX = 100;
    public final static int BOARD_OFFSETY = 80;
    public final static int TILE_SIZEX = 40;
    public final static int TILE_SIZEY = 40;
    public final static int SELECTION_WIDTH = 2;
    public final static int TURN_DURATION = 15;
    public final static boolean DEBUG = false;
    
}