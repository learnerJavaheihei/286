package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SpecialMonsterInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class SnowSquashInstance extends SpecialMonsterInstance
{
	private static final int small_baby_gourd = 13399;	// Молодая Тыква
	private static final int g_small_adult_gourd = 13400;	// Спелая Тыква
	private static final int b_small_adult_gourd = 13401;	// Завядшая Тыква
	private static final int big_baby_gourd = 13403;	// Большая Молодая Тыква
	private static final int g_big_adult_gourd = 13404;	// Большая Спелая Тыква
	private static final int b_big_adult_gourd = 13405;	// Большая Завядшая Тыква
	private static final int kg_small_adult_gourd = 13402;	// Королевская Тыква
	private static final int kg_big_adult_gourd = 13406;	// Большая Королевская Тыква

	public SnowSquashInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, double elementalDamage, boolean elementalCrit)
	{
		if(getNpcId() == b_big_adult_gourd || getNpcId() == g_big_adult_gourd || getNpcId() == kg_big_adult_gourd)
		{
			// Разрешенное оружие для больших тыкв:
			// 4202 Chrono Cithara
			// 5133 Chrono Unitus
			// 5817 Chrono Campana
			// 7058 Chrono Darbuka
			// 8350 Chrono Maracas
			int weaponId = attacker.getActiveWeaponInstance() == null ? 0 : attacker.getActiveWeaponInstance().getItemId();
			if(weaponId != 4202 && weaponId != 5133 && weaponId != 5817 && weaponId != 7058 && weaponId != 8350)
				return;

			damage = 1;
		}
		else if(getNpcId() == g_small_adult_gourd || getNpcId() == b_small_adult_gourd || getNpcId() == kg_small_adult_gourd)
		{
			damage = 5;
		}
		else
			return;

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, elementalDamage, elementalCrit);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
}