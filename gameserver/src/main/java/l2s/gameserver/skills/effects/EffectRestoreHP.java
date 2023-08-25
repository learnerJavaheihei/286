package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExRegenMaxPacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.impl.HashIntSet;

/**
 * @author Bonux
 **/
public class EffectRestoreHP extends EffectRestoreCP
{
	//TODO: Таблица и формулы взяты с https://4gameforum.com/threads/506577/ протестировать формулы и перевести таблицу в формулу
	private static final IntObjectMap<double[]> M_LVL_MOD = new HashIntObjectMap<double[]>();
	static
	{
		M_LVL_MOD.put(1, new double[]{ 7.0999, 8.77566 });
		M_LVL_MOD.put(2, new double[]{ 7.7718, 11.85356 });
		M_LVL_MOD.put(3, new double[]{ 8.4669, 14.62886 });
		M_LVL_MOD.put(4, new double[]{ 9.1852, 17.14116 });
		M_LVL_MOD.put(5, new double[]{ 9.9267, 19.4291 });
		M_LVL_MOD.put(6, new double[]{ 10.6914, 21.53036 });
		M_LVL_MOD.put(7, new double[]{ 11.4793, 23.48166 });
		M_LVL_MOD.put(8, new double[]{ 12.2904, 25.31876 });
		M_LVL_MOD.put(9, new double[]{ 13.1247, 27.07646 });
		M_LVL_MOD.put(10, new double[]{ 13.9822, 28.7886 });
		M_LVL_MOD.put(11, new double[]{ 14.8629, 30.48806 });
		M_LVL_MOD.put(12, new double[]{ 15.7668, 32.20676 });
		M_LVL_MOD.put(13, new double[]{ 16.6939, 33.97566 });
		M_LVL_MOD.put(14, new double[]{ 17.6442, 35.82476 });
		M_LVL_MOD.put(15, new double[]{ 18.6177, 37.7831 });
		M_LVL_MOD.put(16, new double[]{ 19.6144, 39.87876 });
		M_LVL_MOD.put(17, new double[]{ 20.6343, 42.13886 });
		M_LVL_MOD.put(18, new double[]{ 21.6774, 44.58956 });
		M_LVL_MOD.put(19, new double[]{ 22.7437, 47.25606 });
		M_LVL_MOD.put(20, new double[]{ 23.8332, 50.1626 });
		M_LVL_MOD.put(21, new double[]{ 24.9459, 53.33246 });
		M_LVL_MOD.put(22, new double[]{ 26.0818, 56.78796 });
		M_LVL_MOD.put(23, new double[]{ 27.2409, 60.55046 });
		M_LVL_MOD.put(24, new double[]{ 28.4232, 64.64036 });
		M_LVL_MOD.put(25, new double[]{ 29.6287, 69.0771 });
		M_LVL_MOD.put(26, new double[]{ 30.8574, 73.87916 });
		M_LVL_MOD.put(27, new double[]{ 32.1093, 79.06406 });
		M_LVL_MOD.put(28, new double[]{ 33.3844, 84.64836 });
		M_LVL_MOD.put(29, new double[]{ 34.6827, 90.64766 });
		M_LVL_MOD.put(30, new double[]{ 36.0042, 97.0766 });
		M_LVL_MOD.put(31, new double[]{ 37.3489, 103.94886 });
		M_LVL_MOD.put(32, new double[]{ 38.7168, 111.27716 });
		M_LVL_MOD.put(33, new double[]{ 40.1079, 119.07326 });
		M_LVL_MOD.put(34, new double[]{ 41.5222, 127.34796 });
		M_LVL_MOD.put(35, new double[]{ 42.9597, 136.1111 });
		M_LVL_MOD.put(36, new double[]{ 44.4204, 145.37156 });
		M_LVL_MOD.put(37, new double[]{ 45.9043, 155.13726 });
		M_LVL_MOD.put(38, new double[]{ 47.4114, 165.41516 });
		M_LVL_MOD.put(39, new double[]{ 48.9417, 176.21126 });
		M_LVL_MOD.put(40, new double[]{ 50.4952, 187.5306 });
		M_LVL_MOD.put(41, new double[]{ 52.0719, 199.37726 });
		M_LVL_MOD.put(42, new double[]{ 53.6718, 211.75436 });
		M_LVL_MOD.put(43, new double[]{ 55.2949, 224.66406 });
		M_LVL_MOD.put(44, new double[]{ 56.9412, 238.10756 });
		M_LVL_MOD.put(45, new double[]{ 58.6107, 252.0851 });
		M_LVL_MOD.put(46, new double[]{ 60.3034, 266.59596 });
		M_LVL_MOD.put(47, new double[]{ 62.0193, 281.63846 });
		M_LVL_MOD.put(48, new double[]{ 63.7584, 297.20996 });
		M_LVL_MOD.put(49, new double[]{ 65.5207, 313.30686 });
		M_LVL_MOD.put(50, new double[]{ 67.3062, 329.9246 });
		M_LVL_MOD.put(51, new double[]{ 69.1149, 347.05766 });
		M_LVL_MOD.put(52, new double[]{ 70.9468, 364.69956 });
		M_LVL_MOD.put(53, new double[]{ 72.8019, 382.84286 });
		M_LVL_MOD.put(54, new double[]{ 74.6802, 401.47916 });
		M_LVL_MOD.put(55, new double[]{ 76.5817, 420.5991 });
		M_LVL_MOD.put(56, new double[]{ 78.5064, 440.19236 });
		M_LVL_MOD.put(57, new double[]{ 80.4543, 460.24766 });
		M_LVL_MOD.put(58, new double[]{ 82.4254, 480.75276 });
		M_LVL_MOD.put(59, new double[]{ 84.4197, 501.69446 });
		M_LVL_MOD.put(60, new double[]{ 86.4372, 523.0586 });
		M_LVL_MOD.put(61, new double[]{ 88.4779, 544.83006 });
		M_LVL_MOD.put(62, new double[]{ 90.5418, 566.99276 });
		M_LVL_MOD.put(63, new double[]{ 92.6289, 589.52966 });
		M_LVL_MOD.put(64, new double[]{ 94.7392, 612.42276 });
		M_LVL_MOD.put(65, new double[]{ 96.8727, 635.6531 });
		M_LVL_MOD.put(66, new double[]{ 99.0294, 659.20076 });
		M_LVL_MOD.put(67, new double[]{ 101.2093, 683.04486 });
		M_LVL_MOD.put(68, new double[]{ 103.4124, 707.16356 });
		M_LVL_MOD.put(69, new double[]{ 105.6387, 731.53406 });
		M_LVL_MOD.put(70, new double[]{ 107.8882, 756.1326 });
		M_LVL_MOD.put(71, new double[]{ 110.1609, 780.93446 });
		M_LVL_MOD.put(72, new double[]{ 112.4568, 805.91396 });
		M_LVL_MOD.put(73, new double[]{ 114.7759, 831.04446 });
		M_LVL_MOD.put(74, new double[]{ 117.1182, 856.29836 });
		M_LVL_MOD.put(75, new double[]{ 119.4837, 881.6471 });
		M_LVL_MOD.put(76, new double[]{ 121.8724, 907.06116 });
		M_LVL_MOD.put(77, new double[]{ 124.2843, 932.51006 });
		M_LVL_MOD.put(78, new double[]{ 126.7194, 957.96236 });
		M_LVL_MOD.put(79, new double[]{ 129.1777, 983.38566 });
		M_LVL_MOD.put(80, new double[]{ 131.6592, 1008.7466 });
		M_LVL_MOD.put(81, new double[]{ 134.1639, 1034.01086 });
		M_LVL_MOD.put(82, new double[]{ 136.6918, 1059.14316 });
		M_LVL_MOD.put(83, new double[]{ 139.2429, 1084.10726 });
		M_LVL_MOD.put(84, new double[]{ 141.8172, 1108.86596 });
		M_LVL_MOD.put(85, new double[]{ 144.4147, 1133.3811 });
		M_LVL_MOD.put(86, new double[]{ 147.0354, 1157.61356 });
		M_LVL_MOD.put(87, new double[]{ 149.6793, 1181.52326 });
		M_LVL_MOD.put(88, new double[]{ 152.3464, 1205.06916 });
		M_LVL_MOD.put(89, new double[]{ 155.0367, 1228.20926 });
		M_LVL_MOD.put(90, new double[]{ 157.7502, 1250.9006 });
		M_LVL_MOD.put(91, new double[]{ 160.4869, 1273.09926 });
		M_LVL_MOD.put(92, new double[]{ 163.2468, 1294.76036 });
		M_LVL_MOD.put(93, new double[]{ 166.0299, 1315.83806 });
		M_LVL_MOD.put(94, new double[]{ 168.8362, 1336.28556 });
		M_LVL_MOD.put(95, new double[]{ 171.6657, 1356.0551 });
		M_LVL_MOD.put(96, new double[]{ 174.5184, 1375.09796 });
		M_LVL_MOD.put(97, new double[]{ 177.3943, 1393.36446 });
		M_LVL_MOD.put(98, new double[]{ 180.2934, 1410.80396 });
		M_LVL_MOD.put(99, new double[]{ 183.2157, 1427.36486 });
		M_LVL_MOD.put(100, new double[]{ 186.1612, 1442.9946 });
		M_LVL_MOD.put(101, new double[]{ 189.1299, 1457.63966 });
		M_LVL_MOD.put(102, new double[]{ 192.1218, 1471.24556 });
		M_LVL_MOD.put(103, new double[]{ 195.1369, 1483.75686 });
		M_LVL_MOD.put(104, new double[]{ 198.1752, 1495.11716 });
		M_LVL_MOD.put(105, new double[]{ 201.2367, 1505.2691 });
		M_LVL_MOD.put(106, new double[]{ 204.3214, 1514.15436 });
		M_LVL_MOD.put(107, new double[]{ 207.4293, 1521.71366 });
		M_LVL_MOD.put(108, new double[]{ 210.5604, 1527.88676 });
		M_LVL_MOD.put(109, new double[]{ 213.7147, 1532.61246 });
		M_LVL_MOD.put(110, new double[]{ 216.8922, 1535.8286 });
		M_LVL_MOD.put(111, new double[]{ 220.0929, 1537.47206 });
		M_LVL_MOD.put(112, new double[]{ 223.3168, 1537.47876 });
		M_LVL_MOD.put(113, new double[]{ 226.5639, 1535.78366 });
		M_LVL_MOD.put(114, new double[]{ 229.8342, 1532.32076 });
		M_LVL_MOD.put(115, new double[]{ 233.1277, 1527.0231 });
		M_LVL_MOD.put(116, new double[]{ 236.4444, 1519.82276 });
		M_LVL_MOD.put(117, new double[]{ 239.7843, 1510.65086 });
		M_LVL_MOD.put(118, new double[]{ 243.1474, 1499.43756 });
		M_LVL_MOD.put(119, new double[]{ 246.5337, 1486.11206 });
		M_LVL_MOD.put(120, new double[]{ 249.9432, 1470.6026 });
	}

