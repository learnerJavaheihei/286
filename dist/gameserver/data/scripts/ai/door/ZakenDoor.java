package ai.door;

import ai.Zaken;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.ai.DoorAI;
import l2s.gameserver.listener.game.OnGameHourChangeListener;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Bonux
**/
public class ZakenDoor extends DoorAI
{
	private static class DoorOpenController implements OnGameHourChangeListener
	{
		private HardReference<? extends Creature> _actRef;

		public DoorOpenController(DoorInstance actor)
		{
			_actRef = actor.getRef();
		}

		@Override
		public void onChangeHour(int hour, boolean onStart)
		{
			Creature creature = _actRef.get();
			if(creature == null || !creature.isDoor())
				return;

			DoorInstance door = (DoorInstance) creature;
			if(door == null)
				return;

			if(onStart)
			{
				if(hour >= 12 && hour < 1)
					door.openMe();
				else if(canClose())
					door.closeMe();
			}
			else
			{
				if(hour == 0)
					door.openMe();
				else if(hour == 1 && canClose())
					door.closeMe();
			}
		}

		private boolean canClose()
		{
			for(NpcInstance zaken : ReflectionManager.MAIN.getNpcs(true, ZAKEN_NPC_ID))
			{
				if(zaken.getAI() instanceof Zaken)
				{
					if(((Zaken) zaken.getAI()).isTeleportedToShip())
						return false;
				}
			}
			return true;
		}
	}

	private static final int ZAKEN_NPC_ID = 29022;

	public ZakenDoor(final DoorInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new DoorOpenController(actor));
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}
