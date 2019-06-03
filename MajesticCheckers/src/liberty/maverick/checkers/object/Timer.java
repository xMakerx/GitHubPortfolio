package liberty.maverick.checkers.object;

import java.awt.Color;
import java.awt.Graphics2D;

import liberty.maverick.checkers.Board;
import liberty.maverick.checkers.Checkers;
import liberty.maverick.checkers.Globals;
import liberty.maverick.checkers.sound.SoundLoader;

public class Timer extends GameObject {
	
	final Checkers instance;
	private int timeLeft;
	private boolean active;
	private long nextCountdownTime;

	public Timer(Checkers instance, Board board) {
		super(board);
		this.instance = instance;
		this.timeLeft = 0;
		this.active = false;
		this.nextCountdownTime = 0;
	}
	
	public void stop() {
		this.active = false;
		this.reset();
	}
	
	public void reset() {
		this.timeLeft = Globals.TURN_DURATION;
		this.nextCountdownTime = 0;
		
		instance.setTurn((instance.getTurn() == Team.BLACK) ? Team.RED : Team.BLACK);
	}
	
	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}
	
	public int getTimeLeft() {
		return this.timeLeft;
	}
	
	public void setActive(boolean flag) {
		this.active = flag;
		if(!active) {
			reset();
		}else {
			this.timeLeft = Globals.TURN_DURATION;
		}
	}
	
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void tick() {
		if(active) {
			if(nextCountdownTime == 0) {
				nextCountdownTime = System.currentTimeMillis() + 1000;
			}else if(System.currentTimeMillis() >= nextCountdownTime) {
				if((timeLeft - 1) < 0) {
					reset();
					SoundLoader.getSoundByName("TimeUp").play();
				}else {
					timeLeft--;
					if(timeLeft <= 3) SoundLoader.getSoundByName("Tick").play();
				}
				nextCountdownTime = System.currentTimeMillis() + 1000;
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		if(active) {
	    	g.setColor(Color.GRAY);
	    	g.fillOval(Globals.GAME_WIDTH - 78, 22, 60, 60);
	    	
	    	g.setColor(new Color(127, 51, 0));
	    	g.fillOval(Globals.GAME_WIDTH - 80, 20, 60, 60);
	    	
	    	g.setColor(Color.WHITE);
	    	g.fillOval(Globals.GAME_WIDTH - 75, 25, 50, 50);
	    	
	    	int textX = (timeLeft > 9) ? Globals.GAME_WIDTH - 70 : Globals.GAME_WIDTH - 58;
	    	Color color = (timeLeft > 3) ? Color.BLACK : Color.RED;
	    	instance.drawEffectString(g, String.valueOf(timeLeft), 36, color, textX, 62, Color.GRAY, 2);
		}
	}

}
