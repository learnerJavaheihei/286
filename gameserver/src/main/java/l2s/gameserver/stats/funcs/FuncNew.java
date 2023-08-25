package l2s.gameserver.stats.funcs;

import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
 * Хрень временная, чтобы плавно внедрять и переписывать новую систему калькуляции статтов.
 **/
public class FuncNew extends Func {
	private final StatModifierType _modifierType;
	private final Stats _dependStat;

	public FuncNew(Stats stat, int order, Object owner, double value, StatsSet params) {
		super(stat, order, owner, value, params);
		_modifierType = (StatModifierType) params.getObject("mode");
		String dependStatName = params.getString("depend_stat", null);
		_dependStat = dependStatName != null ? Stats.valueOfXml(dependStatName) : null;
	}

	@Override
	public void calc(Env env, StatModifierType modifierType) {
		if (_dependStat != null) {
			if (modifierType == null || modifierType == StatModifierType.DIFF) {
				switch (_dependStat) {
					case POWER_DEFENCE:
						env.value += env.character.getPDef(env.target) * 0.01 * value;
						break;
					case MAGIC_DEFENCE:
						env.value += env.character.getMDef(env.target, env.skill) * 0.01 * value;
						break;
					case POWER_ATTACK:
						env.value += env.character.getPAtk(env.target) * 0.01 * value;
						break;
					case MAGIC_ATTACK:
						env.value += env.character.getMAtk(env.target, env.skill) * 0.01 * value;
						break;
					case BASE_P_CRITICAL_RATE:
						env.value += env.character.getPCriticalHit(env.target) * 0.01 * value;
						break;
					case BASE_M_CRITICAL_RATE:
						env.value += env.character.getMCriticalHit(env.target, env.skill) * 0.01 * value;
						break;
				}
			}
		} else if (_modifierType == modifierType) {
			switch (_modifierType) {
				case DIFF:
				case PER:
					env.value += value;
					break;
			}
		} else if (modifierType == null) {
			switch (_modifierType) {
				case DIFF:
					env.value += value;
					break;
				case PER:
					env.value *= (100. + value) / 100.;
					break;
			}
		}
	}
}
