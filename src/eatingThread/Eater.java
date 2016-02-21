package eatingThread;

import java.util.Random;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;

import fighting.Fighting;
import main.ThreadHandler;
import main.Timer;

public class Eater implements Runnable {

	Random rn;
	Script script;
	Fighting fighter;
	Timer t = new Timer();
	int minHP = 10;
	int HP_BUFFER = 1;
	ThreadHandler threadHandler;

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public Eater(Script script, Fighting fighter) {
		this.script = script;
		this.fighter = fighter;
		rn = new Random(script.myPlayer().getId());
	}

	public void setHealth(int health) {
		this.minHP = health;
		int div = rn.nextInt(5) + 5;
		this.HP_BUFFER = health / div > 0 ? health / div : 2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (!threadHandler.getThreadKillMessage()) {
			try {
				Thread.sleep(rn.nextInt(700) + 500);
			} catch (InterruptedException e) {
				script.log("Sleep in eater failed. Exception:" + e);
				e.printStackTrace();
			}
			if (script.myPlayer().getCurrentHealth() <= (minHP + rn.nextInt(HP_BUFFER))) {
				Item inv = null;
				try {
					inv = script.getInventory().getItem(fighter.getFood());
				} catch (Exception e) {
					script.log("Inventory not obtained. Trying again.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						script.log("Sleeping inside exception handler for checking inv.");
					}
				}
				if (inv != null && inv.hasAction("Eat")) {
					int div = rn.nextInt(5) + 5;
					this.HP_BUFFER = minHP / div > 0 ? minHP / div : 2;
					inv.interact("Eat");
				}
				
			}
		}

	}

}
