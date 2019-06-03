package liberty.maverick.checkers.object;
import java.awt.Graphics2D;

import liberty.maverick.checkers.Board;
import liberty.maverick.checkers.Tile;

/**
 * Checkers!
 * GamePiece.java
 * 
 * @author (Maverick Liberty)
 * @version (September 14, 2016)
 */

public abstract class GamePiece extends GameObject {
    
    protected Tile container;
    
    public GamePiece(Board board) {
        super(board);
    }
    
    public GamePiece(Board board, int x, int y) {
        super(board);
        this.setPosition(x, y);
    }
    
    public void setTile(Tile tile) {
        this.container = tile;
        if(container != null && !container.getChildren().contains(this)) container.addChild(this);
    }
    
    public Tile getTile() {
        return this.container;
    }

	@Override
	public abstract void tick();

	@Override
	public abstract void render(Graphics2D g);
}