package antiban;

import java.util.Random;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.script.Script;

import fighting.Fighting;
import itemManager.ItemManager;
import main.ThreadHandler;
import main.Timer;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;

public class Antiban implements Runnable {
	final int UP_KEY = 38;
	final int LEFT_KEY = 37;
	final int DOWN_KEY = 40;
	final int RIGHT_KEY = 39;
	final String threadName = "AntiBan";

	volatile boolean mouseOwned = false;
	Random rn;
	Random rnOver;
	Script script;
	Fighting fighter;
	ThreadHandler threadHandler;

	enum State {
		CameraMoved, MouseMoved, HoverSkill, RightClicked
	};

	State state;

	Timer t = new Timer();
	OverWatch overWatch;
	ItemManager itemManager;

	
	public void setItemManager(ItemManager itemManager){
		this.itemManager = itemManager;
	}
	public void setOverWatch(OverWatch overWatch){
		this.overWatch = overWatch;
	}
	
	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

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
				Thread.sleep(rn.nextInt(900) + 1200);
			case 1:
				/* Move Mouse */
				threadHandler.logPrint(threadName, "Antiban: Move Mouse.");
				script.log("Antiban: Move Mouse.");
				moveMouse();
				break;
			case 2:
				/* Move Camera */
				moveCamera();
				break;
			case 3:
				/* Right Click Hover */
				threadHandler.logPrint(threadName, "Antiban: Right Click.");
				script.log("Antiban: Right Click.");
				rightClickNext();
				break;
			case 4:
				/* Move Camera */
				moveCamera();
			case 5:
				/* Move Mouse */
				moveCamera();
				break;
			case 6:
				Thread.sleep(rn.nextInt(900) + 400);
				break;
			case 7:
				/* Sleep until 1s after done fishing. */
				while (fighter.isFighting()) {
					Thread.sleep(rn.nextInt(200) + 100);
				}
				break;
			case 8:
				/* Hover Skill. */
				threadHandler.logPrint(threadName,"Antiban: Check XP return to inventory.");
				script.log("Antiban: Check XP return to inventory.");
				if (hoverSkill()) {

					Thread.sleep(rn.nextInt(900) + 300);
					if (script.getTabs().getOpen() != Tab.INVENTORY) {
						criticalTabOpen(Tab.INVENTORY);
					}
				}
				break;
			case 9:
				threadHandler.logPrint(threadName,"Antiban: Check XP & move mouse.");
				script.log("Antiban: Check XP & move mouse.");
				if (hoverSkill()) {
					Thread.sleep(rn.nextInt(900) + 500);
					moveMouse();
				}
				break;
			case 10:
				/* Right Click Hover */
				threadHandler.logPrint(threadName,"Antiban: Right Click medium sleep.");
				script.log("Antiban: Right Click medium sleep.");
				rightClickNext();
				Thread.sleep(rn.nextInt(500) + 900);
				break;
			case 11:
				/* Right Click Hover */
				threadHandler.logPrint(threadName,"Antiban: Right Click until done fighting.");
				script.log("Antiban: Right Click until done fighting.");
				rightClickNext();
				while (fighter.isFighting()) {
					Thread.sleep(100);
				}
				break;
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
				cameraManager();
				break;
			case 18:
				threadHandler.logPrint(threadName,"AntiBan: Open Magic Tab.");
				script.log("AntiBan: Open Magic Tab.");
				Thread.sleep(rn.nextInt(900) + 300);
				if (script.getTabs().getOpen() != Tab.MAGIC) {
					criticalTabOpen(Tab.MAGIC);
				}
				Thread.sleep(rn.nextInt(1000) + 900);
				break;
			case 19:
				threadHandler.logPrint(threadName,"AntiBan: Open Friends Tab.");
				script.log("AntiBan: Open Friends Tab.");
				Thread.sleep(rn.nextInt(900) + 300);
				if (script.getTabs().getOpen() != Tab.FRIENDS) {
					criticalTabOpen(Tab.FRIENDS);
				}
				Thread.sleep(rn.nextInt(1000) + 900);
				break;
			case 20:
				threadHandler.logPrint(threadName,"AntiBan: Open Skills Tab.");
				script.log("AntiBan: Open Skills Tab.");
				Thread.sleep(rn.nextInt(900) + 300);
				if (script.getTabs().getOpen() != Tab.SKILLS) {
					criticalTabOpen(Tab.SKILLS);
				}
				Thread.sleep(rn.nextInt(1000) + 900);
				break;
			case 21:
				/* Move Mouse */
				threadHandler.logPrint(threadName,"Antiban: Move Mouse.");
				script.log("Antiban: Move Mouse.");
				moveMouse();
				break;
			case 22:
				/* Move Mouse */
				threadHandler.logPrint(threadName,"Antiban: Move Mouse.");
				script.log("Antiban: Move Mouse.");
				moveMouse();
				break;
			}
		}
	}

	private boolean criticalTabOpen(Tab tab) throws InterruptedException {
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.ANTIBAN);
		boolean ret = script.getTabs().open(tab);
		releaseMouseOwned();
		return ret;
	}

	public boolean moveCamera() throws InterruptedException {
		if (!script.getMouse().isOnScreen() || state == State.CameraMoved) {
			return false;
		}
		int[] Keys = { UP_KEY, DOWN_KEY, LEFT_KEY, RIGHT_KEY };
		boolean[] taken = { false, false, false, false };
		int[] pos = { 5, 5, 5, 5 };
		pos[0] = rn.nextInt(10) % 4;
		taken[pos[0]] = true;
		pos[1] = rn.nextInt(20) % 4;
		while (pos[1] == pos[0]) {
			if (rn.nextBoolean()) {
				pos[1] = (pos[1] + (rn.nextInt(5) + 5)) % 4;
			} else {
				pos[1] = (pos[1] + (rn.nextInt(1) + 1)) % 4;
			}
		}
		taken[pos[1]] = true;
		for (int i = 0; i < 4; i++) {
			if (!taken[i]) {
				if (rn.nextBoolean()) {
					if (pos[2] == 5) {
						pos[2] = i;
						taken[i] = true;
					} else {
						pos[3] = i;
						taken[i] = true;
					}

				} else {
					if (pos[3] == 5) {
						pos[3] = i;
						taken[i] = true;
					} else {
						pos[2] = i;
						taken[i] = true;
					}
				}
			}
		}

		int camera_end_val = rn.nextInt(50) + 40;
		if (rn.nextInt(100) > rn.nextInt(10) + 20) {
			if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(LEFT_KEY);
			} else if (rn.nextInt(1) == 0) {
				script.getKeyboard().pressKey(RIGHT_KEY);
			}
			Thread.sleep(rn.nextInt(100) + rn.nextInt(100) + 1);
			t.reset();
			if (script.getCamera().getPitchAngle() < camera_end_val) {
				script.getKeyboard().pressKey(UP_KEY);
				while (script.getCamera().getPitchAngle() < camera_end_val && !script.getBot().isHumanInputEnabled()
						&& t.timer(rn.nextInt(1000) + 1500)) {
				}
			} else {
				script.getKeyboard().pressKey(DOWN_KEY);
				while (script.getCamera().getPitchAngle() > camera_end_val && !script.getBot().isHumanInputEnabled()
						&& t.timer(rn.nextInt(1000) + 1500)) {
				}
			}
		} else {
			if (rn.nextInt(100) < rn.nextInt(30) + 70) {
				script.getKeyboard().pressKey(UP_KEY);
			} else {
				script.getKeyboard().pressKey(DOWN_KEY);
			}
			Thread.sleep(rn.nextInt(100) + rn.nextInt(100) + 1);
			if (rn.nextBoolean()) {
				script.getKeyboard().pressKey(LEFT_KEY);
			} else {
				script.getKeyboard().pressKey(RIGHT_KEY);
			}

			Thread.sleep(rn.nextInt(1000) + rn.nextInt(400) + 900);
		}

		for (int i = 0; i < 4; i++) {
			script.getKeyboard().releaseKey(Keys[pos[i]]);
			Thread.sleep(rn.nextInt(100) + rn.nextInt(12));
		}
		state = State.CameraMoved;
		return true;
	}
	
	public void resetMouseOwned(){
		this.mouseOwned = false;
	}
	
	private void releaseMouseOwned(){
		if(mouseOwned){
			threadHandler.releaseMouse();
		}
	}
	
	/**
	 * Move mouse off the screen until player is no longer fishing.
	 * 
	 * @return True if Successful. False if failure.
	 * @throws InterruptedException
	 */
	private boolean moveMouse() throws InterruptedException {
		if (state == State.MouseMoved) {
			return false;
		}
		boolean ret = false;
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.ANTIBAN);
		state = State.MouseMoved;
		switch (rn.nextInt(10)) {
		case 0:
			if (fighter.isFighting()) {
				if (script.getMouse().moveOutsideScreen()) {
					releaseMouseOwned();
					while (fighter.isFighting()) {
						Thread.sleep(rn.nextInt(100) + 100);
					}
					ret = true;
				} else {
					releaseMouseOwned();
				}
			} else {
				releaseMouseOwned();
			}
			break;
		case 1:
			script.getMouse().moveRandomly();
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(300) + 1000);
			ret = true;
			break;
		case 2:
			script.getMouse().moveSlightly();
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(500) + 1000);
			ret = true;
			break;
		case 3:
			script.getMouse().moveVerySlightly();
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(500) + 700);
			ret = true;
			break;
		case 4:
			script.getMouse().moveVerySlightly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveOutsideScreen();
			releaseMouseOwned();
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
			ret = true;
			break;
		case 5:
			script.getMouse().moveSlightly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveRandomly();
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(500) + 700);
			ret = true;
			break;
		case 6:
			script.getMouse().moveRandomly();
			Thread.sleep(rn.nextInt(100) + 100);
			script.getMouse().moveSlightly();
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(500) + 1100);
			ret = true;
			break;
		case 7:
			script.getMouse().moveVerySlightly();
			releaseMouseOwned();
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		case 8:
			script.getMouse().move(rn.nextInt(20) - 10, rn.nextInt(20) - 10);
			releaseMouseOwned();
			Thread.sleep(rn.nextInt(100) + 100);
			ret = true;
			break;
		}

		if (rn.nextInt(10) == 0) {
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		}
		if(mouseOwned){
			releaseMouseOwned();
		}
		return ret;
	}

	private boolean rightClickNext() throws InterruptedException {
		if(itemManager.priorityPickup){
			threadHandler.logPrint(threadName, "Priority pickup enabled, returning.");
			return false;
		}
		if(!script.getMenuAPI().isOpen()){
			NPC next = fighter.getNextMonster();
			if (next != null && next.isVisible()) {	
				while (!(mouseOwned = threadHandler.ownMouse())) {
					Thread.sleep(rn.nextInt(100) + 100);
				}
				EntityDestination targetDest = new EntityDestination(script.getBot(), next);
				overWatch.setState(mouseState.ANTIBAN);
				script.getMouse().click(targetDest, true);
				releaseMouseOwned();
				t.reset();
				while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000) + 150)) {
					Thread.sleep(50);
				}
				Thread.sleep(rn.nextInt(500) + 400);
			} else if (next != null) {
				script.getCamera().toEntity(next);
				if (next.isVisible()) {	
					while (!(mouseOwned = threadHandler.ownMouse())) {
						Thread.sleep(rn.nextInt(100) + 100);
					}
					EntityDestination targetDest = new EntityDestination(script.getBot(), next);
					overWatch.setState(mouseState.ANTIBAN);
					script.getMouse().click(targetDest, true);
					releaseMouseOwned();
					t.reset();
					while (!script.getMenuAPI().isOpen() && t.timer(rn.nextInt(1000) + 150)) {
						Thread.sleep(50);
					}
				} else {
					return false;
				}
			}
		}
		fighter.removeSpuriousRightClicks();
		state = State.RightClicked;
		return true;
	}

	public boolean hoverSkill() throws InterruptedException {
		if (state == State.HoverSkill) {
			return false;
		}
		boolean ret = false;
		Skill[] skillArray = { Skill.HITPOINTS, Skill.PRAYER, (itemManager.getRange() ? Skill.RANGED : Skill.STRENGTH) };
		Skill skill = skillArray[rn.nextInt(skillArray.length - 1)];
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.ANTIBAN);
		if (script.getSkills().hoverSkill(skill)) {
			Thread.sleep(rn.nextInt(300) + 700);
			state = State.HoverSkill;
			ret = true;
		}
		releaseMouseOwned();
		if (rn.nextInt(10) == 0) {
			while (fighter.isFighting()) {
				Thread.sleep(100);
			}
		}
		return ret;
	}

	public void cameraManager() throws InterruptedException {
		int[] sideKey = { LEFT_KEY, RIGHT_KEY };
		int chosenKey = rn.nextInt(100) % 2;
		int[] keysPressed = { UP_KEY, sideKey[chosenKey] };
		int firstReleased = rn.nextInt(150) % 2;
		int nextReleased = (firstReleased + 1) % 2;
		if (script.getCamera().getYawAngle() < 45) {
			int randomChoice = rn.nextInt(700000) % 2;
			int choiceTwo = (randomChoice + 1) % 2;
			script.getKeyboard().pressKey(keysPressed[randomChoice]);
			Thread.sleep(rn.nextInt(10) + 10);
			script.getKeyboard().pressKey(keysPressed[choiceTwo]);
			Thread.sleep(rn.nextInt(1300) + 900);
			script.getKeyboard().releaseKey(keysPressed[firstReleased]);
			Thread.sleep(rn.nextInt(15) + 1);
			script.getKeyboard().releaseKey(keysPressed[nextReleased]);
		}
	}

	@Override
	public void run() {
		synchronized (script) {
			while (!threadHandler.getThreadKillMessage()) {
				while (!script.client.isLoggedIn()) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						threadHandler.logPrint(threadName,"Antiban: Exception in Thread sleep handler." + e);
						script.log("Antiban: Exception in Thread sleep handler." + e);
						e.printStackTrace();
						threadHandler.exceptionPrint(threadName, e);
					}
				}
				rn = new Random(rnOver.nextInt());
				try {
					if (fighter.getCurrent() != null && fighter.getCurrent().getHealthPercent() < 25
							&& state == State.RightClicked) {
						Thread.sleep(100);
					} else {
						AntibanHandler();
					}
					if (rn.nextInt(10) == 0) {
						while (fighter.isFighting()) {
							Thread.sleep(100);
						}
					}
				} catch (Exception e) {
					releaseMouseOwned();
					threadHandler.logPrint(threadName,"Antiban: Exception in AntiBan Thread handler." + e);
					script.log("Antiban: Exception in AntiBan Thread handler." + e);
					e.printStackTrace();
					threadHandler.exceptionPrint(threadName, e);
				}
				try {
					Thread.sleep(rn.nextInt(1000) + 300);
				} catch (Exception e) {
					threadHandler.logPrint(threadName,"Antiban: Exception in Thread sleep handler." + e);
					script.log("Antiban: Exception in Thread sleep handler." + e);
					e.printStackTrace();
					threadHandler.exceptionPrint(threadName, e);
				}
			}
		}
	}
}
