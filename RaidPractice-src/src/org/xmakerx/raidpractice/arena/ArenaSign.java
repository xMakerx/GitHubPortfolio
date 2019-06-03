package org.xmakerx.raidpractice.arena;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.block.Sign;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;

public class ArenaSign {
	
	final RaidPractice instance;
	final Localizer localizer;
	private final String name;
	private final Arena arena;
	private final Sign sign;
	
	public ArenaSign(final RaidPractice main, final Arena arena, final Sign block) {
		this.name = String.format("sign%s", String.format("%1$02d", arena.getArenaSigns().size() + 1));
		this.instance = main;
		this.localizer = main.getLocalizer();
		this.arena = arena;
		this.sign = block;
		block.getBlock().getState().update();
		this.update();
	}
	
	public ArenaSign(final String name, final RaidPractice main, final Arena arena, final Sign block) {
		this.name = name;
		this.instance = main;
		this.localizer = main.getLocalizer();
		this.arena = arena;
		this.sign = block;
		block.getBlock().getState().update();
		this.update();
	}
	
	public void update() {
		final Game game = instance.getArenaManager().getGameFromArena(arena);
		String arenaName = arena.getName();
		final String displayName = (String) arena.getFlag("displayName").getValue();
		
		if(!arenaName.equalsIgnoreCase(displayName)) {
			arenaName = displayName;
		}
		
		if(game == null) {
			throw new NullPointerException(String.format("ArenaSign: Arena %s does not have a game!!!", arena.getName()));
		}else {
			final List<String> lines = localizer.getStringList("defaultSign");
			for(int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				line = line.replaceAll("\\{signTitle\\}", localizer.getString("signTitle"));
				line = line.replaceAll("\\{gameState\\}", game.getState().getText());
				line = line.replaceAll("\\{numPlayers\\}", String.valueOf(game.getPlayers().size()));
				line = line.replaceAll("\\{maxPlayers\\}", String.valueOf(arena.getFlag("maxPlayers").getValue()));
				line = line.replaceAll("\\{arena\\}", arenaName);
				sign.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
			}
			sign.update(true);
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public Arena getArena() {
		return this.arena;
	}
	
	public Sign getSign() {
		return this.sign;
	}
}
