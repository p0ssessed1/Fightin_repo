package dynamicArea;

import org.osbot.rs07.api.map.Position;

public class OffsetPosition {
	public Position NpcPosition;
	public Position OffsetPosition;
	
	public OffsetPosition(Position npc, Position offset_of){
		this.NpcPosition = npc;
		this.OffsetPosition = offset_of;
	}
}