	public class EffectRestoreHPImpl extends EffectHandler
	{
		private double _power = 0.;

		EffectRestoreHPImpl(EffectTemplate template)
		{
			super(template);
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			_power = startHeal(abnormal, effector, effected);
		}

		@Override
		public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
		{
			if(getTemplate().isInstant())
				return false;

			if(effected.isHealBlocked())
				return true;

			HashIntSet updateTypes = new HashIntSet();

			int addToHp = (int) _power;
			if(addToHp > 0)
			{
				addToHp = checkRestoreHpLimits(effected, addToHp);
				effected.setCurrentHp(effected.getCurrentHp() + addToHp, false, false);
				updateTypes.add(StatusUpdatePacket.CUR_HP);
			}

			int addToCp = calcAddToCp(_power, addToHp, effector, effected);
			if(addToCp > 0)
			{
				addToCp = checkRestoreCpLimits(effected, addToCp);
				effected.setCurrentCp(effected.getCurrentCp() + addToCp, false);
				updateTypes.add(StatusUpdatePacket.CUR_CP);
			}

			if(!updateTypes.isEmpty())
			{
				StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, updateTypes.toArray());
				effector.sendPacket(su);
				effected.sendPacket(su);
				effected.broadcastStatusUpdate();
				effected.sendChanges();
			}
			return true;
		}
	}

	private final boolean _cpIncluding;

	public EffectRestoreHP(EffectTemplate template)
	{
		super(template);
		_cpIncluding = getParams().getBool("cp_including", false);
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		startHeal(abnormal, effector, effected);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectRestoreHPImpl(getTemplate());
	}

	private int calcAddToCp(double power, int addToHp, Creature effector, Creature effected)
	{
		int addToCp = 0;
		if(_cpIncluding && effected.isPlayer())
			addToCp = (int) (power - addToHp);
		return addToCp;
	}

	private int checkRestoreHpLimits(Creature effected, double power)
	{
		int newHp = (int) (effected.getCurrentHp() + power);
		newHp = Math.max(0, Math.min(newHp, (int) (effected.getMaxHp() / 100. * effected.getStat().calc(Stats.HP_LIMIT, null, null))));
		newHp = Math.max(0, newHp - (int) effected.getCurrentHp());
		newHp = Math.min(effected.getMaxHp() - (int) effected.getCurrentHp(), newHp);
		return newHp;
	}

	private double startHeal(Abnormal abnormal, Creature effector, Creature effected)
	{
		double power = getValue();
		if(power <= 0)
			return power;

		if(_percent)
			power = effected.getMaxHp() / 100. * power;

		if(getSkill().isHandler())
		{
			if(!_staticPower)
			{
				if(!_ignoreBonuses)
				{
					power += effector.getStat().getAdd(Stats.POTION_HP_HEAL_EFFECT, effected, getSkill());
					power *= effected.getStat().getMul(Stats.POTION_HP_HEAL_EFFECT, effector, getSkill());
				}
			}
		}
		else if(!_percent)
		{
			if(!_staticPower)
			{
				double spiritshot_bonus_1 = getSkill().isSSPossible() && getSkill().getHpConsume() == 0 ? (getSkill().isMagic() ? (effector.getChargedSpiritshotPower() / 100.) : 1.) : 1.;
				double spiritshot_bonus_3 = effector.getChargedSpiritshotHealBonus();
				double mAtk = effector.getMAtk(null, getSkill());
				double staticHealBonus = _ignoreBonuses ? 0 : effector.getStat().getAdd(Stats.HEAL_EFFECT, effected, getSkill());	// статический бонус к лечащим умениям.
				double percentHealBonus = _ignoreBonuses ? 1 : effected.getStat().getMul(Stats.HEAL_EFFECT, effector, getSkill());	// процентный бонус к лечащим умениям.
				int magicLevel = getSkill().getMagicLevel() > 0 ? getSkill().getMagicLevel() : effector.getLevel();
				double[] mLvlMod = M_LVL_MOD.get(Math.min(magicLevel, M_LVL_MOD.size()));
				double magic_level_mod = mLvlMod[0] - Math.max(0, (mLvlMod[1] - mAtk) / 5.);
				double base_heal = (power + Math.sqrt(mAtk + mAtk * spiritshot_bonus_1) + staticHealBonus + spiritshot_bonus_3 * magic_level_mod) * percentHealBonus;
				int effectorLevel = effector.getLevel();
				double lvlMod = effectorLevel < 100 ? ((effectorLevel + 89) / 100.) : ((effectorLevel + 88 + 6 * (effectorLevel - 99)) / 100);
				double weapon_bonus = Math.min(base_heal, lvlMod * effector.getBaseStats().getMAtk() * BaseStats.MEN.calcBonus(effector));

				double heal = base_heal + weapon_bonus;

				power = heal;

				if(getSkill().isMagic())
				{
					if(Formulas.calcMCrit(effector, effected, getSkill()))
					{
						power *= 3;
						effector.sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT));
						effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.CRITICAL_HEAL));
						if(effected.isPlayer() && effector != effected)
							effected.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.CRITICAL_HEAL));
					}
				}
			}
		}
		else
		{
			if(!_staticPower)
			{
				if(!_ignoreBonuses)
				{
					power += effector.getStat().getAdd(Stats.HEAL_EFFECT, effected, getSkill());
					power *= effected.getStat().getMul(Stats.HEAL_EFFECT, effector, getSkill());
				}
			}
		}

		if(effected.isHealBlocked())
			return power;

		int addToHp = (int) power;
		if(!getTemplate().isInstant())
		{
			if(effected.isPlayer() && abnormal != null)
				effected.sendPacket(new ExRegenMaxPacket(addToHp, abnormal.getDuration(), getInterval()));
			return power;
		}

		HashIntSet updateTypes = new HashIntSet();
		if(addToHp > 0)
		{
			addToHp = checkRestoreHpLimits(effected, addToHp);
			if(getSkill().getId() == 4051)
				effected.sendPacket(SystemMsg.REJUVENATING_HP);
			else if(effector != effected)
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(effector).addInteger(addToHp));
			else
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(addToHp));

			effected.setCurrentHp(effected.getCurrentHp() + addToHp, false, false);
			updateTypes.add(StatusUpdatePacket.CUR_HP);
		}

		int addToCp = calcAddToCp(power, addToHp, effector, effected);
		if(addToCp > 0)
		{
			addToCp = checkRestoreCpLimits(effected, addToCp);
			if(effector != effected)
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1).addName(effector).addInteger(addToCp));
			else
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(addToCp));

			effected.setCurrentCp(effected.getCurrentCp() + addToCp, false);
			updateTypes.add(StatusUpdatePacket.CUR_CP);
		}

		if(!updateTypes.isEmpty())
		{
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, updateTypes.toArray());
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
		return power;
	}
}