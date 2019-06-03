package liberty.maverick.checkers;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Arrays;

import liberty.maverick.checkers.object.Team;
import liberty.maverick.checkers.object.Timer;
import liberty.maverick.checkers.sound.SoundLoader;

/**
 * Checkers!
 * 
 * @author (Maverick Liberty)
 * @version (September 12, 2016)
 * 
 */

public class Checkers extends Canvas implements Runnable {
    
	private static final long serialVersionUID = 2910669686415277087L;
	private final GameWindow window;
    private final MouseInput mouseInput;
    private final Board board;
    private final Timer timer;
    private Team currentTurn;
    private Team winner;
    private Thread thread;
    private boolean running;
    private Color clickAnywhere;
    private GameState state;
    private long clickCooldown;
    private long fadeCooldown;
    private long fightCooldown;
    private long gameResetCooldown;
    public boolean begin;
    private int fadeAlpha;
    private int fightY;
    int FPS;
    
    public Checkers() {
    	new SoundLoader();
        this.thread = null;
        this.running = false;
        this.FPS = 0;
        this.clickAnywhere = Color.WHITE;
        this.clickCooldown = 0;
        this.begin = false;
        this.fadeAlpha = 50;
        this.fadeCooldown = 0;
        this.state = GameState.PREGAME;
        this.fightY = 60;
        this.fightCooldown = 0;
        this.gameResetCooldown = 0;
        this.winner = null;
        
        this.board = new Board(this);
        this.mouseInput = new MouseInput(this);
        this.addMouseListener(mouseInput);
        this.addMouseMotionListener(mouseInput);
        this.window = new GameWindow(this);
        this.currentTurn = Team.BLACK;
        
        this.timer = new Timer(this, board);
    
        SoundLoader.getSoundByName("BGM").loop();
        SoundLoader.getSoundByName("BGM").play();
        
        // It's Wednesday, My Dudes!
        this.addKeyListener(new KeyListener() {
        	ArrayList<Integer> keysPressed = new ArrayList<Integer>();
        	ArrayList<Integer> keysNeeded = new ArrayList<Integer>(Arrays.asList(38, 40, 37, 39));

			@Override
			public void keyPressed(KeyEvent evt) {
        		if(keysPressed.size() > 0) {
        			if(keysNeeded.get(keysPressed.size()) == evt.getKeyCode()) {
        				keysPressed.add(evt.getKeyCode());
        				if(keysPressed.size() == keysNeeded.size()) {
        					SoundLoader.getSoundByName("Wednesday").play();
        					keysPressed.clear();
        				}
        			}else {
        				keysPressed.clear();
        			}
        		}else if(keysNeeded.get(0) == evt.getKeyCode()) {
        			keysPressed.add(evt.getKeyCode());
        		}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
    }
    
	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		
		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1) {
				tick();
				delta--;
			}
			
			if(running) render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				this.FPS = frames;
				frames = 0;
			}
		}
		stop();
	}
    
	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		}catch(Exception e) {
			System.out.printf("Failed to end Game thread. Error: %s.", e.getMessage());
		}
	}
	
	public void tick() {
		board.tick();
		timer.tick();
		if(clickCooldown == 0) {
			clickCooldown = System.currentTimeMillis() + 250;
		}else if(System.currentTimeMillis() >= clickCooldown && clickCooldown != 0) {
			clickAnywhere = (clickAnywhere == Color.WHITE) ? Color.YELLOW : Color.WHITE;
			clickCooldown = System.currentTimeMillis() + 250;
		}
		
		if(state == GameState.GAMEOVER) {
			if(System.currentTimeMillis() >= gameResetCooldown) {
				board.reset();
				this.setState(GameState.PLAYING);
			}
		}
		
		if(begin) {
			if(fadeCooldown == 0) {
				fadeCooldown = System.currentTimeMillis() + 250;
			}else if(System.currentTimeMillis() >= fadeCooldown && fadeCooldown != 0) {
				if(state == GameState.PREGAME_FADE_IN) {
					if((fadeAlpha + 20) <= 255) {
						fadeAlpha += 20;
						fadeCooldown = System.currentTimeMillis() + 50;
					}else if(fadeAlpha != 255) {
						fadeAlpha = 255;
						fadeCooldown = System.currentTimeMillis() + 750;
					}else {
						setState(GameState.PREGAME_FADE_OUT);
					}
				}else if(state == GameState.PREGAME_FADE_OUT) {
					if((fadeAlpha - 20) >= 0) {
						fadeAlpha -= 20;
						fadeCooldown = System.currentTimeMillis() + 50;
					}else if(fadeAlpha != 0) {
						fadeAlpha = 0;
						fadeCooldown = System.currentTimeMillis() + 700;
					}else {
						setState(GameState.PLAYING);
					}
				}
			}
		}
		
		if(state == GameState.PLAYING) {
			int normalMoveTime = 50;
			if(System.currentTimeMillis() >= fightCooldown) {
				int division1 = 170;
				int division2 = 280;
				if(fightY >= 60 && fightY < division1) {
					if((fightY + 20) > division1) {
						fightY = division1;
					}else {
						fightY += 20;
					}
					
					if(fightY != division1) {
						fightCooldown = System.currentTimeMillis() + normalMoveTime;
					}else {
						fightCooldown = System.currentTimeMillis() + (normalMoveTime * 4);
						SoundLoader.playSoundByName("Fight");
					}
				}else if(fightY >= division1 && fightY < division2) {
					if((fightY + 20) > division2) {
						fightY = division2;
					}else {
						fightY += 20;
					}
					
					fightCooldown = System.currentTimeMillis() + (normalMoveTime * 2);
				}else if(fightY >= division2 && fightY < Globals.GAME_HEIGHT + 50) {
					if((fightY + 20) > Globals.GAME_HEIGHT + 50) {
						fightY = -100;
					}else {
						fightY += 20;
					}
					
					fightCooldown = System.currentTimeMillis() + normalMoveTime;
				}
			}
		}
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(2);
			return;
		}
		
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.clearRect(0, 0, Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
		g.setColor(new Color(193, 160, 125, 100));
		g.fillRect(0, 0, Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
		board.render(g);
		timer.render(g);
		
		int majesticStart = 147;
		int checkersStart = 307;
		int titleY = 38;
		
		if(state == GameState.PLAYING || state == GameState.GAMEOVER) {
			drawEffectString(g, "Majestic", 36, Color.RED, majesticStart, titleY, 2);
			drawEffectString(g, "Checkers", 36, new Color(0, 148, 255), checkersStart, titleY, 2);
			drawEffectString(g, "Developed by Maverick Liberty", 16, Color.WHITE, majesticStart + 45, titleY + 22, Color.GRAY, 2);
			
			if(currentTurn == Team.RED) {
				drawEffectString(g, "RED'S TURN", 20, Color.RED, majesticStart + 100, 525, Color.GRAY, 2);
			}else {
				drawEffectString(g, "BLACK'S TURN", 20, Color.BLACK, majesticStart + 90, 525, Color.GRAY, 2);
			}
			
			drawEffectString(g, "FIGHT!", 54, Color.RED, majesticStart + 75, fightY, 2);
		}
		
		if(state == GameState.GAMEOVER) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
			
			if(winner == Team.RED) {
				drawEffectString(g, "Red Wins!", 64, Color.RED, majesticStart + 2, 280, 4);
			}else {
				drawEffectString(g, "Black Wins!", 64, Color.BLACK, majesticStart - 24, 280, 4);
			}
			
			drawEffectString(g, "A new game will begin shortly...", 26, Color.WHITE, majesticStart - 38, 310, 4);
		}
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", 1, 12));
		g.drawString("Sounds by freesound.org", 0, Globals.GAME_HEIGHT - 30);
		
		if(state == GameState.PREGAME || state == GameState.PREGAME_FADE_IN) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
			
			drawEffectString(g, "Majestic", 54, Color.RED, majesticStart - 70, 60, 2);
			drawEffectString(g, "Checkers", 54, new Color(0, 148, 255), checkersStart, 60, 2);
			drawEffectString(g, "Developed by Maverick Liberty", 24, Color.WHITE, majesticStart - 10, 90, Color.GRAY, 2);
			drawEffectString(g, "Click anywhere to begin!", 20, clickAnywhere, majesticStart + 45, 290, 2);
			
			g.setFont(new Font("Arial", 1, 16));
			g.setColor(Color.WHITE);
			g.drawString("Checkers is all about RED vs BLACK", majesticStart + 10, 160);
			g.drawString("Take turns moving your pieces safely across the \"lava\"", majesticStart - 50, 180);
			g.drawString("Win the game by \"jumping\" all of your opponents pieces.", majesticStart - 50, 200);
			g.drawString(String.format("Each turn is limited to %d seconds; think ahead!", Globals.TURN_DURATION), majesticStart - 30, 220);
		}
		
		if(begin) {
			g.setColor(new Color(0, 0, 0, fadeAlpha));
			g.fillRect(0, 0, Globals.GAME_WIDTH, Globals.GAME_HEIGHT);
		}
		
		g.dispose();
		bs.show();
	}
	
	public void drawEffectString(Graphics2D g, String text, int fontSize, Color color, int x, int y, int shadowWidth) {
		g.setFont(new Font("Arial", 1, fontSize));
		g.setColor(Color.DARK_GRAY);
		g.drawString(text, x - shadowWidth, y + ((shadowWidth <= 2) ? shadowWidth : 2));
		
		g.setColor(color);
		g.drawString(text, x, y);
	}
	
	public void drawEffectString(Graphics2D g, String text, int fontSize, Color color, int x, int y, Color shadowColor, int shadowWidth) {
		g.setFont(new Font("Arial", 1, fontSize));
		g.setColor(shadowColor);
		g.drawString(text, x - shadowWidth, y + ((shadowWidth <= 2) ? shadowWidth : 2));
		
		g.setColor(color);
		g.drawString(text, x, y);
	}
	
	public void setTurn(Team team) {
		this.currentTurn = team;
		this.board.clearAvailableTiles();
		if(this.board.getSelected() != null) this.board.getSelected().setSelected(false);
		this.board.setSelected(null);
	}
	
	public Team getTurn() {
		return this.currentTurn;
	}
	
	public void setWinner(Team team) {
		this.winner = team;
	}
	
	public Team getWinner() {
		return this.winner;
	}
	
	public void setState(GameState state) {
		if(state == GameState.PREGAME_FADE_IN) {
			this.begin = true;
			this.fadeAlpha = 50;
		}else if(state == GameState.PREGAME_FADE_OUT) {
			this.fadeAlpha = 255;
		}else if(state == GameState.PLAYING) {
			this.timer.setActive(true);
			this.fightCooldown = System.currentTimeMillis() + 500;
			this.fightY = 60;
			this.begin = false;
			this.winner = null;
		}else if(state == GameState.GAMEOVER) {
			setGameResetCooldown();
		}
		
		this.state = state;
	}
	
	public GameState getState() {
		return this.state;
	}
	
	public void setGameResetCooldown() {
		this.gameResetCooldown = System.currentTimeMillis() + 5000;
		this.timer.setActive(false);
	}
	
	public Timer getTimer() {
		return this.timer;
	}
    
    public static void main(String[] args) {
        new Checkers();
    }
    
    public Board getBoard() {
    	return this.board;
    }
    
    public GameWindow getWindow() {
        return this.window;
    }
}