package simpleGui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import banking.Banking;
import eater.Eater;
import fighting.Fighting;
import itemManager.ItemManager;

public class SimpleGui implements ActionListener {

	final Area DRAYNOR = new Area(new Position(3092, 3245, 0), new Position(3093, 3242, 0));
	final Area EAST_FALLY = new Area(new Position(3011, 3355, 0), new Position(3014, 3357, 0));
	final Area WEST_FALLY = new Area(new Position(2945, 3368, 0), new Position(2946, 3371, 0));
	final Area EAST_VARROCK = new Area(new Position(3251, 3240, 0), new Position(3254, 3442, 0));
	final Area WEST_VARROCK = new Area(new Position(3185, 3436, 0), new Position(3182, 3444, 0));
	final Area LUMBRIDGE = new Area(new Position(3208, 3220, 2), new Position(3209, 3218, 2));
	final Area EDGEVILLE = new Area(new Position(3094, 3489, 0), new Position(3092, 3495, 0));
	final Area ALKHARID = new Area(new Position(3269,3169,0), new Position(3270,3164,0));
	final Area[] BANKS = { ALKHARID, EDGEVILLE, DRAYNOR, EAST_FALLY, WEST_FALLY, LUMBRIDGE, EAST_VARROCK, WEST_VARROCK };
	final List<String> BANK_NAMES = new LinkedList<String>(Arrays.asList("Al-Kharid", "Edgeville", "Draynor", "East Falador",
			"West Falador", "Lumbridge", "East Varrock", "West Varrock"));

	Script script;
	boolean setUp = false;
	boolean start = false;
	Banking bank;
	Fighting fight;
	Eater eater;
	ItemManager itemManager;

	JFrame frame = new JFrame("Dynamic Fighter");
	NameFilter<Item> keepItems;
	List<JCheckBox> fightingOptions = new LinkedList<JCheckBox>();
	List<JRadioButton> banks = new LinkedList<JRadioButton>();
	List<JCheckBox> keep = new LinkedList<JCheckBox>();
	List<JRadioButton> food = new LinkedList<JRadioButton>();
	JTextField health = new JTextField();
	JTextField pickupItems = new JTextField();
	JTextField withdrawAmount = new JTextField();
	JTextField arrows = new JTextField();
	JCheckBox prioritizeItems = new JCheckBox();
	JCheckBox boneBurying = new JCheckBox();
	JCheckBox range = new JCheckBox();
	JCheckBox previousSettings = new JCheckBox();
	ButtonGroup bg_f = new ButtonGroup();
	ButtonGroup bg_b = new ButtonGroup();
	ActionFilter<NPC> f;
	String path;
	String filepath;
	JTabbedPane tabbedPanel = new JTabbedPane();

	private static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public SimpleGui(Script script, Fighting fighter, Banking bank, Eater eater, ItemManager itemManager) {
		this.script = script;
		this.fight = fighter;
		this.bank = bank;
		this.eater = eater;
		this.itemManager = itemManager;
		path = System.getProperty("user.dir") + "\\OSBotLogs\\";
	}

	private JComponent fightingSetup(){
		List<NPC> monsters = script.getNpcs().getAll();
		List<String> monsterNames = new LinkedList<String>();
		JComponent fightingPanel = new JPanel(new GridLayout(0, 1));
		JPanel monsterPanel = new JPanel(new GridLayout(0,2));
		JPanel optionPanel = new JPanel(new GridLayout(0,2));
		monsterPanel.add(new Label("Monster to fight:"));
		for (NPC n : monsters) {
			if (n != null && n.hasAction("Attack") && n.exists() && script.map.canReach(n)) {
				script.log("Monster located.");
				int id = n.getId();
				if (id != -1) {
					for (NPC i : script.getNpcs().get(n.getX(), n.getY())) {
						if (i.getId() == id) {
							if (!monsterNames.contains(n.getName())) {
								script.log("Monster added.");
								monsterNames.add(n.getName());
								fightingOptions.add(new JCheckBox(n.getName()));
							}
						}
					}
				}
			}
		}

		for (JCheckBox b : fightingOptions) {
			monsterPanel.add(b);
		}
		fightingPanel.add(monsterPanel);
		optionPanel.add(new JSeparator());
		optionPanel.add(new JSeparator());
		range.setText("Ranging mode.");
		optionPanel.add(range);
		arrows.setText("arrow");
		optionPanel.add(arrows);
		health.setText("Health to eat at");
		optionPanel.add(health);
		fightingPanel.add(optionPanel);
		return fightingPanel;
	}
	
