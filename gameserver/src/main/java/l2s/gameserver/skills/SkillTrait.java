package l2s.gameserver.skills;

import l2s.gameserver.Config;
import l2s.gameserver.stats.Stats;

/**
 * @author UnAfraid, NosBit
 * @reworked by Bonux
 */
public enum SkillTrait
{
	/*0*/UNK_0(SkillTraitType.NONE, null, null),
	/*1*/SWORD(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_SWORD, Stats.DEFENCE_TRAIT_SWORD),
	/*2*/BLUNT(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_BLUNT, Stats.DEFENCE_TRAIT_BLUNT),
	/*3*/DAGGER(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_DAGGER, Stats.DEFENCE_TRAIT_DAGGER),
	/*4*/POLE(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_POLE, Stats.DEFENCE_TRAIT_POLE),
	/*5*/FIST(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_FIST, Stats.DEFENCE_TRAIT_FIST),
	/*6*/BOW(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_BOW, Stats.DEFENCE_TRAIT_BOW),
	/*7*/ETC(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_ETC, Stats.DEFENCE_TRAIT_ETC),
	/*8*/UNK_8(SkillTraitType.NONE, null, null),
	/*9*/POISON(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_POISON, Stats.DEFENCE_TRAIT_POISON)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_POISON_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_POISON_MOD;
		}
	},
	/*10*/HOLD(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_HOLD, Stats.DEFENCE_TRAIT_HOLD)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_HOLD_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_HOLD_MOD;
		}
	},
	/*11*/BLEED(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_BLEED, Stats.DEFENCE_TRAIT_BLEED)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_BLEED_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_BLEED_MOD;
		}
	},
	/*12*/SLEEP(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_SLEEP, Stats.DEFENCE_TRAIT_SLEEP)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_SLEEP_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_SLEEP_MOD;
		}
	},
	/*13*/SHOCK(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_SHOCK, Stats.DEFENCE_TRAIT_SHOCK)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_SHOCK_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_SHOCK_MOD;
		}
	},
	/*14*/DERANGEMENT(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_DERANGEMENT, Stats.DEFENCE_TRAIT_DERANGEMENT)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_DERANGEMENT_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_DERANGEMENT_MOD;
		}
	},
	/*15*/BUG_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_BUG_WEAKNESS, Stats.DEFENCE_TRAIT_BUG_WEAKNESS),
	/*16*/ANIMAL_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_ANIMAL_WEAKNESS, Stats.DEFENCE_TRAIT_ANIMAL_WEAKNESS),
	/*17*/PLANT_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_PLANT_WEAKNESS, Stats.DEFENCE_TRAIT_PLANT_WEAKNESS),
	/*18*/BEAST_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_BEAST_WEAKNESS, Stats.DEFENCE_TRAIT_BEAST_WEAKNESS),
	/*19*/DRAGON_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_DRAGON_WEAKNESS, Stats.DEFENCE_TRAIT_DRAGON_WEAKNESS),
	/*20*/PARALYZE(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_PARALYZE, Stats.DEFENCE_TRAIT_PARALYZE)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_PARALYZE_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_PARALYZE_MOD;
		}
	},
	/*21*/DUAL(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_DUAL, Stats.DEFENCE_TRAIT_DUAL),
	/*22*/DUALFIST(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_DUALFIST, Stats.DEFENCE_TRAIT_DUALFIST),
	/*23*/BOSS(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_BOSS, Stats.DEFENCE_TRAIT_BOSS)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_BOSS_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_BOSS_MOD;
		}
	},
	/*24*/GIANT_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_GIANT_WEAKNESS, Stats.DEFENCE_TRAIT_GIANT_WEAKNESS),
	/*25*/CONSTRUCT_WEAKNESS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_CONSTRUCT_WEAKNESS, Stats.DEFENCE_TRAIT_CONSTRUCT_WEAKNESS),
	/*26*/DEATH(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_DEATH, Stats.DEFENCE_TRAIT_DEATH)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_DEATH_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_DEATH_MOD;
		}
	},
	/*27*/VALAKAS(SkillTraitType.WEAKNESS, Stats.ATTACK_TRAIT_VALAKAS, Stats.DEFENCE_TRAIT_VALAKAS),
	/*28*/UNK_28(SkillTraitType.WEAKNESS, null, null),
	/*29*/UNK_29(SkillTraitType.RESISTANCE, null, null),
	/*30*/ROOT_PHYSICALLY(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_ROOT_PHYSICALLY, Stats.DEFENCE_TRAIT_ROOT_PHYSICALLY)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_ROOT_PHYSICALLY_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_ROOT_PHYSICALLY_MOD;
		}
	},
	/*31*/UNK_31(SkillTraitType.RESISTANCE, null, null),
	/*32*/RAPIER(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_RAPIER, Stats.DEFENCE_TRAIT_RAPIER),
	/*33*/CROSSBOW(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_CROSSBOW, Stats.DEFENCE_TRAIT_CROSSBOW),
	/*34*/ANCIENTSWORD(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_ANCIENTSWORD, Stats.DEFENCE_TRAIT_ANCIENTSWORD),
	/*35*/TURN_STONE(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_TURN_STONE, Stats.DEFENCE_TRAIT_TURN_STONE)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_TURN_STONE_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_TURN_STONE_MOD;
		}
	},
	/*36*/GUST(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_GUST, Stats.DEFENCE_TRAIT_GUST)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_GUST_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_GUST_MOD;
		}
	},
	/*37*/PHYSICAL_BLOCKADE(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_PHYSICAL_BLOCKADE, Stats.DEFENCE_TRAIT_PHYSICAL_BLOCKADE)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_PHYSICAL_BLOCKADE_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_PHYSICAL_BLOCKADE_MOD;
		}
	},
	/*38*/TARGET(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_TARGET, Stats.DEFENCE_TRAIT_TARGET)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_TARGET_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_TARGET_MOD;
		}
	},
	/*39*/PHYSICAL_WEAKNESS(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_PHYSICAL_WEAKNESS, Stats.DEFENCE_TRAIT_PHYSICAL_WEAKNESS)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_PHYSICAL_WEAKNESS_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_PHYSICAL_WEAKNESS_MOD;
		}
	},
	/*40*/MAGICAL_WEAKNESS(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_MAGICAL_WEAKNESS, Stats.DEFENCE_TRAIT_MAGICAL_WEAKNESS)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_MAGICAL_WEAKNESS_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_MAGICAL_WEAKNESS_MOD;
		}
	},
	/*41*/DUALDAGGER(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_DUALDAGGER, Stats.DEFENCE_TRAIT_DUALDAGGER),
	/*42*/DUALBLUNT(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_DUALBLUNT, Stats.DEFENCE_TRAIT_DUALBLUNT),
	/*43*/KNOCKBACK(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_KNOCKBACK, Stats.DEFENCE_TRAIT_KNOCKBACK)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_KNOCKBACK_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_KNOCKBACK_MOD;
		}
	},
	/*44*/KNOCKDOWN(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_KNOCKDOWN, Stats.DEFENCE_TRAIT_KNOCKDOWN)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_KNOCKDOWN_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_KNOCKDOWN_MOD;
		}
	},
	/*45*/PULL(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_PULL, Stats.DEFENCE_TRAIT_PULL)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_PULL_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_PULL_MOD;
		}
	},
	/*46*/HATE(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_HATE, Stats.DEFENCE_TRAIT_HATE)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_HATE_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_HATE_MOD;
		}
	},
	/*47*/AGGRESSION(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_AGGRESSION, Stats.DEFENCE_TRAIT_AGGRESSION)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_AGGRESSION_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_AGGRESSION_MOD;
		}
	},
	/*48*/AIRBIND(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_AIRBIND, Stats.DEFENCE_TRAIT_AIRBIND)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_AIRBIND_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_AIRBIND_MOD;
		}
	},
	/*49*/DISARM(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_DISARM, Stats.DEFENCE_TRAIT_DISARM)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_DISARM_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_DISARM_MOD;
		}
	},
	/*50*/DEPORT(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_DEPORT, Stats.DEFENCE_TRAIT_DEPORT)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_DEPORT_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_DEPORT_MOD;
		}
	},
	/*51*/CHANGEBODY(SkillTraitType.RESISTANCE, Stats.ATTACK_TRAIT_CHANGEBODY, Stats.DEFENCE_TRAIT_CHANGEBODY)
	{
		@Override
		public double getAttackMod()
		{
			return Config.ATTACK_TRAIT_CHANGEBODY_MOD;
		}

		@Override
		public double getDefenceMod()
		{
			return Config.DEFENCE_TRAIT_CHANGEBODY_MOD;
		}
	},
	/*52*/TWOHANDCROSSBOW(SkillTraitType.WEAPON, Stats.ATTACK_TRAIT_TWOHANDCROSSBOW, Stats.DEFENCE_TRAIT_TWOHANDCROSSBOW),
	/*53*/NONE(SkillTraitType.NONE, null, null);

	public static final SkillTrait[] VALUES = values();

	private final SkillTraitType _type;
	private final Stats _attack;
	private final Stats _defence;

	private SkillTrait(SkillTraitType type, Stats attack, Stats defence)
	{
		_type = type;
		_attack = attack;
		_defence = defence;
	}

	public int getId()
	{
		return ordinal();
	}

	public SkillTraitType getType()
	{
		return _type;
	}

	public Stats getAttack()
	{
		return _attack;
	}

	public Stats getDefence()
	{
		return _defence;
	}

	public double getAttackMod()
	{
		return 1.;
	}

	public double getDefenceMod()
	{
		return 1.;
	}
}
