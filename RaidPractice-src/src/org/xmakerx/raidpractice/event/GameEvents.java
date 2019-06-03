package org.xmakerx.raidpractice.event;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xmakerx.raidpractice.Database;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.ScoreboardBank;
import org.xmakerx.raidpractice.arena.Arena;
import org.xmakerx.raidpractice.arena.ArenaManager;
import org.xmakerx.raidpractice.arena.ArenaSign;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.Game.GameState;
import org.xmakerx.raidpractice.arena.GamePlayer;
import org.xmakerx.raidpractice.arena.GamePlayer.PlayerState;
import org.xmakerx.raidpractice.menu.MenuManager;
import org.xmakerx.raidpractice.menu.ShopMenu;

public class GameEvents implements Listener {
	
	final RaidPractice instance;
	final ArenaManager arenaManager;
	final Localizer localizer;
	final Database db;
	
	public GameEvents(final RaidPractice main) {
		this.instance = main;
		this.arenaManager = instance.getArenaManager();
		this.localizer = main.getLocalizer();
		this.db = instance.getStatsDatabase();
	}
	
	public void givePlayerDefaultBoard(final Player player) {
		final ScoreboardBank bank = instance.getSettings().getScoreboardBank();
		bank.prepareForUse(player, bank.getServerBoard(), null).build(player);
	}
	
