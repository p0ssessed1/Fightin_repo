package fighting;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.script.Script;

import dynamicArea.DynamicArea;
import main.Timer;
import main.ThreadHandler;

public class Fighting {

	Random rn;
	public Script script;
	DynamicArea dynamicArea;
	String action;
	volatile NPC current;
	NameFilter<Item> foodItem;
	ThreadHandler threadHandler;

	List<Area> fightingAreas = new LinkedList<Area>();
	NameFilter<NPC> monsterFilter;
	ActionFilter<NPC> actionFilter;
	Timer t = new Timer();

	NPC rightClicked = null;

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

	public Fighting(Script script) {
		this.script = script;
		this.dynamicArea = new DynamicArea(this);
		rn = new Random(script.myPlayer().getId());
		fightingAreas = dynamicArea.CreateAreas(script.getNpcs().getAll());
		if (fightingAreas.isEmpty()) {
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
		if (current != null) {
			for (int i = 0; i < 15; i++) {
				Script.sleep(rn.nextInt(150) + 50);
				try {
					if (script.myPlayer().isAnimating() || current.getCurrentHealth() > 0
							|| script.getCombat().isFighting()) {
						return true;
					} else if (i == 14) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * CODE SNIPPED: Optional<NPC> lobster =
	 * getNpcs().getAll().stream().filter(o -> o.hasAction("Cage")).min(new
	 * Comparator<NPC>() {
	 * 
	 * @Override public int compare(NPC a, NPC b) { return
	 *           getMap().distance(a.getPosition()) -
	 *           getMap().distance(b.getPosition()); } });
	 *           if(lobster.isPresent()){ lobster.get().interact("Cage"); }
	 */

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
				if (script.getWalking().webWalk(dynamicArea.getClosestArea(fightingAreas))) {
					Script.sleep(rn.nextInt(100) + 200);
					walked = true;
				}
			}

		}
		dynamicArea.addExclusiveAreas(script.getNpcs().getAll(), fightingAreas);
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
		NPC monster;
		int timeoutVal = script.getSettings().isRunning() ? 7000 : 10000;
		dynamicArea.addExclusiveAreas(script.getNpcs().getAll(), fightingAreas);
		if (rn.nextInt(5) < 2) {
			monster = script.getNpcs().closest(true, n -> actionFilter.match(n) && monsterFilter.match(n)
					&& n.isVisible() && !(n.isUnderAttack() || n.isAnimating()));
		} else {
			monster = script.getNpcs().closest(true,
					n -> actionFilter.match(n) && monsterFilter.match(n) && !(n.isUnderAttack() || n.isAnimating()));
		}

		if (isNpcValid(monster)) {
			monster.interact(action);
			t.reset();
			while (!script.myPlayer().isMoving() && t.timer(rn.nextInt(3000) + timeoutVal)) {
				Script.sleep(rn.nextInt(400) + 200);
			}
			t.reset();
			while (!(animated = script.myPlayer().isAnimating()) && t.timer(rn.nextInt(500) + 2500)) {
				Script.sleep(rn.nextInt(400) + 200);
			}

			if (animated) {
				script.log("Attacking " + monster.getName());
				current = monster;
			} else {
				current = null;
			}
			return animated;
		}
		return false;
	}

	/**
	 * Checks if spot is valid as a fighting spot.
	 * 
	 * @param npc
	 * @return True : fighting spot valid. False : Not a fighting spot.
	 */
	public boolean isNpcValid(NPC npc) {
		if (npc != null && script.map.canReach(npc)) {
			if (actionFilter.match(npc) && monsterFilter.match(npc)) {
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
		dynamicArea.addExclusiveAreas(npcs, fightingAreas);
		rightClicked = nearest;
		return nearest;
	}

	public boolean clickNextMonster() throws InterruptedException {
		boolean animated = false;
		if (rightClicked == null && current == rightClicked) {
			while (script.getMenuAPI().isOpen()) {
				Script.sleep(rn.nextInt() % 100);
				script.getMouse().moveRandomly();
				Script.sleep(rn.nextInt(500) + 400);
			}
			current = null;
			rightClicked = null;
			return false;
		}
		int timeoutVal = script.getSettings().isRunning() ? 7000 : 10000;
		if (script.getMenuAPI().isOpen() && !rightClicked.isUnderAttack() && script.getMenuAPI().selectAction(action)) {
			while (script.myPlayer().isMoving() && t.timer(rn.nextInt(3000) + timeoutVal)) {
				Script.sleep(100);
			}
			t.reset();
			while (!(animated = script.myPlayer().isAnimating()) && t.timer(rn.nextInt(500) + 2500)) {
				Script.sleep(rn.nextInt(400) + 200);
			}

			if (animated) {
				script.log("Arracking: " + rightClicked.getName());
				current = rightClicked;
			} else {
				current = null;
			}
		} else {
			if (rn.nextInt() % 10 > 5) {
				rightClicked = getNextMonster();
				EntityDestination targetDest = new EntityDestination(script.getBot(), rightClicked);
				script.getMouse().click(targetDest, false);
				while (script.myPlayer().isMoving() && t.timer(rn.nextInt(2500) + timeoutVal)) {
					Script.sleep(100);
				}
				t.reset();
				while (!(animated = script.myPlayer().isAnimating()) && t.timer(rn.nextInt(500) + 2500)) {
					Script.sleep(rn.nextInt(400) + 200);
				}

				if (animated) {
					script.log("Arracking: " + rightClicked.getName());
					current = rightClicked;
				} else {
					current = null;
				}
			} else {
				while (script.getMenuAPI().isOpen()) {
					Script.sleep(rn.nextInt() % 100);
					script.getMouse().moveRandomly();
					Script.sleep(rn.nextInt(500) + 400);
				}
			}
		}

		rightClicked = null;
		return animated;
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
				Area area = npc.getArea(2);
				script.getWalking().walk(area);
			}
			return true;
		} else {
			walkToArea();
		}
		return false;
	}
}
