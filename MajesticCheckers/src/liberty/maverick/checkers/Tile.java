package liberty.maverick.checkers;
/**
 * Checkers!
 * Tile.java
 * 
 * @author (Maverick Liberty)
 * @version (September 14, 2016)
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;

import liberty.maverick.checkers.object.GameObject;
import liberty.maverick.checkers.object.GamePiece;
import liberty.maverick.checkers.object.PlayPiece;

public class Tile extends GameObject {
    
    private final int boardX, boardY;
    private Color color;
    private HashSet<GameObject> children;
    private boolean selected;
    
    public Tile(Board board, int x, int y, int boardX, int boardY, Color color) {
    	super(board);
        this.x = x;
        this.y = y;
        this.boardX = boardX;
        this.boardY = boardY;
        this.color = color;
        this.children = new HashSet<GameObject>();
        this.selected = false;
    }
    
    public void mouseOver() {
    	for(GameObject obj : children) {
    		if(obj instanceof PlayPiece) {
    			((PlayPiece) obj).setMouseOver(true);
    		}
    	}
    }
    
    public void mouseExited() {
    	for(GameObject obj : children) {
    		if(obj instanceof PlayPiece) {
    			((PlayPiece) obj).setMouseOver(false);
    		}
    	}
    }

	public void tick() {
		for(GameObject obj : children) {
			obj.tick();
		}
	}

	public void render(Graphics2D g) {
		g.setColor(color);
		g.fillRect(x, y, Globals.TILE_SIZEX, Globals.TILE_SIZEY);
		
		for(GameObject obj : children) {
			obj.render(g);
		}
	}
    
    public void setSelected(boolean flag) {
        this.selected = flag;
        this.board.setSelected(this);
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
    public void addChild(GameObject object) {
        children.add(object);
    }
    
    public boolean removeChild(GameObject object) {
        if(children.contains(object)) {
            children.remove(object);
            return true;
        }
        
        return false;
    }
    
    public PlayPiece getPiece() {
    	if(!isEmpty()) {
    		for(GameObject obj : children) {
    			if(obj instanceof GamePiece) {
    				GamePiece gp = (GamePiece) obj;
    				if(obj instanceof PlayPiece) {
    					PlayPiece piece = (PlayPiece) gp;
    					return piece;
    				}
    			}
    		}
    	}
    	
    	return null;
    }
    
    public HashSet<GameObject> getChildren() {
        return this.children;
    }
    
    public boolean isEmpty() {
    	return getChildren().size() == 0;
    }
    
    public int getBoardX() {
        return this.boardX;
    }
    
    public int getBoardY() {
        return this.boardY;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return this.color;
    }
}
