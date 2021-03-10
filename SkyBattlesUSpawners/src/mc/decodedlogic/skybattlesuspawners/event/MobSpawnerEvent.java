package mc.decodedlogic.skybattlesuspawners.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public abstract class MobSpawnerEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	protected final MobSpawner spawner;
	protected boolean cancelled;
	
	public MobSpawnerEvent(MobSpawner spawner) {
		this.spawner = spawner;
		this.cancelled = false;
	}
	
	public MobSpawner getSpawner() {
		return this.spawner;
	}
	
	public void setCancelled(boolean flag) {
		this.cancelled = flag;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}

	public HandlerList getHandlers() {
		return MobSpawnerEvent.HANDLERS_LIST;
	}

}
