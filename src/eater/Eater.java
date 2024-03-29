package eater;

import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import fighting.Fighting;
import main.ThreadHandler;
import main.Timer;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;

public class Eater implements Runnable {
	final String threadName = "Eater";
	Random rn;
	Script script;
	Fighting fighter;
	Timer t = new Timer();
	int minHP = 25;
	int hpBuffer = 8;
	int hpToEatAt = 17;
	volatile boolean mouseOwned = false;
	ThreadHandler threadHandler;
	OverWatch overWatch;

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public void setOverWatch(OverWatch overWatch) {
		this.overWatch = overWatch;
	}

	public Eater(Script script, Fighting fighter) {
		this.script = script;
		this.fighter = fighter;
		rn = new Random(script.myPlayer().getId());
	}

	public void setHealth(int health) {
		script.log("Health set to " + health);
		this.minHP = health;
		this.hpBuffer = script.getSkills().getStatic(Skill.HITPOINTS)/8;
		this.hpToEatAt = minHP + rn.nextInt(hpBuffer);
	}

	private boolean criticalEat(Item food) throws InterruptedException{
		boolean ret = false;
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.INVINTERACTION);
		ret = food.interact("Eat");
		releaseMouseOwned();
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private boolean tryEat() throws InterruptedException {
		this.hpToEatAt = minHP + rn.nextInt(hpBuffer);
		boolean ret = false;
		ActionFilter<Item> eat = new ActionFilter<Item>("Eat");
		Item food = script.getInventory().getItem(eat);
		Thread.sleep(rn.nextInt(700) + 600);
		if (food != null) {
			boolean wasMoving = script.myPlayer().isMoving();
			ret = criticalEat(food);
			int hpThreshold = rn.nextInt(10) + 75;
			while (((script.getSkills().getDynamic(Skill.HITPOINTS) * 100)
					/ script.getSkills().getStatic(Skill.HITPOINTS)) < hpThreshold) {
				food = script.getInventory().getItem(eat);
				if(food != null) {
					criticalEat(food);
				} else {
					break;
				}
				Thread.sleep(rn.nextInt(550) + 450);
			}
			if (wasMoving) {
				Thread.sleep(rn.nextInt(900) + 300);
				if (fighter.getCurrent() != null && fighter.getCurrent().exists()) {
					fighter.criticalAttack(fighter.getCurrent());
				} else if (fighter.getRightClicked() != null && fighter.getRightClicked().exists()) {
					fighter.criticalAttack(fighter.getRightClicked());
				}
			} else if (fighter.getCurrent() != null && fighter.getCurrent().isVisible()) {
				if (rn.nextInt(10) < 8) {
					if (script.getSkills().getDynamic(Skill.HITPOINTS) > (minHP + rn.nextInt(hpBuffer))
							&& fighter.getCurrent() != null && fighter.getCurrent().getCurrentHealth() > 1) {
						Thread.sleep(rn.nextInt(900) + 900);
						fighter.criticalAttack(fighter.getCurrent());
					}
				}
			}
		}
		return ret;
	}

	private void releaseMouseOwned() {
		if (mouseOwned) {
			mouseOwned = threadHandler.releaseMouse();
		}
	}

	public void resetMouseOwned() {
		this.mouseOwned = false;
	}

	@Override
	public void run() {
		synchronized (fighter) {

			while (!threadHandler.getThreadKillMessage()) {
				try {
					Thread.sleep(rn.nextInt(900) + 600);
				} catch (Exception e) {
					script.log("Exception in Thread sleep handler." + e);
					e.printStackTrace();
					threadHandler.exceptionPrint(threadName, e);
				}
				while (!script.client.isLoggedIn()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						script.log("Exception in Thread sleep handler." + e);
					}
				}
				try {
					Thread.sleep(rn.nextInt(700) + 500);
				} catch (Exception e) {
					script.log("Sleep in eater failed. Exception:" + e);
					e.printStackTrace();
					threadHandler.exceptionPrint(threadName, e);
				}
				if (script.getSkills().getDynamic(Skill.HITPOINTS) < hpToEatAt) {
					try {
						tryEat();
					} catch (Exception e) {
						releaseMouseOwned();
						script.log("Exception in tryEat. " + e);
						e.printStackTrace();
						threadHandler.exceptionPrint(threadName, e);
					}
				}
			}
		}
	}
}
