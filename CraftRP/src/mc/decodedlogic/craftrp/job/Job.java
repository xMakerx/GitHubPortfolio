package mc.decodedlogic.craftrp.job;

import java.util.HashSet;
import java.util.List;

import mc.decodedlogic.craftrp.ownable.Owner;
import mc.decodedlogic.craftrp.ownable.Property;
import mc.decodedlogic.craftrp.util.Denominable;
import net.md_5.bungee.api.ChatColor;

public enum Job implements Owner, Denominable {
    
    CITIZEN("Citizen", null, ChatColor.DARK_GREEN, 10.0),
    HOBO("Hobo", null, ChatColor.GOLD, 0.0),
    CIVIL_PROTECTION("Civil Protection", null, ChatColor.DARK_AQUA, 45.0),
    MAYOR("Mayor", null, ChatColor.DARK_RED, 200.0),
    GANGSTER("Gangster", null, ChatColor.GRAY, 30.0);
    
    private String name;
    private List<String> description;
    private ChatColor roleColor;
    private HashSet<Property> properties;
    
    private double salary;
    
    private Job(String name, List<String> description, ChatColor roleColor, double salary) {
        this.name = name;
        this.description = description;
        this.roleColor = roleColor;
        this.salary = salary;
        this.properties = new HashSet<Property>();
    }
    
    public void setName(String newName) {
        this.name = newName;
    }
    
    public String getName() {
        return roleColor + this.name;
    }
    
    public String getNoColorName() {
        return this.name;
    }
    
    public void setDescription(List<String> newDescription) {
        this.description = newDescription;
    }
    
    public List<String> getDescription() {
        return this.description;
    }
    
    public void setRoleColor(ChatColor newRoleColor) {
        this.roleColor = newRoleColor;
    }
    
    public ChatColor getRoleColor() {
        return this.roleColor;
    }
    
    public void setSalary(double newSalary) {
        this.salary = newSalary;
    }
    
    public double getSalary() {
        return this.salary;
    }
    
    public HashSet<Property> getProperties() {
        return this.properties;
    }
    
}
