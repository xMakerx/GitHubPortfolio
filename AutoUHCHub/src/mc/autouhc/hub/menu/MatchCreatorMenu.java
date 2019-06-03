package mc.autouhc.hub.menu;

import mc.autouhc.hub.AutoUHCHub;
import mc.autouhc.hub.Messages;
import mc.autouhc.hub.Settings;
import mc.autouhc.hub.creator.MatchCreator;
import mc.autouhc.hub.util.ConfigUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class MatchCreatorMenu extends Menu {

    protected final Messages msgs;
    protected final Settings settings;
    protected final MatchCreator matchCreator;
    protected boolean opened;

    public MatchCreatorMenu(AutoUHCHub instance, String invTitle, Player player, MatchCreator matchCreator) {
        super(instance, player, Bukkit.createInventory(null, 27, invTitle));
        this.msgs = instance.getMessages();
        this.settings = instance.getSettings();
        this.matchCreator = matchCreator;
        this.opened = false;
    }

    protected static String generateTitle(Messages msgs, String title) {
        return msgs.color(String.format(msgs.getRawMessage("matchCreatorMenuTitle"), msgs.getRawMessage(title)));
    }

    protected void openIfNeeded() {
        if(!opened) {
            viewer.openInventory(ui);
            opened = true;
        }
    }

    public void closed() {
        // Let's send the close message.
        String[] split = msgs.getRawMessage("matchCreatorEarlyClose").split("\\{beginClick\\}");
        String[] secondSplit = split[1].split("\\{endClick\\}");
        
        TextComponent firstSect = new TextComponent(split[0]);
        TextComponent clickableSect = new TextComponent(secondSplit[0]);
        clickableSect.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "uhc creatematch"));
        TextComponent lastSect = new TextComponent(secondSplit[1]);
        viewer.spigot().sendMessage(new TextComponent(System.lineSeparator() + System.lineSeparator() + System.lineSeparator()), 
                firstSect, clickableSect, lastSect);
        
        matchCreator.setDestroyCooldown(new BukkitRunnable() {
            
            public void run() {
                main.removeMatchCreator(viewer);
            }
            
        }.runTaskLater(main, 2400L));
    }

    public void show() {
        ui.clear();
        
        if(matchCreator.getDestroyCooldown() != null) {
            matchCreator.getDestroyCooldown().cancel();
            matchCreator.setDestroyCooldown(null);
        }

        // Let's setup the background.
        for(int i = 0; i < ui.getSize(); i++) {
            final ItemStack bgItem = ConfigUtils.nameItem(main.getSettings().getCreatorBackgroundItem(), " ");
            ui.setItem(i, bgItem);
        }

        // Let's add the next and back buttons
        final ItemStack backBtn = ConfigUtils.nameItem(main.getSettings().getBackPageItem(), msgs.color(msgs.getRawMessage("back")));
        ui.setItem(18, backBtn);

        final ItemStack nextBtn = ConfigUtils.nameItem(main.getSettings().getNextPageItem(), msgs.color(msgs.getRawMessage("next")));
        ui.setItem(26, nextBtn);
    }

    public MatchCreator getMatchCreator() {
        return this.matchCreator;
    }

}
