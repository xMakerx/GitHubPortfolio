package mc.decodedlogic.craftrp.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class MessageUtils {
    
    public static void sendActionBarMessage(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
    
    public static void sendActionBarMessage(Player p, String msg, Sound sound, float volume, float pitch) {
        sendActionBarMessage(p, msg);
        
        if(sound != null && volume > 0.0F) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }
    
    public static void sendActionBarMessage(Player p, String msg, Sound sound) {
        sendActionBarMessage(p, msg, sound, 1.0F, 1.0F);
    }
}
