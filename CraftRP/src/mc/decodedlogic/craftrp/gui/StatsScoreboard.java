package mc.decodedlogic.craftrp.gui;

import org.bukkit.scoreboard.DisplaySlot;

import mc.decodedlogic.craftrp.player.CRPPlayer;
import net.md_5.bungee.api.ChatColor;

public class StatsScoreboard extends CRPScoreboard {
    
    public StatsScoreboard(CRPPlayer viewer) {
        super(viewer, "&6&lCraftRP", DisplaySlot.SIDEBAR);
        
        this.update();
    }
    
    public void update() {
        super.update();
        
        blankLine();
        addLine(getJobLine());
        addLine(getSalaryLine());
        blankLine();
        addLine(getWantedRtgLine());
        
        showTo(VIEWER.getPlayer());
    }
    
    public String getJobLine() {
        return "Job: " + VIEWER.getJob().getName();
    }
    
    public String getSalaryLine() {
        int salary = (int) VIEWER.getJob().getSalary();
        return "Salary: $" + salary + "/hr";
    }
    
    public String getWantedRtgLine() {
        ChatColor[] colors = new ChatColor[] {
          ChatColor.GREEN,
          ChatColor.YELLOW,
          ChatColor.GOLD,
          ChatColor.RED,
          ChatColor.DARK_RED
        };
        
        int colorIndex = 0;
        int rating = VIEWER.getWantedRating();
        double perct = ((double) rating) / ((double) CRPPlayer.MAX_WANTED_RATING);
        
        if(perct >= 0.9) {
            colorIndex = 4;
        } else if(perct >= 0.8) {
            colorIndex = 3;
        }else if(perct >= 0.5) {
            colorIndex = 2;
        }else if(perct >= 0.2) {
            colorIndex = 1;
        }
        
        
        return "Wanted ✯: " + colors[colorIndex] + rating;
    }

}
