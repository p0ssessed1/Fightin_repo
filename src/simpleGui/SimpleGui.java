package simpleGui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import banking.Banking;
import eatingThread.Eater;
import fighting.Fighting;
import groundItemManager.GroundItemManager;

public class SimpleGui implements ActionListener {

	final Area DRAYNOR = new Area(new Position(3092, 3245, 0), new Position(3093, 3242, 0));
	final Area EAST_FALLY = new Area(new Position(3011, 3355, 0), new Position(3014, 3357, 0));
	final Area WEST_FALLY = new Area(new Position(2945, 3368, 0), new Position(2946, 3371, 0));
	final Area EAST_VARROCK = new Area(new Position(3251, 3240, 0), new Position(3254, 3442, 0));
	final Area WEST_VARROCK = new Area(new Position(3185, 3436, 0), new Position(3182, 3444, 0));
	final Area LUMBRIDGE = new Area(new Position(3208, 3220, 2), new Position(3209, 3218, 2));
	final Area EDGEVILLE = new Area(new Position(3094, 3489, 0), new Position(3092, 3495, 0));
	final Area[] BANKS = { DRAYNOR, EAST_FALLY, WEST_FALLY, EAST_VARROCK, WEST_VARROCK, LUMBRIDGE, EDGEVILLE };
	final List<String> BANK_NAMES = new LinkedList<String>(Arrays.asList("Draynor", "East Falador", "West Falador",
			"East Varrock", "West Varrock", "Lumbridge", "Edgeville"));

	Script script;
	boolean setUp = false;
	boolean start = false;
	Banking bank;
	Fighting fight;
	Eater eater;
	GroundItemManager itemManager;

	JFrame frame = new JFrame("Dynamic Fighter");
	NameFilter<Item> keepItems;
	List<JCheckBox> fightingOptions = new LinkedList<JCheckBox>();
	List<JRadioButton> banks = new LinkedList<JRadioButton>();
	List<JCheckBox> keep = new LinkedList<JCheckBox>();
	List<JRadioButton> food = new LinkedList<JRadioButton>();
	JTextField health = new JTextField();
	JTextField pickupItems = new JTextField();
	ButtonGroup bg_f = new ButtonGroup();
	ButtonGroup bg_b = new ButtonGroup();
	ActionFilter<NPC> f;
	/*
	 * TODO - implement different types of fight, since one action can mean
	 * multiple different fight types.
	 */
	JPanel panel = new JPanel(new GridLayout(0, 1));

	public SimpleGui(Script script, Fighting fighter, Banking bank, Eater eater, GroundItemManager itemManager) {
		this.script = script;
		this.fight = fighter;
		this.bank = bank;
		this.eater = eater;
		this.itemManager = itemManager;
	}

	/**
	 * Sets up a simple Gui with start button.
	 * 
	 * @return Void
	 */
	public void Setup() {
		List<NPC> monsters = script.getNpcs().getAll();
		List<String> monsterNames = new LinkedList<String>();
		JPanel fightingPanel = new JPanel(new GridLayout(0, 3));
		fightingPanel.add(new Label("Monster to fight:"));
		fightingPanel.add(new Label("   "));
		JPanel bankPanel = new JPanel(new GridLayout(0, 3));
		bankPanel.add(new Label("Banks: "));
		bankPanel.add(new Label("       "));
		JPanel foodPanel = new JPanel(new GridLayout(0, 2));
		foodPanel.add(new Label("Choose Food from inventory:"));
		JPanel optionPanel = new JPanel(new GridLayout(0, 2));
		optionPanel.add(new Label("Keep Items:"));
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
			fightingPanel.add(b);
		}

		for (String s : BANK_NAMES) {
			banks.add(new JRadioButton(s));
		}

		for (JRadioButton b : banks) {
			bg_b.add(b);
			bankPanel.add(b);
		}

		Item[] inv = script.getInventory().getItems();
		List<String> exclusiveInv = new LinkedList<String>();
		List<Item> exclusiveItemInv = new LinkedList<Item>();
		for (Item i : inv) {
			if (i != null) {
				if (!exclusiveInv.contains(i.getName())) {
					exclusiveInv.add(i.getName());
					exclusiveItemInv.add(i);
				}
			}
		}

		for (Item i : exclusiveItemInv) {
			if (i != null && i.hasAction("Eat")) {
				food.add(new JRadioButton(i.getName()));
			}
		}

		for (JRadioButton b : food) {
			bg_f.add(b);
			foodPanel.add(b);
		}

		for (String i : exclusiveInv) {
			if (i != null) {
				keep.add(new JCheckBox(i));
			}
		}

		for (JCheckBox b : keep) {
			optionPanel.add(b);
		}

		optionPanel.add(new Label("Health to Eat at:"));
		optionPanel.add(health);
		optionPanel.add(new Label("Items to pick up (seperate by ;):"));
		optionPanel.add(pickupItems);

		JButton start = new JButton("Start");
		start.addActionListener(this);
		panel.add(fightingPanel, BorderLayout.NORTH);
		panel.add(bankPanel);
		panel.add(optionPanel);
		panel.add(foodPanel);
		panel.add(start);
		frame.add(panel);
		frame.pack();
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
		List<String> to_keep = new LinkedList<String>();
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
				i++;
			}
		}
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
		}

		String selectedBank = null;
		for (JRadioButton b : banks) {
			if (b.isSelected()) {
				selectedBank = b.getText();
			}
		}
		String[] selectedMonsters = new String[fightingOptions.size()];
		int index = 0;
		for (JCheckBox f : fightingOptions) {
			if (f.isSelected()) {
				selectedMonsters[index] = f.getText();
				index++;
			}
		}
		if (selectedBank != null) {
			bank.setArea(BANKS[BANK_NAMES.indexOf(selectedBank)]);
		}
		try {
			if (health != null && Integer.valueOf(health.getText()) != null) {
				eater.setHealth(Integer.valueOf(health.getText()));
			}
		} catch (Exception e) {
			script.log("Setting health failed. Exception: " + e);
		}

		if (pickupItems.getText() != null) {
			StringTokenizer st = new StringTokenizer(pickupItems.getText(), ";,.");
			String[] items = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				items[i] = st.nextToken();
				i++;
			}
			itemManager.setItemFilter(items);
		}

		fight.setMonsters(selectedMonsters);
		script.log("Changing start.");
		start = true;
		// } catch (Exception e) {
		// script.log("Exception in actionPerformed." + e);
		// }
	}

}
