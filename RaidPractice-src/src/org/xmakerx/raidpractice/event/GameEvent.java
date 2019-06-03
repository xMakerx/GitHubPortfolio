package org.xmakerx.raidpractice.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.xmakerx.raidpractice.arena.Game;

public class GameEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private final Game game;
	
	public GameEvent(final Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlersList() {
		return handlers;
	}
}
