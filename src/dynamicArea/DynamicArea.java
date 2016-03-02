package dynamicArea;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import fighting.Fighting;

public class DynamicArea {
	final int MAX_AREA_SIZE = 3;
	Area overallArea;
	int overallRadius;
	Random rn;

	Fighting fighter;

	public DynamicArea(Fighting fighter) {
		this.fighter = fighter;
		overallRadius = Script.random(8, 12);
		rn = new Random(fighter.script.myPlayer().getId());
	}

	public List<Area> CreateAreas(List<NPC> spots) {
		List<Area> areas = new LinkedList<Area>();
		overallArea = fighter.script.myPlayer().getArea(overallRadius);
		areas.add(fighter.script.myPlayer().getArea(MAX_AREA_SIZE));
		return areas;
	}
	
	public Area getOverallArea(){
		return this.overallArea;
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

	public boolean addExclusiveAreas(List<NPC> spots, List<Area> currentAreas, Filter<NPC> npcFilter) {
		if(currentAreas.size() > 120){
			return false;
		}
		List<Area> areas = new LinkedList<Area>();
		areas = currentAreas;
		int count = 0;
		boolean added = false;
		for (int i = 0; i < spots.size(); i++) {
			if (fighter.isNpcValid(spots.get(i))) {
				if (overallArea.contains(spots.get(i)) && npcFilter.match(spots.get(i))) {
					areas.add(spots.get(i).getArea(MAX_AREA_SIZE));
					added = true;
					count++;
				}
			}
		}
		if(added){
			fighter.script.log("Added " + count + " areas");
		}

		return added;
	}
	
	public Area getRandomArea(List<Area> areas){
		Area area = areas.get(rn.nextInt(areas.size()));
		return area;
	}
}
