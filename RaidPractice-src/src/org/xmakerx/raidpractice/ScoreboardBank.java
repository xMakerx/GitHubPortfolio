package org.xmakerx.raidpractice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.scoreboard.RPScoreboard;

public class ScoreboardBank {
	
	final RaidPractice instance;
	private final ConfigurationSection scoreboards;
	private RPScoreboard serverBoard;
	private RPScoreboard arenaBoard;
	private RPScoreboard arenaLobbyBoard;
	
	public ScoreboardBank(final RaidPractice main, final ConfigurationSection scoreboards) {
		this.instance = main;
		this.scoreboards = scoreboards;
		this.serverBoard = null;
		this.arenaBoard = null;
		this.arenaLobbyBoard = null;
	}
	
	public void buildBoards() {
		final String[] boardTypes = {"mainBoard", "arenaBoard", "arenaLobbyBoard"};
		
		for(String boardType : boardTypes) {
			final List<String> lines = scoreboards.getStringList(boardType);
			final RPScoreboard board = new RPScoreboard(instance, lines.get(0), DisplaySlot.SIDEBAR);
			
			// Let's handle each line now.
			for(int i = 1; i < lines.size(); i++) {
				final String line = lines.get(i);
				if(line.isEmpty()) {
					board.blankLine();
				}else {
					board.addLine(line);
				}
			}
			
			if(boardType == "mainBoard") {
				this.serverBoard = board;
			}else if(boardType == "arenaBoard") {
				this.arenaBoard = board;
			}else {
				this.arenaLobbyBoard = board;
			}
		}
	}
	
	public RPScoreboard prepareForUse(final Player player, final RPScoreboard board, final Game game) {
		final Database database = instance.getStatsDatabase();
		final HashMap<Integer, String> lines = board.getLines();
		final String points = String.valueOf(database.getPoints(player));
		final String keys = String.valueOf(database.getKeys(player));
		final String gamesPlayed = String.valueOf(database.getGamesPlayed(player));
		final String gamesLost = String.valueOf(database.getGamesLost(player));
		final String gamesWon = String.valueOf(database.getGamesWon(player));
		final String onlinePlayers = String.valueOf(instance.getServer().getOnlinePlayers().size());
		final String maxPlayers = String.valueOf(instance.getServer().getMaxPlayers());
		String arenaPlayers = "0";
		String maxArenaPlayers = "0";
		String prepareTimeLeft = "0";
		String timeLeft = "00:00";
		
		final RPScoreboard newBoard = new RPScoreboard(instance, board.getTitle(), board.getDisplaySlot());
		
		if(game != null) {
			arenaPlayers = String.valueOf(game.getPlayers().size());
			maxArenaPlayers = String.valueOf(String.valueOf((int)game.getArena().getFlag("maxPlayers").getValue()));
			prepareTimeLeft = String.valueOf(game.getPrepareTimeLeft());
			
			if(game.getTimeLeft() != null) {
				timeLeft = game.getTimeLeft().parse();
			}
		}
		
		for(Map.Entry<Integer, String> entry : lines.entrySet()) {
			final int index = entry.getKey();
			String line = entry.getValue();
			line = line.replaceAll("\\{points\\}", points);
			line = line.replaceAll("\\{keys\\}", keys);
			line = line.replaceAll("\\{gamesPlayed\\}", gamesPlayed);
			line = line.replaceAll("\\{gamesLost\\}", gamesLost);
			line = line.replaceAll("\\{gamesWon\\}", gamesWon);
			line = line.replaceAll("\\{onlinePlayers\\}", onlinePlayers);
			line = line.replaceAll("\\{maxPlayers\\}", maxPlayers);
			
			if(game != null) {
				// We need to fix the color for the preparing time.
				int splitIndex = line.indexOf("{arenaStartTime}");
				if(splitIndex != -1) {
					String[] split = line.split(Pattern.quote("{arenaStartTime}"), splitIndex);
					split[1] = "{arenaStartTime}".concat(split[1]);
					
					if(Integer.valueOf(prepareTimeLeft) > 3) {
						split[1] = "&a".concat(split[1]);
					}else {
						split[1] = "&c".concat(split[1]);
					}
					line = split[0].concat(split[1]);
				}
				
				line = line.replaceAll("\\{arena\\}", game.getArena().getName());
				line = line.replaceAll("\\{arenaPlayers\\}", arenaPlayers);
				line = line.replaceAll("\\{arenaMaxPlayers\\}", maxArenaPlayers);
				line = line.replaceAll("\\{arenaStartTime\\}", prepareTimeLeft);
				line = line.replaceAll("\\{arenaTimeLeft\\}", timeLeft);
			}
			
			line = ChatColor.translateAlternateColorCodes('&', line);
			newBoard.setLine(index, line);
		}
		return newBoard;
	}
	
	public RPScoreboard getServerBoard() {
		return this.serverBoard;
	}
	
	public RPScoreboard getArenaBoard() {
		return this.arenaBoard;
	}
	
	public RPScoreboard getArenaLobbyBoard() {
		return this.arenaLobbyBoard;
	}
}
