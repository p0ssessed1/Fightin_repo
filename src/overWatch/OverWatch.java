package overWatch;

import java.awt.Point;
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

	final long eatingTime = 10000;
	final long attackingTime = 15000;
	final long pickingUpTime = 15000;
	final long antiBanTime = 5000;

	Random rn;
	volatile mouseState state;
	long timeStamp = 0;

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
		if (state != mouseState.NoChange) {
			timeStamp = System.currentTimeMillis();
			this.state = state;
		}
	}

	public void resetState() {
		state = mouseState.None;
	}

	private long timeHeld() {
		return System.currentTimeMillis() - timeStamp;
	}
	
	private boolean isMouseMoving(){
		Point startPos = script.getMouse().getPosition();
		Point endPos = null;
		for(int i = 0; i < 20; i++){
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				script.log("isMouseMoving sleep exception.");
				e.printStackTrace();
			}
			endPos = script.getMouse().getPosition();
			if((endPos.x != startPos.x) || (endPos.y != startPos.y)){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void run() {
		while (!threadHandler.getThreadKillMessage()) {
			if (!script.client.isLoggedIn()) {
				threadHandler.releaseMouse();
				try {
					Thread.sleep(rn.nextInt(1000) + 1000);
				} catch (InterruptedException e) {
					script.log("Failed sleep in OverWatch run logged out.");
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(rn.nextInt(1500) + 100);
			} catch (InterruptedException e) {
				script.log("Failed sleep in OverWatch run.");
				e.printStackTrace();
			}

			switch (state) {
			case Eating:
				if (timeHeld() > eatingTime) {
					if (!isMouseMoving()) {
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case Attacking:
				if (timeHeld() > attackingTime) {
					script.log("Attacking over time.");
					if (!isMouseMoving()) {
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case PickingUp:
				if (timeHeld() > pickingUpTime) {
					if (!isMouseMoving()) {
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case AntiBan:
				if (timeHeld() > antiBanTime) {
					if (!isMouseMoving()) {
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case Walking:
				try {
					Thread.sleep(rn.nextInt(500) + 500);
				} catch (InterruptedException e) {
					script.log("Walking state sleep exception.");
					e.printStackTrace();
				}
				if(!script.myPlayer().isMoving()){
					if (!isMouseMoving()) {
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case None:
				break;
			default:
				script.log("Have an Invalid state here. State: " + state);
				break;
			}
		}
	}
}
