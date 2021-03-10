package mc.decodedlogic.skybattlesuspawners.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mc.decodedlogic.skybattlesuspawners.Settings;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.api.SpawnerTransactionEvent;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerTransactionEvent;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.logging.ModificationRecord;
import mc.decodedlogic.skybattlesuspawners.logging.Transaction;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton.Data;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.milkbowl.vault.economy.Economy;

public class SpawnerMenu extends Menu {
	
	private final MobSpawner spawner;
	
	// Reset button stuff
	private MenuButton resetBtn;
	private long lastResetBtnClickMs;
	private BukkitTask resetTimer;
	
	public SpawnerMenu(Player VIEWER, MobSpawner spawner) {
	    super(VIEWER);
		this.spawner = spawner;
		this.lastResetBtnClickMs = -1L;
		this.resetTimer = null;
	}
	
	public void handleResetStart() {
	    endResetTimer();
	    this.lastResetBtnClickMs = System.currentTimeMillis();
	    
	    int slot = resetBtn.getSlot();
	    ItemStack item = inv.getItem(slot);
	    if(item != null) {
	        ItemMeta meta = item.getItemMeta();
	        
	        List<String> desc = USpawners.get().getSettings().getResetConfirmation();
	        
	        for(int i = 0; i < desc.size(); i++) {
	            String line = desc.get(i);
	            desc.set(i, Utils.mkDisplayReady(replaceVariablesWithValues(line)));
	        }
	        
	        meta.setLore(desc);
	        item.setItemMeta(meta);
	        
	        inv.setItem(slot, item);
	        VIEWER.updateInventory();
	    }
	    
	    this.resetTimer = new BukkitRunnable() {
	        
	        public void run() {
	            endResetTimer();
	        }
	        
	    }.runTaskLater(USpawners.get(), 20L * 5);
	}
	
	public boolean canResetSpawner() {
	    long startMs = lastResetBtnClickMs;
	    return startMs != -1 && (System.currentTimeMillis() < (startMs + 5000));
	}
	
	public void endResetTimer() {
	    if(resetTimer != null) {
	        this.resetTimer.cancel();
	        this.resetTimer = null;
	    }
	    
	    if(resetBtn != null) {
	        // Reset button to default text.
	        resetBtn.generate();
	        VIEWER.updateInventory();
	    }
	    
	    this.lastResetBtnClickMs = -1L;
	}
	
	public void open(boolean openInv) {
		final Settings settings = USpawners.get().getSettings();
		final String pageName = (pageIndex == 0) ? settings.getAddRemovePageName() : settings.getUpgradePageName();
		
		if(openInv) {
		    // Create a new inventory with the correct name for the page.
		    String name = settings.correctLongNames(replaceVariablesWithValues(pageName));
		    this.inv = Bukkit.createInventory(VIEWER, 27, Utils.mkDisplayReady(name));
		}
		
		this.buttons.clear();
		
		for(Data btnData : MenuButton.Data.values()) {
		    if(btnData == Data.CREATION_EVENT || 
		            btnData == Data.TRANSACTION_EVENT || 
		            btnData == Data.DELETION_EVENT || 
		            btnData == Data.OPEN_SPAWNER_LOGS) continue;
			if(btnData.getPageIndex() == pageIndex) {
				final MenuButton btn = new MenuButton(this, btnData);
				if(btnData != Data.EMPTY_SLOT && btnData != Data.UPGRADE_SLOT) {
					btn.generate();
					buttons.add(btn);
					
					if(btnData == Data.RESET) {
					    this.resetBtn = btn;
					}
				}
			}
		}
		
		if(pageIndex == 1) {
		    
		    List<SpawnerUpgrade> upgrades = spawner.getType().getUpgrades();
		    for(int i = 0; i < upgrades.size(); i++) {
		        MenuButton btn = new MenuButton(this, Data.UPGRADE_SLOT, i);
		        btn.generate();
		        this.buttons.add(btn);
		    }
		    
			for(int i = 0; i < inv.getSize(); i++) {
				final ItemStack item = inv.getItem(i);
				
				if((item == null || (item != null && item.getType() == Material.AIR))) {
					final MenuButton btn = new MenuButton(this, Data.EMPTY_SLOT);
					btn.setSlot(i);
					btn.generate();
				}
			}
		}
		
		if(openInv) VIEWER.openInventory(inv);
		VIEWER.updateInventory();
		
		// We have to do this because a close inventory event was called when #openInventory() was called.
		MenuManager.setMenu(VIEWER.getUniqueId(), this);
	}
	
