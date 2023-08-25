package l2s.gameserver.stats.funcs;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

/**
 * @author Bonux
 * Временный изжоп для нормальной работы абсорба, пока не перепишем движек статтов под ПТС.
 **/
public class FuncAbsorb extends Func {
    private final double chance;

    public FuncAbsorb(Stats stat, int order, Object owner, double value, StatsSet params) {
        super(stat, order, owner, value, params);
        chance = params.getDouble("chance", 100.);
    }

    @Override
    public void calc(Env env, StatModifierType modifierType) {
        double chance = this.chance * Config.ALT_VAMPIRIC_CHANCE_MOD;
        if (chance >= 100 || Rnd.chance(chance))
            env.value += value;
    }
}
