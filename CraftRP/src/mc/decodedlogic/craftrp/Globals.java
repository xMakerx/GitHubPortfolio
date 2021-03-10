package mc.decodedlogic.craftrp;

import static mc.decodedlogic.craftrp.util.MessageUtils.sendActionBarMessage;

import java.util.UUID;

import org.bukkit.Sound;

import mc.decodedlogic.craftrp.player.CRPPlayer;
import net.md_5.bungee.api.ChatColor;

public final class Globals {
    
    public enum GameAction {
        PURCHASE, TOO_FAR
    }
    
    public static final Sound SOUND_FAILURE = Sound.ENTITY_VILLAGER_NO;
    public static final Sound SOUND_PURCHASE = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static final Sound SOUND_POP_UP = Sound.ENTITY_CHICKEN_EGG;
    
    public static final String MSG_TOO_FAR_AWAY = ChatColor.RED + "You're too far away!";
    public static final String MSG_PURCHASED = "Purchased!";
    
    public static final UUID CARDBOARD_BOX_UUID = UUID.fromString("6906b80f-1ce8-4b6f-8d28-8ba706a72a45");
    
    private static void __handlePurchase(CRPPlayer player) {
        sendActionBarMessage(player.getPlayer(), MSG_PURCHASED, SOUND_PURCHASE);
    }
    
    private static void __handleTooFarAway(CRPPlayer player) {
        sendActionBarMessage(player.getPlayer(), MSG_TOO_FAR_AWAY, SOUND_FAILURE);
    }
    
    
    public static void handleAction(CRPPlayer player, GameAction action) {
        if(player != null) {
            if(action == GameAction.PURCHASE) {
                __handlePurchase(player);
            }else if(action == GameAction.TOO_FAR) {
                __handleTooFarAway(player);
            }
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Globals for time
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static final int MILLISECONDS_PER_TICK = 50;
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final long TICKS_PER_SECOND = 20;
    
    public static final long DEFAULT_ACCESS_COOLDOWN_TIME = MILLISECONDS_PER_SECOND/2;
    public static final long DISPLAY_HELP_MENU_TIME = MILLISECONDS_PER_SECOND*5;
    
    /**
     * Checks if enough milliseconds have elapsed since "compareTime"
     * @param compareTime - The time to check
     * @param milliseconds - How many milliseconds must elapse.
     * @return
     */
    
    public static boolean hasEnoughMillisecondsElapsed(long compareTime, long milliseconds) {
        long now = System.currentTimeMillis();
        return ((now - compareTime) >= milliseconds);
    }
    
    /**
     * Checks if enough seconds have elapsed since "compareTime"
     * @param compareTime - The time to check.
     * @param seconds - How many seconds must elapse.
     * @see {@link #hasEnoughMillisecondsElapsed(long, long)} - Called under the hood after
     * conversion from milliseconds to seconds.
     * @return
     */
    
    public static boolean hasEnoughSecondsElapsed(long compareTime, double seconds) {
        return hasEnoughMillisecondsElapsed(compareTime, 
                secondsToMilliseconds(seconds));
    }
    
    /**
     * Converts the specified seconds to milliseconds.
     * @param seconds - double-precision seconds to convert
     * @return
     */
    
    public static final long secondsToMilliseconds(double seconds) {
        return (long) (seconds * ((double) MILLISECONDS_PER_SECOND));
    }
    
    /**
     * How long ago the specified time in milliseconds was.
     * @param timeMs - Time in milliseconds
     * @return time (in milliseconds) since the specified time.
     */
    
    public static final long millisecondsSince(long timeMs) {
        long now = System.currentTimeMillis();
        return (now - timeMs);
    }
    
    /**
     * Converts the specified milliseconds to double-precision seconds
     * @param milliseconds
     * @return seconds with double precision
     */
    
    public static final double millisecondsToSeconds(long milliseconds) {
        return (double) milliseconds / ((double) MILLISECONDS_PER_SECOND);
    }
    
    /**
     * Converts the specified milliseconds into double-precision ticks
     * @param milliseconds
     * @return ticks with double precision
     */
    
    public static final double millisecondsToTicks(long milliseconds) {
        return (double) milliseconds / MILLISECONDS_PER_TICK;
    }
    
    private Globals() {}
    
}
