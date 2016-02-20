package fighting;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import dynamicArea.DynamicArea;
import main.Timer;

public class Fighting {

	/* TODO - Implement different fight types. */
	Random rn;
	public Script script;
	DynamicArea dynamicArea;
	String action;
	volatile NPC current;

	List<Area> fightingAreas = new LinkedList<Area>();
	NameFilter<NPC> monsterFilter;
	ActionFilter<NPC> actionFilter;
	Timer t = new Timer();

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
		if (isInArea()) {
			if (current != null) {
				for (int i = 0; i < 15; i++) {
					Script.sleep(rn.nextInt(150) + 50);
					if (script.myPlayer().isAnimating() ||
						current.getCurrentHealth() > 0 ||
						script.getCombat().isFighting()) {
						return true;
					} else if (i == 14) {
						return false;
					}
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
	 * Walk to the closest fighting area. Uses WebWalk.
	 * 
	 * @return True if walking is successful. False if already in area or
	 *         unsuccessful.
	 */
	public boolean walkToArea() {
		if (!fightingAreas.contains(script.myPlayer())) {
			script.log("fighting aeas is empty?" + fightingAreas.isEmpty());
			script.log("closest areas is null? " + (dynamicArea.getClosestArea(fightingAreas) == null));
			return script.getWalking().webWalk(dynamicArea.getClosestArea(fightingAreas));
		} else {
			return false;
		}
	}

	/**
	 * Attemps to fight with a timeout. Walks to nearest fighting area, Looks
	 * for fight that has been previously set up. Attemps to fight from that
	 * location.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean attack() throws InterruptedException {
		boolean animated = false;
		for (Area a : fightingAreas) {
			if (!a.contains(script.myPlayer())) {
				if (!walkToArea()) {
					return false;
				}
			}
		}
		if (isInArea()) {
			Timer local_timer = new Timer();
			while (local_timer.timer(30000)) {
				NPC monster = script.getNpcs().closest(true, n -> actionFilter.match(n));
				if (isNpcValid(monster)) {
					// script.log("About to interract.");
					monster.interact(action);
					t.reset();
					while (!script.myPlayer().isMoving() && t.timer(rn.nextInt(1000) + 5000)) {
						Script.sleep(rn.nextInt(200) + 100);
					}
					t.reset();
					while (!(animated = script.myPlayer().isAnimating()) && t.timer(rn.nextInt(500) + 2500)) {
						Script.sleep(rn.nextInt(200) + 100);
					}
					if (animated) {
						current = monster;
					} else {
						current = null;
					}
					return animated;
				}
			}
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
			if (a.contains(script.myPosition())) {
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
	public NPC getNextMonster() {
		List<NPC> npcs = script.getNpcs().getAll();
		Position myPosition = script.myPosition();
		NPC temp1;
		NPC temp2;
		NPC nearest = null;
		for (int i = 1; i < npcs.size(); i++) {
			temp1 = npcs.get(i);
			temp2 = npcs.get(i - 1);
			if (myPosition.distance(temp1) > myPosition.distance(temp2)) {
				if (monsterFilter.match(temp2)) {
					if (temp2 != current) {
						nearest = temp2;
					}
				}
			} else {
				if (isNpcValid(npcs.get(i))) {
					if (monsterFilter.match(temp1)) {
						if (temp1 != current) {
							nearest = temp1;
						}
					}
				}
			}
		}

		return nearest;
	}

	public boolean clickNextMonster(NPC npc) throws InterruptedException {
		if (script.getMenuAPI().isOpen()) {
			if (script.getMenuAPI().selectAction(action)) {
				while (script.myPlayer().isMoving() && t.timer(rn.nextInt(2500) + 1000)) {
					Script.sleep(100);
				}
				if (isFighting()) {
					current = npc;
				} else {
					current = null;
				}
				return true;
			}
		}
		return false;
	}
}
