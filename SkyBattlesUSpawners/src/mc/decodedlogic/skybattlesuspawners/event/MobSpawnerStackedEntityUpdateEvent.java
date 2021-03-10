package mc.decodedlogic.skybattlesuspawners.event;

import org.bukkit.entity.Entity;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public class MobSpawnerStackedEntityUpdateEvent extends MobSpawnerEvent {
	
	private final Entity entity;
	
	public MobSpawnerStackedEntityUpdateEvent(Entity entity, MobSpawner spawner) {
		super(spawner);
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return this.entity;
	}

}
