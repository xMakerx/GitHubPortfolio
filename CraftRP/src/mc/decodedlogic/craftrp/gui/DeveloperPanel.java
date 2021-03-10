package mc.decodedlogic.craftrp.gui;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import lib.decodedlogic.gui.ButtonElement;
import lib.decodedlogic.gui.Command;
import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.entity.CRPDoor;
import mc.decodedlogic.craftrp.entity.CRPMoneyPrinter;
import mc.decodedlogic.craftrp.entity.CRPMoneyPrinter.PrinterType;
import mc.decodedlogic.craftrp.entity.CRPWindow;
import mc.decodedlogic.craftrp.item.CRPDiagnostic;
import mc.decodedlogic.craftrp.item.CRPLockpick;
import mc.decodedlogic.craftrp.item.CRPRPG;
import mc.decodedlogic.craftrp.item.CRPThrowableDebris;
import mc.decodedlogic.craftrp.item.CRPThrowableDebris.Debris;
import mc.decodedlogic.craftrp.job.Job;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import net.md_5.bungee.api.ChatColor;

public class DeveloperPanel extends RPGUI {

    public DeveloperPanel(CRPPlayer player) {
        super(player);
        generate();
    }

    @Override
    public void generate() {
        this.inv = Bukkit.createInventory(null, 36, "DEVELOPER PANEL");
        ButtonElement spawnDoors = new ButtonElement(this, 10, new Command() {

            @Override
            public void execute() {
                CRPDoor door = new CRPDoor(PLAYER.getPlayer().getTargetBlock(null, 5).getLocation());
                
                if(door.isRegistered()) {
                    PLAYER.getPlayer().sendMessage("Door generated!");
                }
            }
            
        });
        
        ItemBuilder sdItem = new ItemBuilder(Material.SPRUCE_DOOR, "Spawn Normal Door");
        spawnDoors.setItem(sdItem.get());
        this.add(spawnDoors);
        
        ButtonElement spawnCPDoors = new ButtonElement(this, 11, new Command() {

            @Override
            public void execute() {
                
                CRPDoor door = new CRPDoor(PLAYER.getPlayer().getTargetBlock(null, 5).getLocation());
                
                if(door.isRegistered()) {
                    door.addOwner(Job.CIVIL_PROTECTION);
                    door.setOpen(false, true);
                    door.setLocked(true);
                    PLAYER.getPlayer().sendMessage("Door generated!");
                }
            }
            
        });
        
        ItemBuilder cpItem = new ItemBuilder(Material.IRON_DOOR, "Spawn CP Door");
        spawnCPDoors.setItem(cpItem.get());
        this.add(spawnCPDoors);
        
        int index = 12;
        for(PrinterType type : PrinterType.values()) {
            ButtonElement spawnPrinter = new ButtonElement(this, index, new Command() {
                
                public void execute() {
                    Location l = PLAYER.getPlayer().getLocation().clone();
                    
                    if(l.getBlock().isEmpty()) {
                        CRPMoneyPrinter printer = new CRPMoneyPrinter(type, PLAYER);
                        
                        if(printer.canPlaceHere(PLAYER, l)) {
                            PLAYER.getPlayer().setVelocity(new Vector(0, 0.8, 0));
                            new BukkitRunnable() {
                                
                                public void run() {
                                    printer.onPlayerPlace(PLAYER, l);
                                }
                                
                            }.runTaskLater(CraftRP.get(), 4L);
                        }
                    }
                }
            });
            
            ItemBuilder item = new ItemBuilder(Material.LOOM, ChatColor.WHITE + "Spawn " + type.getTextColor() + type.getName() + ChatColor.RESET + " Printer");
            spawnPrinter.setItem(item.get());
            this.add(spawnPrinter);
            index++;
            
        }
        
        ButtonElement spawnRPG = new ButtonElement(this, 16, new Command() {
            
            public void execute() {
                CRPRPG rpg = new CRPRPG();
                ItemStack mcRPG = rpg.generateMCItem();
                PLAYER.getPlayer().getInventory().addItem(mcRPG);
                PLAYER.getPlayer().getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 64));
            }
            
        });
        
        ItemBuilder rpg = new ItemBuilder(Material.BOW, ChatColor.WHITE + "Spawn RPG");
        spawnRPG.setItem(rpg.get());
        this.add(spawnRPG);
        
        ButtonElement spawnGarbage = new ButtonElement(this, 19, new Command() {
            
            public void execute() {
                CRPThrowableDebris debris = new CRPThrowableDebris();
                int id = ThreadLocalRandom.current().nextInt(0, 4);
                
                for(Debris d : Debris.values()) {
                    if(d.getID() == ((byte) id)) {
                        PLAYER.getPlayer().getInventory().addItem(debris.generateMCItem(d));
                    }
                }
            }
            
        });
        
        ItemBuilder garbage = new ItemBuilder(Material.HOPPER, ChatColor.WHITE + "Spawn Garbage");
        spawnGarbage.setItem(garbage.get());
        this.add(spawnGarbage);
        
        ButtonElement spawnWindow = new ButtonElement(this, 20, new Command() {
            
            public void execute() {
                CRPWindow window = new CRPWindow(PLAYER.getPlayer().getTargetBlock(null, 5).getLocation());
                
                if(window.isRegistered()) {
                    
                    PLAYER.getPlayer().sendMessage("Window generated!");
                }
            }
            
        });
        
        ItemBuilder window = new ItemBuilder(Material.GLASS_PANE, ChatColor.WHITE + "Spawn Window");
        spawnWindow.setItem(window.get());
        this.add(spawnWindow);
        
        ButtonElement spawnPick = new ButtonElement(this, 21, new Command() {
            
            public void execute() {
                CRPLockpick lp = new CRPLockpick();
                ItemStack mcLP = lp.generateMCItem();
                PLAYER.getPlayer().getInventory().addItem(mcLP);
            }
            
        });
        
        ItemBuilder pick = new ItemBuilder(Material.WOODEN_HOE, ChatColor.WHITE + "Spawn Lockpick");
        spawnPick.setItem(pick.get());
        this.add(spawnPick);
        
        ButtonElement spawnDiag = new ButtonElement(this, 22, new Command() {
            
            public void execute() {
                CRPDiagnostic d = new CRPDiagnostic();
                PLAYER.getPlayer().getInventory().addItem(d.generateMCItem());
            }
            
        });
        
        ItemBuilder d = new ItemBuilder(Material.COMPARATOR, ChatColor.WHITE + "Spawn Diagnostic");
        spawnDiag.setItem(d.get());
        this.add(spawnDiag);
        
        update();
    }

    @Override
    public void onClosed() {
        // TODO Auto-generated method stub
        
    }

}
