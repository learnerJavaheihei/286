package l2s.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;

/**
 * @author NB4L1
 */
public final class MovementController
{
	private static final MovementController _instance = new MovementController();

	public static MovementController getInstance()
	{
		return _instance;
	}

	private final Set<Creature> _movingCreatures = ConcurrentHashMap.newKeySet();
	
	protected MovementController()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::run, 100, 100, TimeUnit.MILLISECONDS);
	}

	public void registerMovement(final Creature creature)
	{
		_movingCreatures.add(creature);
	}

	public void removeMovement(final Creature creature)
	{
		_movingCreatures.remove(creature);
	}

	public boolean isMoving(final Creature creature)
	{
		return _movingCreatures.contains(creature);
	}

	public void run()
	{
		_movingCreatures.removeIf(creature -> creature.getMovement().updatePosition());
	}
}
