package org.xmakerx.raidpractice.util;

public class TimeParser {
	
	private Time time;
	
	public TimeParser(Time parseTime) {
		this.time = parseTime;
	}
	
	public String parse() {
		int minutes = time.getMinutes();
		int seconds = time.getSeconds();
		String parsedTime = "";
		
		if(seconds >= 10) { 
			if(minutes > 9) {
				parsedTime = String.format("%s:%s", minutes, seconds);
			}else {
				parsedTime = String.format("0%s:%s", minutes, seconds);
			}
		}else {
			if(minutes <= 9) {
				parsedTime = String.format("0%s:0%s", minutes, seconds);
			}else {
				parsedTime = String.format("%s:0%s", minutes, seconds);
			}
		}
		
		return parsedTime;
	}

}
