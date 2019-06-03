package org.xmakerx.raidpractice.event;

import org.bukkit.event.HandlerList;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.GamePlayer;

public class GameLeaveEvent extends GameEvent {
	
	private final GamePlayer gPlayer;
	private static final HandlerList handlers = new HandlerList();
	
	public GameLeaveEvent(final Game game, final GamePlayer gPlayer) {
		super(game);
		this.gPlayer = gPlayer;
	}
	
	public GamePlayer getGamePlayer() {
		return this.gPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