	private JComponent bankSetup(){
		JComponent bankPanel = new JPanel(new GridLayout(0, 1));
		JPanel banksPanel = new JPanel(new GridLayout(0, 2));
		JPanel keepPanel = new JPanel(new GridLayout(0, 2));
		JPanel foodPanel = new JPanel(new GridLayout(0, 2));
		Item[] inv = script.getInventory().getItems();
		List<String> exclusiveInv = new LinkedList<String>();
		List<Item> exclusiveItemInv = new LinkedList<Item>();

		banksPanel.add(new Label("Banks: "));
		for (String s : BANK_NAMES) {
			banks.add(new JRadioButton(s));
		}

		for (JRadioButton b : banks) {
			bg_b.add(b);
			banksPanel.add(b);
		}

		for (Item i : inv) {
			if (i != null) {
				if (!exclusiveInv.contains(i.getName())) {
					exclusiveInv.add(i.getName());
					exclusiveItemInv.add(i);
				}
			}
		}

		for (String i : exclusiveInv) {
			if (i != null) {
				keep.add(new JCheckBox(i));
			}
		}

		keepPanel.add(new JSeparator());
		keepPanel.add(new JSeparator());
		keepPanel.add(new Label("Keep Items:"));
		for (JCheckBox b : keep) {
			keepPanel.add(b);
		}

		for (Item i : exclusiveItemInv) {
			if (i != null && i.hasAction("Eat")) {
				food.add(new JRadioButton(i.getName()));
			}
		}

		foodPanel.add(new JSeparator());
		foodPanel.add(new JSeparator());
		foodPanel.add(new Label("Choose Food from inventory:"));
		for (JRadioButton b : food) {
			bg_f.add(b);
			foodPanel.add(b);
		}

		withdrawAmount.setText("Food withdraw amount");
		foodPanel.add(withdrawAmount);
		
		bankPanel.add(banksPanel);
		bankPanel.add(keepPanel);
		bankPanel.add(foodPanel);

		return bankPanel;
	}
	
	private JComponent optionSetup(){
		JComponent optionPanel = new JPanel();

		boneBurying.setText("Bury Bones?");
		optionPanel.add(boneBurying);
		prioritizeItems.setText("Always pick up");
		optionPanel.add(prioritizeItems);
		pickupItems.setText("Items to pick up (seperate by ;)");
		optionPanel.add(pickupItems);

		return optionPanel;
	}
	
	/**
	 * Sets up a simple Gui with start button.
	 * 
	 * @return Void
	 */
	public void Setup() {
		JMenuBar mb = new JMenuBar();
		JComponent fightingPanel = fightingSetup();
		JComponent bankPanel = bankSetup();
		JComponent optionPanel = optionSetup();

		JButton start = new JButton("Start");
		start.addActionListener(this);
		tabbedPanel.addTab("Fighting Options", null, fightingPanel, "Options for fighting.");
		tabbedPanel.addTab("Banking Options", null, bankPanel, "Options for banking.");
		tabbedPanel.addTab("Options", null, optionPanel, "Miscellanious options");
		previousSettings.setText("Use previous settings?");
		mb.add(previousSettings, BorderLayout.WEST);
		mb.add(start, BorderLayout.EAST);
		frame.setJMenuBar(mb);
		frame.add(tabbedPanel);
		frame.pack();

		filepath = path + "Settings\\";
		File dir = new File(filepath);
		dir.mkdirs();
		filepath = filepath + "FighterSettings.txt";

		setUp = true;
	}

