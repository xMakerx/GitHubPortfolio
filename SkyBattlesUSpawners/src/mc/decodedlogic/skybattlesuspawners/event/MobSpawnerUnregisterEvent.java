package mc.decodedlogic.skybattlesuspawners.event;

import org.bukkit.event.HandlerList;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;

public class MobSpawnerUnregisterEvent extends MobSpawnerEvent {
	
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	
	/**
	 * Called when a {@link MobSpawner} is unregistered within {@link SpawnerManager}.
	 * If this event is cancelled, {@link SpawnerManager#register(MobSpawner)} is called.
	 * <b>This event is called prior to {@link MobSpawner#cleanup()}.</b>
	 * @param spawner
	 */
	
	public MobSpawnerUnregisterEvent(MobSpawner spawner) {
		super(spawner);
		
		if(cancelled) {
			SpawnerManager.register(spawner);
		}
	}


	public HandlerList getHandlers() {
		return MobSpawnerUnregisterEvent.HANDLERS_LIST;
	}

}
