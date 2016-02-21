package dynamicArea;

import java.util.LinkedList;
import java.util.List;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import fighting.Fighting;

public class DynamicArea {
	final int MAX_AREA_SIZE = 3;
	Area overallArea;
	int overallRadius;

	Fighting fighter;

	public DynamicArea(Fighting fighter) {
		this.fighter = fighter;
		overallRadius = Script.random(12, 15);
	}

	public List<Area> CreateAreas(List<NPC> spots) {
		List<Area> areas = new LinkedList<Area>();
		overallArea = fighter.script.myPlayer().getArea(overallRadius);
		areas.add(fighter.script.myPlayer().getArea(MAX_AREA_SIZE));
		return areas;
	}

	/**
	 * Get the closest area in which to fighter that does not contain the
	 * player.
	 * 
	 * @return Area: The area in which to fighter.
	 */
	public Area getClosestArea(List<Area> areas) {
		Position myPosition = fighter.script.myPlayer().getPosition();
		Position tryPosition = areas.get(0).getRandomPosition();
		Area closestArea = areas.get(0);

		for (Area a : areas) {
			if (myPosition.distance(a.getRandomPosition()) < myPosition.distance(tryPosition)
					&& !a.contains(fighter.script.myPlayer())) {
				tryPosition = a.getRandomPosition();
				closestArea = a;
			} else if (a.contains(fighter.script.myPlayer())) {
				return a;
			}
		}
		return closestArea;
	}

	public boolean addExclusiveAreas(List<NPC> spots, List<Area> currentAreas) {
		if(currentAreas.size() > 100){
			return false;
		}
		List<Area> areas = new LinkedList<Area>();
		areas = currentAreas;
		boolean added = false;
		for (int i = 0; i < spots.size(); i++) {
			if (fighter.isNpcValid(spots.get(i))) {
				if (overallArea.contains(spots.get(i))) {
					fighter.script.log("Added area: " + spots.get(i).getArea(MAX_AREA_SIZE));
					areas.add(spots.get(i).getArea(MAX_AREA_SIZE));
				}
				added = true;
			}
		}

		return added;
	}
}
