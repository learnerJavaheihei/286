package handler.dailymissions;

/**
 * @author Bonux
 **/
public class KillProcellaRaid extends DailyHunting {
	private final int[] MONSTER_IDS = {
			29107    // Прочелла
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
