package antiban;

import java.util.Random;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.script.Script;

import fighting.Fighting;
import main.Timer;

public class Antiban implements Runnable {
	final int UP_KEY = 38;
	final int LEFT_KEY = 37;
	final int DOWN_KEY = 40;
	final int RIGHT_KEY = 39;

	Random rn;
	Random rnOver;
	Script script;
	Fighting fighter;
	Timer t = new Timer();

	public Antiban(Script script, Fighting fighter) {
		this.script = script;
		this.fighter = fighter;
		rnOver = new Random(script.myPlayer().getId());
	}

	/**
	 * All anti ban methods other than move camera
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void AntibanHandler() throws InterruptedException {
		if (fighter.isFighting()) {
			switch (rn.nextInt(30)) {
			case 0:
				script.log("Script Sleep Medium Period");
				Script.sleep(rn.nextInt(500) + 900);
			case 1:
				/* Move Mouse */
				script.log("Move Mouse.");
				moveMouse();
				break;
			case 2:
				/* Move Camera */
				script.log("Move Camera.");
				moveCamera();
				break;
			case 3:
				/* Right Click Hover */
				script.log("Right Click.");
				rightClickNext();
				break;
			case 4:
				/* Move Camera */
				script.log("Move Camera.");
				moveCamera();
			case 5:
				/* Move Mouse */
				script.log("Move Camera.");
				moveCamera();
				break;
			case 6:
				script.log("Script Sleep small period");
				Script.sleep(rn.nextInt(400) + 400);
				break;
			case 7:
				/* Sleep until 1s after done fishing. */
				script.log("Antiban Sleep during combat.");
				while (fighter.isFighting()) {
					Thread.sleep(rn.nextInt(100) + 100);
				}
				break;
			case 8:
				/* Hover Skill. */
				script.log("Check XP return to inventory.");
				if (hoverSkill()) {

					Thread.sleep(rn.nextInt(900) + 300);
					if (script.getTabs().getOpen() != Tab.INVENTORY) {
						script.getTabs().open(Tab.INVENTORY);
					}
				}
				break;
			case 9:
				script.log("Check XP & move mouse.");
				if (hoverSkill()) {
					Thread.sleep(rn.nextInt(900) + 500);
					moveMouse();
				}
				break;
			}
		}
	}

	private boolean moveCamera() throws InterruptedException {
		if (!script.getMouse().isOnScreen()) {
			return false;
		}
		boolean left = false;
		boolean right = false;
		boolean down = false;
		boolean up = false;
		int camera_end_val = rn.nextInt(50) + 40;
		if (rn.nextInt(100) > rn.nextInt(10) + 20) {
			if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(LEFT_KEY);
				left = true;
			} else if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(RIGHT_KEY);
				right = true;
			}

			t.reset();
			if (script.getCamera().getPitchAngle() < camera_end_val) {
				script.getKeyboard().pressKey(UP_KEY);
				up = true;
				while (script.getCamera().getPitchAngle() < camera_end_val && !script.getBot().isHumanInputEnabled()
						&& t.timer(2000)) {
				}
			} else {
				script.getKeyboard().pressKey(DOWN_KEY);
				down = true;
				while (script.getCamera().getPitchAngle() > camera_end_val && !script.getBot().isHumanInputEnabled()
						&& t.timer(2000)) {
				}
			}
		} else {
			if (rn.nextBoolean()) {
				script.getKeyboard().pressKey(LEFT_KEY);
				left = true;
			} else {
				script.getKeyboard().pressKey(RIGHT_KEY);
				right = true;
			}
			if (rn.nextInt(100) < rn.nextInt(50) + 20) {
				script.getKeyboard().pressKey(UP_KEY);
				up = true;
			} else {
				script.getKeyboard().pressKey(DOWN_KEY);
				down = true;
			}

			Thread.sleep(rn.nextInt(500) + 700);
		}
		if (up) {
			script.getKeyboard().releaseKey(UP_KEY);
		}
		if (left) {
			script.getKeyboard().releaseKey(LEFT_KEY);
		}
		if (right) {
			script.getKeyboard().releaseKey(RIGHT_KEY);
		}
		if (down) {
			script.getKeyboard().releaseKey(DOWN_KEY);
		}
		return true;
	}

	/**
	 * Move mouse off the screen until player is no longer fishing.
	 * 
	 * @return True if Successful. False if failure.
	 * @throws InterruptedException
	 */
	private boolean moveMouse() throws InterruptedException {
		switch (rn.nextInt(10)) {
		case 0:
			if (fighter.isFighting()) {
				if (script.getMouse().moveOutsideScreen()) {
					while (fighter.isFighting()) {
						Thread.sleep(rn.nextInt(100) + 100);
					}
					return true;
				}
			}
		case 1:
			script.getMouse().moveRandomly();
			Thread.sleep(rn.nextInt(300) + 1000);
			return true;
		case 2:
			script.getMouse().moveSlightly();
			Thread.sleep(rn.nextInt(500) + 1000);
			return true;
		case 3:
			script.getMouse().moveVerySlightly();
			Thread.sleep(rn.nextInt(500) + 700);
			return true;
		case 4:
			script.getMouse().moveVerySlightly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveOutsideScreen();
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
			return true;
		case 5:
			script.getMouse().moveSlightly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveRandomly();
			Thread.sleep(rn.nextInt(500) + 700);
			return true;
		case 6:
			script.getMouse().moveRandomly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveSlightly();
			Thread.sleep(rn.nextInt(500) + 1100);
			return true;
		case 7:
			script.getMouse().moveVerySlightly();
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		case 8:
			script.getMouse().move(rn.nextInt(20) - 10, rn.nextInt(20) - 10);
			Thread.sleep(rn.nextInt(100) + 100);
			return true;
		}

		if (rn.nextInt(10) == 0) {
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		}

		return false;
	}

	private boolean rightClickNext() throws InterruptedException {
		NPC next = fighter.getNextMonster();
		if (next.isVisible()) {
			EntityDestination targetDest = new EntityDestination(script.getBot(), next);

			script.getMouse().click(targetDest, true);
			t.reset();
			while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000) + 150)) {
				Thread.sleep(50);
			}
		} else {
			script.getCamera().toEntity(next);
			if (next.isVisible()) {
				EntityDestination targetDest = new EntityDestination(script.getBot(), next);

				script.getMouse().click(targetDest, true);
				t.reset();
				while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000) + 150)) {
					Thread.sleep(50);
				}
			} else {
				return false;
			}
		}

		if (rn.nextInt(10) == 0) {
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		}

		return true;
	}

	public boolean hoverSkill() throws InterruptedException {
		Skill[] skillArray = { Skill.ATTACK, Skill.STRENGTH, Skill.HITPOINTS, Skill.DEFENCE, Skill.PRAYER };
		Skill skill = skillArray[rn.nextInt(skillArray.length - 1)];
		if (script.getSkills().hoverSkill(skill)) {
			Thread.sleep(rn.nextInt(300) + 700);
			return true;
		}

		if (rn.nextInt(10) == 0) {
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		}
		return false;
	}

	@Override
	public void run() {
		while (true) {
			rn = new Random(rnOver.nextInt());
			try {
				AntibanHandler();
				if (rn.nextInt(10) == 0) {
					while (fighter.isFighting()) {
						Thread.sleep(100);
					}
				}
			} catch (InterruptedException e) {
				script.log("Exception in AntiBan Thread handler." + e);
				e.printStackTrace();
			}
			try {
				Thread.sleep(rn.nextInt(100) + 200);
			} catch (InterruptedException e) {
				script.log("Exception in Thread sleep handler." + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
