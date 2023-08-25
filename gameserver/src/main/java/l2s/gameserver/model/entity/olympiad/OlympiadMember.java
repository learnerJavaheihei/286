package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.data.xml.holder.SkillHolder;
import org.apache.commons.lang3.StringUtils;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadMatchEndPacket;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadModePacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadMember
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadMember.class);

	private String _name = StringUtils.EMPTY;
	private String _clanName = StringUtils.EMPTY;
	private int _classId;
	private double _damage; // !!! ЭТО ПОЛУЧЕННЫЙ УРОН, А НЕ НАНЕСЕНННЫЙ!!! НЕ ПУТАТЬ!!!

	private final int _objId;
	private final OlympiadGame _game;
	private final CompType _type;
	private final int _side;

	private Player _player;
	private Location _returnLoc = null;

	public OlympiadMember(int obj_id, OlympiadGame game, int side)
	{
		String player_name = "";
		Player player = GameObjectsStorage.getPlayer(obj_id);
		if(player != null)
			player_name = player.getName();
		else
		{
			String name = Olympiad.getParticipantName(obj_id);
			if(name != null)
				player_name = name;
		}

		_objId = obj_id;
		_name = player_name;
		_game = game;
		_type = game.getType();
		_side = side;

		_player = player;
		if(_player == null)
			return;

		_clanName = player.getClan() == null ? StringUtils.EMPTY : player.getClan().getName();
		_classId = player.getActiveClassId();

		player.setOlympiadSide(side);
		player.setOlympiadGame(game);
	}

	public OlympiadParticipiantData getStat()
	{
		return Olympiad.getParticipantInfo(_objId);
	}

	public void incGameCount()
	{
		OlympiadParticipiantData data = getStat();
		switch(_type)
		{
			case CLASSED:
				data.setClassedGamesCount(data.getClassedGamesCount() + 1);
				break;
			case NON_CLASSED:
				data.setNonClassedGamesCount(data.getNonClassedGamesCount() + 1);
				break;
		}
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
		{
			OlympiadParticipiantData data = getStat();
			int points = data.getPoints();
			int diff = Math.min(OlympiadGame.MAX_POINTS_LOOSE, points / _type.getLooseMult());
			data.setPoints(points - diff);
			Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

			// TODO: Снести подробный лог после исправления беспричинного отъёма очков.
			Player player = _player;
			if(player == null)
				Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
			else
			{
				if(player.isLogoutStarted())
					Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
				if(!player.isConnected())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(player.getOlympiadGame() == null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGame() == null", "olympiad");
				if(player.isInArenaObserverMode())
					Log.add("Olympiad info: " + _name + " crashed coz player.isInArenaObserverMode()", "olympiad");
			}
		}
	}

	public boolean checkPlayer()
	{
		Player player = _player;
		if(player == null || player.isLogoutStarted() || player.getOlympiadGame() == null || player.isInObserverMode())
			return false;
		return true;
	}

	public void portPlayerToArena()
	{
		Player player = _player;
		if(!checkPlayer() || player.isTeleporting())
		{
			_player = null;
			return;
		}

		DuelEvent duel = player.getEvent(DuelEvent.class);
		if(duel != null)
			duel.abortDuel(player);

		_returnLoc = player.getStablePoint() == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player.getStablePoint();

		if(player.isDead())
			player.setPendingRevive(true);
		if(player.isSitting())
			player.standUp();

		player.setTarget(null);
		player.setIsInOlympiadMode(true);

		player.getInventory().validateItems();

		player.leaveParty(false);

		Reflection ref = _game.getReflection();
		InstantZone instantZone = ref.getInstancedZone();

		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());

		player.setStablePoint(_returnLoc);
		player.teleToLocation(tele, ref);

		player.sendPacket(new ExOlympiadModePacket(_side));
	}

	public void portPlayerBack()
	{
		Player player = _player;
		if(player == null)
			return;

		if(_returnLoc == null) // игрока не портнуло на стадион
			return;

		player.setIsInOlympiadMode(false);
		player.setOlympiadSide(-1);
		player.setOlympiadGame(null);

		// Удаляем баффы и чужие кубики
		for(Abnormal abnormal : player.getAbnormalList())
		{
			if(!player.isSpecialAbnormal(abnormal.getSkill()))
				abnormal.exit();
		}

		for(Cubic cubic : player.getCubics())
		{
			if(player.getSkillLevel(cubic.getSkill().getId()) <= 0)
				cubic.delete();
		}

		for(Servitor servitor : player.getServitors())
			servitor.getAbnormalList().stopAll();

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new RevivePacket(player));
			//player.broadcastStatusUpdate();
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);

		// Возвращаем клановые скиллы если репутация положительная.
		if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
			player.getClan().enableSkills(player);

		// Активируем геройские скиллы.
		player.activateHeroSkills(true);

		// Обновляем скилл лист, после добавления скилов
		player.sendSkillList();
		player.sendPacket(new ExOlympiadModePacket(0));
		player.sendPacket(new ExOlympiadMatchEndPacket());

		player.setStablePoint(null);
		player.teleToLocation(_returnLoc, ReflectionManager.MAIN);
	}

	public void preparePlayer1()
	{
		Player player = _player;
		if(player == null)
			return;

		if(player.isInObserverMode())
			player.leaveObserverMode();

		// Un activate clan skills
		if(player.getClan() != null)
			player.getClan().disableSkills(player);

		// Деактивируем геройские скиллы.
		player.activateHeroSkills(false);

		// Abort casting if player casting
		if(player.isCastingNow())
			player.abortCast(true, true);

		// Abort attack if player attacking
		if(player.isAttackingNow())
			player.abortAttack(true, true);

		// Удаляем баффы и чужие кубики
		for(Abnormal abnormal : player.getAbnormalList())
		{
			if(!player.isSpecialAbnormal(abnormal.getSkill()))
				abnormal.exit();
		}

		for(Cubic cubic : player.getCubics())
		{
			if(player.getSkillLevel(cubic.getSkill().getId()) <= 0)
				cubic.delete();
		}

		// Remove Servitor's Buffs
		for(Servitor servitor : player.getServitors())
		{
			if(servitor.isPet())
				servitor.unSummon(false);
			else
			{
				servitor.getAbnormalList().stopAll();
				servitor.transferOwnerBuffs();
			}
		}

		// unsummon agathion
		if(player.getAgathionId() > 0)
			player.deleteAgathion();

		// Сброс кулдауна всех скилов, время отката которых меньше 15 минут
		for(TimeStamp sts : player.getSkillReuses())
		{
			if(sts == null)
				continue;
			Skill skill = SkillHolder.getInstance().getSkill(sts.getId(), sts.getLevel());
			if(skill == null)
				continue;
			if(skill.getReuseDelay() <= 900000L)
				player.enableSkill(skill);
		}

		// Обновляем скилл лист, после удаления скилов
		player.sendSkillList();

		// Проверяем одетые вещи на возможность ношения.
		player.getInventory().validateItems();

		// remove bsps/sps/ss automation
		player.removeAutoShots(true);

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void preparePlayer2()
	{
		Player player = _player;
		if(player == null)
			return;

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void saveParticipantData()
	{
		OlympiadDatabase.saveParticipantData(_objId);
	}

	public void logout()
	{
		_player = null;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getName()
	{
		return _name;
	}

	public void addDamage(double d)
	{
		_damage += d;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getObjectId()
	{
		return _objId;
	}
}