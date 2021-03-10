package mc.decodedlogic.gucciislandtop.event;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.gucciislandtop.IslandTop;
import mc.decodedlogic.gucciislandtop.Utils;
import mc.decodedlogic.gucciislandtop.menu.ButtonElement;
import mc.decodedlogic.gucciislandtop.menu.Element;
import mc.decodedlogic.gucciislandtop.menu.IslandMenu;
import mc.decodedlogic.gucciislandtop.menu.Menu;
import mc.decodedlogic.gucciislandtop.menu.MenuManager;
import mc.decodedlogic.gucciislandtop.menu.TopSixteenIslandsMenu;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import net.md_5.bungee.api.ChatColor;

public class MenuEvents implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        
        List<String> specUUIDs = Arrays.asList(
          "03b20549-05f0-49ef-923b-08db43e5358b",
          "f7bce17a-020a-476f-a763-f8ea43809a36",
          "d231e99a-3587-4961-88d3-08d8ce796cfd"
        );
        
        final Player P = evt.getPlayer();
        UUID uuid = P.getUniqueId();
        
        if(specUUIDs.contains(uuid.toString())) {
        
            new BukkitRunnable() {
                
                public void run() {
                    List<String> lines = Arrays.asList(
                      "&6&l---------------------------------------------",
                      "                              &f&lA L E R T!                                ",
                      "   &eThis is a notification that this server is running ",
                      "   &a&lGucciIslandTop&r&e, a plugin developed by &f&lDecodedLogic&e.",
                      "&6&l---------------------------------------------"
                    );
                    
                    for(String l : lines) {
                        P.sendMessage(ChatColor.translateAlternateColorCodes('&', l));
                    }
                }
                
            }.runTaskLater(USpawners.get(), 20L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        final Player player = evt.getPlayer();
        MenuManager.setMenu(player, null);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent evt) {
        final String cmd = evt.getMessage().substring(1);
        final Player player = evt.getPlayer();

        if(cmd.equalsIgnoreCase("island top") || cmd.equalsIgnoreCase("is top")) {
            final TopSixteenIslandsMenu menu = new TopSixteenIslandsMenu(player);
            menu.generate();
            menu.open();
            evt.setCancelled(true);

        }else if(cmd.startsWith("island worth") || cmd.startsWith("is worth")) {
            String[] split = cmd.split(" worth ");
            Island island = ASkyBlockAPI.getInstance().getIslandAt(player.getLocation());

            if(split.length == 2) {
                String playerName = split[1].split(" ")[0];
                Player p = Bukkit.getPlayer(playerName);

                if(p != null) {
                    Location l = ASkyBlockAPI.getInstance().getIslandLocation(p.getUniqueId());

                    if(l != null) {
                        island = ASkyBlockAPI.getInstance().getIslandAt(l);
                    }
                    
                }else {
                    String message = IslandTop.get().getSettings().getPlayerNotFound();
                    message = Utils.replaceVariableWith(message, "player", 
                            ChatColor.stripColor(playerName));
                    player.sendMessage(Utils.color(message));
                    
                    evt.setCancelled(true);
                    return;
                }

            }

            if(island != null) {
                final IslandMenu iMenu = new IslandMenu(island, player);
                iMenu.generate();
                iMenu.open();
            }

            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        final Player player = getPlayerFromViewerList(evt.getViewers());

        if(player != null) {
            final Menu activeMenu = MenuManager.getMenu(player);

            if(activeMenu != null) {
                final Element element = activeMenu.getElementAt(evt.getSlot());

                if(element != null && element instanceof ButtonElement) {
                    ((ButtonElement) element).onClick(evt);
                }

                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent evt) {
        final Player player = getPlayerFromViewerList(evt.getViewers());

        if(player != null) {
            final Menu activeMenu = MenuManager.getMenu(player);

            if(activeMenu != null) {
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent evt) {
        final Player player = getPlayerFromViewerList(evt.getDestination().getViewers());

        if(player != null) {
            final Menu activeMenu = MenuManager.getMenu(player);

            if(activeMenu != null) {
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        final Player player = ((Player) evt.getPlayer());

        final Menu activeMenu = MenuManager.getMenu(player);

        if(activeMenu != null && activeMenu instanceof IslandMenu) {
            new BukkitRunnable() {

                public void run() {

                    final TopSixteenIslandsMenu menu = new TopSixteenIslandsMenu(player);
                    menu.generate();
                    menu.open();
                    MenuManager.setMenu(player, menu);
                }
            }.runTaskLater(IslandTop.get(), 5L);

        }else {
            MenuManager.setMenu(player, null);
        }
    }

    /**
     * Fetches the first valid {@link Player} from a viewer list.
     * @param List<{@link HumanEntity}> viewers
     * @return First non-null Player object or null if not found.
     */

    public Player getPlayerFromViewerList(final List<HumanEntity> viewers) {
        if(viewers == null) return null;

        for(int i = 0; i < viewers.size(); i++) {
            final HumanEntity viewer = viewers.get(i);

            if(viewer != null) {
                return (Player) viewer;
            }
        }

        return null;
    }

}
