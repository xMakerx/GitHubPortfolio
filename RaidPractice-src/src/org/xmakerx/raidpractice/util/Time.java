package org.xmakerx.raidpractice.util;

public class Time {
	
	private int minutes;
	private int seconds;
	
	public Time(int startMins, int startSecs) {
		this.minutes = startMins;
		this.seconds = startSecs;
	}
	
	public String parse() {
		TimeParser parser = new TimeParser(this);
		String parsedTime =	parser.parse();
		return parsedTime;
	}
	
	public void subtract(Time time) {
		if(time.getMinutes() > this.minutes) {
			this.minutes = time.getMinutes() - this.minutes;
		}else if(time.getMinutes() != this.minutes) {
			this.minutes -= time.getMinutes();
		}
		
		if(time.getSeconds() > this.seconds) {
			if(time.getSeconds() == 1 && this.seconds == 0) {
				seconds = 59;
				minutes -= 1;
			}else {
				this.seconds = time.getSeconds() - this.seconds;
			}
		}else if(time.getSeconds() != this.seconds){
			this.seconds -= time.getSeconds();
		}else {
			this.seconds = 0;
		}
	}
	
	public boolean isGreaterThan(Time time) {
		if(this.minutes > time.getMinutes() && this.seconds > time.getSeconds()) {
			return true;
		}
		return false;
	}
	
	public void setMinutes(int newMinutes) {
		this.minutes = newMinutes;
	}
	
	public int getMinutes() {
		return this.minutes;
	}
	
	public void setSeconds(int newSeconds) {
		this.seconds = newSeconds;
		if(this.seconds == 0) {
			seconds = 59;
			if(this.minutes > 0) {
				minutes -= 1;
			}
		}else {
			if(this.seconds > 59) {
				this.seconds = this.seconds - 59;
				this.minutes += Math.round(this.seconds / 60);
			}
		}
	}
	
	public int getSeconds() {
		return this.seconds;
	}
}
