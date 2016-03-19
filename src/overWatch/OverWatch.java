package overWatch;

import java.awt.Point;
import java.util.Random;

import org.osbot.rs07.script.Script;

import antiban.Antiban;
import eatingThread.Eater;
import fighting.Fighting;
import groundItemManager.GroundItemManager;
import main.ThreadHandler;

public class OverWatch implements Runnable {
	Script script;
	Fighting fighter;
	Antiban antiban;
	Eater eater;
	ThreadHandler threadHandler;
	GroundItemManager itemManager;

	final long eatingTime = 10000;
	final long attackingTime = 15000;
	final long pickingUpTime = 15000;
	final long antiBanTime = 5000;
	final String threadName = "overWatch";
	
	int loggoutCount = 0;
	Random rn;
	volatile mouseState state;
	long timeStamp = 0;

	public enum mouseState {
		Eating, PickingUp, Attacking, Walking, AntiBan, None, NoChange
	};

	public OverWatch(Script script, Fighting fighter, Antiban antiban, Eater eater, GroundItemManager itemManager) {
		this.script = script;
		this.fighter = fighter;
		this.antiban = antiban;
		this.eater = eater;
		this.itemManager = itemManager;

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
			} catch (Exception e) {
				threadHandler.logPrint(threadName, "isMouseMoving sleep exception.");
				script.log("isMouseMoving sleep exception.");
				e.printStackTrace();
				threadHandler.exceptionPrint(threadName, e);
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
				loggoutCount++;
				fighter.reset();
				if(loggoutCount > 2){
					script.stop();
				}
				threadHandler.releaseMouse();
				while(!script.client.isLoggedIn()){
					try {
						Thread.sleep(rn.nextInt(1000) + 1000);
					} catch (Exception e) {
						threadHandler.logPrint(threadName, "Failed sleep in OverWatch run logged out.");
						script.log("Failed sleep in OverWatch run logged out.");
						threadHandler.exceptionPrint(threadName, e);
						e.printStackTrace();
					}
				}
			}

			try {
				Thread.sleep(rn.nextInt(1500) + 100);
			} catch (Exception e) {
				threadHandler.logPrint(threadName,"Failed sleep in OverWatch run.");
				script.log("Failed sleep in OverWatch run.");
				e.printStackTrace();
				threadHandler.exceptionPrint(threadName, e);
			}

			switch (state) {
			case Eating:
				loggoutCount = 0;
				if (timeHeld() > eatingTime) {
					if (!isMouseMoving()) {
						threadHandler.logPrint(threadName,"OverWatch released mouse after " + state + " Takeover.");
						script.log("OverWatch released mouse after " + state + " Takeover.");
						eater.resetMouseOwned();
						threadHandler.releaseMouse();
					}
				}
				break;
			case Attacking:
				loggoutCount = 0;
				if (timeHeld() > attackingTime) {
					threadHandler.logPrint(threadName,"Attacking over time.");
					script.log("Attacking over time.");
					if (!isMouseMoving()) {
						threadHandler.logPrint(threadName,"OverWatch released mouse after " + state + " Takeover.");
						script.log("OverWatch released mouse after " + state + " Takeover.");
						fighter.resetMouseOwned();
						threadHandler.releaseMouse();
					}
				}
				break;
			case PickingUp:
				loggoutCount = 0;
				if (timeHeld() > pickingUpTime) {
					if (!isMouseMoving()) {
						threadHandler.logPrint(threadName,"OverWatch released mouse after " + state + " Takeover.");
						script.log("OverWatch released mouse after " + state + " Takeover.");
						itemManager.resetMouseOwned();
						threadHandler.releaseMouse();
					}
				}
				break;
			case AntiBan:
				loggoutCount = 0;
				if (timeHeld() > antiBanTime) {
					if (!isMouseMoving()) {
						threadHandler.logPrint(threadName,"OverWatch released mouse after " + state + " Takeover.");
						script.log("OverWatch released mouse after " + state + " Takeover.");
						antiban.resetMouseOwned();
						threadHandler.releaseMouse();
					}
				}
				break;
			case Walking:
				loggoutCount = 0;
				try {
					Thread.sleep(rn.nextInt(500) + 500);
				} catch (Exception e) {
					threadHandler.logPrint(threadName,"Walking state sleep exception.");
					script.log("Walking state sleep exception.");
					e.printStackTrace();
					threadHandler.exceptionPrint(threadName, e);
				}
				if(!script.myPlayer().isMoving()){
					if (!isMouseMoving()) {
						threadHandler.logPrint(threadName,"OverWatch released mouse after " + state + " Takeover.");
						script.log("OverWatch released mouse after " + state + " Takeover.");
						threadHandler.releaseMouse();
					}
				}
				break;
			case None:
				break;
			default:
				threadHandler.logPrint(threadName, "Have an Invalid state here. State: " + state);
				script.log("Have an Invalid state here. State: " + state);
				break;
			}
		}
	}
}