	@EventHandler
	public void onGameLeave(final GameLeaveEvent evt) {
		givePlayerDefaultBoard(evt.getGamePlayer().getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(final PlayerQuitEvent evt) {
		final Player player = evt.getPlayer();
		final Game game = arenaManager.getGameFromPlayer(player);
		if(game != null) {
			game.leave(player);
		}
		for(final Player oPlayer : Bukkit.getOnlinePlayers()) {
			if(instance.getArenaManager().getGameFromPlayer(oPlayer) == null) {
				givePlayerDefaultBoard(oPlayer);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent evt) {
		final Entity ent = evt.getEntity();
		if(ent instanceof Player) {
			final Player player = (Player)ent;
			final Game game = arenaManager.getGameFromPlayer(player);
			if(game != null) {
				final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
				if(gPlayer != null) {
					if(gPlayer.getState() == PlayerState.SPECTATING) {
						evt.setCancelled(true);
					}else if(gPlayer.getState() == PlayerState.PLAYING && player.getHealth() - evt.getDamage() <= 0) {
						game.handleDeath(gPlayer);
						evt.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(final PlayerPickupItemEvent evt) {
		final Player player = evt.getPlayer();
		final Game game = arenaManager.getGameFromPlayer(player);
		if(game != null) {
			final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
			if(gPlayer != null) {
				if(gPlayer.getState() == PlayerState.SPECTATING) {
					evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(final PlayerDropItemEvent evt) {
		final Player player = evt.getPlayer();
		final Game game = arenaManager.getGameFromPlayer(player);
		if(game != null) {
			final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
			if(gPlayer != null) {
				evt.setCancelled(!((boolean)game.getArena().getFlag("itemDrops").getValue()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent evt) {
		final Player player = evt.getEntity();
		final Game game = arenaManager.getGameFromPlayer(player);
		if(game != null) {
			final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
			if(gPlayer != null) {
				game.handleDeath(gPlayer);
			}
		}
	}
	
	@EventHandler
	public void onBlockBurned(final BlockBurnEvent evt) {
		final Block block = evt.getBlock();
		for(final Arena arena : arenaManager.getArenas()) {
			if(arena.getRegion().contains(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ())) {
				evt.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler
	public void onFireSpread(final BlockSpreadEvent evt) {
		final Block block = evt.getSource();
		if(block.getType() == Material.FIRE) {
			for(final Arena arena : arenaManager.getArenas()) {
				if(arena.getRegion().contains(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ())) {
					evt.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onSignEdit(final SignChangeEvent evt) {
		final Player player = evt.getPlayer();

		if(evt.getLine(0).equalsIgnoreCase("[raidpractice]") || evt.getLine(0).equalsIgnoreCase("[rp]")) {
			// Let's make sure the editor has permission to make a RP sign.
			if(player.hasPermission("rp.create")) {
				final String arenaName = evt.getLine(1);
				if(arenaName.isEmpty()) {
					player.sendMessage(localizer.getMessage("mustSpecifyArenaName"));
					evt.setLine(0, ChatColor.DARK_RED + "[RaidPractice]");
				}else if(arenaManager.isNameAvailable(arenaName)) {
					player.sendMessage(localizer.getMessage("arenaNonExistant"));
					evt.setLine(0, ChatColor.DARK_RED + "[RaidPractice]");
				}else {
					// We have to wait a tick for the sign to update.
					Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
						public void run() {
							final Arena arena = arenaManager.getArenaByName(arenaManager.correctUnderscores(arenaName));
							arena.addArenaSign(new ArenaSign(instance, arena, (Sign) evt.getBlock().getState()));
							arenaManager.saveArena(arena);
							
							String signMsg = localizer.getString("signCreated");
							signMsg = signMsg.replaceAll("\\{arena\\}", arena.getName());
							player.sendMessage(localizer.getMessage(ChatColor.translateAlternateColorCodes('&', signMsg)));
						}
					}, 1L);
				}
			}else {
				player.sendMessage(localizer.getMessage("noPermission"));
			}
		}
	}
	
	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent evt) {
		final Block block = evt.getBlock();
		evt.setCancelled(arenaManager.isBlacklisted(block.getLocation()));
		
		if(block.getType() == Material.DRAGON_EGG) {
			for(Game game : arenaManager.getGames()) {
				if(block.getLocation().equals(game.getEggLocation())) {
					evt.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent evt) {
		final Block block = evt.getBlock();
		final Player player = evt.getPlayer();
		boolean hasEditSession = false;
		
		if(player != null) {
			hasEditSession = instance.getCommands().getArenaCommands().hasEditSession(player);
		}
		
		if(arenaManager.isClaim(block.getLocation()) && !hasEditSession) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHungry(final FoodLevelChangeEvent evt) {
		final Player player = (Player)evt.getEntity();
		final Game game = arenaManager.getGameFromPlayer(player);
		
		if(game != null) {
			evt.setCancelled(!((boolean)game.getArena().getFlag("hunger").getValue()));
		}
	}
	 
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent evt) {
		final Block block = evt.getBlock();
		final Player player = evt.getPlayer();
		boolean hasEditSession = false;
		
		if(player != null) {
			hasEditSession = instance.getCommands().getArenaCommands().hasEditSession(player);
		}

		if(arenaManager.isClaim(block.getLocation()) && !hasEditSession || arenaManager.isBlacklisted(block.getLocation()) && !hasEditSession) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent evt) {
		for(Block block : new ArrayList<Block>(evt.blockList())) {
			if(arenaManager.isBlacklisted(block.getLocation())) {
				evt.blockList().remove(block);
			}
		}
	}
	
	private void handleSpectatorItem(final GamePlayer gPlayer, final ItemStack item) {
		final Player player = gPlayer.getPlayer();
		final Game game = arenaManager.getGameFromPlayer(player);
		
		if(item.hasItemMeta()) {
			final ItemMeta meta = item.getItemMeta();
			if(meta.getDisplayName().equalsIgnoreCase(localizer.getColoredString("exitArena"))) {
				game.leave(player);
			}else if(meta.getDisplayName().equalsIgnoreCase(localizer.getColoredString("teleportToPlayer"))) {
				final ArrayList<GamePlayer> alivePlayers = new ArrayList<GamePlayer>();
				for(final GamePlayer oPlayer : game.getPlayers()) {
					if(oPlayer.getState() == PlayerState.PLAYING) {
						alivePlayers.add(oPlayer);
					}
				}
				final GamePlayer tpPlayer = alivePlayers.get(ThreadLocalRandom.current().nextInt(0, alivePlayers.size()));
				player.teleport(tpPlayer.getPlayer().getLocation(), TeleportCause.PLUGIN);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent evt) {
		final Player player = evt.getPlayer();
		final Game game = arenaManager.getGameFromPlayer(player);
		final Block block = evt.getClickedBlock();
		final Action action = evt.getAction();
		GamePlayer gPlayer = null;
		
		if(game != null) {
			gPlayer = game.getGamePlayerFromPlayer(player);
		}
		
		if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
			if(game == null) {
				for(Arena arena : arenaManager.getArenas()) {
					for(ArenaSign sign : arena.getArenaSigns()) {
						if(sign.getSign().getLocation().equals(block.getLocation())) {
							final Arena editArena = instance.getCommands().getArenaCommands().getArenaEditing(player);
							if(editArena != null && editArena.equals(arena)) {
								break;
							}
							arenaManager.getGameFromArena(arena).attemptJoin(player);
							
							if(player.getGameMode() == GameMode.CREATIVE) {
								evt.setCancelled(true);
							}
							return;
						}
					}
				}
			}else if(game != null && block.getType() == Material.DRAGON_EGG) {
				// A GamePlayer interacted with a dragon egg.
				if(gPlayer != null && gPlayer.getState() == PlayerState.PLAYING) {
					game.handleEggBroken(gPlayer);
					evt.setCancelled(true);
				}
			}else if(game != null && gPlayer != null) {
				if(gPlayer.getState() == PlayerState.SPECTATING) {
					if(player.getInventory().getItemInMainHand() != null) {
						handleSpectatorItem(gPlayer, player.getInventory().getItemInMainHand());
						evt.setCancelled(true);
					}
				}
			}
		}else if(game != null && gPlayer != null) {
			if(gPlayer.getState() == PlayerState.SPECTATING) {
				handleSpectatorItem(gPlayer, player.getInventory().getItemInMainHand());
			}else if(game.getState() == GameState.WAITING && player.getInventory().getItemInMainHand() != null) {
				if(player.getInventory().getItemInMainHand().getType() == instance.getSettings().getShopItem()) { 
					MenuManager.checkoutMenu(player, new ShopMenu(instance, player));
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent evt) {
		final Entity attacker = evt.getDamager();
		final Entity victim = evt.getEntity();
		
		if(attacker instanceof Player && victim instanceof Player) {
			final Player damager = (Player)attacker;
			final Player hurt = (Player)victim;
			final Game attackerGame = arenaManager.getGameFromPlayer(damager);
			
			if(attackerGame != null) {
				final boolean pvpEnabled = (boolean) attackerGame.getArena().getFlag("pvp").getValue();
				final boolean sameGames = attackerGame.equals(arenaManager.getGameFromPlayer(hurt));
						
				if(sameGames && !pvpEnabled || sameGames && attackerGame.isEndOfRound() || sameGames && attackerGame.getState() == GameState.WAITING) {
					evt.setCancelled(true);
				}
			}
		}
	}
}
