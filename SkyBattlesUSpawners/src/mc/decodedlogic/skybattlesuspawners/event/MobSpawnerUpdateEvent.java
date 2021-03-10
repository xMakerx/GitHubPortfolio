package mc.decodedlogic.skybattlesuspawners.event;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public class MobSpawnerUpdateEvent extends MobSpawnerEvent {
	
	public MobSpawnerUpdateEvent(final MobSpawner spawner) {
		super(spawner);
	}
}
