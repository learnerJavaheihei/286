package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.List;

public class PDAM_PATTACK_MUL extends Skill {
    /**
     * Внимание!!! У наследников вручную надо поменять тип на public
     *
     * @param set парамерты скилла
     */

    private double  _value;
    public PDAM_PATTACK_MUL(StatsSet set) {
        super(set);
    }
    @Override
    protected void useSkill(Creature activeChar, Creature target, boolean reflected)
    {
        loop:
        for (EffectTemplate effectTemplate : getEffectTemplates(EffectUseType.NORMAL)) {
            for (FuncTemplate attachedFunc : effectTemplate.getAttachedFuncs()) {
                if ("P_DAMAGE_ATTACK_MUL".equalsIgnoreCase(attachedFunc._stat.toString())) {
                    _value = attachedFunc._params.getDouble("value",1.0);
                    break loop;
                }
            }
        }
        _value = Math.max(_value, Stats.P_DAMAGE_ATTACK_MUL.getInit());
        final Creature realTarget = reflected ? activeChar : target;

        double damage = _value * activeChar.getPAtk(target);
        realTarget.reduceCurrentHp(damage, activeChar, this, true, true, true, true, false, false, damage != 0, true, false, false, false, 0, false);

    }

}
