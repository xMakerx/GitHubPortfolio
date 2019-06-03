package org.xmakerx.raidpractice.arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.xmakerx.raidpractice.Database;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.ScoreboardBank;
import org.xmakerx.raidpractice.Settings;
import org.xmakerx.raidpractice.arena.GamePlayer.PlayerState;
import org.xmakerx.raidpractice.arena.Spawn.SpawnType;
import org.xmakerx.raidpractice.event.GameJoinEvent;
import org.xmakerx.raidpractice.event.GameLeaveEvent;
import org.xmakerx.raidpractice.scoreboard.RPScoreboard;
import org.xmakerx.raidpractice.shop.Perk;
import org.xmakerx.raidpractice.shop.RespawnItem;
import org.xmakerx.raidpractice.util.FireworkEffectBuilder;
import org.xmakerx.raidpractice.util.SchematicManager;
import org.xmakerx.raidpractice.util.TextUtil;
import org.xmakerx.raidpractice.util.Time;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class Game {
	
	public enum GameState {
		WAITING, IN_GAME, DISABLED, SETTING_UP;
		
		private String text;
		
		public void setText(final String title) {
			this.text = title;
		}
		
		public String getText() {
			return this.text;
		}
	}
	
	final RaidPractice instance;
	final ScoreboardBank sbBank;
	private final Arena arena;
	private final HashSet<GamePlayer> players;
	private GameState state;
	private boolean endOfRound;
	private int prepareTimeLeft;
	private Time timeLeft;
	private Location eggLocation;
	
	protected boolean eggBroken;
	protected BukkitTask prepareTimer;
	protected BukkitTask timer;
	protected BukkitTask lobbyTimer;
	protected SchematicManager schemMgr;
	
	// This handles non-random spawns.
	private final HashSet<Spawn> availableSpawns;
	
	public Game(final RaidPractice main, final Arena arena) {
		this.instance = main;
		this.sbBank = instance.getSettings().getScoreboardBank();
		this.arena = arena;
		this.players = new HashSet<GamePlayer>();
		this.state = GameState.SETTING_UP;
		this.eggLocation = null;
		this.eggBroken = false;
		this.endOfRound = false;
		this.prepareTimeLeft = instance.getSettings().getPrepareTime();
		this.prepareTimer = null;
		this.timer = null;
		this.timeLeft = null;
		this.availableSpawns = new HashSet<Spawn>();
		this.lobbyTimer = null;
		this.schemMgr = new SchematicManager(main.getWorldEdit(), arena.getWorld());
		this.reset();
	}
	
	public void updateVariables() {
		final int arenaTime = (int)arena.getFlag("timelimit").getValue();
		int minutes = 0;
		int seconds = 0;
		if(arenaTime >= 60) {
			minutes = (int) (arenaTime / 60);
			seconds = (int) ((arenaTime - (minutes * 60)) / 60);
		}else {
			minutes = 0;
			seconds = arenaTime;
		}
		timeLeft = new Time(minutes, seconds);
	}
	
	private void startLobbyTimer() {
		if(lobbyTimer != null) return;
		lobbyTimer = new BukkitRunnable() {

			@Override
			public void run() {
				if(players.size() == 0) {
					lobbyTimer.cancel();
					lobbyTimer = null;
				}else {
					if(enoughPlayers() && prepareTimer == null) {
						startPreparingTimer();
						lobbyTimer.cancel();
						lobbyTimer = null;
					}else {
						for(final GamePlayer player : players) {
							TextUtil.sendActionBarMsg(player.getPlayer(), instance.getLocalizer().getColoredString("waitingForPlayers"));
						}
					}
				}
			}
			
		}.runTaskTimer(instance, 40L, 0L);
	}
	
	private void startPreparingTimer() {
		if(prepareTimer != null) return;
		prepareTimeLeft = instance.getSettings().getPrepareTime();
		
		if(players.size() == (int)arena.getFlag("maxPlayers").getValue()) {
			prepareTimeLeft = instance.getSettings().getGameFullPrepareTime();
		}
		
		final Game game = this;
		
		for(final GamePlayer player : players) {
			final RPScoreboard board = sbBank.prepareForUse(player.getPlayer(), sbBank.getArenaLobbyBoard(), game);
			board.build(player.getPlayer());
		}
		
		prepareTimer = new BukkitRunnable() {
			final int startPlayers = players.size();
			boolean correctedTimer = false;
			@Override
			public void run() {
				if(prepareTimer.getTaskId() != this.getTaskId()) {
					this.cancel();
					return;
				}
				if(timer != null) {
					prepareTimer.cancel();
					prepareTimer = null;
				}
				if(!enoughPlayers()) {
					prepareTimer.cancel();
					prepareTimer = null;
					prepareTimeLeft = instance.getSettings().getPrepareTime();
					for(GamePlayer player : players) {
						player.prepare();
					}
					startLobbyTimer();
				}else {
					if(!correctedTimer) {
						if(players.size() == (int)arena.getFlag("maxPlayers").getValue() && players.size() > startPlayers) {
							prepareTimeLeft = instance.getSettings().getGameFullPrepareTime();
							correctedTimer = true;
						}
					}
					
					for(GamePlayer player : players) {
						if(prepareTimeLeft > -1 && prepareTimeLeft > -1) {
							player.getPlayer().setLevel(prepareTimeLeft);
							Sound sound = instance.getSettings().getTickSound();
							if(prepareTimeLeft <= 3) {
								sound = instance.getSettings().getCountdownSound();
							}
							player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), sound, 1F, 1F);
							if(prepareTimeLeft == 0) {
								if(((boolean)arena.getFlag("randomSpawns").getValue())) {
									player.start(getRandomSpawn(SpawnType.PLAYER));
								}else {
									player.start(getRandomAvailableSpawn());
								}
							}
						}
						final RPScoreboard board = sbBank.prepareForUse(player.getPlayer(), sbBank.getArenaLobbyBoard(), game);
						board.build(player.getPlayer());
					}
					
					prepareTimeLeft -= 1;
					if(prepareTimeLeft == -1 && timer == null) {
						state = GameState.IN_GAME;
						startTimeLeftClock();
						updateSigns();
						this.cancel();
						prepareTimer = null;
					}
				}
			}
			
		}.runTaskTimer(instance, 20L, 20L);
	}
	
	private Spawn getRandomAvailableSpawn() {
		for(final Spawn spawn : availableSpawns) {
			availableSpawns.remove(spawn);
			return spawn;
		}
		return null;
	}
	
	private void startTimeLeftClock() {
		if(timer == null) {
			updateVariables();

			final Game game = this;
			
			timer = new BukkitRunnable() {
				int elapsedTime = 0;
				
				@Override
				public void run() {
					for(final GamePlayer gPlayer : players) {
						final Player player = gPlayer.getPlayer();
						final RPScoreboard board = sbBank.prepareForUse(player, sbBank.getArenaBoard(), game);
						board.build(player);
					}
					elapsedTime += 1;
					if(timeLeft.getMinutes() > 0 || timeLeft.getSeconds() > 0) {
						timeLeft.subtract(new Time(0, 1));
					}else if(elapsedTime == ((int)arena.getFlag("timeLimit").getValue()) + 1) {
						gameOver();
						this.cancel();
						timer = null;
					}
				}
				
			}.runTaskTimer(instance, 20L, 20L);
		}
	}
	
	private void join(final Player player) {
		// This player is attempting to join our game.
		if(!isPlaying(player)) {
			final GamePlayer gPlayer = new GamePlayer(instance, player, arena);
			players.add(gPlayer);
			
			// Let's make all the players visible.
			changePlayerVisibilityFor(gPlayer, true);
			
			if(state == GameState.WAITING) {
				// Let's have the player go to a spawn.
				final Spawn lobbySpawn = getRandomSpawn(SpawnType.LOBBY);
				if(lobbySpawn != null) {
					player.teleport(lobbySpawn.getLocation(), TeleportCause.PLUGIN);
				}
				
				// Show shop
				final ItemStack item = new ItemStack(instance.getSettings().getShopItem(), 1);
				final ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(instance.getLocalizer().getColoredString("shop"));
				item.setItemMeta(meta);
				player.getInventory().setItem(4, item);
				player.updateInventory();
				
				if(lobbyTimer == null) {
					startLobbyTimer();
				}
				
				String joinMsg = instance.getLocalizer().getString("playerJoined");
				joinMsg = joinMsg.replaceAll("\\{player\\}", player.getName());
				joinMsg = joinMsg.replaceAll("\\{players\\}", String.valueOf(players.size()));
				joinMsg = joinMsg.replaceAll("\\{maxPlayers\\}", String.valueOf((int)arena.getFlag("maxPlayers").getValue()));
				joinMsg = ChatColor.translateAlternateColorCodes('&', joinMsg);
				sendMessageToPlayers(joinMsg);
				
				if(!enoughPlayers()) {
					String moreMsg = instance.getLocalizer().getString("morePlayersNeeded");
					int neededPlayers = (int) ((float) (int)arena.getFlag("maxPlayers").getValue() * 0.5);
					moreMsg = moreMsg.replaceAll("\\{neededPlayers\\}", String.valueOf(neededPlayers));
					moreMsg = ChatColor.translateAlternateColorCodes('&', moreMsg);
					sendMessageToPlayers(moreMsg);
				}else if(prepareTimer == null) {
					sendMessageToPlayers(instance.getLocalizer().getColoredString("startingCountdown"));
				}
				
			}else if(state == GameState.IN_GAME) {
				// Let's start spectating.
				gPlayer.startSpectating();
				updateSpectators();
			}
			Bukkit.getServer().getPluginManager().callEvent(new GameJoinEvent(this, gPlayer));
			updateSigns();
		}
	}
	
	private void sendMessageToPlayers(final String message) {
		for(GamePlayer player : players) {
			player.getPlayer().sendMessage(message);
		}
	}
	
	
	
	private boolean enoughPlayers() {
		final double percentage = 0.5;
		float maxPlayers = (int)arena.getFlag("maxPlayers").getValue();
		if(maxPlayers == 0) {
			maxPlayers = 1;
		}
		return ((float)players.size() / maxPlayers) >= percentage;
	}
	
	private void gameOver() {
		if(endOfRound) return;
		final Database db = instance.getStatsDatabase();
		this.endOfRound = true;
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		for(final GamePlayer player : players) {
			if(eggBroken) {
				// The players won!
				String pointsMsg = instance.getLocalizer().getString("pointsEarned");
				pointsMsg = pointsMsg.replaceAll("\\{points\\}", String.valueOf(player.getPointsEarned()));
				pointsMsg = ChatColor.translateAlternateColorCodes('&', pointsMsg);
				TextUtil.sendTitle(player.getPlayer(), 
					instance.getLocalizer().getColoredString("missionCompleted"), 
					pointsMsg, 
				1, 4, 1);
				final Player pObj = player.getPlayer();
				pObj.getWorld().playSound(pObj.getLocation(), instance.getSettings().getWinSound(), 1F, 1F);
			}else {
				// They lost!
				TextUtil.sendTitle(player.getPlayer(), 
						instance.getLocalizer().getColoredString("missionFailed"), 
						instance.getLocalizer().getColoredString("noPoints"), 
				1, 4, 1);
				db.setGamesPlayed(player.getPlayer(), db.getGamesPlayed(player.getPlayer()) + 1);
				db.setGamesLost(player.getPlayer(), db.getGamesLost(player.getPlayer()) + 1);
				final Player pObj = player.getPlayer();
				pObj.getWorld().playSound(pObj.getLocation(), instance.getSettings().getLoseSound(), 1F, 1F);
			}
		}
		instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
			public void run() {
				reset();
			}
		}, 200L);
	}
	
	private void updateSpectators() {
		final HashSet<GamePlayer> spectators = getSpectators();
		for(GamePlayer spectator : spectators) {
			changePlayerVisibilityFor(spectator, false);
			for(GamePlayer player : players) {
				if(!player.equals(spectator)) {
					spectator.getPlayer().showPlayer(player.getPlayer());
				}
			}
		}
		for(GamePlayer player : players) {
			if(player.getState() == PlayerState.PLAYING) {
				for(GamePlayer spectator : spectators) {
					player.getPlayer().hidePlayer(spectator.getPlayer());
				}
			}
		}
	}
	
	public void handleEggBroken(final GamePlayer breaker) {
		this.eggBroken = true;
		this.eggLocation.getBlock().setType(Material.AIR);
		final Database db = instance.getStatsDatabase();
		final Settings settings = instance.getSettings();
		
		// Let's award everyone points.
		for(GamePlayer player : players) {
			int pointsToGive = settings.getAliveWinPoints();
			
			// Let's award the breaker more points.
			if(player.equals(breaker) || players.size() == 1) pointsToGive = settings.getEggBrokenPoints();
			
			if(player.getState() == PlayerState.SPECTATING) pointsToGive = settings.getDeathWinPoints();
			
			db.setPoints(player.getPlayer(), db.getPoints(player.getPlayer()) + pointsToGive);
			db.setGamesPlayed(player.getPlayer(), db.getGamesPlayed(player.getPlayer()) + 1);
			db.setGamesWon(player.getPlayer(), db.getGamesWon(player.getPlayer()) + 1);
			player.setPointsEarned(pointsToGive);
			
			// Let's make all the players visible.
			changePlayerVisibilityFor(player, true);
			
			// Let's shoot a firework at the player's location.
			final FireworkEffect effect = FireworkEffectBuilder.buildRandomEffect();
			final Location pLoc = player.getPlayer().getLocation();
			final Location fLoc = new Location(pLoc.getWorld(), pLoc.getX() + 0.5, pLoc.getY() + 1, pLoc.getZ() + 0.5);
			final Firework firework = (Firework) pLoc.getWorld().spawnEntity(fLoc, EntityType.FIREWORK);
			final FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(1);
			firework.setFireworkMeta(meta);
		}
		gameOver();
	}
	
	public void handleDeath(final GamePlayer player) {
		for(final Perk perk : player.getPerks()) {
			if(perk instanceof RespawnItem) {
				this.respawnPlayer(player);
				player.removePerk(perk);
				return;
			}
		}

		startSpectating(player);
		// The last player died.
		if(getSpectators().size() > 0 && getSpectators().size() == players.size()) {
			// Game over, they lost. Let's wait 2 seconds then do game over.
			instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				public void run() {
					gameOver();
				}
			}, 40L);
		}
	}
	
	public void startSpectating(final GamePlayer player) {
		// Let's start spectating.
		if(player != null) {
			player.startSpectating();
			updateSpectators();
			if(players.size() == 1 && !endOfRound) {
				instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
					public void run() {
						gameOver();
					}
				}, 40L);
			}
		}
	}
	
	public void changePlayerVisibilityFor(final GamePlayer player, final boolean show) {
		final HashSet<GamePlayer> otherPlayers = new HashSet<GamePlayer>(players);
		otherPlayers.remove(player);
		
		for(GamePlayer comrade : otherPlayers) {
			if(show) {
				player.getPlayer().showPlayer(comrade.getPlayer());
			}else {
				player.getPlayer().hidePlayer(comrade.getPlayer());
			}
		}
	}
	
	public HashSet<GamePlayer> getSpectators() {
		final HashSet<GamePlayer> spectators = new HashSet<GamePlayer>();
		for(GamePlayer player : players) {
			if(player.getState() == PlayerState.SPECTATING) {
				spectators.add(player);
			}
		}
		return spectators;
	}
	
	public void respawnPlayer(final GamePlayer player) {
		for(GamePlayer gPlayer : players) {
			if(!gPlayer.getPlayer().equals(player)) {
				gPlayer.getPlayer().showPlayer(player.getPlayer());
			}
		}
		player.prepare();
		player.start(getRandomSpawn(SpawnType.PLAYER));
		final Player pObj = player.getPlayer();
		pObj.getWorld().playSound(pObj.getLocation(), instance.getSettings().getRespawnSound(), 1F, 1F);
		pObj.getWorld().playEffect(pObj.getLocation(), Effect.SMOKE, 10);
		updateSpectators();
	}
	
	public boolean attemptJoin(final Player player) {
		if(players.size() != (int)arena.getFlag("maxPlayers").getValue()) {
			if(state == GameState.WAITING || state == GameState.IN_GAME) {
				join(player);
			}else {
				player.sendMessage(instance.getLocalizer().getMessage("gameDisabled"));
			}
		}else {
			player.sendMessage(instance.getLocalizer().getMessage("gameFull"));
		}
		return false;
	}
	
	public void leave(final Player player) {
		final GamePlayer gPlayer = getGamePlayerFromPlayer(player);
		if(gPlayer == null) {
			instance.getLogger().warning(String.format("%s attempted to leave a game they weren't in!!!", player.getName()));
			return;
		}else {
			gPlayer.reset();
			players.remove(gPlayer);
			if(players.size() == 0) {
				// Let's reset, no need to do gameOver.
				this.reset();
			}
		}
		Bukkit.getServer().getPluginManager().callEvent(new GameLeaveEvent(this, gPlayer));
		updateSigns();
	}
	
	public void updateSigns() {
		for(ArenaSign sign : arena.getArenaSigns()) {
			sign.update();
		}
	}
	
	public boolean isPlaying(final Player player) {	
		for(GamePlayer gPlayer : players) {
			if(gPlayer.getPlayer().equals(player)) {
				return true;
			}
		}
		return false;
	}
	
	public HashSet<Spawn> getSpawnsOfType(final SpawnType type) {
		final HashSet<Spawn> filteredSpawns = new HashSet<Spawn>();
		for(Spawn spawn : arena.getSpawns()) {
			if(spawn.getType().name().equalsIgnoreCase(type.name())) {
				filteredSpawns.add(spawn);
			}
		}
		return filteredSpawns;
	}
	
	public Spawn getRandomSpawn(final SpawnType type) {
		final ArrayList<Spawn> spawns = new ArrayList<Spawn>();
		for(final Spawn spawn : getSpawnsOfType(type)) {
			spawns.add(spawn);
		}
		if(spawns.size() > 0) {
			final Spawn spawn = spawns.get(ThreadLocalRandom.current().nextInt(0, spawns.size()));
			return spawn;
		}
		return null;
	}
	
	public GameState getState() {
		return this.state;
	}
	
	private void placeEgg() {
		this.eggBroken = false;
		
		// Let's destroy other eggs in our area before we place another.
		final BlockVector minVect = arena.getRegion().getMinimumPoint();
		final BlockVector maxVect = arena.getRegion().getMaximumPoint();
		final Location min = new Location(arena.getWorld(), minVect.getX(), minVect.getY(), minVect.getZ());
		final Location max = new Location(arena.getWorld(), maxVect.getX(), maxVect.getY(), maxVect.getZ());
		
		for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for(int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					final Location blockLoc = new Location(arena.getWorld(), x, y, z);
					if(blockLoc.getBlock().getType() == Material.DRAGON_EGG) {
						blockLoc.getBlock().setType(Material.AIR);
					}
				}
			}
		}
		
		// Let's place a new egg.
		if(getSpawnsOfType(SpawnType.EGG).size() > 0) {
			final Spawn choice = getRandomSpawn(SpawnType.EGG);
			final Location loc = choice.getLocation();
			
			// Let's set the block as an egg.
			loc.getBlock().setType(Material.DRAGON_EGG);
			this.eggLocation = loc;
		}
	}
	
	public int getPrepareTimeLeft() {
		return this.prepareTimeLeft;
	}
	
	public Time getTimeLeft() {
		return this.timeLeft;
	}
	
	public void reset() {
		this.endOfRound = false;
		// Let's stop the timer.
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		// Let's make all spawns available again.
		availableSpawns.clear();
		for(final Spawn spawn : arena.getSpawns()) {
			if(spawn.getType() == SpawnType.PLAYER) {
				if(!availableSpawns.contains(spawn)) {
					availableSpawns.add(spawn);
				}
			}
		}
		
		// Let's reset all our players.
		Iterator<GamePlayer> it = players.iterator();
		while(it.hasNext()) {
			final GamePlayer player = it.next();
			player.reset();
			instance.getGameEvents().givePlayerDefaultBoard(player.getPlayer());
		}
		
		// Let's clear the players set out.
		players.clear();
		
		// Let's remove drops in our area.
		final Collection<Item> entities = arena.getWorld().getEntitiesByClass(Item.class);
		
		for(Item entity : entities) {
			if(arena.getRegion().contains(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ())) {
				entity.remove();
			}
		}
		
		// Let's restore the arena to its initial state.
		try {
			final File schematicFile = new File(instance.getDataFolder() + String.format("/arenas/%s", arena.getSchematicName()));
			schemMgr.pasteSchematic(schemMgr.loadSchematic(schematicFile), arena.getWorld(), true, false);
		} catch (DataException | IOException | NullPointerException | MaxChangedBlocksException e) {
			instance.getLogger().severe(String.format("Could not restore Arena %s back to its original state.", instance.getArenaManager().correctSpaces(arena.getName())));
		}
		
		// Let's place a new egg.
		this.placeEgg();
		this.state = GameState.WAITING;
		this.updateSigns();
	}
	
	public boolean isEndOfRound() {
		return this.endOfRound;
	}
	
	public Location getEggLocation() {
		return this.eggLocation;
	}
	
	public GamePlayer getGamePlayerFromPlayer(final Player player) {
		for(GamePlayer gPlayer : players) {
			if(gPlayer.getPlayer().equals(player)) {
				return gPlayer;
			}
		}
		return null;
	}
	
	public HashSet<GamePlayer> getPlayers() {
		return this.players;
	}
	
	public Arena getArena() {
		return this.arena;
	}
}
