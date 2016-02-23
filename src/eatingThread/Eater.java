package eatingThread;

import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
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
	int HP_BUFFER = 5;
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
		script.log("Health set to " + health);
		this.minHP = health;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		synchronized (fighter) {
			while (!threadHandler.getThreadKillMessage()) {
				try {
					Thread.sleep(rn.nextInt(900) + 600);
				} catch (InterruptedException e2) {
					script.log("Exception in Thread sleep handler." + e2);
				}
				if (!script.client.isLoggedIn()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						script.log("Exception in Thread sleep handler." + e);
					}
				}
				try {
					Thread.sleep(rn.nextInt(700) + 500);
				} catch (InterruptedException e) {
					script.log("Sleep in eater failed. Exception:" + e);
					e.printStackTrace();
				}
				ActionFilter<Item> eat = new ActionFilter<Item>("Eat");
				if (script.getSkills().getDynamic(Skill.HITPOINTS) < (minHP + rn.nextInt(HP_BUFFER))) {
					Item food = script.getInventory().getItem(eat);
					try {
						Thread.sleep(rn.nextInt(700) + 600);
					} catch (InterruptedException e1) {
						script.log("Sleeping inside exception handler for checking inv.");
					}
					if (food != null) {
						food.interact("Eat");
						if (fighter.getCurrent() != null && fighter.getCurrent().isVisible()) {
							if (rn.nextInt(10) < 8) {
								try {
									Thread.sleep(rn.nextInt(900) + 900);
								} catch (InterruptedException e1) {
									script.log("Sleeping inside exception handler for checking inv.");
								}

								if (script.getSkills().getDynamic(Skill.HITPOINTS) > (minHP + rn.nextInt(HP_BUFFER))
										&& fighter.getCurrent().getCurrentHealth() > 1) {
									fighter.getCurrent().interact("Attack");
								}
							}
						}
					}
				}
			}
		}
	}
}
