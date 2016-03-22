package fighting;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.script.Script;

import dynamicArea.DynamicArea;
import main.Timer;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;
import utils.Utils;
import main.ThreadHandler;

public class Fighting {
	final String threadName = "Fighting";

	Random rn;
	public Script script;
	DynamicArea dynamicArea;
	String action;
	volatile NPC current;
	NameFilter<Item> foodItem;
	ThreadHandler threadHandler;
	Utils utils;
	volatile boolean mouseOwned = false;

	List<Area> fightingAreas = new LinkedList<Area>();
	NameFilter<NPC> monsterFilter;
	List<String> monsterNames = new LinkedList<String>();
	ActionFilter<NPC> actionFilter;
	Timer t = new Timer();

	NPC rightClicked = null;
	OverWatch overWatch;

	public void setOverWatch(OverWatch overWatch) {
		this.overWatch = overWatch;
	}

	public void reset() {
		current = null;
		rightClicked = null;
	}

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public void setFood(String foodItem) {
		this.foodItem = new NameFilter<Item>(foodItem);
	}

	public NameFilter<Item> getFood() {
		return foodItem;
	}

	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	public Fighting(Script script) {
		this.script = script;
		this.dynamicArea = new DynamicArea(this);
		rn = new Random(script.myPlayer().getId());
		fightingAreas = dynamicArea.CreateAreas(script.getNpcs().getAll());
		if (fightingAreas.isEmpty()) {
			threadHandler.logPrint(threadName, "No fighting areas retrieved.");
			script.log("No fighting areas retrieved.");
			script.stop(false);
		}
	}

	/**
	 * Check if the player is fighting via a poling method. May take 2s if not
	 * fighting.
	 * 
	 * @return True if fighting (animating) False otherwise
	 * @throws InterruptedException
	 */
	public boolean isFighting() throws InterruptedException {
		for (int i = 0; i < 15; i++) {
			Script.sleep(rn.nextInt(75) + 50);
			try {
				if (script.myPlayer().isAnimating() || (current != null && current.isInteracting(script.myPlayer())
						&& current.getCurrentHealth() > 0) || script.getCombat().isFighting()) {
					return true;
				} else if (i == 14) {
					// current = null;
					return false;
				}
			} catch (Exception e) {
				threadHandler.exceptionPrint(threadName, e);
				return false;
			}
		}
		return false;
	}

	/**
	 * Walk to the closest fighting area. Uses WebWalk. Gathers new areas to
	 * walk to based off of monster locations.
	 * 
	 * @return True if walking is successful. False if already in area or
	 *         unsuccessful.
	 * @throws InterruptedException
	 */
	public boolean walkToArea() throws InterruptedException {
		boolean walked = false;
		if (!fightingAreas.contains(script.myPlayer())) {
			if (!dynamicArea.getClosestArea(fightingAreas).contains(script.myPlayer())) {
				while (!(mouseOwned = threadHandler.ownMouse())) {
					Script.sleep(rn.nextInt(100) + 100);
				}
				overWatch.setState(mouseState.WALKING);
				if (script.getWalking().webWalk(dynamicArea.getRandomArea(fightingAreas))) {
					Script.sleep(rn.nextInt(100) + 200);
					walked = true;
				}
			}

		}
		releaseMouseOwned();
		dynamicArea.addExclusiveAreas(script.getNpcs().getAll(), fightingAreas, monsterFilter);
		return walked;
	}

