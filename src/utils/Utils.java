package utils;

import java.awt.Rectangle;
import java.util.Random;

import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;

import main.ThreadHandler;
import overWatch.OverWatch;
import overWatch.OverWatch.mouseState;

public class Utils {
	ThreadHandler threadHandler;
	Script script;
	OverWatch overWatch;
	
	Random rn;
	boolean mouseOwned = false;
		
	public Utils(Script script, ThreadHandler threadHandler, OverWatch overWatch){
		this.script = script;
		this.threadHandler = threadHandler;
		this.overWatch = overWatch;
		rn = new Random(script.myPlayer().getId());
	}
	
	public boolean selectMenuOption(String action, String noun) throws InterruptedException
	{
		if (!script.getMenuAPI().isOpen()) return false;
		boolean isSuccessful = false;
		String[] nounArray = {noun,null};
		String[] actionArray = {action,null};
		int index = script.getMenuAPI().getMenuIndex(nounArray, actionArray);
		if(index == -1){
			return false;
		}
		Option target = script.getMenuAPI().getMenu().get(index);
		Rectangle optionRectangle = script.getMenuAPI().getOptionRectangle(index);
		if (target.name.contains(noun) && target.action.contains(action))
		{
			while (!threadHandler.ownMouse()) {
				Thread.sleep(rn.nextInt(100) + 100);
			}
			overWatch.setState(mouseState.UTILS);
			isSuccessful = script.getMouse().click(new RectangleDestination(script.getBot(), optionRectangle), false);
			threadHandler.releaseMouse();
		}
		return isSuccessful;
	}
}
