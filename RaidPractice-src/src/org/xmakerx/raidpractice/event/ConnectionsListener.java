package org.xmakerx.raidpractice.event;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.xmakerx.raidpractice.Database;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;

/**
 * This handles incoming and outgoing players.
 * Sets up the initial scoreboard and loads up data.
 */

public class ConnectionsListener implements Listener {
	
	final RaidPractice instance;
	final Localizer localizer;
	
	public ConnectionsListener(final RaidPractice main) {
		this.instance = main;
		this.localizer = instance.getLocalizer();
	}
	
	@EventHandler
	public void onChat(final AsyncPlayerChatEvent evt) {
		Player player = evt.getPlayer();
		evt.setFormat(getChatPrefix(player) + evt.getFormat());
	}
	
	public String getChatPrefix(final Player player) {
		Database db = instance.getStatsDatabase();
		String prefix = localizer.getString("chatPrefix");
		prefix = prefix.replaceAll("\\{score\\}", String.valueOf(db.getPoints(player)));
		return ChatColor.translateAlternateColorCodes('&', prefix);
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent evt) {
		final Player player = evt.getPlayer();
		if(!instance.getStatsDatabase().hasData(player)) {
			instance.getStatsDatabase().createEntry(player);
		}
		
		for(final Player oPlayer : Bukkit.getOnlinePlayers()) {
			if(instance.getArenaManager().getGameFromPlayer(oPlayer) == null) {
				instance.getGameEvents().givePlayerDefaultBoard(oPlayer);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent evt) {
		for(final Player oPlayer : Bukkit.getOnlinePlayers()) {
			if(instance.getArenaManager().getGameFromPlayer(oPlayer) == null) {
				instance.getGameEvents().givePlayerDefaultBoard(oPlayer);
			}
		}
	}
}