	/**
	 * Displays gui set up previously
	 * 
	 * @return True if display successful\n False if not set up.
	 * @throws InterruptedException
	 */
	public boolean Display() throws InterruptedException {
		if (setUp) {
			frame.setVisible(true);
			while (!start) {
				Script.sleep(100);
			}
			frame.dispose();
		}
		return setUp;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		/**
		 * Write to file with an option per line, with semicolons between each
		 * item on the line. Order of items in file: 1. Items to keep. 2. Chosen
		 * Food. 3. Bank name. 4. Monster names. 5. Health to eat at. 6.
		 * Withdraw amount. 7. Items to pick up. 8. Priority pick up. 9. TODO -
		 * Bone burying.
		 */
		List<String> to_keep = new LinkedList<String>();
		String[] selectedMonsters = new String[fightingOptions.size()];
		BufferedWriter writer = null;
		String arrowName = null;
		if (previousSettings.isSelected()) {
			try {
				readFile();
			} catch (Exception e) {
				e.printStackTrace();
				script.stop();
			}
			start = true;
			return;
		} else {
			try {
				PrintWriter tempPrintWriter = new PrintWriter(filepath);
				tempPrintWriter.print("");
				tempPrintWriter.close();
				writer = new BufferedWriter(new FileWriter(filepath, true));
			} catch (IOException e) {
				e.printStackTrace();
				script.stop();
			}
		}
		String[] keeping = null;
		String foodChosen = null;
		JCheckBox local;
		for (int i = 0; i < keep.size(); i++) {
			local = keep.get(i);
			if (local.isSelected()) {
				to_keep.add(local.getText());
			}
		}
		if (!to_keep.isEmpty()) {
			keeping = new String[to_keep.size()];
			int i = 0;
			for (String s : to_keep) {
				keeping[i] = s;
				writeToFile(s, writer);
				i++;
			}
		} else {
			writeToFile("None", writer);
		}
		nextSetting(writer);

		if (keeping != null) {
			keepItems = new NameFilter<Item>(keeping);
			bank.setKeepItems(keepItems);
		}

		for (JRadioButton b : food) {
			if (b.isSelected()) {
				foodChosen = b.getText();
			}
		}
		if (foodChosen != null) {
			bank.setFood(foodChosen);
			fight.setFood(foodChosen);
			writeToFile(foodChosen, writer);
		} else {
			writeToFile("None", writer);
		}
		nextSetting(writer);

		String selectedBank = null;
		for (JRadioButton b : banks) {
			if (b.isSelected()) {
				selectedBank = b.getText();
				writeToFile(b.getText(), writer);
			}
		}
		if (selectedBank != null) {
			bank.setArea(BANKS[BANK_NAMES.indexOf(selectedBank)]);
		} else {
			writeToFile("None", writer);
		}
		nextSetting(writer);

		int index = 0;
		for (JCheckBox f : fightingOptions) {
			if (f.isSelected()) {
				selectedMonsters[index] = f.getText();
				writeToFile(f.getText(), writer);
				index++;
			}
		}

		if (index != 0) {
			fight.setMonsters(selectedMonsters);
		} else {
			writeToFile("None", writer);
		}
		nextSetting(writer);

		try {
			if (health != null && isNumeric(health.getText())) {
				eater.setHealth(Integer.valueOf(health.getText()));
				writeToFile(health.getText(), writer);
			} else {
				writeToFile("None", writer);
			}
			nextSetting(writer);

			if (withdrawAmount != null && isNumeric(withdrawAmount.getText())) {
				bank.setFoodAmount(Integer.valueOf(withdrawAmount.getText()));
				writeToFile(withdrawAmount.getText(), writer);
			} else {
				writeToFile("None", writer);
			}
			nextSetting(writer);

		} catch (Exception e) {
			script.log("Setting health failed. Exception: " + e);
			e.printStackTrace();
			script.stop();
		}
		if (arrows.getText() != null && range.isSelected()) {
			script.log("Setting Arrows: " + arrows.getText());
			arrowName = arrows.getText();
			itemManager.setArrows(arrowName);
			writeToFile(arrowName, writer);
		}
		if (pickupItems.getText() != null) {
			StringTokenizer st = new StringTokenizer(pickupItems.getText(), ";,.");
			String[] items = new String[st.countTokens() + (range.isSelected() ? 1 : 0)];
			int i = 0;
			while (st.hasMoreTokens()) {
				items[i] = st.nextToken();
				writeToFile(items[i], writer);
				i++;
			}
			if (range.isSelected() && arrowName != null) {
				items[i] = arrowName;
				i++;
			}
			if (i != 0) {
				itemManager.setItemFilter(items);
			} else {
				writeToFile("None", writer);
			}
		}
		nextSetting(writer);

		if (prioritizeItems.isSelected()) {
			itemManager.setPriorityPickup();
			writeToFile("True", writer);
		} else {
			writeToFile("False", writer);
		}
		nextSetting(writer);

		if (boneBurying.isSelected()) {
			itemManager.setBurying(true);
			writeToFile("True", writer);
		} else {
			writeToFile("False", writer);
		}
		nextSetting(writer);

		if (arrowName != null) {
			writeToFile(arrowName, writer);
		} else {
			writeToFile("None", writer);
		}
		nextSetting(writer);

		if (range.isSelected()) {
			itemManager.setRange(true);
			writeToFile("True", writer);
		} else {
			writeToFile("False", writer);
		}
		nextSetting(writer);

		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		script.log("Starting...");
		start = true;
	}

