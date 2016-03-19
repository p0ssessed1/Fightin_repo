package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
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

	final String threadName = "ThreadHandler";

	long timestamp = 0;
	long TIMEOUT_TIME_MS = 0;
	Antiban antiban;
	Eater eater;
	GroundItemManager itemManager;
	Script script;
	Random rn;
	FileWriter Efile;
	FileWriter Lfile;
	BufferedWriter Ewriter = null;
	BufferedWriter Lwriter = null;

	List<Thread> threadBank = new LinkedList<Thread>();

	boolean settupStatus = false;
	volatile boolean killThread = false;
	AtomicBoolean mouseFlag = new AtomicBoolean(true);
	AtomicBoolean writeLogFlag = new AtomicBoolean(true);
	AtomicBoolean writeExceptionFlag = new AtomicBoolean(true);
	OverWatch overWatch;

	public ThreadHandler(Script script, Antiban antiban, Eater eater, GroundItemManager itemManager) {
		this.script = script;
		this.eater = eater;
		this.antiban = antiban;
		this.itemManager = itemManager;
		rn = new Random(script.myPlayer().getId());
		timestamp = System.currentTimeMillis();
		TIMEOUT_TIME_MS = rn.nextInt(100000) + 120000;
		Calendar c = Calendar.getInstance();
		String path = System.getProperty("user.dir") + "\\OSBotLogs\\";
		String filepath = path + "ErrorList\\" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.MONTH) + "\\"
				+ c.get(Calendar.DATE);
		File dir = new File(filepath);
		dir.mkdirs();
		String filename = filepath + "\\" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + ".txt";
		try {
			Efile = new FileWriter(filename, true);
			Ewriter = new BufferedWriter(Efile);
			filepath = path + "LogList\\" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.MONTH) + "\\"
					+ c.get(Calendar.DATE);
			dir = new File(filepath);
			dir.mkdirs();
			filename = filepath + "\\" + c.get(Calendar.HOUR_OF_DAY) + "_"
					+ c.get(Calendar.MINUTE) + ".txt";
			Lfile = new FileWriter(filename, true);
			Lwriter = new BufferedWriter(Lfile);
		} catch (Exception e) {
			script.log("Error creating buffers and files.");
			e.printStackTrace();
		}

		Date date = new Date(System.currentTimeMillis());
		try {
			if (Ewriter != null) {
				Ewriter.write("=====================================");
				Ewriter.write("Starting: " + date.toString());
				Ewriter.write("=====================================");
				Ewriter.newLine();
				Ewriter.flush();
			}
			if (Lwriter != null) {
				Lwriter.write("=====================================");
				Lwriter.write("Starting: " + date.toString());
				Lwriter.write("=====================================");
				Lwriter.newLine();
				Lwriter.flush();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Thread getEatingThread() {
		return eatingThread;
	}

	public Thread getAntiBanThread() {
		return antiBanThread;
	}

	public void setOverWatch(OverWatch overWatch) {
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

	public boolean isSettup() {
		return settupStatus;
	}

	public void kill() {
		killThread = true;
	}

	public boolean getThreadKillMessage() {
		return killThread;
	}

	public boolean releaseMouse() {
		overWatch.resetState();
		this.mouseFlag.set(true);
		return false;
	}

	public boolean ownMouse() {
		boolean ret = false;
		if ((ret = this.mouseFlag.getAndSet(false))) {
			timestamp = System.currentTimeMillis();
			TIMEOUT_TIME_MS = rn.nextInt(100000) + 120000;
		} else {
			if (isTimedOut()) {
				logPrint(threadName, "Timed out, releasing mouse.");
				script.log("Timed out, releasing mouse.");
				releaseMouse();
			}
		}
		return ret;
	}

	private boolean isTimedOut() {
		return (System.currentTimeMillis() > (timestamp + TIMEOUT_TIME_MS) && !this.mouseFlag.get());
	}

	public void exceptionPrint(String thread, Exception e) {
		while (writeExceptionFlag.getAndSet(false)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
		}
		Calendar c = Calendar.getInstance();
		try {
			Ewriter.write("=====================================");
			Ewriter.newLine();
			Ewriter.write(e.getMessage());
			Ewriter.newLine();
			Ewriter.write(thread+ ": " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)
					+ "." + c.get(Calendar.MILLISECOND) + ": ");
			Ewriter.newLine();
			StackTraceElement[] element = e.getStackTrace();
			for (StackTraceElement ste : element) {
				Ewriter.write(ste.toString());
				Ewriter.newLine();
			}
			Ewriter.write("=====================================");
			Ewriter.newLine();
			Ewriter.flush();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writeExceptionFlag.set(true);
	}

	public void logPrint(String location, String message) {
		while (writeLogFlag.getAndSet(false)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Calendar c = Calendar.getInstance();
		try {
			Lwriter.write(location + ": " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":"
					+ c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + ": " + message);
			Lwriter.newLine();
			Lwriter.flush();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writeLogFlag.set(true);
	}

	public BufferedWriter getExceptionFile() {
		return this.Ewriter;
	}

	public BufferedWriter getLogFile() {
		return this.Lwriter;
	}
}
