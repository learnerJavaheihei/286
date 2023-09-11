package handler.dailymissions;

/**
 * @author Bonux
 **/
public class KillNebulaRaid extends DailyHunting {
	private final int[] MONSTER_IDS = {
			29106    // Небула
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}

	@Override
	public boolean isReusable()
	{
		return false;
	}
}
