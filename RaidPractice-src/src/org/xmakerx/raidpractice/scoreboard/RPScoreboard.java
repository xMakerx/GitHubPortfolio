package org.xmakerx.raidpractice.scoreboard;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.xmakerx.raidpractice.Database;
import org.xmakerx.raidpractice.RaidPractice;

public class RPScoreboard {
	
	final RaidPractice instance;
	final Database database;
	final HashMap<Integer, String> lines;
	private String title;
	private DisplaySlot slot;
	private Scoreboard scoreboard;
	private Objective obj;
	
	public RPScoreboard(final RaidPractice main, final String title, final DisplaySlot slot) {
		this.instance = main;
		this.title = title;
		this.database = instance.getStatsDatabase();
		this.lines = new HashMap<Integer, String>();
		this.slot = slot;
	}
	
	public void addLine(final String line) {
		lines.put(lines.size(), line);
	}
	
	public void setLine(final int line, String text) {
		lines.put(line, text);
	}
	
	public void removeLine(final int line) {
		if(lines.containsKey(line)) {
			lines.remove(line);
		}
	}
	
	public void blankLine() {
		String str = " ";
		int blanks = 0;
		for(Map.Entry<Integer, String> entry : lines.entrySet()) {
			final String line = entry.getValue();
			if(line.length() - line.replaceAll(" ", "").length() > 0) {
				blanks++;
			}
		}
		for(int i = 0; i != blanks; i++) {
			str = str.concat(" ");
		}
		addLine(str);
	}
	
	private void organizeLines() {
		int lineNum = lines.size();
		
		for(final Map.Entry<Integer, String> entry : lines.entrySet()) {
			final String text = entry.getValue();
			final Score score = obj.getScore(text);
			score.setScore(lineNum);
			lineNum -= 1;
		}
	}
	
	public void build(final Player player) {
		// Updating this to handle the uncolored lines.
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = scoreboard.registerNewObjective("board", "dummy");
		obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
		obj.setDisplaySlot(slot);
		organizeLines();
		player.setScoreboard(scoreboard);
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public DisplaySlot getDisplaySlot() {
		return this.slot;
	}
	
	public Scoreboard getBoard() {
		return this.scoreboard;
	}
	
	public HashMap<Integer, String> getLines() {
		return this.lines;
	}
}
