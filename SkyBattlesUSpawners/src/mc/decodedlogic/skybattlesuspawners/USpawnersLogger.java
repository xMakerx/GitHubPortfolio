package mc.decodedlogic.skybattlesuspawners;

import net.md_5.bungee.api.ChatColor;

public class USpawnersLogger {
	
	public enum LogLevel {
		DEBUG(ChatColor.WHITE, 3),
		INFO(ChatColor.GRAY, 2),
		WARNING(ChatColor.YELLOW, 1),
		ERROR(ChatColor.RED, 0);
		
		private final ChatColor color;
		private final int level;
		
		private LogLevel(ChatColor color, int level) {
			this.color = color;
			this.level = level;
		}
		
		public ChatColor getColor() {
			return this.color;
		}
		
		public int getPriorityLevel() {
			return this.level;
		}
		
		public static LogLevel getByName(String name) {
			for(LogLevel level : values()) {
				if(level.toString().equalsIgnoreCase(name)) {
					return level;
				}
			}
			
			return null;
		}
	}
	
	private final String category;
	private LogLevel level;
	
	public USpawnersLogger(String category) {
		this.category = category;
		this.level = LogLevel.INFO;
	}
	
	public USpawnersLogger(String category, LogLevel initLevel) {
		this.category = category;
		this.level = initLevel;
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
	
	public boolean warning(String message) {
		return logMessage(message, LogLevel.WARNING);
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
		final USpawners main = USpawners.get();
		String pluginName = main.getDescription().getName();
		String formattedMsg = String.format("%s::%s(%s): %s", pluginName,
				category, 
				msgLevel.toString().toLowerCase(), 
				msgLevel.getColor() + message);
		
		LogLevel plLevel = main.getLogLevel();
		
		// We only print out messages if their priority level is lower or equal
		// to the plugin's print level.
		if(plLevel.getPriorityLevel() >= msgLevel.getPriorityLevel()) {
			main.getServer().getConsoleSender().sendMessage(formattedMsg);
			return true;
		}
		
		return false;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public void setLogLevel(LogLevel level) {
		this.level = level;
	}
	
	public LogLevel getLevel() {
		return this.level;
	}

}
