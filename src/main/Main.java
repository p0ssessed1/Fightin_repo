package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import antiban.Antiban;
import banking.Banking;
import eatingThread.Eater;
import fighting.Fighting;
import groundItemManager.GroundItemManager;
import groundItemManager.GroundItemManager.pickupRC;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;
import simpleGui.SimpleGui;

@ScriptManifest(author = "EmbeddedJ", info = "Dynamic fighter", name = "Beta Dynamic fighter v0.8", version = .8, logo = "")
public class Main extends Script {
	Banking bank;
	Fighting fighter;
	Antiban antiban;
	Eater eater;
	GroundItemManager itemManager;
	ThreadHandler threadHandler;
	OverWatch overWatch;

	long timeStart;
	int startStrengthLvl;
	int startStrengthXp;
	int startAttackLvl;
	int startAttackXp;
	int startHpLvl;
	int startHpXp;
	
	int pickupCount = 0;
	
	@Override
	public void onStart() throws InterruptedException {
		log("Starting.");
		bank = new Banking(this);
		log("Initialized banks");
		fighter = new Fighting(this);
		log("Initialized fighter");
		antiban = new Antiban(this, fighter);
		log("Initialized antiban");
		eater = new Eater(this, fighter);
		log("Initialized eating");
		itemManager = new GroundItemManager(this, bank, fighter);
		log("Initialized ground item manager");
		threadHandler = new ThreadHandler(this, antiban, eater, itemManager);
		fighter.setThreadHandler(threadHandler);
		bank.setThreadHandler(threadHandler);
		antiban.setThreadHandler(threadHandler);
		eater.setThreadHandler(threadHandler);
		itemManager.setThreadHandler(threadHandler);
		log("Initialized ThreadHandler");
		overWatch = new OverWatch(this,fighter,antiban,eater);
		overWatch.setThreadHandler(threadHandler);
		overWatch.setState(mouseState.None);
		
		eater.setOverWatch(overWatch);
		fighter.setOverWatch(overWatch);
		antiban.setOverWatch(overWatch);
		threadHandler.setOverWatch(overWatch);
		itemManager.setOverWatch(overWatch);

		getKeyboard().initializeModule();
		getCamera().initializeModule();
		getInventory().initializeModule();
		getCombat().initializeModule();
		getMouse().initializeModule();

		timeStart = System.currentTimeMillis();
		startStrengthLvl = getExperienceTracker().getGainedLevels(Skill.STRENGTH);
		startStrengthXp = getExperienceTracker().getGainedXP(Skill.STRENGTH);
		startAttackLvl = getExperienceTracker().getGainedLevels(Skill.ATTACK);
		startAttackXp = getExperienceTracker().getGainedXP(Skill.ATTACK);
		startHpLvl = getExperienceTracker().getGainedLevels(Skill.HITPOINTS);
		startHpXp = getExperienceTracker().getGainedXP(Skill.HITPOINTS);

		SimpleGui gui = new SimpleGui(this, this.fighter, this.bank, this.eater, this.itemManager);
		log("Initialized gui");
		gui.Setup();
		log("Setup Gui");
		gui.Display();
	}

	int failCount = 0;

	@Override
	public int onLoop() throws InterruptedException {
		if (failCount > 3) {
			stop(true);
		}
		while (!client.isLoggedIn()) {
			Script.sleep(1000);
		}
		sleep(random(450, 700));
		if (!threadHandler.isSettup()) {
			threadHandler.settup();
		}
		
		banking();
		fighting();
		fighter.removeSpuriousRightClicks();
		
		if (random(0, 1) == 0) {
			if (getSettings().getRunEnergy() > random(15, 40)) {
				if (!getSettings().isRunning()) {
					getSettings().setRunning(true);
				}
			}
		}

		return 0;
	}

	public void banking() throws InterruptedException{
		if (!fighter.hasFood()) {
			if (bank.bank()) {
				log("Banking Succesful");
				fighter.reset();
				if (!fighter.isInArea()) {
					log("Not In area.");
					if (!fighter.walkToArea()) {
						log("Couldn't walk back to area.");
						failCount++;
					} else {
						failCount = 0;
					}
				}
			} else {
				log("Banking Failed");
			}
		}
	}
	
	public void fighting() throws InterruptedException{
		if (!fighter.isFighting()) {
			if(random(0,4) == 0 || pickupCount > random(4,7)){
				if(pickupRC.RC_FAIL == itemManager.pickupItems()){
					return;
				}
				pickupCount = 0;
			} else {
				pickupCount++;
			}
			if (fighter.attack()) {
				log("Attacked.");
				sleep(100);
			} else {
				log("Failed to attack.");
				if (!fighter.walkToNpcs()) {
					if (!fighter.isInArea()) {
						log("Not In area.");
						if (!fighter.walkToArea()) {
							log("Couldn't walk back to area.");
							failCount++;
						} else {
							failCount = 0;
						}
					}
				} else {
					fighter.attack();
				}
			}
		} else {
			sleep(random(500, 700));
		}
	}
	
	@Override
	public void onMessage(Message message) throws InterruptedException {
		log("onMessage: " + message.getMessage());
		if (message.getMessage().contains("Oh dear")) {
			died();
		} else if (message.getMessage().contains("already under")) {
			alreadyUnderAttack();
		}
	}

	private void alreadyUnderAttack() throws InterruptedException {
//		if (!fighter.attackAttacker()) {
			Thread.sleep(random(2000, 4000) + 2000);
//		}
	}

	private void died() throws InterruptedException {
		Script.sleep(random(150, 500));
		Item[] inv = getInventory().getItems();
		for (Item i : inv) {
			if (i != null && i.hasAction("Wield")) {
				i.interact("Wield");
				sleep(random(250, 600));
			}
		}
		fighter.reset();

	}

	@Override
	public void onPaint(Graphics2D g) {
		long timeElapsed = System.currentTimeMillis() - timeStart;
		long seconds = (timeElapsed / 1000) % 60;
		long minutes = (timeElapsed / (1000 * 60)) % 60;
		long hours = (timeElapsed / (1000 * 60 * 60)) % 24;
		g.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
		g.setColor(Color.white);

		g.drawString("x", (int) getMouse().getPosition().getX() - 4, (int) getMouse().getPosition().getY() + 5);
		g.drawString("Time Running: " + (hours >= 10 ? "" + hours : "0" + hours) + ":"
				+ (minutes >= 10 ? "" + minutes : "0" + minutes) + ":" + (seconds >= 10 ? "" + seconds : "0" + seconds),
				8, 50);
		g.drawString("Strength XP: " + (getExperienceTracker().getGainedXP(Skill.STRENGTH) - startStrengthXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.STRENGTH) - startStrengthLvl) + ")", 8, 65);
		g.drawString("Attack XP: " + (getExperienceTracker().getGainedXP(Skill.ATTACK) - startAttackXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.ATTACK) - startAttackLvl) + ")", 8, 80);
		g.drawString("Hitpoints XP: " + (getExperienceTracker().getGainedXP(Skill.HITPOINTS) - startHpXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.HITPOINTS) - startHpLvl) + ")", 8, 95);

	}

	@Override
	public void onExit() {
		threadHandler.kill();
	}
}
