package mc.decodedlogic.craftrp.hologram;

import org.bukkit.Location;
import org.bukkit.Sound;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.entity.CRPEntity;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;
import mc.decodedlogic.craftrp.util.BlockUtils;

public class RPHologram {
    
    public static final Sound DEFAULT_APPEAR_SOUND = Sound.ENTITY_CHICKEN_EGG;
    public static final Sound DEFAULT_DISAPPEAR_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;
    
    protected final HologramContainer CONTAINER;
    
    protected String name;
    protected Hologram hologram;
    protected Hologram originalHG;
    protected boolean matchOriginal;
    
    protected Location origin;
    protected HologramMovement movement;
    protected HologramVisibility visibility;
    
    protected Sound popupSound;
    protected Sound disappearSound;
    
    public RPHologram(HologramContainer container) {
        this.CONTAINER = container;
        
        this.name = "";
        this.hologram = null;
        this.originalHG = null;
        this.matchOriginal = true;
        this.origin = null;
        this.movement = HologramMovement.STATIONARY;
        this.visibility = HologramVisibility.ALL_VIEWERS;
        this.popupSound = null;
        this.disappearSound = null;
    }
    
    public void update(CRPPlayer p) {
        if(hologram != null) {
            if(movement == HologramMovement.DYNAMIC) {
                /*
                Location l = new Location(origin.getWorld(), origin.getX(), origin.getY() + 1.5, origin.getZ() + 0.5);
                Vector v = p.getPlayer().getLocation().getDirection();
                l.subtract(v);*/
                
                Location l = origin.clone();
                l.add(0, 1.5, 0);
                
                Location l2 = p.getPlayer().getEyeLocation().clone();
                l2.add(p.getPlayer().getLocation().getDirection().multiply(0.55d));
                
                Location hL = BlockUtils.getMidpoint(l, l2);
                
                hologram.teleport(hL);
            }
            
            // Let's copy the lines from the original hologram if we need to.
            // This will update the cloned hologram so it matches the original.
            if(matchOriginal && originalHG != null) {
                int ogLines = originalHG.size();
                int hLines = hologram.size();
                
                if(ogLines != hLines) {
                    hologram.clearLines();
                    
                    for(int i = 0; i < ogLines; i++) {
                        HologramLine line = originalHG.getLine(i);
                        
                        if(line instanceof TextLine) {
                            TextLine tl = (TextLine) line;
                            hologram.insertTextLine(i, tl.getText());
                        }else if(line instanceof ItemLine) {
                            ItemLine iTl = (ItemLine) line;
                            hologram.insertItemLine(i, iTl.getItemStack());
                        }
                    }
                }
            }
        }
    }
    
    public void showTo(CRPPlayer p) {
        RPHologram hg = p.getHologramByName(name);
        if(hologram != null && hg == null) {
            hg = (visibility == HologramVisibility.SINGLE_VIEWER) ? this.clone() : this;
            
            hg.getHologram().getVisibilityManager().showTo(p.getPlayer());
            p.addVisibleHologram(hg);
            
            playSound(p, popupSound);
            hg.update(p);
        }
    }
    
    public void hideFrom(CRPPlayer p, boolean removeFromVisibleHolograms) {
        RPHologram hg = p.getHologramByName(name);
        if(hologram != null && hg != null) {
            
            if(visibility == HologramVisibility.SINGLE_VIEWER) {
                hologram.delete();
                hologram = null;
            }else {
                hologram.getVisibilityManager().resetVisibility(p.getPlayer());
            }
            
            if(removeFromVisibleHolograms) p.removeVisibleHologram(hg);
            playSound(p, disappearSound);
        }
    }
    
    public void hideFrom(CRPPlayer p) {
        hideFrom(p, true);
    }
    
    private void playSound(CRPPlayer p, Sound s) {
        if(s != null) {
            Location l = hologram.getLocation();
            p.getPlayer().playSound(l, s, 1F, 1F);
        }
    }
    
    public RPHologram clone() {
        RPHologram clone = new RPHologram(CONTAINER);
        clone.setName(name);
        
        Hologram hgClone = HologramsAPI.createHologram(CraftRP.get(), origin);
        hgClone.getVisibilityManager().setVisibleByDefault(hologram.getVisibilityManager().isVisibleByDefault());
        
        for(int i = 0; i < hologram.size(); i++) {
            HologramLine line = hologram.getLine(i);
            
            if(line instanceof TextLine) {
                TextLine tl = (TextLine) line;
                hgClone.insertTextLine(i, tl.getText());
            }else if(line instanceof ItemLine) {
                ItemLine il = (ItemLine) line;
                hgClone.insertItemLine(i, il.getItemStack());
            }
        }
        
        clone.originalHG = this.hologram;
        clone.setHologram(hgClone);
        clone.setMovement(movement);
        clone.setPopupSound(popupSound);
        clone.setDisappearSound(disappearSound);
        clone.setOrigin(origin);
        return clone;
    }
    
    public void hideFromViewers() {
        CRPEntity parent = CONTAINER.getParent();
        
        for(long playerId : parent.getViewers()) {
            CRPPlayer player = CRPPlayerManager.getCRPPlayerFromId(playerId);
            
            if(player != null) {
                RPHologram hologram = player.getHologramByName(name);
                if(hologram != null) hologram.hideFrom(player);
            }
        }
    }
    
    public void destroy() {
        hideFromViewers();
        
        try {
            hologram.delete();
            hologram = null;
        } catch (Exception e) {}
    }
    
    public HologramContainer getContainer() {
        return this.CONTAINER;
    }
    
    public void setName(String newName) {
        this.name = newName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setHologram(Hologram newHologram) {
        this.hologram = newHologram;
    }
    
    public Hologram getHologram() {
        return this.hologram;
    }
    
    public void setOrigin(Location newOrigin) {
        this.origin = newOrigin;
    }
    
    public Location getOrigin() {
        return this.origin;
    }
    
    public void setMovement(HologramMovement newMovement) {
        this.movement = newMovement;
    }
    
    public HologramMovement getMovement() {
        return this.movement;
    }
    
    public void setVisibility(HologramVisibility newVisibility) {
        this.visibility = newVisibility;
    }
    
    public HologramVisibility getVisibility() {
        return this.visibility;
    }
    
    public void setPopupSound(Sound newSound) {
        this.popupSound = newSound;
    }
    
    public Sound getPopupSound() {
        return this.popupSound;
    }
    
    public void setDisappearSound(Sound newSound) {
        this.disappearSound = newSound;
    }
    
    public Sound getDisappearSound() {
        return this.disappearSound;
    }
    
    public boolean equals(RPHologram hologram) {
        return hologram.getName().equalsIgnoreCase(hologram.getName());
    }

}