	private String replaceVariablesWithValues(final String baseString) {
		String result = Utils.replaceVariableWith(baseString, "amount", spawner.getSize());
		
		String upgradeName = (spawner.getUpgrade() == SpawnerUpgrade.DEFAULT) ? spawner.getUpgrade().getName() : spawner.getUpgrade().getDisplayName();
		result = Utils.replaceVariableWith(result, "spawnerType", spawner.getTypeName(false));
		result = Utils.replaceVariableWith(result, "upgrade", upgradeName);
		return result;
	}
	
	public void click(InventoryClickEvent evt) {
	    if(spawner == null) {
	        evt.setCancelled(true);
	        evt.setResult(Result.DENY);
	        this.close();
	        return;
	    }
	    
		final Inventory clickedInv = evt.getClickedInventory();
		final Settings settings = USpawners.get().getSettings();
		if(clickedInv != null && clickedInv.equals(this.inv)) {
            PluginManager pm = USpawners.get().getServer().getPluginManager();
			final Data clickedBtn = getClickedButtonType(evt.getRawSlot());
		
			if(clickedBtn == null) return;

			int numUpgrades = spawner.getType().getUpgrades().size();
			if(clickedBtn == Data.NEXT_PAGE && numUpgrades > 0) {
				pageIndex++;
				open(true);
			}else if(clickedBtn == Data.UPGRADE_SLOT) {
			    
			    int index = -1;
			    
			    for(MenuButton btn : buttons) {
			        if(btn.getSlot() == evt.getRawSlot()) {
			            index = btn.getUpgradeIndex();
			            break;
			        }
			    }
			    
				int curUpgradeIndex = spawner.getUpgradeIndex();
				
				if(index > curUpgradeIndex && Utils.canAccessSpawner(VIEWER, spawner)) {
					try {
						final SpawnerUpgrade upgrade = spawner.getType().getUpgrades().get(index);
						final Economy econ = USpawners.get().getEconomy();
						final double balance = econ.getBalance(VIEWER);
						final double cost = upgrade.calculateCost(spawner); 
						
						if(balance >= cost) {
                            // Let's propagate the event for Outposts!
                            // Does this count as outsourcing? Lol
                            SpawnerTransactionEvent ste = new SpawnerTransactionEvent(VIEWER, cost);
                            pm.callEvent(ste);
                            
                            Transaction t = new Transaction(VIEWER.getUniqueId(), 
                                    Transaction.SPAWNER_UPGRADE, cost, upgrade, spawner.size());
                            MobSpawnerTransactionEvent mste = new MobSpawnerTransactionEvent(spawner, VIEWER, t);
                            pm.callEvent(mste);
                            
                            // If the event is cancelled, we won't continue.
                            if(ste.isCancelled() || mste.isCancelled()) return;
                            
                            LocationLog.addRecord(spawner.getLocation(), t);
						    
							spawner.setUpgrade(upgrade);
							spawner.update();
							SpawnerManager.storeSpawnerData(spawner);
							econ.withdrawPlayer(VIEWER, cost);
							
							String chatMsg = settings.getUpgradeMade();
							chatMsg = replaceVariablesWithValues(chatMsg);
							chatMsg = Utils.replaceVariableWith(chatMsg, "cost", cost);
							VIEWER.sendMessage(Utils.mkDisplayReady(chatMsg));
						}else {
							VIEWER.sendMessage(Utils.mkDisplayReady(settings.getNoMoney()));
						}
						
					} catch (IndexOutOfBoundsException e) {
					    throw e;
					}
				}else if(index < curUpgradeIndex) {
					VIEWER.sendMessage(Utils.mkDisplayReady(settings.getDowngrade()));
				}
				
				close();
			}else if(clickedBtn.name().equalsIgnoreCase("RESET")) {
			    
			    if(Utils.canAccessSpawner(VIEWER, spawner)) {
			        if(!canResetSpawner()) {
			            handleResetStart();
			        }else {
                        Transaction t = new Transaction(VIEWER.getUniqueId(), 
                                Transaction.SPAWNER_RESET, 0, 0);
                        MobSpawnerTransactionEvent mste = new MobSpawnerTransactionEvent(spawner, VIEWER, t);
                        pm.callEvent(mste);
                        
                        // If the event is cancelled, we won't continue.
                        if(mste.isCancelled()) return;
                        
                        LocationLog.addRecord(spawner.getLocation(), t);
			            spawner.reset();
			           
			            String msg = USpawners.get().getSettings().getSpawnerReset();
			            msg = this.replaceVariablesWithValues(msg);
			            VIEWER.sendMessage(Utils.color(msg));
			            
			            endResetTimer();
			            
			            buttons.stream().forEach(btn -> btn.generate());
			            this.close();
			        }
			    }
				
			}else {
				final String btnName = clickedBtn.name();
				final String[] split = btnName.split("_");
				int amount = 0;
				
				try {
					amount = Integer.parseInt(split[1]);
				} catch (NumberFormatException e) {
					// This means that we're withdrawing or depositing all.
					amount = -1;
				}
				
				int size = spawner.getSize();
				
				if(btnName.startsWith("DEPOSIT")) {
					int amountLeft = (amount == -1) ? (spawner.getMaxSize() - size) : amount;
					int contributed = 0;
					
					for(int i = 0; i < VIEWER.getInventory().getContents().length; i++) {
						final ItemStack item = VIEWER.getInventory().getContents()[i];
						if(amountLeft == 0) break;
						
						if(Utils.isSpawnerFor(spawner.getType(), spawner.getUpgrade(), item, true)) {
							if(item.getAmount() < amountLeft) {
								amountLeft -= item.getAmount();
								contributed += item.getAmount();
								VIEWER.getInventory().removeItem(item);
							}else {
								contributed += amountLeft;
								item.setAmount(item.getAmount() - amountLeft);
								amountLeft = 0;
								VIEWER.getInventory().setItem(i, item);
							}
						}
					}
					
					if(contributed > 0) {
                        Transaction t = new Transaction(VIEWER.getUniqueId(), 
                                Transaction.SPAWNER_DEPOSIT, 0, contributed);
                        MobSpawnerTransactionEvent mste = new MobSpawnerTransactionEvent(spawner, VIEWER, t);
                        pm.callEvent(mste);
                        
                        // If the event is cancelled, we won't continue.
                        if(mste.isCancelled()) return;
                        
                        LocationLog.addRecord(spawner.getLocation(), t);
					    
						String chatMsg = settings.getDepositMade();
						chatMsg = Utils.replaceVariableWith(chatMsg, "amount", contributed);
						chatMsg = replaceVariablesWithValues(chatMsg);
						VIEWER.sendMessage(Utils.mkDisplayReady(chatMsg));
						
						spawner.setSize(size + contributed);
						VIEWER.updateInventory();
					}else {
						VIEWER.sendMessage(Utils.mkDisplayReady(settings.getNoneToDeposit()));
						close();
					}
					
				}else if(btnName.startsWith("WITHDRAW")) {
					final EntityType type = spawner.getCreatureSpawner().getSpawnedType();
					int amountLeft = -1;
					
					// Let's calculate the amount left.
					if(amount > (size) || amount == -1) {
						amountLeft = size;
					}else {
						amountLeft = amount;
					}
					
					int newQuantity = (size - amountLeft);
					
					final List<ItemStack> returnSpawners = new ArrayList<ItemStack>();
					
					while(amountLeft > 0) {
						int stackAmount = ((amountLeft - 64) >= 0) ? 64 : amountLeft;
						int upgradeIndex = spawner.getUpgradeIndex();
						returnSpawners.add(Utils.generateSpawner(type, stackAmount, upgradeIndex));
						amountLeft -= stackAmount;
					}
					
					final ItemStack[] array = new ItemStack[returnSpawners.size()];
					for(int i = 0; i < array.length; i++) {
						array[i] = returnSpawners.get(i);
					}
					
					final HashMap<Integer, ItemStack> leftovers = VIEWER.getInventory().addItem(array);
					
					for(ItemStack leftover : leftovers.values()) {
						newQuantity += leftover.getAmount();
					}
					
					int withdrawn = (size-newQuantity);
                    Transaction t = new Transaction(VIEWER.getUniqueId(), 
                            Transaction.SPAWNER_WITHDRAW, 0, withdrawn);
                    MobSpawnerTransactionEvent mste = new MobSpawnerTransactionEvent(spawner, VIEWER, t);
                    pm.callEvent(mste);
                    
                    LocationLog.addRecord(spawner.getLocation(), t);
                    
                    if(spawner.size() - withdrawn <= 0) {
                        ModificationRecord d = new ModificationRecord(VIEWER.getUniqueId(), false);
                        LocationLog.addRecord(spawner.getLocation(), d);
                    }
                    
                    // If the event is cancelled, we won't continue.
                    if(mste.isCancelled()) return;
					
					String chatMsg = settings.getWithdrawMade();
					chatMsg = Utils.replaceVariableWith(chatMsg, "amount", withdrawn);
					chatMsg = replaceVariablesWithValues(chatMsg);
					VIEWER.sendMessage(Utils.mkDisplayReady(chatMsg));
					spawner.setSize(newQuantity);
				}
				
				if(!spawner.empty()) {
				    spawner.update();
				    spawner.save();
				    open(false);
				}else {
				    close();
				}
			}
		}
	}
	
	public MobSpawner getSpawner() {
		return this.spawner;
	}
	
}
