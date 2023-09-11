package handler.dailymissions;

/**
 * @author Bonux
 **/
public class KillIgnisRaid extends DailyHunting {
	private final int[] MONSTER_IDS = {
			29105    // Игнис
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
