package org.xmakerx.raidpractice.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.GamePlayer;

public class GameJoinEvent extends GameEvent implements Cancellable {
	
	private final GamePlayer gPlayer;
	private boolean cancelled;
	private static final HandlerList handlers = new HandlerList();
	
	public GameJoinEvent(final Game game, final GamePlayer gPlayer) {
		super(game);
		this.gPlayer = gPlayer;
		this.cancelled = false;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.cancelled = flag;
		
		if(flag) {
			// When canceled, let's make the player leave.
			getGame().leave(gPlayer.getPlayer());
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
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
