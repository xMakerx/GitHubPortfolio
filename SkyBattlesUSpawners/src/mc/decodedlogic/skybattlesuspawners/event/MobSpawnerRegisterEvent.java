package mc.decodedlogic.skybattlesuspawners.event;

import org.bukkit.event.HandlerList;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;

public class MobSpawnerRegisterEvent extends MobSpawnerEvent {
	
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	
	/**
	 * Called when a {@link MobSpawner} is registered within {@link SpawnerManager}.
	 * If this event is cancelled, {@link SpawnerManager#unregister(MobSpawner)} is called.
	 * However, the data file for the spawner <b>ISN'T</b> deleted. You must do that manually.
	 * @param spawner
	 */
	
	public MobSpawnerRegisterEvent(MobSpawner spawner) {
		super(spawner);
		
		if(cancelled) {
			SpawnerManager.unregister(spawner);
		}
	}


	public HandlerList getHandlers() {
		return MobSpawnerRegisterEvent.HANDLERS_LIST;
	}

}
