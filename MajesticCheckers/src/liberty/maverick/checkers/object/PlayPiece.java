package liberty.maverick.checkers.object;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;

import liberty.maverick.checkers.Board;
import liberty.maverick.checkers.Globals;
import liberty.maverick.checkers.Tile;
import liberty.maverick.checkers.sound.SoundLoader;

public class PlayPiece extends GamePiece {
	
	private final Team team;
	private PieceType type;
	private boolean mouseOver;
	private Point moveTo;

	public PlayPiece(Board board, Team team, int x, int y) {
		super(board, x, y);
		this.team = team;
		this.type = PieceType.NORMAL;
		this.mouseOver = false;
		this.moveTo = null;
	}
	
	public void setType(PieceType type) {
		this.type = type;
	}
	
	public PieceType getType() {
		return this.type;
	}
	
	public Team getTeam() {
		return this.team;
	}
	
	public void moveToTile(Tile tile) {
		setPosition(tile.getX() + 1, tile.getY() + 1);
		setTile(tile);
		
		if(tile.getBoardY() == 0 && team == Team.BLACK || tile.getBoardY() == 9 && team == Team.RED) {
			setType(PieceType.QUEEN);
			SoundLoader.getSoundByName("Queened").play();
		}
	}
	
    public HashMap<Tile, Tile> getAvailableMoveTiles() {
    	HashMap<Tile, Tile> tiles = new HashMap<Tile, Tile>();
    	if(container != null) {
    		int x = container.getBoardX();
    		int y = container.getBoardY();
    		
    		Tile topRight = board.getTile(x + 1, y - 1);
    		Tile topLeft = board.getTile(x - 1, y - 1);
    		Tile btmRight = board.getTile(x + 1, y + 1);
    		Tile btmLeft = board.getTile(x - 1, y + 1);
    		
    		if(team.getMovementDirection() == 0 || type == PieceType.QUEEN) {
    			if(topRight != null) {
    				if(!topRight.isEmpty()) {
    					Tile jTR = topRight.getPiece().getJumpableTile(this, 2);
    					if(jTR != null) {
    						tiles.put(jTR, topRight);
    					}
    				}else {
    					tiles.put(topRight, null);
    				}
    			}
    			
    			if(topLeft != null) {
    				if(!topLeft.isEmpty()) {
    					Tile jTL = topLeft.getPiece().getJumpableTile(this, 3);
    					if(jTL != null) {
    						tiles.put(jTL, topLeft);
    					}
    				}else {
    					tiles.put(topLeft, null);
    				}
    			}
    		}
    		
    		if(team.getMovementDirection() == 1 || type == PieceType.QUEEN) {
    			if(btmRight != null) {
    				if(!btmRight.isEmpty()) {
    					Tile jBR = btmRight.getPiece().getJumpableTile(this, 0);
    					if(jBR != null) {
    						tiles.put(jBR, btmRight);
    					}
    				}else {
    					tiles.put(btmRight, null);
    				}
    			}
    			
    			if(btmLeft != null) {
    				if(!btmLeft.isEmpty()) {
    					Tile jBL = btmLeft.getPiece().getJumpableTile(this, 1);
    					if(jBL != null) {
    						tiles.put(jBL, btmLeft);
    					}
    				}else {
    					tiles.put(btmLeft, null);
    				}
    			}
    		}
    	}
    	
    	return tiles;
    }
    
    /*
     * Direction is the location the piece that wants the jump is.
     * 0 - Top Left
     * 1 - Top Right
     * 2 - Bottom Left
     * 3 - Bottom Right
     */
    
    public Tile getJumpableTile(PlayPiece piece, int direction) {
		int x = container.getBoardX();
		int y = container.getBoardY();
		Tile topRight = board.getTile(x + 1, y - 1);
		Tile topLeft = board.getTile(x - 1, y - 1);
		Tile btmRight = board.getTile(x + 1, y + 1);
		Tile btmLeft = board.getTile(x - 1, y + 1);
		
		if(team != piece.getTeam()) {
	    	if(direction == 0 && btmRight != null) {
	    		if(btmRight.isEmpty()) return btmRight;
	    	}else if(direction == 1 && btmLeft != null) {
	    		if(btmLeft.isEmpty()) return btmLeft;
	    	}else if(direction == 2 && topRight != null) {
	    		if(topRight.isEmpty()) return topRight;
	    	}else if(direction == 3 && topLeft != null) {
	    		if(topLeft.isEmpty()) return topLeft;
	    	}
		}
    	
    	return null;
    }
    
    public void setMoveTo(Point point) {
    	this.moveTo = point;
    }
    
    public Point getMoveTo() {
    	return this.moveTo;
    }

	@Override
	public void tick() {
		if(velX > 0 && moveTo != null) {
			if(velX > 0 && (x + velX) > moveTo.getX() || velX < 0 && (x + velX) < moveTo.getX()) {
				x = (int) moveTo.getX();
				velX = 0;
			}else {
				x += velX;
				if(x == moveTo.getX()) velX = 0;
			}
		}
		
		if(velY > 0 && moveTo != null) {
			if((velY > 0 && (y + velY) > moveTo.getY() || velY < 0 && (y + velY) < moveTo.getY())) {
				y = (int) moveTo.getY();
				velY = 0;
			}else {
				y += velY;
				if(y == moveTo.getY()) velY = 0;
			}
		}
	}
	
	public void setTile(Tile tile) {
		if(container != null) container.removeChild(this);
        this.container = tile;
        if(container != null && !container.getChildren().contains(this)) {
        	container.addChild(this);
        	setPosition(container.getX() + 1, container.getY() + 1);
        }
	}

	@Override
	public void render(Graphics2D g) {
		//if(container != null) {
			Color pColor = team.getPrimaryColor();
			Color sColor = team.getSecondaryColor();
			
			// Let's update the ghost colors.
			if(mouseOver && team == Team.GHOST) {
				pColor = new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), pColor.getAlpha() + 50);
				sColor = new Color(sColor.getRed(), sColor.getGreen(), sColor.getBlue(), sColor.getAlpha() + 50);
			}
			
			if(type == PieceType.QUEEN) {
				g.setColor((team == Team.BLACK) ? Color.BLUE : Color.GRAY);
				g.fillOval(x - 2, y - 2, Globals.TILE_SIZEX + 1, Globals.TILE_SIZEY + 1);
			}
			
			g.setColor(sColor);
			g.fillOval(x, y, Globals.TILE_SIZEX - 2, Globals.TILE_SIZEY - 2);
			
			g.setColor(pColor);
			g.fillOval(x, y, Globals.TILE_SIZEX - 4, Globals.TILE_SIZEY - 4);
			
			if(type == PieceType.QUEEN) {
				g.setFont(new Font("Arial", 1, 34));
				
				g.setColor(Color.GRAY);
				g.drawString("Q", x + 4, y + 29);
				
				g.setColor(Color.YELLOW);
				g.drawString("Q", x + 6, y + 31);
			}
		//}
	}
	
	public void setMouseOver(boolean flag) {
		this.mouseOver = flag;
		if(mouseOver && team == Team.GHOST) SoundLoader.getSoundByName("MouseOver").play();
	}
	
	public boolean isMouseOver() {
		return this.mouseOver;
	}

}
