package main;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osbot.rs07.script.Script;

import antiban.Antiban;
import eatingThread.Eater;

public class ThreadHandler {
	Thread antiBanThread;
	Thread eatingThread;
	Antiban antiban;
	Eater eater;
	Script script;
	
	List<Thread> threadBank = new LinkedList<Thread>();
	
	boolean settupStatus = false;
	volatile boolean killThread = false;
	AtomicBoolean mouseFlag = new AtomicBoolean(true);

	public ThreadHandler( Script script, Antiban antiban, Eater eater){
		this.script = script;
		this.eater = eater;
		this.antiban = antiban;
	}
	
	public Thread getEatingThread(){
		return eatingThread;
	}
	
	public Thread getAntiBanThread(){
		return antiBanThread;
	}
	
	public void settup() throws InterruptedException {
		antiBanThread = new Thread(antiban);
		threadBank.add(antiBanThread);
		antiBanThread.setPriority(Thread.MIN_PRIORITY);
		script.log("Created new Antiban thread.");
		antiBanThread.setDaemon(true);
		antiBanThread.start();
		script.log("Started new Antiban thread.");
		
		eatingThread = new Thread(eater);
		threadBank.add(eatingThread);
		eatingThread.setPriority(Thread.MIN_PRIORITY);
		eatingThread.setDaemon(true);
		eatingThread.start();
		script.log("Started new Eating thread.");
		
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
	
	public void releaseMouse(){
		this.mouseFlag.set(true);
	}
	
	public boolean ownMouse(){
		return this.mouseFlag.getAndSet(false);
	}
}
