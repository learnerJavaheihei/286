package ai;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.PositionUtils;

/**
 * @author Bonux
**/
public class Zaken extends Fighter
{
	// Skill's
	private static final SkillEntry S_ZAKEN_TRANS_NIGHT2DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4223, 1);
	private static final SkillEntry S_ZAKEN_TRANS_DAY2NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4224, 1);

	private static final SkillEntry S_ZAKEN_REGEN1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4227, 1);
	private static final SkillEntry S_ZAKEN_REGEN2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4242, 1);

	private static final SkillEntry S_ZAKEN_RANGE_DRAIN_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50006, 1);
	private static final SkillEntry S_ZAKEN_RANGE_DRAIN_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50006, 2);
	private static final SkillEntry S_ZAKEN_RANGE_DUAL_ATTACK_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50007, 1);
	private static final SkillEntry S_ZAKEN_RANGE_DUAL_ATTACK_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50007, 2);
	private static final SkillEntry S_ZAKEN_KNOCKDOWN_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50008, 1);
	private static final SkillEntry S_ZAKEN_KNOCKDOWN_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50008, 2);

	//private static final SkillEntry S_ANTI_STRIDER_SLOW = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4258, 1);

	// NPC's
	private static final int SEER_NPC_ID = 24032;	// Пророк
	private static final int GUARDIAN_SPIRIT_NPC_ID = 24033;	// Дух Стражника
	private static final int MIDNIGHT_SAIRON_NPC_ID = 24034;	// Ночная Сайрона
	private static final int DAYMEN_NPC_ID = 24035;	// Даймон
	private static final int TELEPORTER_NPC_ID = 34016;	// Телепорт

	private static final int TELEPORTER_DESPAWN_TIME = 15 * 60 * 1000; // 15 min

	private static final int[][] COORDS = new int[][]{
		{ 42280, 206008, -3752 },
		{ 43784, 210328, -3712 },
		{ 46424, 205640, -3640 },
		{ 42280, 206008, -3752 },
		{ 43784, 210328, -3712 },
		{ 46424, 205640, -3640 },
		{ 42280, 206008, -3752 },
		{ 43784, 210328, -3712 },
		{ 46424, 205640, -3640 },
		{ 42280, 206008, -3752 },
		{ 43784, 210328, -3712 },
		{ 46424, 205640, -3640 },
		{ 42280, 206008, -3752 },
		{ 43784, 210328, -3712 },
		{ 46424, 205640, -3640 }
		/* { 53950, 219860, -3488 },
		{ 55980, 219820, -3488 },
		{ 54950, 218790, -3488 },
		{ 55970, 217770, -3488 },
		{ 53930, 217760, -3488 },
		{ 55970, 217770, -3216 },
		{ 55980, 219920, -3216 },
		{ 54960, 218790, -3216 },
		{ 53950, 219860, -3216 },
		{ 53930, 217760, -3216 },
		{ 55970, 217770, -2944 },
		{ 55980, 219920, -2944 },
		{ 54960, 218790, -2944 },
		{ 53950, 219860, -2944 },
		{ 53930, 217760, -2944 } */
	};

	private static final Location ZAKEN_SHIP_COORD = new Location(52211, 217238, -3359);

	private static final int ZAKEN_DOOR_ID = 21240006;

	private static final int TELEPORT_TO_SHIP_CHANCE = 80;

	private int _teleportX, _teleportY, _teleportZ;
	private boolean _teleportedToPrivates = false;
	private boolean _teleportedToShip = false;
	private boolean _canTeleportedToShip = false;
	private int _teleportCheckHpModifier = 0;

	private int _rememberedTargetsCount = 0;
	private Creature _rememberTarget1 = null;
	private Creature _rememberTarget2 = null;
	private Creature _rememberTarget3 = null;
	private Creature _rememberTarget4 = null;
	private Creature _rememberTarget5 = null;

	private int _spawnStage = 0;

	private Creature _mostHatedTarget = null;
	private int _mostHatedTargetCheckCount = 0;

	public Zaken(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtNoDesire()
	{
		addMoveAroundDesire(5, 5);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();

		effectMusic("BS01_A", 10000);

		_teleportedToPrivates = false;
		_teleportedToShip = false;
		_canTeleportedToShip = Rnd.chance(TELEPORT_TO_SHIP_CHANCE);
		_teleportX = actor.getX();
		_teleportY = actor.getY();
		_teleportZ = actor.getZ();
		_rememberedTargetsCount = 0;
		// TODO: if(reply == 0)
		// reply == 0 - respawned after death
		// reply == 1 - spawned after server restart
		//{
			_teleportCheckHpModifier = 3;
		//}
		_mostHatedTargetCheckCount = 0;
		/*if(actor.inMyTerritory(actor)) На классике миньоны спавнятся не при спавне закена, а когда???
		{
			_spawnStage = 1;
			addTimer(1003, 1700);
		}*/
		addTimer(1001, 1000);
	}

	private boolean isNight()
	{
		return getActor().getAbnormalList().getAbnormalLevel(S_ZAKEN_TRANS_DAY2NIGHT.getTemplate().getAbnormalType()) == 1;
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		//super.onEvtTimer(timerId, arg1, arg2);

		if(timerId == 1001)
		{
			if(GameTimeController.getInstance().getGameHour() < 5)
			{
				if(getActor().getAbnormalList().getAbnormalLevel(S_ZAKEN_TRANS_DAY2NIGHT.getTemplate().getAbnormalType()) == -1)
				{
					addUseSkillDesire(getActor(), S_ZAKEN_TRANS_DAY2NIGHT, 1, 1, 10000000);
					_teleportX = getActor().getX();
					_teleportY = getActor().getY();
					_teleportZ = getActor().getZ();
				}
				if(getActor().getAbnormalList().getAbnormalLevel(S_ZAKEN_REGEN1.getTemplate().getAbnormalType()) == -1)
				{
					addUseSkillDesire(getActor(), S_ZAKEN_REGEN1, 1, 1, 10000000);
				}
				if(!getActor().getMovement().isMoving() && !_teleportedToPrivates)
				{
					boolean teleport = true;
					boolean temp = false;

					Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
					if(topDesireTarget != null)
					{
						if(topDesireTarget.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;
					}

					if(!temp)
						teleport = false;

					if(_rememberedTargetsCount > 0)
					{
						if(_rememberTarget1.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;

						if(!temp)
							teleport = false;
					}
					if(_rememberedTargetsCount > 1)
					{
						if(_rememberTarget2.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;

						if(!temp)
							teleport = false;
					}
					if(_rememberedTargetsCount > 2)
					{
						if(_rememberTarget3.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;

						if(!temp)
							teleport = false;
					}
					if(_rememberedTargetsCount > 3)
					{
						if(_rememberTarget4.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;

						if(!temp)
							teleport = false;
					}
					if(_rememberedTargetsCount > 4)
					{
						if(_rememberTarget5.getDistance(_teleportX, _teleportY) > 1500)
							temp = true;
						else
							temp = false;

						if(!temp)
							teleport = false;
					}

					if(teleport)
					{
						_rememberedTargetsCount = 0;
						int loc[] = Rnd.get(COORDS);
						_teleportX = loc[0] + Rnd.get(10);//原來650
						_teleportY = loc[1] + Rnd.get(10);
						_teleportZ = loc[2];
						showMessage(NpcString.HA_HA_HA_TRY_AND_FIND_ME, 10000);
						getActor().setSpawnedLoc(new Location(_teleportX, _teleportY, _teleportZ));
						getActor().teleToLocation(_teleportX, _teleportY, _teleportZ);
						getActor().getAggroList().clear(true);
					}
				}
				if(Rnd.get(20) < 1 && !_teleportedToPrivates)
				{
					_teleportX = getActor().getX();
					_teleportY = getActor().getY();
					_teleportZ = getActor().getZ();
				}
				if(!getActor().getMovement().isMoving() && _mostHatedTargetCheckCount == 0)
				{
					Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
					if(topDesireTarget != null)
					{
						_mostHatedTarget = topDesireTarget;
						_mostHatedTargetCheckCount = 1;
					}
				}
				else if(!getActor().getMovement().isMoving() && _mostHatedTargetCheckCount != 0)
				{
					Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
					if(topDesireTarget != null)
					{
						if(_mostHatedTarget == topDesireTarget)
						{
							_mostHatedTargetCheckCount++;
						}
						else
						{
							_mostHatedTargetCheckCount = 1;
							_mostHatedTarget = topDesireTarget;
						}
					}
				}

				if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					_mostHatedTargetCheckCount = 0;

				if(_mostHatedTargetCheckCount > 5)
				{
					getActor().getAggroList().remove(_mostHatedTarget, true);
					_mostHatedTargetCheckCount = 0;
				}
			}
			else
			{
				if(getActor().getAbnormalList().getAbnormalLevel(S_ZAKEN_TRANS_DAY2NIGHT.getTemplate().getAbnormalType()) == 1)
				{
					addUseSkillDesire(getActor(), S_ZAKEN_TRANS_NIGHT2DAY, 1, 1, 10000000);
					_teleportCheckHpModifier = 3;
				}
				if(getActor().getAbnormalList().getAbnormalLevel(S_ZAKEN_REGEN1.getTemplate().getAbnormalType()) == 11)
				{
					addUseSkillDesire(getActor(), S_ZAKEN_REGEN2, 1, 1, 10000000);
				}
				if(Rnd.get(40) < 1)
				{
					int loc[] = Rnd.get(COORDS);
					_teleportX = loc[0] + Rnd.get(10);//原來650
					_teleportY = loc[1] + Rnd.get(10);
					_teleportZ = loc[2];
					showMessage(NpcString.HA_HA_HA_TRY_AND_FIND_ME, 10000);
					getActor().setSpawnedLoc(new Location(_teleportX, _teleportY, _teleportZ));
					getActor().teleToLocation(_teleportX, _teleportY, _teleportZ);
					getActor().getAggroList().clear(true);
				}
			}
			addTimer(1001, (30 * 1000));
		}
		else if(timerId == 1002)
		{
			_rememberedTargetsCount = 0;
			showMessage(NpcString.HA_HA_HA_TRY_AND_FIND_ME, 10000);
			getActor().setSpawnedLoc(new Location(_teleportX, _teleportY, _teleportZ));
			getActor().teleToLocation(_teleportX, _teleportY, _teleportZ);
			getActor().getAggroList().clear(true);
			_teleportedToPrivates = false;
		}
		else if(timerId == 1003)
		{
			if(_spawnStage == 1)
			{
				for(int i = 0; i < 15; i++)
				{
					for(int c = 0; c < 1; c++)
					{
						int loc[] = COORDS[i];
						int x = loc[0] + Rnd.get(10);//原來650
						int y = loc[1] + Rnd.get(10);
						int z = loc[2];
						createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, x, y, z, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
					}
				}
				_spawnStage = 2;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 2)
			{
				for(int i = 0; i < 15; i++)
				{
					for(int c = 0; c < 1; c++)
					{
						int loc[] = COORDS[i];
						int x = loc[0] + Rnd.get(10);//原來650
						int y = loc[1] + Rnd.get(10);
						int z = loc[2];
						createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, x, y, z, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
					}
				}
				_spawnStage = 3;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 3)
			{
				for(int i = 0; i < 15; i++)
				{
					for(int c = 0; c < 2; c++)
					{
						int loc[] = COORDS[i];
						int x = loc[0] + Rnd.get(10);//原來650
						int y = loc[1] + Rnd.get(10);
						int z = loc[2];
						createOnePrivateEx(SEER_NPC_ID, null, 0, 0, x, y, z, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
					}
				}
				_spawnStage = 4;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 4)
			{
				for(int i = 0; i < 15; i++)
				{
					for(int c = 0; c < 5; c++)
					{
						int loc[] = COORDS[i];
						int x = loc[0] + Rnd.get(10);//原來650
						int y = loc[1] + Rnd.get(10);
						int z = loc[2];
						createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, x, y, z, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
					}
				}
				_spawnStage = 5;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 5)
			{
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 52675, 219371, -3290, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 52687, 219596, -3368, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 52672, 219740, -3418, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 52857, 219992, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 52959, 219997, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 53381, 220151, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54236, 220948, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54885, 220144, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55264, 219860, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55399, 220263, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55679, 220129, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 56276, 220783, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 57173, 220234, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 56267, 218826, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56294, 219482, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 56094, 219113, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56364, 218967, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 57113, 218079, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56186, 217153, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55440, 218081, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55202, 217940, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55225, 218236, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54973, 218075, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 53412, 218077, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54226, 218797, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54394, 219067, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54139, 219253, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 54262, 219480, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				_spawnStage = 6;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 6)
			{
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 53412, 218077, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54413, 217132, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 54841, 217132, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 55372, 217128, -3343, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 55893, 217122, -3488, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 56282, 217237, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 56963, 218080, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 56267, 218826, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56294, 219482, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 56094, 219113, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56364, 218967, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 56276, 220783, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 57173, 220234, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54885, 220144, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55264, 219860, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55399, 220263, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55679, 220129, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54236, 220948, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54464, 219095, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54226, 218797, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54394, 219067, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54139, 219253, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 54262, 219480, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 53412, 218077, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55440, 218081, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55202, 217940, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55225, 218236, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54973, 218075, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				_spawnStage = 7;
				addTimer(1003, 1700);
			}
			else if(_spawnStage == 7)
			{
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54228, 217504, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54181, 217168, -3216, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 54714, 217123, -3168, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 55298, 217127, -3073, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 55787, 217130, -2993, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 56284, 217216, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 56963, 218080, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 56267, 218826, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56294, 219482, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 56094, 219113, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 56364, 218967, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 56276, 220783, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 57173, 220234, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54885, 220144, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55264, 219860, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55399, 220263, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55679, 220129, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54236, 220948, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54464, 219095, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54226, 218797, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(SEER_NPC_ID, null, 0, 0, 54394, 219067, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54139, 219253, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(MIDNIGHT_SAIRON_NPC_ID, null, 0, 0, 54262, 219480, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 53412, 218077, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 54280, 217200, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55440, 218081, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(GUARDIAN_SPIRIT_NPC_ID, null, 0, 0, 55202, 217940, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 55225, 218236, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				createOnePrivateEx(DAYMEN_NPC_ID, null, 0, 0, 54973, 218075, -2944, PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
				_spawnStage = 8;
			}
		}
	}

	@Override
	protected void onEvtPartyAttacked(NpcInstance minion, Creature attacker, int damage)
	{
		//super.onEvtPartyAttacked(minion, attacker, damage);

		if(GameTimeController.getInstance().getGameHour() < 5)
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && !_teleportedToPrivates && damage < 10 && Rnd.get(30 * 15) < 1)
			{
				_teleportedToPrivates = true;
				_teleportX = minion.getX();
				_teleportY = minion.getY();
				_teleportZ = minion.getZ();
				addTimer(1002, 300);
			}
		}
	}

	@Override
	protected void onEvtPartyDied(NpcInstance minion, Creature killer)
	{
		//super.onEvtPartyDied(minion, killer);

		if(minion != getActor())
		{
			createOnePrivateEx(minion.getNpcId(), minion.getParameter("ai_type", null), 0, 30 + Rnd.get(60), minion.getX(), minion.getY(), minion.getZ(), PositionUtils.convertDegreeToClientHeading(Rnd.get(360)), 0, 0, 0);
		}
	}
	/*EventHandler PARTY_DIED(private)
	{
		if( private != myself.sm )
		{
			myself::CreateOnePrivateEx(private.summoner_id,private.,0,( 30 + gg::Rand(60) ),private.,private.npc_class_id,private.,gg::Rand(360),0,0,0);
		}
	}*/

	@Override
	protected void onEvtSeeCreatue(Creature creature)
	{
		//super.onEvtSeeCreatue(creature);

		if(_spawnStage == 0) // TODO: Проверить когда должны начинать спавниться миньоны.
		{
			if(getActor().inMyTerritory(getActor()))
			{
				_spawnStage = 1;
				addTimer(1003, 1700);
			}
		}

		if(creature.getZ() > (getActor().getZ() - 100) && creature.getZ() < (getActor().getZ() + 100))
		{
			if(!creature.isPlayer() && !creature.isSummon())
				return;

			if(getActor().getLifeTime() > 0 && getActor().inMyTerritory(getActor()))
			{
				addAttackDesire(creature, 1, 200);
			}
			if(_rememberedTargetsCount < 5 && Rnd.get(3) < 1)
			{
				if(_rememberedTargetsCount == 0)
				{
					_rememberTarget1 = creature;
				}
				else if(_rememberedTargetsCount == 1)
				{
					_rememberTarget2 = creature;
				}
				else if(_rememberedTargetsCount == 2)
				{
					_rememberTarget3 = creature;
				}
				else if(_rememberedTargetsCount == 3)
				{
					_rememberTarget4 = creature;
				}
				else if(_rememberedTargetsCount == 4)
				{
					_rememberTarget5 = creature;
				}
				_rememberedTargetsCount++;
			}
			if(Rnd.get(15) < 1)
			{
				int skillRandom = Rnd.get(15 * 15);
				if(skillRandom < 2)
				{
					teleportPlayer(creature);
				}
				else if(skillRandom < 4)
				{
					addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_KNOCKDOWN_NIGHT : S_ZAKEN_KNOCKDOWN_DAY, 0, 1, 1000000);
				}
				else if(skillRandom < 8)
				{
					addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DRAIN_NIGHT : S_ZAKEN_RANGE_DRAIN_DAY, 0, 1, 1000000);
					showMessage(NpcString.YOUR_BLOOD_WILL_BE_MY_FLESH, 10000);
				}
				else if(skillRandom < 15)
				{
					Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
					if(creature != topDesireTarget && getActor().getDistance(creature) < 100)
					{
						addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DUAL_ATTACK_NIGHT : S_ZAKEN_RANGE_DUAL_ATTACK_DAY, 0, 1, 1000000);
						showMessage(NpcString.LOSERS_YOU_FALL_UNDER_MY_SWORD, 10000);
					}
				}
			}
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);

		/*if(attacker.isMounted() && attacker.getAbnormalList().getAbnormalLevel(S_ANTI_STRIDER_SLOW.getAbnormalType()) <= 0)
		{
			if(S_ANTI_STRIDER_SLOW.getMpConsume1() < getActor().getCurrentMp() && S_ANTI_STRIDER_SLOW.getHpConsume() < getActor().getCurrentHp() && !getActor().isSkillDisabled(S_ANTI_STRIDER_SLOW))
			{
				addUseSkillDesire(attacker, S_ANTI_STRIDER_SLOW, 0, 1, 1000000);
			}
		}*/

		if(attacker.isPlayer() || attacker.isSummon())
		{
			addAttackDesire(attacker, 1, (long) (((damage / getActor().getMaxHp()) / 0.050000) * 20000));
		}

		if(Rnd.get(10) < 1 && (attacker.isPlayer() || attacker.isSummon()))
		{
			int skillRandom = Rnd.get(15 * 15);
			if(skillRandom < 2)
			{
				teleportPlayer(attacker);
			}
			else if(skillRandom < 4)
			{
				addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_KNOCKDOWN_NIGHT : S_ZAKEN_KNOCKDOWN_DAY, 0, 1, 1000000);
			}
			else if(skillRandom < 8)
			{
				addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DRAIN_NIGHT : S_ZAKEN_RANGE_DRAIN_DAY, 0, 1, 1000000);
				showMessage(NpcString.YOUR_BLOOD_WILL_BE_MY_FLESH, 10000);
			}
			else if(skillRandom < 15)
			{
				Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
				if(attacker != topDesireTarget && getActor().getDistance(attacker) < 100)
				{
					addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DUAL_ATTACK_NIGHT : S_ZAKEN_RANGE_DUAL_ATTACK_DAY, 0, 1, 1000000);
					showMessage(NpcString.LOSERS_YOU_FALL_UNDER_MY_SWORD, 10000);
				}
			}
		}

		if(_teleportedToShip)
		{
			//
		}
		else if(getActor().getCurrentHpPercents() < 30 && _canTeleportedToShip)
		{
			_teleportedToShip = true;
			_teleportX = ZAKEN_SHIP_COORD.getX();
			_teleportY = ZAKEN_SHIP_COORD.getY();
			_teleportZ = ZAKEN_SHIP_COORD.getZ();
			showMessage(NpcString.NOT_BAD_LETS_GO_TO_THE_DECK_ITS_TIME_TO_FINISH_WITH_IT, 10000);
			getActor().setSpawnedLoc(new Location(_teleportX, _teleportY, _teleportZ));
			getActor().teleToLocation(_teleportX, _teleportY, _teleportZ);
			getActor().getAggroList().clear(true);

			DoorInstance door = getActor().getReflection().getDoor(ZAKEN_DOOR_ID);
			if(!door.isOpen())
				door.openMe();
		}
		else if(GameTimeController.getInstance().getGameHour() < 5)
		{
			//
		}
		else if(getActor().getCurrentHp() < ((getActor().getMaxHp() * _teleportCheckHpModifier) / 4))
		{
			_teleportCheckHpModifier--;
			int loc[] = Rnd.get(COORDS);
			_teleportX = loc[0] + Rnd.get(10);//原來650
			_teleportY = loc[1] + Rnd.get(10);
			_teleportZ = loc[2];
			showMessage(NpcString.HA_HA_HA_TRY_AND_FIND_ME, 10000);
			getActor().setSpawnedLoc(new Location(_teleportX, _teleportY, _teleportZ));
			getActor().teleToLocation(_teleportX, _teleportY, _teleportZ);
			getActor().getAggroList().clear(true);
		}
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster, Creature target)
	{
		//super.onEvtSeeSpell(skill, caster, target);

		if(skill.getEffectPoint() > 0 && !skill.isDebuff())
		{
			addAttackDesire(caster, 1, ((skill.getEffectPoint() / getActor().getMaxHp()) * 10) * 150);
		}

		if(Rnd.get(12) < 1)
		{
			int skillRandom = Rnd.get(15 * 15);
			if(skillRandom < 2)
			{
				teleportPlayer(caster);
			}
			else if(skillRandom < 4)
			{
				addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_KNOCKDOWN_NIGHT : S_ZAKEN_KNOCKDOWN_DAY, 0, 1, 1000000);
			}
			else if(skillRandom < 8)
			{
				addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DRAIN_NIGHT : S_ZAKEN_RANGE_DRAIN_DAY, 0, 1, 1000000);
				showMessage(NpcString.YOUR_BLOOD_WILL_BE_MY_FLESH, 10000);
			}
			else if(skillRandom < 15)
			{
				Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
				if(caster != topDesireTarget && getActor().getDistance(caster) < 100)
				{
					addUseSkillDesire(getActor(), isNight() ? S_ZAKEN_RANGE_DUAL_ATTACK_NIGHT : S_ZAKEN_RANGE_DUAL_ATTACK_DAY, 0, 1, 1000000);
					showMessage(NpcString.LOSERS_YOU_FALL_UNDER_MY_SWORD, 10000);
				}
			}
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		effectMusic("BS02_D", 10000);
		NpcUtils.spawnSingle(TELEPORTER_NPC_ID, getActor().getLoc(), getActor().getReflection(), TELEPORTER_DESPAWN_TIME);
	}

	private void teleportPlayer(Creature creature)
	{
		if(!creature.isPlayer())
			return;

		Player player = creature.getPlayer();
		int loc[] = Rnd.get(COORDS);
		int x = loc[0] + Rnd.get(10);//原來650
		int y = loc[1] + Rnd.get(10);
		int z = loc[2];
		showMessage(NpcString.YOURE_THE_WEAKEST_SHOO, 10000);
		player.teleToLocation(x, y, z);
		getActor().getAggroList().remove(player, true);
	}

	private void effectMusic(String sound, int radius)
	{
		NpcInstance actor = getActor();

		PlaySoundPacket ps = new PlaySoundPacket(PlaySoundPacket.Type.SOUND, sound, 0, 0, actor.getLoc());
		for(Player player : World.getAroundPlayers(actor, radius))
			player.sendPacket(ps);
	}

	private void showMessage(NpcString npcString, int radius)
	{
		NpcInstance actor = getActor();

		ExShowScreenMessage msg = new ExShowScreenMessage(npcString, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true);
		for(Player player : World.getAroundPlayers(actor, radius))
			player.sendPacket(msg);
	}

	public boolean isTeleportedToShip()
	{
		return _teleportedToShip;
	}
}
