package main;

public class Timer {
	long time;
	
	public Timer(){
		time = System.currentTimeMillis();
	}
	
	public boolean timer(int timeoutValue){
		long timeoutTime = time+timeoutValue;
		
		return timeoutTime > System.currentTimeMillis();
	}
	
	public void reset(){
		time = System.currentTimeMillis();
	}
}
