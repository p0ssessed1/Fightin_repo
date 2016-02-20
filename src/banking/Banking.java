package banking;

import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;

import main.Timer;

public class Banking {

	Random rn;
	Script script;
	NameFilter<Item> keepItems;

	Area bankArea;
	Timer t = new Timer();

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

	public boolean walkToArea() {
		if (!bankArea.contains(script.myPlayer())) {
			return script.getWalking().webWalk(bankArea);
		}
		return false;
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

	public boolean bank() throws InterruptedException {
		boolean isOpen = false;
		if (!bankArea.contains(script.myPlayer())) {
			if (!walkToArea()) {
				script.log("Could not walk to area.");
				return false;
			}
		}
		t.reset();
		while(t.timer(10000)){
			if (Script.random(0, 1) == 0) {
				NPC banker = getBanker();
				if(banker!= null){
					banker.interact("Bank");
					break;
				}
			} else {
				RS2Object bank = getBankBooth();
				if(bank != null){
					bank.interact("Bank");
					break;
				}
			}
		}
		t.reset();
		while (!(isOpen = script.getBank().isOpen()) && t.timer(rn.nextInt(2000) + 1000))
			;
		if (!isOpen) {
			script.log("Could not open bank.");
			return false;
		}
		script.sleep(rn.nextInt(1000) + 250);
		if (script.getBank().depositAllExcept(keepItems)) {
			return true;
		} else {
			script.log("Could not deposit all.");
			return false;
		}
	}
}
