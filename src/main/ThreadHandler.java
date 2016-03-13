package main;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osbot.rs07.script.Script;

import antiban.Antiban;
import eatingThread.Eater;
import groundItemManager.GroundItemManager;
import overWatch.OverWatch;

public class ThreadHandler {
	Thread antiBanThread;
	Thread eatingThread;
	Thread groundItemManagerThread;
	Thread overWatchThread;

	long timestamp = 0;
	long TIMEOUT_TIME_MS = 0;
	Antiban antiban;
	Eater eater;
	GroundItemManager itemManager;
	Script script;
	Random rn;
	
	List<Thread> threadBank = new LinkedList<Thread>();
	
	boolean settupStatus = false;
	volatile boolean killThread = false;
	AtomicBoolean mouseFlag = new AtomicBoolean(true);
	OverWatch overWatch;

	public ThreadHandler( Script script, Antiban antiban, Eater eater, GroundItemManager itemManager){
		this.script = script;
		this.eater = eater;
		this.antiban = antiban;
		this.itemManager = itemManager;
		rn = new Random(script.myPlayer().getId());
		timestamp = System.currentTimeMillis();
		TIMEOUT_TIME_MS = rn.nextInt(100000) + 120000;
	}
	
	public Thread getEatingThread(){
		return eatingThread;
	}
	
	public Thread getAntiBanThread(){
		return antiBanThread;
	}
	
	public void setOverWatch(OverWatch overWatch){
		this.overWatch = overWatch;
	}
	
	public void settup() throws InterruptedException {
		antiBanThread = new Thread(antiban);
		threadBank.add(antiBanThread);
		antiBanThread.setPriority(Thread.MIN_PRIORITY);
		script.log("Created new Antiban thread.");
		antiBanThread.start();
		script.log("Started new Antiban thread.");
		
		eatingThread = new Thread(eater);
		threadBank.add(eatingThread);
		eatingThread.setPriority(Thread.MIN_PRIORITY);
		eatingThread.start();
		script.log("Started new Eating thread.");
		
		overWatchThread = new Thread(overWatch);
		threadBank.add(overWatchThread);
		overWatchThread.setPriority(Thread.NORM_PRIORITY);
		overWatchThread.start();
		script.log("Started new OverWatch thread.");
		
		settupStatus = true;
	}
	
	public boolean isSettup(){
		return settupStatus;
	}
	
	public void kill(){
		killThread = true;
	}
	
	public boolean getThreadKillMessage(){
		return killThread;
	}
	
	public boolean releaseMouse(){
		overWatch.resetState();
		this.mouseFlag.set(true);
		return false;
	}
	
	public boolean ownMouse(){
		boolean ret = false;
		if((ret = this.mouseFlag.getAndSet(false)))
		{
			timestamp = System.currentTimeMillis();
			TIMEOUT_TIME_MS = rn.nextInt(100000) + 120000;
		} else {
			if(isTimedOut()){
				script.log("Timed out, releasing mouse.");
				releaseMouse();
			}
		}
		return ret;
	}
	
	private boolean isTimedOut(){
		return (System.currentTimeMillis() > (timestamp + TIMEOUT_TIME_MS) &&
				!this.mouseFlag.get());
	}
}
