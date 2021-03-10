package mc.decodedlogic.craftrp.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lib.decodedlogic.gui.ButtonElement;
import mc.decodedlogic.craftrp.entity.CRPDoor;
import mc.decodedlogic.craftrp.ownable.Owner;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import net.md_5.bungee.api.ChatColor;

public class DoorGUI extends RPGUI {
    
    private final CRPDoor DOOR;
    
    public DoorGUI(CRPPlayer rpPlayer, CRPDoor door) {
        super(rpPlayer);
        this.DOOR = door;
        
        if(DOOR != null && DOOR.isRegistered()) generate();
    }

    @Override
    public void generate() {
        PLAYER.setFocusedEntity(null);
        
        this.inv = Bukkit.createInventory(null, 27, "Manage Door");
        
        ButtonElement setTitleBtn = new ButtonElement(this, 4, () -> {
            
        });
        
        setTitleBtn.setItem(getSetTitleItem());
        this.add(setTitleBtn);
        
        ButtonElement toggleLockBtn = new ButtonElement(this, 10, () -> {
            if(!DOOR.isOpen()) {
                DOOR.setLocked(!DOOR.isLocked(), PLAYER.getPlayer());
                update();
            }
        });

        toggleLockBtn.setItem(getToggleLockItem());
        this.add(toggleLockBtn);
        
        ButtonElement setOwnersBtn = new ButtonElement(this, 11, () -> {
            
        });
        
        setOwnersBtn.setItem(getSetOwnersItem());
        this.add(setOwnersBtn);
        
        ButtonElement lpAlarmBtn = new ButtonElement(this, 12, () -> {
            
        });
        
        lpAlarmBtn.setItem(getLockpickAlarmItem());
        this.add(lpAlarmBtn);
        
        ButtonElement sellDoorBtn = new ButtonElement(this, 15, () -> {
            
        });
        
        sellDoorBtn.setItem(getSellDoorItem());
        this.add(sellDoorBtn);
        
        this.update();
    }
    
    public void update() {
        ButtonElement toggleLockBtn = (ButtonElement) this.get(10);
        toggleLockBtn.setItem(getToggleLockItem());
        
        ButtonElement setOwnersBtn = (ButtonElement) this.get(11);
        setOwnersBtn.setItem(getSetOwnersItem());
        
        PLAYER.getPlayer().updateInventory();
        super.update();
    }
    
    public ItemStack getSetTitleItem() {
        ItemBuilder stItem = new ItemBuilder(Material.NAME_TAG, ChatColor.translateAlternateColorCodes('&', "&7&lSet Door Title"));
        return stItem.get();
    }
    
    public ItemStack getToggleLockItem() {
        ItemBuilder tLItem = new ItemBuilder(Material.LEVER, ChatColor.translateAlternateColorCodes('&', "&7&lLock/Unlock"));
        tLItem.setDescription(Arrays.asList(ChatColor.WHITE + String.format("Door is %s", 
                DOOR.getStateName()) + ChatColor.WHITE + "."));
        return tLItem.get();
    }
    
    public ItemStack getSetOwnersItem() {
        ItemBuilder sOItem = new ItemBuilder(Material.PLAYER_HEAD, ChatColor.translateAlternateColorCodes('&', "&7&lSet Owners..."));
        sOItem.setDescription(getOwnersList());
        return sOItem.get();
    }
    
    public ItemStack getLockpickAlarmItem() {
        String title = (DOOR.hasLockpickAlarm()) ? 
                ChatColor.translateAlternateColorCodes('&', "&7&lLockpick Alarm") : 
                    ChatColor.translateAlternateColorCodes('&', "&7&lPurchase Lockpick Alarm");
        String desc = (DOOR.hasLockpickAlarm()) ? ChatColor.RED + "ARMED" : 
            ChatColor.translateAlternateColorCodes('&', String.format("&fCost: &a$%s", DOOR.getLockpickAlarmCost()));
        
        ItemBuilder lpAlarmItem = new ItemBuilder(Material.TRIPWIRE_HOOK, title);
        lpAlarmItem.setDescription(Arrays.asList(desc));
        return lpAlarmItem.get();
    }
    
    public ItemStack getSellDoorItem() {
        ItemBuilder sdItem = new ItemBuilder(Material.EMERALD, ChatColor.translateAlternateColorCodes('&', "&7&lSell Door"));
        sdItem.setDescription(Arrays.asList(ChatColor.translateAlternateColorCodes('&', String.format("&fValue: &a$%s", DOOR.getValue()))));
        return sdItem.get();
    }
    
    public List<String> getOwnersList() {
        List<String> ownerList = new ArrayList<String>();
        
        for(Owner owner : DOOR.getOwners()) {
            if(owner instanceof CRPPlayer) {
                CRPPlayer player = (CRPPlayer) owner;
                ownerList.add(ChatColor.WHITE + ChatColor.stripColor(player.getPlayer().getDisplayName()));
            }
        }
        
        return ownerList;
    }

    @Override
    public void onClosed() {
        // TODO Auto-generated method stub

    }

}
