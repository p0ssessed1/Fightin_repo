package groundItemManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import banking.Banking;
import dynamicArea.DynamicArea;
import fighting.Fighting;
import main.ThreadHandler;
import main.Timer;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;

public class GroundItemManager {

	Random rn;
	Script script;
	ThreadHandler threadHandler;
	Banking banker;
	Fighting fighter;
	DynamicArea dynamicArea;

	Timer t = new Timer();
	boolean enabled = false;
	boolean priorityPickup = false;
	Filter<GroundItem> itemFilter;
	List<GroundItem> items;
	List<GroundItem> filteredItems = new LinkedList<GroundItem>();
	OverWatch overWatch;

	public enum pickupRC {
		RC_OK, RC_NONE, RC_FAIL
	};

	public GroundItemManager(Script script, Banking banker, Fighting fighter) {
		this.script = script;
		this.banker = banker;
		this.fighter = fighter;
		this.dynamicArea = fighter.getDynamicArea();
		rn = new Random(script.myPlayer().getId());
	}

	public void setOverWatch(OverWatch overWatch){
		this.overWatch = overWatch;
	}
	
	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public void setItemFilter(String[] items) {
		itemFilter = new NameFilter<GroundItem>(items);
		if (items != null && items.length > 0) {
			this.enabled = true;
		}
	}

	public void setPriorityPickup() {
		this.priorityPickup = true;
	}

	public boolean isItems() {
		return !filteredItems.isEmpty();
	}

	private boolean walkToItem(GroundItem gi) throws InterruptedException {
		boolean ret = false;
		if (gi != null) {
			while (!threadHandler.ownMouse()) {
				Script.sleep(rn.nextInt(100) + 100);
			}
			overWatch.setState(mouseState.Walking);
			script.getWalking().walk(gi.getArea(2));
			threadHandler.releaseMouse();
			Script.sleep(rn.nextInt(300) + 350);
		}
		return ret;
	}

	public boolean isGroundItemValid(GroundItem groundItem) {
		if (groundItem != null) {
			if (itemFilter.match(groundItem)) {
				int id = groundItem.getId();
				if (id != -1) {
					for (GroundItem i : script.getGroundItems().get(groundItem.getX(), groundItem.getY())) {
						if (i.getId() == id) {
							if (script.map.canReach(groundItem)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean eatIfLow() throws InterruptedException {
		boolean ret = false;
		Item food = script.getInventory().getItem(i -> i.hasAction("Eat"));
		if (food == null) {
			return false;
		}
		if (priorityPickup) {
			while (!threadHandler.ownMouse()) {
				Script.sleep(rn.nextInt(100) + 100);
			}
			overWatch.setState(mouseState.Eating);
			food.interact("Eat");
			threadHandler.releaseMouse();
		} else {
			if (script.getSkills().getDynamic(
					Skill.HITPOINTS) < (script.getSkills().getStatic(Skill.HITPOINTS) - (8 + rn.nextInt(3)))) {
				while (!threadHandler.ownMouse()) {
					Script.sleep(rn.nextInt(100) + 100);
				}
				overWatch.setState(mouseState.Eating);
				food.interact("Eat");
				threadHandler.releaseMouse();
				ret = true;
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public pickupRC pickupItems() throws InterruptedException {
		fighter.clearMonsters();
		pickupRC ret = pickupRC.RC_NONE;
		boolean animated = false;
		if (!enabled) {
			return ret;
		}
		GroundItem item;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 25000;
		Script.sleep(rn.nextInt(700) + 850);
		while ((item = script.getGroundItems().closest(true, gi -> itemFilter.match(gi) && isGroundItemValid(gi)
				&& dynamicArea.getOverallArea().contains(gi))) != null) {
			if (script.getInventory().isFull()) {
				if (!eatIfLow()) {
					return ret;
				}
			}
			if (!item.isVisible() && rn.nextInt(1000) > rn.nextInt(300) + 200) {
				walkToItem(item);
			}
			criticalPickup(item);
			Script.sleep(rn.nextInt(400) + 550);
			t.reset();
			while (!(animated = script.myPlayer().isAnimating()) && script.myPlayer().isMoving()
					&& t.timer(rn.nextInt(2500) + timeoutVal)) {
			}
			if (animated) {
				/* Means I miss-clicked and am attacking. */
				Script.sleep(rn.nextInt(1000) + 1000);
				return pickupRC.RC_FAIL;
			}
			ret = pickupRC.RC_OK;
			Script.sleep(rn.nextInt(150) + 50);
		}
		return ret;
	}

	private boolean criticalPickup(GroundItem item) throws InterruptedException {
		while (!threadHandler.ownMouse()) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.PickingUp);
		boolean ret = item.interact("Take");
		threadHandler.releaseMouse();
		return ret;
	}
}
