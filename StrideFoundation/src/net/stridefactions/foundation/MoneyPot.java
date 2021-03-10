package net.stridefactions.foundation;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MoneyPot {
	
	private static double balance = 0;
	private static double maxBalance = 200;
	private static int progress = 0;
	
	public static void setMaxBalance(final double maxBalance) {
		MoneyPot.maxBalance = maxBalance;
	}
	
	public static double getMaxBalance() {
		return MoneyPot.maxBalance;
	}
	
	public static void setBalance(final double balance, final boolean isAdjust) {
		double previousBalance = MoneyPot.balance;
		int previousProgress = MoneyPot.progress;
		
		MoneyPot.balance = balance;
		MoneyPot.progress = (int) Math.round((balance / maxBalance) * 100);
		HologramManager.updateHolograms();
		
		if(previousBalance < getMaxBalance() && balance >= maxBalance) {
			StrideFoundation.getInstance().startEffects();
			
			new BukkitRunnable() {
				
				public void run() {
					setBalance(0, true);
					this.cancel();
				}
				
			}.runTaskLater(StrideFoundation.getInstance(), StrideFoundation.getInstance().getSettings().getPotResetTime() * 20);
		}
		
		if(!isAdjust) {
			for(Map.Entry<Integer, List<String>> entry : StrideFoundation.getInstance().getSettings().getCommands().entrySet()) {
				final List<String> cmds = entry.getValue();
				final int trigger = entry.getKey();
				
				if(previousProgress < trigger && progress >= trigger) {
					for(String cmd : cmds) {
						if(cmd.contains("{player}")) {
							for(final Player player : StrideFoundation.getInstance().getServer().getOnlinePlayers()) {
								cmd = cmd.replaceAll("\\{player\\}", player.getName());
								StrideFoundation.getInstance().getServer().dispatchCommand(
										StrideFoundation.getInstance().getServer().getConsoleSender(), cmd);
							}
						}else {
							StrideFoundation.getInstance().getServer().dispatchCommand(
									StrideFoundation.getInstance().getServer().getConsoleSender(), cmd);
						}
					}
				}
			}
		}
	}
	
	public static double getBalance() {
		return MoneyPot.balance;
	}
	
	public static int getProgress() {
		return MoneyPot.progress;
	}
	
	public static String getProgressString() {
		final Settings settings = StrideFoundation.getInstance().getSettings();
		String msg = settings.getString("progressPerct");
		msg = msg.replaceAll("\\{progress\\}", String.valueOf(getProgress()));
		return msg;
	}
	
	public static String getProgressBar() {
		int greenSquares = Math.round(5 * ((float) balance / (float) maxBalance));
		greenSquares = (greenSquares > 5) ? 5 : greenSquares;
		String progressTxt = StrideFoundation.getInstance().getSettings().getString("progress").concat(" ");
		for(int g = 0; g < greenSquares; g++) {
			progressTxt = progressTxt.concat("&a■&r");
		}
		
		for(int r = 0; r < (5 - greenSquares); r++) {
			progressTxt = progressTxt.concat("&7■&r");
		}
		
		return StrideFoundation.getInstance().getSettings().color(progressTxt);
	}
}
