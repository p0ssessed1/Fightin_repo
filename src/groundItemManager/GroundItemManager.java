package groundItemManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.script.Script;

import main.ThreadHandler;
import main.Timer;

public class GroundItemManager {

	Random rn;
	Script script;
	ThreadHandler threadHandler;

	Timer t = new Timer();
	boolean enabled = false;
	Filter<GroundItem> itemFilter;
	List<GroundItem> items;
	List<GroundItem> filteredItems = new LinkedList<GroundItem>();

	public GroundItemManager(Script script) {
		this.script = script;
		rn = new Random(script.myPlayer().getId());
	}

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public void setItemFilter(String[] items) {
		itemFilter = new NameFilter<GroundItem>(items);
		if(items != null && items.length > 0){
			this.enabled = true;
		}
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
	public boolean pickupItems() throws InterruptedException {
		boolean ret = false;
		if(!enabled){
			return ret;
		}
		GroundItem item;
		int timeoutVal = script.getSettings().isRunning() ? 10000 : 25000;
		Script.sleep(rn.nextInt(700) + 850);
		while((item = script.getGroundItems().closest(true, gi -> itemFilter.match(gi) && isGroundItemValid(gi))) != null){
			if (script.getInventory().isFull()) {
				return ret;
			}
			if (!item.isVisible() && rn.nextInt(1000) > rn.nextInt(300) + 200) {
				walkToItem(item);
			}
			criticalPickup(item);
			Script.sleep(rn.nextInt(300) + 350);
			t.reset();
			while (script.myPlayer().isMoving() && t.timer(rn.nextInt(2500) + timeoutVal)) {
			}
			Script.sleep(rn.nextInt(350) + 150);
		}
		return ret;
	}

	private boolean criticalPickup(GroundItem item) throws InterruptedException {
		while (!threadHandler.ownMouse()) {
			Thread.sleep(rn.nextInt(100) + 100);
		}
		boolean ret = item.interact("Take");
		threadHandler.releaseMouse();
		return ret;
	}
}
