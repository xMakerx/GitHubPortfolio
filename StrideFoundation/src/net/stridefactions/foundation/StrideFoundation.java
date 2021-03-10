package net.stridefactions.foundation;

import io.netty.util.internal.ThreadLocalRandom;

import java.util.ArrayList;
import java.util.UUID;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.stridefactions.foundation.gui.MenuManager;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;

public class StrideFoundation extends JavaPlugin implements Listener {
	
	private static StrideFoundation instance;
	private Settings settings;
	private Data data;
	private Economy economy;
	private MenuManager menuMgr;
	
	private ArrayList<Item> effectDrops = new ArrayList<Item>();
	
	// Let's initialize the boolean to prevent an error.
	boolean successfulStart = false;
	
	public void onEnable() {
		StrideFoundation.instance = this;
		
		// Let's check for our dependencies.
		final Vault vault = (Vault) getRequiredDependency("Vault", Vault.class);
		final HolographicDisplays holograms = (HolographicDisplays) getRequiredDependency("HolographicDisplays", HolographicDisplays.class);
		if(holograms == null || vault == null) return;
		
		// Let's set up the economy.
		final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp != null) {
			final Plugin plugin = rsp.getPlugin();
			this.economy = rsp.getProvider();
			getLogger().info(String.format("Successfully hooked into %s [%s]!", 
					plugin.getDescription().getName(), 
			plugin.getDescription().getVersion()));
		}else {
			getLogger().severe("An economy plugin could not be found. You must install one to use this plugin.");
			return;
		}
		
		// Let's begin our services.
		this.settings = new Settings();
		this.settings.loadData();
		
		this.menuMgr = new MenuManager();
		
		// Let's register our events.
		getServer().getPluginManager().registerEvents(menuMgr, this);
		getServer().getPluginManager().registerEvents(this, this);
		
		// Let's set up the commands.
		final Commands cmds = new Commands();
		getCommand("pot").setExecutor(cmds);
		getCommand("foundation").setExecutor(cmds);
		
		this.data = new Data();
		this.successfulStart = true;
		
		new BukkitRunnable() {
			
			public void run() {
				instance.getData().loadData();
			}
		}.runTaskLater(this, 5L);
	}
	
	public void onDisable() {
		if(successfulStart) {
			// Let's end all our services.
			this.data.saveData();
			HologramManager.clear();
			this.settings = null;
			this.data = null;
			
			for(final UUID uuid : menuMgr.getMenus().keySet()) {
				final Player player = getServer().getPlayer(uuid);
				if(player != null) {
					menuMgr.removeMenu(player);
				}
			}
			
		}else {
			// Potentially a crash or a missing dependency.
		}
	}
	
	public void startEffects() {
		final Location loc = StrideFoundation.getInstance().getData().getEffectBlock();
		final Sound reach = StrideSound.POT_FULL.getSound();
		if(reach != null) {
			loc.getWorld().playSound(loc, reach, 1F, 1F);
		}
		
		if(loc != null) {
			effectDrops.clear();
			
			new BukkitRunnable() {
				
				int neededTicks = 20 * getSettings().getEffectTime();
				int ticks = 0;
				
				public void run() {
					final int firstItemAmt = ThreadLocalRandom.current().nextInt(4, 6) + 1;
					final int secondItemAmt = ThreadLocalRandom.current().nextInt(4, 6) + 1;
					final ItemStack firstItem = StrideItem.EFFECT_01.buildItem();
					final ItemStack secondItem = StrideItem.EFFECT_02.buildItem();
					
					for(int i = 0; i < firstItemAmt; i++) {
						final ItemStack item = firstItem.clone();
						final int x = ThreadLocalRandom.current().nextInt(-6, 6) + 1;
						final int z = ThreadLocalRandom.current().nextInt(-6, 6) + 1;
						final Item drop = loc.getWorld().dropItem(new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z), item);
						effectDrops.add(drop);
					}
					
					for(int i = 0; i < secondItemAmt; i++) {
						final ItemStack item = secondItem.clone();
						final int x = ThreadLocalRandom.current().nextInt(-6, 6) + 1;
						final int z = ThreadLocalRandom.current().nextInt(-6, 6) + 1;
						final Item drop = loc.getWorld().dropItem(new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z), item);
						effectDrops.add(drop);
					}
					
					final Firework fw = (Firework) loc.getWorld().spawnEntity(new Location(loc.getWorld(), loc.getX(), loc.getY() + 4, loc.getZ()), EntityType.FIREWORK);
					final FireworkMeta meta = fw.getFireworkMeta();
					meta.addEffect(FireworkEffect.builder()
						.trail(true)
						.with(Type.BALL_LARGE).withColor(Color.LIME)
						.withColor(Color.WHITE)
					.build());
					fw.setFireworkMeta(meta);
					
					if(ticks + 20 < neededTicks) {
						ticks += 20;
					}else {
						this.cancel();
					}
				}
				
			}.runTaskTimer(this, 1L, 20L);
			
			new BukkitRunnable() {
				
				public void run() {
					for(final Item item : effectDrops) {
						item.remove();
					}
				}
				
			}.runTaskLater(this, 200L);
			
		}else {
			StrideFoundation.getInstance().getLogger().info("No effect block set, no effects will go off.");
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(final PlayerPickupItemEvent evt) {
		final ItemStack item = evt.getItem().getItemStack();
		if(item.isSimilar(StrideItem.EFFECT_01.buildItem()) || item.isSimilar(StrideItem.EFFECT_02.buildItem())) {
			evt.setCancelled(true);
		}
	}
	
	public JavaPlugin getRequiredDependency(final String name, final Class<? extends JavaPlugin> returnType) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(name);
		
		if(plugin != null && returnType.isInstance(plugin)) {
			getLogger().info(String.format("Successfully hooked into %s [%s]!", 
					plugin.getDescription().getName(), 
			plugin.getDescription().getVersion()));
			
			return returnType.cast(plugin);
		}
		
		getLogger().severe(String.format("%s could not be found. You must install it to use this plugin.", name));
		setEnabled(false);
		return null;
	}
	
	public Settings getSettings() {
		return (successfulStart) ? this.settings : null;
	}
	
	public Data getData() {
		return (successfulStart) ? this.data : null;
	}
	
	public Economy getEconomy() {
		return (successfulStart) ? this.economy : null;
	}
	
	public MenuManager getMenuManager() {
		return (successfulStart) ? this.menuMgr : null;
	}
	
	public static StrideFoundation getInstance() {
		return StrideFoundation.instance;
	}
}
