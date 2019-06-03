package liberty.maverick.checkers.object;
/**
 * Checkers!
 * GameObject.java
 * 
 * @author (Maverick Liberty)
 * @version (September 14, 2016)
 */

import java.awt.Graphics2D;
import java.awt.Point;

import liberty.maverick.checkers.Board;

public abstract class GameObject {
    
    protected Board board;
    protected int x, y;
    protected int velX, velY;
    
    public GameObject(Board board) {
        this.board = board;
        this.x = 0;
        this.y = 0;
        this.velX = 0;
        this.velY = 0;;
    }
    
    public abstract void tick();
    public abstract void render(Graphics2D g);
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Point getPosition() {
        return new Point(this.x, this.y);
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getX() {
        return this.x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getY() {
        return this.y;
    }
    
    public void setVelocityX(int velX) {
    	this.velX = velX;
    }
    
    public int getVelocityX() {
    	return this.velX;
    }
    
    public void setVelocityY(int velY) {
    	this.velY = velY;
    }
    
    public int getVelocityY() {
    	return this.velY;
    }
    
    public Board getBoard() {
    	return this.board;
    }
}