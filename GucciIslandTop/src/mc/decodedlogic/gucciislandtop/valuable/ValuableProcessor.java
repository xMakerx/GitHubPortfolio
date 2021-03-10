package mc.decodedlogic.gucciislandtop.valuable;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.Utils;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;

public class ValuableProcessor extends BukkitRunnable {
	
	private final Island island;
	private final int level;
	private final boolean lateCalculation;
	
	public static final int MIN_Y = 0;
	public static final int MAX_Y = 256;
	
	public ValuableProcessor(Island island, int level, boolean lateCalculation) {
		this.island = island;
		this.level = level;
		this.lateCalculation = lateCalculation;
		
		if(island == null) {
			throw new NullPointerException("Cannot process a null Island!");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		Map<MaterialData, Valuable> checkValuables = new HashMap<MaterialData, Valuable>();
		for(Valuable valuable : ValuableManager.getRegisteredValuables()) {
			final Valuable dValuable = new Valuable(valuable.getMaterialData());
			dValuable.setName(valuable.getName());
			dValuable.setQuantity(0);
			dValuable.setWorth(valuable.getWorth());
			dValuable.setIconData(valuable.getIconData());
			dValuable.setSlot(valuable.getSlot());
			dValuable.setHeadName(valuable.getHeadName());
			
			for(Attribute attr : valuable.getAttributes()) {
				final Attribute dAttr = new Attribute(attr.getName(), attr.getConfigName());
				dAttr.setWorth(attr.getWorth());
				dValuable.addAttribute(dAttr);
			}
			
			checkValuables.put(valuable.getMaterialData(), dValuable);
		}
		
		final World world = island.getCenter().getWorld();
		
		final int zDelta = island.getMinProtectedZ() + island.getProtectionSize();
		int minZ = Math.min(island.getMinProtectedZ(), zDelta);
		int maxZ = (minZ == island.getMinProtectedZ()) ? zDelta : island.getMinProtectedZ();
		
		final int xDelta = island.getMinProtectedX() + island.getProtectionSize();
		int minX = Math.min(island.getMinProtectedX(), xDelta);
		int maxX = (minX == island.getMinProtectedX()) ? xDelta : island.getMinProtectedX();
		
		double worth = 0.0;
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = MIN_Y; y <= MAX_Y; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					final Block block = world.getBlockAt(x, y, z);
					
					if(block.getType() != Material.AIR) {
						final MaterialData matData = new MaterialData(block.getType(), block.getData());
						Valuable matchValuable = checkValuables.get(matData);
						MobSpawner mSpawner = null;
						int quantity = 1;
						
						if(block.getType() == Material.MOB_SPAWNER) {
							for(MobSpawner spawner : SpawnerManager.getMobSpawners()) {
								if(spawner.getLocation().equals(block.getLocation())) {
									mSpawner = spawner;
									break;
								}
							}
						}
						
						if(matchValuable != null || mSpawner != null) {
							
							
							if(mSpawner != null && matchValuable == null) {
								final MaterialData fixedData = new MaterialData(block.getType(), (byte) mSpawner.getType().getData());
								matchValuable = checkValuables.get(fixedData);
								
								if(matchValuable != null) { 
									quantity = mSpawner.getQuantity();
									
									for(Attribute attr : matchValuable.getAttributes()) {
										String cfgName = attr.getConfigName();
										
										if(cfgName.contains("Upgrade")) {
											String upgradeName = cfgName.substring(0, cfgName.indexOf("Upgrade"));
											
											if(upgradeName.contains("_")) {
												upgradeName = upgradeName.replaceAll("\\_", " ");
											}
											
											final SpawnerUpgrade upgrade = mSpawner.getUpgrade();
											
											if(upgrade != null && upgrade.getName().equalsIgnoreCase(upgradeName)) {
												attr.setQuantity(attr.getQuantity() + quantity);
												quantity = 0;
												break;
											}
										}
									}
								}
							}
							
							matchValuable.setQuantity(matchValuable.getQuantity() + quantity);
						}
					}
				}
			}
		}
		
		for(Valuable valuable : checkValuables.values()) {
			worth += valuable.calculateOverallWorth();
		}
		
		Utils.addTopIslandEntry(island.getOwner(), worth, checkValuables.values(), level, lateCalculation);
		
	}
	
	public Island getIsland() {
		return this.island;
	}
	
	public int getLevel() {
		return this.level;
	}

}
