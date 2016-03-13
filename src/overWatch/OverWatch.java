package overWatch;

import java.util.Random;

import org.osbot.rs07.script.Script;

import antiban.Antiban;
import eatingThread.Eater;
import fighting.Fighting;
import main.ThreadHandler;

public class OverWatch implements Runnable {
	Script script;
	Fighting fighter;
	Antiban antiban;
	Eater eater;
	ThreadHandler threadHandler;

	Random rn;
	mouseState state;

	public enum mouseState {
		Eating, PickingUp, Attacking, Walking, AntiBan, None, NoChange
	};

	public OverWatch(Script script, Fighting fighter, Antiban antiban, Eater eater) {
		this.script = script;
		this.fighter = fighter;
		this.antiban = antiban;
		this.eater = eater;

		rn = new Random(script.myPlayer().getId());
	}

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public void setState(mouseState state) {
		this.state = state;
	}

	public void resetState(){
		state = mouseState.None;
	}
	
	@Override
	public void run() {
		while (threadHandler.getThreadKillMessage()) {
			if (!script.client.isLoggedIn()) {
				threadHandler.releaseMouse();
				try {
					Thread.sleep(rn.nextInt(1000) + 1000);
				} catch (InterruptedException e) {
					script.log("Failed sleep in OverWatch run.");
					e.printStackTrace();
				}
			}

		}

	}

}