	private void writeToFile(String s, BufferedWriter writer) {
		try {
			if (s != null && writer != null) {
				writer.write(s + ";");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void nextSetting(BufferedWriter writer) {
		try {
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readFile() throws IOException {
		/**
		 * Write to file with an option per line, with semicolons between each
		 * item on the line. Order of items in file: 1. Items to keep. 2. Chosen
		 * Food. 3. Bank name. 4. Monster names. 5. Health to eat at. 6.
		 * Withdraw amount. 7. Items to pick up. 8. Priority pick up. 9. TODO -
		 * Bone burying.
		 */
		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		String line;
		int lineNo = 0;
		while ((line = reader.readLine()) != null) {
			int i = 0;
			StringTokenizer st = new StringTokenizer(line, ";");
			String[] localStrings = new String[st.countTokens()];
			while (st.hasMoreTokens()) {
				localStrings[i] = st.nextToken();
				i++;
			}

			switch (lineNo) {
			case 0:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting Keep Items...");
					keepItems = new NameFilter<Item>(localStrings);
					bank.setKeepItems(keepItems);
				}
				break;
			case 1:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting Food: " + localStrings[0]);
					bank.setFood(localStrings[0]);
					fight.setFood(localStrings[0]);
				}
				break;
			case 2:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting Bank: " + localStrings[0]);
					bank.setArea(BANKS[BANK_NAMES.indexOf(localStrings[0])]);
				}
				break;
			case 3:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting monsters...");
					fight.setMonsters(localStrings);
				}
				break;
			case 4:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting health: " + localStrings[0]);
					eater.setHealth(Integer.valueOf(localStrings[0]));
				}
				break;
			case 5:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting Food amount: " + localStrings[0]);
					bank.setFoodAmount(Integer.valueOf(localStrings[0]));
				}
				break;
			case 6:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting items to pickup...");
					itemManager.setItemFilter(localStrings);
				}
				break;
			case 7:
				if ((i != 0) && localStrings[0].contains("T")) {
					script.log("Setting priority pickup: " + localStrings[0]);
					itemManager.setPriorityPickup();
				}
				break;
			case 8:
				if ((i != 0) && localStrings[0].contains("T")) {
					script.log("Setting Bone Burying: " + localStrings[0]);
					itemManager.setBurying(true);
				}
				break;
			case 9:
				if ((i != 0) && !localStrings[0].contains("None")) {
					script.log("Setting arrow: " + localStrings[0]);
					itemManager.setArrows(localStrings[0]);
				}
				break;
			case 10:
				if ((i != 0) && localStrings[0].contains("T")) {
					script.log("Setting Ranging: " + localStrings[0]);
					itemManager.setRange(true);
				}
				break;
			default:
				break;
			}
			lineNo++;
		}

		reader.close();
	}

}
