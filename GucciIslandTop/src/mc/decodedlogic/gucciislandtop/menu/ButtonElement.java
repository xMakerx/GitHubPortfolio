package mc.decodedlogic.gucciislandtop.menu;

import org.bukkit.event.inventory.InventoryClickEvent;

public class ButtonElement extends Element {
	
	protected Command command;
	
	public ButtonElement(Menu gui, int slot) {
		super(gui, slot);
		this.command = null;
	}
	
	public ButtonElement(Menu gui, int slot, Command command) {
		super(gui, slot);
		this.command = command;
	}
	
	public void onClick(InventoryClickEvent evt) {
		if(state != State.DISABLED && command != null) {
			command.execute();
		}
	}
	
	public void setClickCommand(Command command) {
		this.command = command;
	}
	
	public Command getClickCommand() {
		return this.command;
	}
	
}
