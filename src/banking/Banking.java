package banking;

import java.util.Random;

import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;

import main.ThreadHandler;
import main.Timer;

public class Banking {

	Random rn;
	Script script;
	ThreadHandler threadHandler;

	NameFilter<Item> keepItems;
	NameFilter<Item> foodItems;

	Area bankArea;
	Timer t = new Timer();

	public void setThreadHandler(ThreadHandler threadHandler) {
		this.threadHandler = threadHandler;
	}

	public Banking(Script script) {
		this.script = script;
		rn = new Random(script.myPlayer().getId());
	}

	public boolean isInArea() {
		return bankArea.contains(script.myPosition());
	}

	public void setArea(Area a) {
		this.bankArea = a;
	}

	public Area getArea() {
		return bankArea;
	}

	public void setKeepItems(NameFilter<Item> keepItemsFilter) {
		this.keepItems = keepItemsFilter;
	}

	public void setFood(String foodItem) {
		this.foodItems = new NameFilter<Item>(foodItem);
	}

	public boolean walkToArea() throws InterruptedException {
		boolean ret = false;
		while(!threadHandler.ownMouse()){
			Script.sleep(rn.nextInt(100) + 100);
		}
		if (!bankArea.contains(script.myPlayer())) {
			ret = script.getWalking().webWalk(bankArea);
		}
		threadHandler.releaseMouse();
		return ret;
	}

	private NPC getBanker() {
		NPC banker = script.getNpcs().closestThatContains("Bank");
		if (banker != null && banker.hasAction("Bank")) {
			int id = banker.getId();
			if (id != -1) {
				for (NPC i : script.getNpcs().get(banker.getX(), banker.getY())) {
					if (i.getId() == id) {
						return banker;
					}
				}
			}
		}
		return null;
	}

	private RS2Object getBankBooth() {
		RS2Object bank = script.getObjects().closestThatContains("Bank Booth");
		if (bank != null && bank.hasAction("Bank")) {
			return bank;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean bank() throws InterruptedException {
		boolean isOpen = false;
		if (bankArea == null) {
			return false;
		}
		if (!bankArea.contains(script.myPlayer())) {
			if (!walkToArea()) {
				script.log("Could not walk to area.");
				return false;
			}
		}
		boolean banked = false;
		boolean bankerChosen = false;
		NPC banker = getBanker();
		RS2Object bankBooth = getBankBooth();
		t.reset();
		if (Script.random(0, 1) == 0) {
			if (banker != null) {
				bankerChosen = true;
			}
		} else {
			if (bankBooth != null) {
				bankerChosen = false;
			}
		}
		while (!banked) {
			if (bankerChosen) {
				if (banker != null) {
					criticalBank(banker);
				} else {
					banker = getBanker();
				}
			} else {
				if (bankBooth != null) {
					criticalBank(bankBooth);
				} else {
					bankBooth = getBankBooth();
				}
			}
			Script.sleep(rn.nextInt(1000) + 250);
			t.reset();
			while (!(isOpen = script.getBank().isOpen()) && t.timer(rn.nextInt(2000) + 1500))
				;
			if (isOpen) {
				Script.sleep(rn.nextInt(1000) + 250);
				if (keepItems != null) {
					if (script.getBank().depositAllExcept(keepItems)) {
						Script.sleep(rn.nextInt(1000) + 250);
						if (foodItems != null) {
							if (script.getBank().contains(foodItems)) {
								script.getBank().withdrawAll(foodItems);
								banked = true;
							} else {
								script.stop(true);
							}
						}
					}
				} else {
					if (!script.getInventory().isEmpty()) {
						script.getBank().depositAll();
					}
					Script.sleep(rn.nextInt(1000) + 250);
					if (foodItems != null) {
						if (script.getBank().contains(foodItems)) {
							script.getBank().withdrawAll(foodItems);
							banked = true;
						} else {
							script.stop(true);
						}
					}
				}
			}
			Script.sleep(rn.nextInt(1000) + 250);
		}

		return banked;
	}

	private boolean criticalBank(RS2Object bankBooth) throws InterruptedException {
		boolean ret = false;
		while (!threadHandler.ownMouse()) {
			Script.sleep(rn.nextInt(101) + 100);
		}
		ret = bankBooth.interact("Bank");
		threadHandler.releaseMouse();
		return ret;
	}
	
	private boolean criticalBank(NPC banker) throws InterruptedException {
		boolean ret = false;
		while (!threadHandler.ownMouse()) {
			Script.sleep(rn.nextInt(101) + 100);
		}
		ret = banker.interact("Bank");
		threadHandler.releaseMouse();
		return ret;
	}
}
