package liberty.maverick.dragonscale.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import liberty.maverick.dragonscale.DragonScale;
import liberty.maverick.dragonscale.DragonScaleDatabase;
import liberty.maverick.dragonscale.DragonScaleLogger;
import liberty.maverick.dragonscale.DragonScaleSettings;
import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;
import liberty.maverick.dragonscale.util.DragonUtils;

public class MineEventListener implements Listener {
	
	final DragonScale main;
	final DragonScaleSettings settings;
	final DragonScaleDatabase database;
	final DragonScaleLogger logger;
	
	public MineEventListener(DragonScale mainInstance) {
		this.main = mainInstance;
		this.settings = main.getSettings();
		this.database = main.getSystemDatabase();
		this.logger = new DragonScaleLogger(main, "MineEvents");
	}
	
	@EventHandler
	public void onDragonScaleMineEvent(final DragonScaleMineEvent evt) {
		final DragonScalePickaxe pickaxe = evt.getPickaxe();
		pickaxe.processRewards(evt.getBlock());
	}
	
	@EventHandler
	public void onBlockBreakEvent(final BlockBreakEvent evt) {
		final Player player = evt.getPlayer();
		final Block minedBlock = evt.getBlock();

		final boolean isPluginPickaxe = DragonScalePickaxe.isPluginPickaxe(main, player.getInventory().getItemInMainHand());
		final boolean canBuildHere = DragonUtils.canBuildHere(player, minedBlock.getLocation());
		
		if(isPluginPickaxe && canBuildHere && !evt.isCancelled()) {
			final DragonScalePickaxe pickaxe = database.getPickaxeData(player.getUniqueId().toString());
			
			if(pickaxe != null && settings.isInBlockTable(minedBlock)) {
				// We only want to call our custom mine event when a player mines a block in the block table.
				pickaxe.onMine(minedBlock);
				
				// Let's create and call our custom event.
				final DragonScaleMineEvent mineEvt = new DragonScaleMineEvent(pickaxe, minedBlock, pickaxe.getMinedThisPeriod(), player);
				main.getServer().getPluginManager().callEvent(mineEvt);
				
			}else if(pickaxe == null) {
				logger.error(String.format("Player \"%s\" doesn't have a valid DragonScalePickaxe instance!!!", 
						player.getDisplayName()));
			}
		}
	}
}