	/**
	 * Attempts to fight with a timeout. Looks for monster that has been
	 * previously set up. Attempts to attack.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public boolean attack() throws InterruptedException {
		boolean animated = false;
		if (script.getMenuAPI().isOpen()) {
			return clickNextMonster();
		}
		if (attackAttacker()) {
			return true;
		}
		NPC monster;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 30000;
		dynamicArea.addExclusiveAreas(script.getNpcs().getAll(), fightingAreas, monsterFilter);
		if (rn.nextInt(5) < 2) {
			monster = script.getNpcs().closest(true, n -> actionFilter.match(n) && monsterFilter.match(n)
					&& n.isVisible() && !(n.isUnderAttack() || n.isAnimating() || !n.isAttackable()));
		} else {
			monster = script.getNpcs().closest(true,
					n -> actionFilter.match(n) && monsterFilter.match(n) && !(n.isUnderAttack() || n.isAnimating()));
		}

		if (isNpcValid(monster)) {
			criticalAttack(monster);
			animated = attackDelay(timeoutVal);
			if (animated) {
				threadHandler.logPrint(threadName, "Attacking " + monster.getName());
				script.log("Attacking " + monster.getName());
				current = monster;
			} else {
				current = null;
			}
			return animated;
		}
		return false;
	}

	public boolean criticalAttack(NPC monster) throws InterruptedException {
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Script.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.ATTACKING);
		boolean ret = monster.interact(action);
		releaseMouseOwned();
		return ret;
	}

	/**
	 * Checks if spot is valid as a fighting spot.
	 * 
	 * @param npc
	 * @return True : fighting spot valid. False : Not a fighting spot.
	 */
	public boolean isNpcValid(NPC npc) {
		if (npc != null && script.map.canReach(npc)) {
			if (actionFilter != null && monsterFilter != null && actionFilter.match(npc) && monsterFilter.match(npc)) {
				int id = npc.getId();
				if (id != -1) {
					for (NPC i : script.getNpcs().get(npc.getX(), npc.getY())) {
						if (i.getId() == id) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Return if the player.
	 * 
	 * @return True : Player is in the fighting area. False: Player is not in
	 *         the fighting area.
	 */
	public boolean isInArea() {
		for (Area a : fightingAreas) {
			if (a.contains(script.myPlayer())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set action locally.
	 * 
	 * @param action
	 *            : Action to set.
	 */
	private void setAction(String action) {
		this.action = action;
		actionFilter = new ActionFilter<NPC>(action);
	}

	/**
	 * Set the type of fight that one wishes to fight. Also sets the action that
	 * corresponds to the fight and the type of spot.
	 * 
	 * @param fight
	 *            : Name of fight to fight.
	 */
	public void setMonsters(String[] fight) {
		this.monsterFilter = new NameFilter<NPC>(fight);
		for (String f : fight) {
			if (f != null) {
				this.monsterNames.add(f);
			}
		}
		setAction("Attack");
	}

	/**
	 * Gets the next spot to fight that is not the current spot.
	 * 
	 * @return NPC : npc of next spot that is closest null if none.
	 */
	@SuppressWarnings("unchecked")
	public NPC getNextMonster() {
		List<NPC> npcs = script.getNpcs().getAll();
		NPC nearest;
		if (rn.nextBoolean()) {
			nearest = script.getNpcs().closest(true, n -> actionFilter.match(n) && monsterFilter.match(n)
					&& n.isVisible() && !(n.isUnderAttack() || n.isAnimating()));
		} else {
			nearest = script.getNpcs().closest(true,
					n -> actionFilter.match(n) && monsterFilter.match(n) && !(n.isUnderAttack() || n.isAnimating()));
		}
		dynamicArea.addExclusiveAreas(npcs, fightingAreas, monsterFilter);
		rightClicked = nearest;
		return nearest;
	}

	private boolean attackDelay(int timeoutVal) throws InterruptedException {
		boolean animated = false;
		t.reset();
		while (script.myPlayer().isMoving() && t.timer(rn.nextInt(3000) + timeoutVal)) {
			Script.sleep(100);
		}
		t.reset();
		while (!(animated = script.myPlayer().isAnimating()) && t.timer(rn.nextInt(500) + 5000)) {
			Script.sleep(rn.nextInt(400) + 200);
		}
		return animated;
	}

	public boolean clickNextMonster() throws InterruptedException {
		boolean animated = false;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 30000;
		if (rightClicked != null){
			if (utils.selectMenuOption("Attack", rightClicked.getName())) {
				animated = attackDelay(timeoutVal);
				if (animated) {
					threadHandler.logPrint(threadName,
							"Attacking: " + rightClicked != null ? rightClicked.getName() : null);
					script.log("Attacking: " + rightClicked != null ? rightClicked.getName() : null);
					current = rightClicked;
				} else {
					current = null;
				}
			}
		} else {
			threadHandler.logPrint(threadName, "Removing Menu");
			script.log("Removing menu.");
			removeMenu();
		}

		rightClicked = null;
		return animated;
	}

	public DynamicArea getDynamicArea() {
		return this.dynamicArea;
	}

	public NPC getCurrent() {
		return current;
	}

	public boolean hasFood() {
		Item[] inv = script.getInventory().getItems();
		for (Item i : inv) {
			if (i != null && i.hasAction("Eat")) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean walkToNpcs() throws InterruptedException {
		if (isInArea()) {
			NPC npc = script.getNpcs().closest(true, n -> monsterFilter.match(n) && !n.isUnderAttack());
			if (npc != null) {
				while (!(mouseOwned = threadHandler.ownMouse())) {
					Script.sleep(rn.nextInt(100) + 100);
				}
				overWatch.setState(mouseState.WALKING);
				script.getWalking().walk(npc.getArea(2));
				releaseMouseOwned();
			}
			return true;
		} else {
			walkToArea();
		}
		return false;
	}

	public void removeMenu() throws InterruptedException {
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Script.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.ANTIBAN);
		while (script.getMenuAPI().isOpen()) {
			Script.sleep(rn.nextInt(100000) % 100);
			script.getMouse().moveRandomly();
			releaseMouseOwned();
			Script.sleep(rn.nextInt(500) + 400);
		}
		releaseMouseOwned();
	}

	public void removeSpuriousRightClicks() throws InterruptedException {
		if (script.getMenuAPI().isOpen() && rightClicked != null && !rightClicked.isUnderAttack()) {
			List<Option> menu = script.getMenuAPI().getMenu();
			for (Option o : menu) {
				if (o.action.contains("Attack")) {
					for (String s : monsterNames) {
						if (o.name.contains(s)) {
							return;
						}
					}
				}
			}
			removeMenu();
			rightClicked = null;
		}
	}

	public void resetMouseOwned() {
		this.mouseOwned = false;
	}

	private void releaseMouseOwned() {
		if (mouseOwned) {
			threadHandler.releaseMouse();
		}
	}

	public NPC getRightClicked() {
		return this.rightClicked;
	}

	private NPC searchForAttacker() throws InterruptedException {
		List<NPC> npcs = script.getNpcs().getAll();
		for (NPC n : npcs) {
			if (isNpcValid(n) && n.isInteracting(script.myPlayer())) {
				return n;
			}
		}
		return null;
	}

	public boolean attackAttacker() throws InterruptedException {
		NPC attacker;
		boolean ret = false;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 30000;
		if ((attacker = searchForAttacker()) != null) {
			current = attacker;
			if (criticalAttack(attacker)) {
				ret = attackDelay(timeoutVal);
			}
		}

		return ret;
	}
}
