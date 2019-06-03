package liberty.maverick.checkers.object;
import java.awt.Color;

import liberty.maverick.checkers.Globals;

/**
 * Checkers!
 * Team.java
 * 
 * @author (Maverick Liberty)
 * @version (October 10, 2016)
 */


public enum Team {
	BLACK(Color.DARK_GRAY, Color.BLACK, 0), 
	RED(Color.RED, Color.BLACK, 1),
	GHOST(Globals.GHOST_PRIMARY, Globals.GHOST_SECONDARY, -1);
	
	private final Color primaryColor;
	private final Color secondaryColor;
	private final int movementDirection;
	private int piecesLeft;
	
	private Team(Color primaryColor, Color secondaryColor, int movementDirection) {
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.movementDirection = movementDirection;
		this.piecesLeft = 0;
	}
	
	public void setPiecesLeft(int piecesLeft) {
		this.piecesLeft = piecesLeft;
	}
	
	public int getPiecesLeft() {
		return this.piecesLeft;
	}
	
	public Color getPrimaryColor() {
		return this.primaryColor;
	}
	
	public Color getSecondaryColor() {
		return this.secondaryColor;
	}
	
	public int getMovementDirection() {
		return this.movementDirection;
	}
}
