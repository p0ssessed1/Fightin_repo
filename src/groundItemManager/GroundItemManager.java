package groundItemManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import banking.Banking;
import dynamicArea.DynamicArea;
import fighting.Fighting;
import main.ThreadHandler;
import main.Timer;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;

@SuppressWarnings("unchecked")
public class GroundItemManager {

	Random rn;
	Script script;
	ThreadHandler threadHandler;
	Banking banker;
	Fighting fighter;
	DynamicArea dynamicArea;

	Timer t = new Timer();
	boolean enabled = false;
	boolean buryingEnabled = false;
	public boolean priorityPickup = false;
	Filter<GroundItem> itemFilter;
	List<GroundItem> items;
	List<GroundItem> filteredItems = new LinkedList<GroundItem>();
	OverWatch overWatch;
	
	volatile boolean mouseOwned;

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
	
	public boolean getBurying(){
		return this.buryingEnabled;
	}
	
	public void setBurying(boolean bury){
		this.buryingEnabled = bury; 
	}
	
	private boolean boneBurryier() throws InterruptedException{
		if(!buryingEnabled){
			return false;
		}
		boolean ret = false;
		Item bone;
		while(null != (bone = script.getInventory().getItem(i -> (i.getName().contains("Bone") || i.getName().contains("bone"))  && i.hasAction("Bury")))){
			while (!threadHandler.ownMouse()) {
				Script.sleep(rn.nextInt(100) + 100);
			}
			overWatch.setState(mouseState.Eating);
			ret = bone.interact("Bury");
			threadHandler.releaseMouse();
			Thread.sleep(rn.nextInt(500) + 250);
		}
		return ret;
	}
	
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

	public pickupRC pickupItems() throws InterruptedException {
		NPC dieing = fighter.getCurrent();
		fighter.reset();
		pickupRC ret = pickupRC.RC_NONE;
		boolean animated = false;
		if (!enabled) {
			return ret;
		}
		GroundItem item;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 25000;
		int animatingTimeout = rn.nextInt(700) + 850;
		int itemTimeout = rn.nextInt(300) + 300;
		t.reset();
		while(dieing != null && dieing.isAnimating() && t.timer(animatingTimeout)){
			Script.sleep(50);
		}
		t.reset();
		/* TODO - Update if monsters that drop nothing 100% are being killed. */
		while(dieing != null && !script.getGroundItems().get(dieing.getPosition().getX(), dieing.getPosition().getY()).isEmpty() && t.timer(itemTimeout))
		{
			Script.sleep(50);
		}
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
				script.log("MissClicked monster...");
				Script.sleep(rn.nextInt(1000) + 5000);
				return pickupRC.RC_FAIL;
			}
			ret = pickupRC.RC_OK;
			Script.sleep(rn.nextInt(100) + 25);
		}

		boneBurryier();
		
		return ret;
	}
	
	private void releaseMouseOwned(){
		if(mouseOwned){
			mouseOwned = threadHandler.releaseMouse();
		}
	}
	
	public void resetMouseOwned(){
		this.mouseOwned = false;
	}
	
	private boolean criticalPickup(GroundItem item) throws InterruptedException {
		while (!(mouseOwned = threadHandler.ownMouse())) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		overWatch.setState(mouseState.PickingUp);
		boolean ret = item.interact("Take");
		releaseMouseOwned();
		return ret;
	}
}
