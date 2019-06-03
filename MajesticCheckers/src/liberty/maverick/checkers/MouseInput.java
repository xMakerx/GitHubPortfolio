package liberty.maverick.checkers;
/**
 * Checkers!
 * MouseInput.java
 * 
 * @author (Maverick Liberty)
 * @version (September 12, 2016)
 */

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import liberty.maverick.checkers.object.GameObject;
import liberty.maverick.checkers.object.PlayPiece;
import liberty.maverick.checkers.object.Team;
import liberty.maverick.checkers.sound.SoundLoader;

public class MouseInput implements MouseListener, MouseMotionListener {
    
    final Checkers instance;
    private final HashSet<Tile> tilesOver;
    long clickCooldown;
    
    public MouseInput(Checkers instance) {
        this.instance = instance;
        this.tilesOver = new HashSet<Tile>();
        this.clickCooldown = 0;
    }
    
    public ArrayList<Tile> getTilesMouseWithin(int mX, int mY) {
        final Tile[][] tiles = instance.getBoard().getTiles();
        final ArrayList<Tile> tilesIn = new ArrayList<Tile>();
        for(int x = 0; x < tiles[0].length; x++) {
            for(int y = 0; y < tiles[1].length; y++) {
                final Tile tile = tiles[x][y];
                final int tX = tile.getX();
                final int tY = tile.getY();
                if(tX <= mX && mX < (tX + Globals.TILE_SIZEX) && tY <= mY && mY <= (tY + Globals.TILE_SIZEY)) {
                	tilesIn.add(tile);
                }
            }
        }
        
        return tilesIn;
    }
    
    public Tile getTileMouseWithin(int mX, int mY) {
        final Tile[][] tiles = instance.getBoard().getTiles();
        for(int x = 0; x < tiles[0].length; x++) {
            for(int y = 0; y < tiles[1].length; y++) {
                final Tile tile = tiles[x][y];
                final int tX = tile.getX();
                final int tY = tile.getY();
                if(tX <= mX && mX < (tX + Globals.TILE_SIZEX) && tY <= mY && mY <= (tY + Globals.TILE_SIZEY)) {
                	return tile;
                }
            }
        }
        
        return null;
    }
    
    
    public void mouseEntered(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}
    public void mousePressed(MouseEvent evt) {
        if(System.currentTimeMillis() >= clickCooldown) {
        	if(instance.getState() == GameState.PLAYING) {
	        	Tile tile = getTileMouseWithin(evt.getX(), evt.getY());
	        	boolean isAvailable = instance.getBoard().getAvailableTiles().keySet().contains(tile);
	        	
	        	if(tile != null && !isAvailable) {
	        		// We have to reset the available tiles since we've clicked a new tile.
	        		instance.getBoard().clearAvailableTiles();
	        		int x = tile.getBoardX();
	        		int y = tile.getBoardY();
	        		
	        		if(Globals.DEBUG) System.out.printf("Clicked tile X: %s, Y: %s.\n", x, y);
	        		
	                Tile selected = instance.getBoard().getSelected();
	                if(selected != null) {
	                	selected.setSelected(false);
	                	instance.getBoard().setSelected(null);
	                }
	        		
	        		// Let's see if the tile we clicked has a piece on it.
	                if(tile.getChildren().size() == 1) {
	                	PlayPiece piece = (PlayPiece) tile.getChildren().toArray()[0];
	                	if(piece.getTeam() == instance.getTurn()) {
		                	HashMap<Tile, Tile> aTiles = piece.getAvailableMoveTiles();
		                	for(Tile aTile : aTiles.keySet()) {
		                		PlayPiece ghost = new PlayPiece(instance.getBoard(), Team.GHOST, 0, 0);
		                		ghost.setTile(aTile);
		                	}
		                	
		                	instance.getBoard().setAvailableTiles(aTiles);
			                tile.setSelected((tile.isSelected()) ? false : true);
			                SoundLoader.getSoundByName("MouseClick").play();
	                	}
	                }
	        	}else if(isAvailable && instance.getBoard().getSelected() != null) {
					Tile jumpTile = instance.getBoard().getAvailableTiles().get(tile);
	        		instance.getBoard().clearAvailableTiles();
	        		for(GameObject obj : instance.getBoard().getSelected().getChildren()) {
	        			if(obj instanceof PlayPiece) {
	        				PlayPiece piece = (PlayPiece) obj;
	        				piece.moveToTile(tile);
							instance.getTimer().reset();
	    					SoundLoader.getSoundByName("MouseClick").play();
	        				
	        				if(jumpTile != null) {
	        					PlayPiece c = jumpTile.getPiece();
	        					if(c.getTeam() == Team.RED) {
	        						instance.getBoard().addRedSidebarPiece(c);
	        					}else if(c.getTeam() == Team.BLACK) {
	        						instance.getBoard().addBlackSidebarPiece(c);
	        					}
	        					
	        					SoundLoader.getSoundByName("Drop").play();
	        					c.getTeam().setPiecesLeft(c.getTeam().getPiecesLeft() - 1);
	    						jumpTile.removeChild(c);
	    						
        						if(c.getTeam().getPiecesLeft() == 0) {
        							instance.setWinner((c.getTeam() == Team.BLACK) ? Team.RED : Team.BLACK);
        							instance.setState(GameState.GAMEOVER);
        						}
	        				}
	        			}
	        		}
	        		
	        		if(instance.getBoard().getSelected() != null) {
	        			instance.getBoard().getSelected().setSelected(false);
	        			instance.getBoard().setSelected(null);
	        		}
	            }
        	}else if(instance.getState() == GameState.PREGAME) {
        		instance.setState(GameState.PREGAME_FADE_IN);
        	}
            
            clickCooldown = System.currentTimeMillis() + 25;
        }
    }
    
    public void mouseReleased(MouseEvent evt) {}
    public void mouseClicked(MouseEvent evt) {}
    public void mouseMoved(MouseEvent evt) {
    	ArrayList<Tile> tiles = getTilesMouseWithin(evt.getX(), evt.getY());
    	
    	for(int i = 0; i < tilesOver.size(); i++) {
    		Tile tile = (Tile) tilesOver.toArray()[i];
    		if(!tiles.contains(tile)) {
    			tilesOver.remove(tile);
    			tile.mouseExited();
    		}
    	}
    	
    	for(Tile tile : tiles) {
    		if(tile != null && !tilesOver.contains(tile)) {
    			tilesOver.add(tile);
    			tile.mouseOver();
    		}
    	}
    	
    }
    public void mouseDragged(MouseEvent evt) {}
}