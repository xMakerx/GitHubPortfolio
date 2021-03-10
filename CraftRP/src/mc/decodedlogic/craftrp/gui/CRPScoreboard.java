package mc.decodedlogic.craftrp.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import lib.decodedlogic.core.internal.DecodedString;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class CRPScoreboard {
    
    protected final Scoreboard HUD;
    protected final CRPPlayer VIEWER;
    
    protected DisplaySlot slot;
    protected Objective obj;
    
    protected String title;
    protected List<String> lines;
    protected int blankLines;
    
    protected boolean built;
    
    public CRPScoreboard(CRPPlayer viewer) {
        this.VIEWER = viewer;
        this.HUD = Bukkit.getScoreboardManager().getNewScoreboard();
        this.slot = null;
        this.obj = null;
        this.title = "";
        this.lines = new ArrayList<String>();
        this.blankLines = 0;
        this.built = false;
    }
    
    public CRPScoreboard(CRPPlayer viewer, String title, DisplaySlot slot) {
        this(viewer);
        this.obj = HUD.registerNewObjective("board", "dummy", 
                DecodedString.color(title));
        this.obj.setDisplaySlot(slot);
        this.title = title;
    }
    
    public void update() {
        if(built) {
            for(String s : lines) {
                HUD.resetScores(s);
            }
            
            lines.clear();
            blankLines = 0;
        }
    }
    
    public void showTo(Player player) {
        if((obj == null || HUD.getObjectives().size() == 0) || player == null) return;
        
        int lineNum = lines.size();
        
        for(String line : lines) {
            Score s = obj.getScore(line);
            s.setScore(lineNum);
            lineNum--;
        }
        
        player.setScoreboard(HUD);
        built = true;
    }
    
    public CRPPlayer getViewer() {
        return this.VIEWER;
    }
    
    public Scoreboard getScoreboard() {
        return this.HUD;
    }
    
    public void setDisplaySlot(DisplaySlot newSlot) {
        this.slot = newSlot;
    }
    
    public DisplaySlot getDisplaySlot() {
        return this.slot;
    }
    
    public void setObjective(Objective newObj) {
        this.obj = newObj;
    }
    
    public Objective getObjective() {
        return this.obj;
    }
    
    public void setTitle(String newTitle) {
        this.title = DecodedString.color(newTitle);
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void addLine(String line) {
        lines.add(line);
    }
    
    public void removeLine(String line) {
        if(!lines.remove(line)) {
            
            String nf = ChatColor.stripColor(line);
            char[] lineChars = nf.toCharArray();
            
            int index = 0;
            boolean delete = false;
            
            for(String sbLine : lines) {
                if(sbLine.length() < nf.length()) continue;
                
                char[] sbLineChars = ChatColor.stripColor(sbLine).toCharArray();
                
                int numCorrect = 0;
                
                for(int i = 0; i < sbLine.length(); i++) {
                    if(i >= lineChars.length) break;

                    if(sbLineChars[i] == lineChars[i]) numCorrect++;
                }
                
                // Let's determine if "line" is referring to "sbLine"
                double perctCorrect = ((double) numCorrect) / ((double) sbLine.length());
                if(numCorrect == nf.length() || perctCorrect > 0.55d) {
                    delete = true;
                    break;
                }
                
                index++;
            }
            
            // Remove the line that matches the input.
            if(delete) lines.remove(index);
        }
    }
    
    public void blankLine() {
        String line = " ";
        
        for(int i = 0; i < blankLines; i++) {
            line = line.concat(" ");
        }
        
        blankLines++;
        addLine(line);
    }
    
    public List<String> getLines() {
        return this.lines;
    }
    
}
