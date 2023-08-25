package l2s.gameserver.templates.fakeplayer.actions;

import l2s.gameserver.ai.FakeAI;

/**
 * @author Bonux
 **/
public class StopFarmAction extends MoveAction {
	public StopFarmAction(double chance) {
		super(chance);
	}

	@Override
	public boolean performAction(FakeAI ai) {
		if (ai.clearCurrentFarmZone())
			return true;
		return false;
	}
}