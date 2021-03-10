package mc.decodedlogic.craftrp.event.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.item.CRPItem;
import mc.decodedlogic.craftrp.item.CRPItemManager;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;

public class PlayerManagerEvents implements Listener {
    
    public static final long JOIN_REGISTER_DELAY_TICKS = 2L;
    
    final CraftRP MAIN;
    
    public PlayerManagerEvents(CraftRP inst) {
        this.MAIN = inst;
        
        MAIN.getServer().getOnlinePlayers().forEach(p -> registerPlayer(p));
    }
    
    private void registerPlayer(Player p) {
        boolean hasMeta = p.hasMetadata(CRPPlayerManager.METADATA_CRPPLAYER_ID_KEY);
        
        if(!hasMeta || (hasMeta && p.getMetadata(CRPPlayerManager.METADATA_CRPPLAYER_ID_KEY).size() == 0)) {
            CRPPlayer rpPlayer = new CRPPlayer(p);
            CRPPlayerManager.registerPlayer(rpPlayer);
            
            // This will update out of date items.
            for(int i = 0; i < p.getInventory().getContents().length; i++) {
                ItemStack item = p.getInventory().getContents()[i];
                if(item == null || (item != null && item.getType() == Material.AIR)) continue;
                
                CRPItem rpItem = CRPItemManager.getCRPItemFromItemStack(item);
                
                if(rpItem != null) {
                    ItemStack latestVerItem = rpItem.getUpdatedItem(item);
                    latestVerItem.setAmount(item.getAmount());
                    p.getInventory().setItem(i, latestVerItem);
                }
            }
            
            p.updateInventory();
            MAIN.getServer().getPluginManager().callEvent(new PlayerItemHeldEvent(p, -1, p.getInventory().getHeldItemSlot()));
        }
    }
    
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player p = evt.getPlayer();
        
        new BukkitRunnable() {
            
            public void run() {
                registerPlayer(p);
            }
            
        }.runTaskLater(MAIN, JOIN_REGISTER_DELAY_TICKS);
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent evt) {
        Player p = evt.getPlayer();
        CRPPlayer rpPlayer = CRPPlayerManager.getCRPPlayerFromPlayer(p);
        
        if(rpPlayer != null) CRPPlayerManager.unregisterPlayer(rpPlayer);
    }
    
}
