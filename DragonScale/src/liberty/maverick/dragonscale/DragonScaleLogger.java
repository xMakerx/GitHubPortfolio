package liberty.maverick.dragonscale;

import net.md_5.bungee.api.ChatColor;

public class DragonScaleLogger {
	
	public enum LogLevel {
		ERROR(ChatColor.RED, 0), INFO(ChatColor.WHITE, 1), DEBUG(ChatColor.YELLOW, 2);
		
		private final ChatColor color;
		private final int priority;
		
		private LogLevel(ChatColor logColor, int priorityLevel) {
			this.color = logColor;
			this.priority = priorityLevel;
		}
		
		/**
		 * Fetches the color of messages logged at this level.
		 * @return {@link ChatColor} value.
		 */
		
		public ChatColor getChatColor() {
			return this.color;
		}
		
		/**
		 * Fetches the priority level of the message. Lower number = higher priority. 0=Highest priority.
		 * @return
		 */
		
		public int getPriorityLevel() {
			return this.priority;
		}
	}
	
	final DragonScale main;
	private final String category;
	
	/**
	 * Creates a new DragonScaleLogger instance for the specified category.
	 * Categories are basically names that make it easier to track where messages are coming from.
	 * @param {@link DragonScaleInstance} The main instance of the plugin.
	 * @param The category name.
	 */
	
	public DragonScaleLogger(final DragonScale mainInstance, final String category) {
		this.main = mainInstance;
		this.category = category;
	}
	
	/**
	 * The following are handy functions that log messages at a specific log level.
	 * They return whether or not the message was sent to the console.
	 */
	
	public boolean error(String message) {
		return logMessage(message, LogLevel.ERROR);
	}
	
	public boolean info(String message) {
		return logMessage(message, LogLevel.INFO);
	}
	
	public boolean debug(String message) {
		return logMessage(message, LogLevel.DEBUG);
	}
	
	/**
	 * Internal method that tries to print out the specified message at the specified
	 * {@link LogLevel}.
	 * @param The message to print out.
	 * @param The {@link LogLevel} of the message.
	 * @return Returns whether or not the message was sent to the console. If it wasn't sent, the
	 * plugin's log level was lower than the message's log level.
	 */
	
	private boolean logMessage(String message, final LogLevel msgLevel) {
		String pluginName = main.getDescription().getName();
		String formattedMsg = String.format("%s::%s(%s): %s", pluginName,
				category, 
				msgLevel.toString().toLowerCase(), 
				msgLevel.getChatColor() + message);
		
		LogLevel plLevel = main.getLogLevel();
		
		// We only print out messages if their priority level is lower or equal
		// to the plugin's print level.
		if(plLevel.getPriorityLevel() >= msgLevel.getPriorityLevel()) {
			main.getServer().getConsoleSender().sendMessage(formattedMsg);
			return true;
		}
		
		return false;
	}

}
