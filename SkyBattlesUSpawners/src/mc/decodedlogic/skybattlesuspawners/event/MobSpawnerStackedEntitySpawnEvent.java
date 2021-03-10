package mc.decodedlogic.skybattlesuspawners.event;

import org.bukkit.entity.Entity;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public class MobSpawnerStackedEntitySpawnEvent extends MobSpawnerEvent {
	
	private final Entity entity;
	private final int quantity;
	
	public MobSpawnerStackedEntitySpawnEvent(Entity entity, int quantity, MobSpawner spawner) {
		super(spawner);
		this.entity = entity;
		this.quantity = quantity;
		
		if(cancelled) {
			entity.remove();
		}
	}
	
	public Entity getEntity() {
		return this.entity;
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
}
