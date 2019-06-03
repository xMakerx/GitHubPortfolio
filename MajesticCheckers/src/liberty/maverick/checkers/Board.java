package liberty.maverick.checkers;
/**
 * Checkers!
 * Board.java
 * 
 * @author (Maverick Liberty)
 * @version (September 14, 2016)
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;

import liberty.maverick.checkers.object.GameObject;
import liberty.maverick.checkers.object.PlayPiece;
import liberty.maverick.checkers.object.Team;

public class Board {

	final Checkers instance;
    final Tile[][] tiles;
    private Tile selected;
    private HashMap<Tile, Tile> availableTiles;
    private ArrayList<PlayPiece> redSidebar;
    private ArrayList<PlayPiece> blackSidebar;
    static Board bInstance;
    
    public Board(final Checkers instance) {
        Board.bInstance = this;
        this.instance = instance;
        this.tiles = new Tile[Globals.BOARD_WIDTH / Globals.TILE_SIZEX][Globals.BOARD_HEIGHT / Globals.TILE_SIZEY];
        this.selected = null;
        this.availableTiles = new HashMap<Tile, Tile>();
        this.redSidebar = new ArrayList<PlayPiece>();
        this.blackSidebar = new ArrayList<PlayPiece>();
        this.createTiles();
        this.setupPieces();
    }
    
    public void addRedSidebarPiece(PlayPiece piece) {
    	int x = (redSidebar.size() % 2 == 0) ? 15 : 45;
    	int y = tiles[0][0].getY() + 10 + ((redSidebar.size() > 0) ? 40 * redSidebar.size() : 0);
    	piece.setPosition(x, y);
    	redSidebar.add(piece);
    }
    
    public ArrayList<PlayPiece> getRedSidebar() {
    	return this.redSidebar;
    }
    
    public void addBlackSidebarPiece(PlayPiece piece) {
    	int x = 500 + ((blackSidebar.size() % 2 == 0) ? 15 : 45);
    	int y = tiles[0][0].getY() + 10 + ((blackSidebar.size() > 0) ? 40 * blackSidebar.size() : 0);
    	piece.setPosition(x, y);
    	blackSidebar.add(piece);
    }
    
    public ArrayList<PlayPiece> getBlackSidebar() {
    	return this.blackSidebar;
    }
    
    public void reset() {
    	for(int x = 0; x < tiles[0].length; x++) {
    		for(int y = 0; y < tiles[1].length; y++) {
    			Tile tile = tiles[x][y];
    			PlayPiece piece = tile.getPiece();
    			if(piece != null) {
    				tile.removeChild(piece);
    			}
    		}
    	}
    	
    	blackSidebar.clear();
    	redSidebar.clear();
    	instance.getTimer().reset();
    	setupPieces();
    }
    
    public void setupPieces() {
    	int bX = 0;
    	int bY = 0;
    	Team.RED.setPiecesLeft(0);
    	Team.BLACK.setPiecesLeft(0);
    	for(int i = 0; i < 20; i++) {
    		Tile tile = tiles[bX][bY];
    		Team team = (i <= 9) ? Team.RED : Team.BLACK;
        	PlayPiece piece = new PlayPiece(this, team, 0, 0);
        	piece.setTile(tile);
        	team.setPiecesLeft(team.getPiecesLeft() + 1);
        	
        	if(bY == 0) {
        		bY = 1;
        	}else if(bY == 1) {
        		bY = 0;
        	}else if(bY == 8) {
        		bY = 9;
        	}else {
        		bY = 8;
        	}
        	
        	if(i == 9) {
        		bX = 0;
        		bY = 8;
        	}else {
        		bX++;
        	}
    	}
    }
    
    public void createTiles() {
        Color color = Globals.BOARD_PRIMARY;
        int x = Globals.BOARD_OFFSETX;
        int y = Globals.BOARD_OFFSETY;
        int boxes = Globals.BOARD_WIDTH / Globals.TILE_SIZEX * (Globals.BOARD_HEIGHT / Globals.TILE_SIZEY);
        int tileX = 0;
        int tileY = 0;
        
        for(int i = 0; i < boxes; i++) {
            this.tiles[tileX][tileY] = new Tile(this, x, y, tileX, tileY, color);
            
            if(x == (Globals.BOARD_OFFSETX + Globals.BOARD_WIDTH) - Globals.TILE_SIZEX) {
                x = Globals.BOARD_OFFSETX;
                tileX = 0;
                y += Globals.TILE_SIZEY;
                tileY += 1;
            }else {
                x += Globals.TILE_SIZEX;
                tileX++;
                color = (color == Globals.BOARD_PRIMARY) ? Globals.BOARD_SECONDARY : Globals.BOARD_PRIMARY;
            }
        }
    }
    
    public void render(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        
        g.setColor(Color.GRAY);
        g.fillRect(Globals.BOARD_OFFSETX + 4, Globals.BOARD_OFFSETY + 4, Globals.BOARD_WIDTH + 4, Globals.BOARD_HEIGHT + 4);
        
        for(int x = 0; x < tiles[0].length; x++) {
            for(int y = 0; y < tiles[1].length; y++) {
                final Tile tile = this.tiles[x][y];
                tile.render(g);
            }
        }
        
        if(selected != null) {
	        g.setStroke(new BasicStroke(Globals.SELECTION_WIDTH));
	        g.setColor(Color.YELLOW);
	        g.drawRect(selected.getX() - 1, selected.getY() - 1, Globals.TILE_SIZEX + 1, Globals.TILE_SIZEY + 1);
        }
        
    	g.setStroke(new BasicStroke(2));
    	g.setColor(Color.DARK_GRAY);
    	g.drawRect(Globals.BOARD_OFFSETX - 1, Globals.BOARD_OFFSETY - 1, Globals.BOARD_WIDTH + 2, Globals.BOARD_HEIGHT + 2);
        
        g.setStroke(oldStroke);
        
        for(int i = 0; i < redSidebar.size(); i++) {
        	PlayPiece redPiece = redSidebar.get(i);
        	redPiece.render(g);
        }
        
        for(int i = 0; i < blackSidebar.size(); i++) {
        	PlayPiece blackPiece = blackSidebar.get(i);
        	blackPiece.render(g);
        }
    }
    
    public void tick() {
        for(int x = 0; x < tiles[0].length; x++) {
            for(int y = 0; y < tiles[1].length; y++) {
                final Tile tile = this.tiles[x][y];
                tile.tick();
            }
        }
    }
    
    public void clearAvailableTiles() {
    	for(int i = 0; i < availableTiles.keySet().size(); i++) {
    		Tile tile = (Tile) availableTiles.keySet().toArray()[i];
    		for(int c = 0; c < tile.getChildren().size(); c++) {
    			GameObject obj = (GameObject) tile.getChildren().toArray()[c];
    			if(obj instanceof PlayPiece) {
    				PlayPiece piece = (PlayPiece) obj;
    				if(piece.getTeam() == Team.GHOST) {
    					tile.removeChild(piece);
    				}
    			}
    		}
    	}
    	
    	availableTiles.clear();
    }
    
    public void setAvailableTiles(HashMap<Tile, Tile> tiles) {
    	this.availableTiles = tiles;
    }
    
    public HashMap<Tile, Tile> getAvailableTiles() {
    	return this.availableTiles;
    }
    
    public void setSelected(Tile tile) {
    	this.selected = tile;
    }
    
    public Tile getSelected() {
    	return this.selected;
    }
    
    public Tile getTile(int x, int y) {
    	if(0 <= x && x < tiles[0].length && 0 <= y && y < tiles[1].length) {
    		return this.tiles[x][y];
    	}
    	
    	return null;
    }
    
    public Tile[][] getTiles() {
        return this.tiles;
    }
    
    public Board getInstance() {
    	return Board.bInstance;
    }
}

