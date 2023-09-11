package spawns;

import java.util.concurrent.Future;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.script.OnInitScriptListener;

/**
 * @author L2-scripts.com - (SanyaDC)
 */
public class TreasureChest implements OnInitScriptListener
{
	private static final long RESPAWN_DELAY = 3600000L; // Через каждые 30 минут меняем локацию. Проверить на оффе.
	private static final String[] SPAWN_GROUPS = {

			"treasure1",
			"treasure2",
			"treasure3",
			"treasureparty1",
			"treasureparty2",
			"treasureparty3",
		};

	private int _currentSpawnedGroup = -1;
	private Future<?> _respawnTask;

	@Override
	public void onInit()
	{
		_currentSpawnedGroup = Rnd.get(SPAWN_GROUPS.length);

		String groupName = SPAWN_GROUPS[_currentSpawnedGroup];
		SpawnManager.getInstance().spawn(groupName);

		_respawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RespawnTask(), RESPAWN_DELAY, RESPAWN_DELAY);
	}

	private void stopRespawnTask()
	{
		if(_respawnTask != null)
		{
			_respawnTask.cancel(false);
			_respawnTask = null;
		}
	}

	private class RespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			int newSpawnGroup = 0;
			if(SPAWN_GROUPS.length > 1) // Чтобы группы спавна не повторялись.
			{
				newSpawnGroup = Rnd.get(SPAWN_GROUPS.length);
				while(newSpawnGroup == _currentSpawnedGroup)
				{
					newSpawnGroup = Rnd.get(SPAWN_GROUPS.length);
				}
			}

			String groupName = SPAWN_GROUPS[_currentSpawnedGroup];
			SpawnManager.getInstance().despawn(groupName);


			groupName = SPAWN_GROUPS[newSpawnGroup];
			SpawnManager.getInstance().spawn(groupName);

			_currentSpawnedGroup = newSpawnGroup;
		}
	}
}