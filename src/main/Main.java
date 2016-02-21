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
import simpleGui.SimpleGui;

@ScriptManifest(author = "EmbeddedJ", info = "Dynamic fighter", name = "Beta Dynamic fighter v0.1", version = .1, logo = "")
public class Main extends Script {
	Banking bank;
	Fighting fighter;
	Antiban antiban;
	Eater eater;
	ThreadHandler threadHandler;
	
	long timeStart;
	int startStrengthLvl;
	int startStrengthXp;
	int startAttackLvl;
	int startAttackXp;
	int startHpLvl;
	int startHpXp;

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
		threadHandler = new ThreadHandler(this, antiban, eater);
		fighter.setThreadHandler(threadHandler);
		bank.setThreadHandler(threadHandler);

		getKeyboard().initializeModule();
		getCamera().initializeModule();
		timeStart = System.currentTimeMillis();
		startStrengthLvl = getExperienceTracker().getGainedLevels(Skill.STRENGTH);
		startStrengthXp = getExperienceTracker().getGainedXP(Skill.STRENGTH);
		startAttackLvl = getExperienceTracker().getGainedLevels(Skill.ATTACK);
		startAttackXp = getExperienceTracker().getGainedXP(Skill.ATTACK);
		startHpLvl = getExperienceTracker().getGainedLevels(Skill.HITPOINTS);
		startHpXp = getExperienceTracker().getGainedXP(Skill.HITPOINTS);

		SimpleGui gui = new SimpleGui(this, this.fighter, this.bank, this.eater);
		log("Initialized gui");
		gui.Setup();
		log("Setup Gui");
		gui.Display();
	}

	@Override
	public int onLoop() throws InterruptedException {
		sleep(random(250,500));
		if(!threadHandler.isSettup()){
			threadHandler.settup();
		}
		if (!fighter.isFighting()) {
			if (fighter.attack()) {
				log("fighting.");
				sleep(100);
			} else {
				log("fighting False");
			}
		}
		if (getInventory().isEmpty()) {
			if (bank.bank()) {
				log("Banking Succesful");
			} else {
				log("Banking Failed");
			}
		}

		return 0;
	}

	@Override
	public void onMessage(Message message) throws InterruptedException {
		if (message.getMessage().contains("Oh dear")) {
			died();
		}
	}

	private void died() throws InterruptedException {
		Item[] inv = getInventory().getItems();
		for (Item i : inv) {
			if (i != null && i.hasAction("Wield")) {
				i.interact("Wield");
				sleep(random(250, 600));
			}
		}
		
		bank.bank();
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
				8, 65);
		g.drawString("fightering XP: " + (getExperienceTracker().getGainedXP(Skill.STRENGTH) - startStrengthXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.STRENGTH) - startStrengthLvl) + ")", 8, 80);
		g.drawString("fightering XP: " + (getExperienceTracker().getGainedXP(Skill.ATTACK) - startAttackXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.ATTACK) - startAttackLvl) + ")", 8, 80);
		g.drawString("fightering XP: " + (getExperienceTracker().getGainedXP(Skill.HITPOINTS) - startHpXp) + " ("
				+ (getExperienceTracker().getGainedLevels(Skill.HITPOINTS) - startHpLvl) + ")", 8, 80);

	}

	@Override
	public void onExit() {
		threadHandler.kill();
	}
}
