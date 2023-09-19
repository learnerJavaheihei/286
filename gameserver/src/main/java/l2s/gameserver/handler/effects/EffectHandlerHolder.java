package l2s.gameserver.handler.effects;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.skills.effects.*;
import l2s.gameserver.skills.effects.consume.*;
import l2s.gameserver.skills.effects.instant.*;
import l2s.gameserver.skills.effects.permanent.*;
import l2s.gameserver.skills.effects.tick.*;
import l2s.gameserver.templates.skill.EffectTemplate;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Bonux
**/
public class EffectHandlerHolder extends AbstractHolder
{
	private static final EffectHandlerHolder _instance = new EffectHandlerHolder();

	public static EffectHandlerHolder getInstance()
	{
		return _instance;
	}

	private Map<String, Constructor<? extends EffectHandler>> _handlerConstructors = new HashMap<String, Constructor<? extends EffectHandler>>();

	private EffectHandlerHolder()
	{
		// Old Effect
		// TODO: Remake to offlike effects
		registerHandler(EffectAddSkills.class);
		registerHandler(EffectAgathionResurrect.class);
		registerHandler(EffectBetray.class);
		registerHandler(EffectBuff.class);
		registerHandler(EffectDamageBlock.class);
		registerHandler(EffectDistortedSpace.class);
		registerHandler(EffectCharge.class);
		registerHandler(EffectCharmOfCourage.class);
		registerHandler(EffectCPDamPercent.class);
		registerHandler(EffectDamageHealToEffector.class);
		registerHandler(EffectDestroySummon.class);
		registerHandler(EffectDeathImmunity.class);
		registerHandler(EffectDisarm.class);
		registerHandler(EffectDiscord.class);
		registerHandler(EffectDispelOnHit.class);
		registerHandler(EffectEffectImmunity.class);
		registerHandler(EffectEnervation.class);
		registerHandler(EffectFakeDeath.class);
		registerHandler(EffectFear.class);
		registerHandler(EffectMoveToEffector.class);
		registerHandler(EffectGrow.class);
		registerHandler(EffectHate.class);
		registerHandler(EffectHealBlock.class);
		registerHandler(EffectHPDamPercent.class);
		registerHandler(EffectHpToOne.class);
		registerHandler(EffectIgnoreSkill.class);
		registerHandler(EffectInterrupt.class);
		registerHandler(EffectInvulnerable.class);
		registerHandler(EffectInvisible.class);
		registerHandler(EffectLockInventory.class);
		registerHandler(EffectCurseOfLifeFlow.class);
		registerHandler(EffectLaksis.class);
		registerHandler(EffectLDManaDamOverTime.class);
		registerHandler(EffectManaDamOverTime.class);
		registerHandler(EffectMeditation.class);
		registerHandler(EffectMPDamPercent.class);
		registerHandler(EffectMute.class);
		registerHandler(EffectMuteChance.class);
		registerHandler(EffectMuteAll.class);
		registerHandler(EffectMutation.class);
		registerHandler(EffectMuteAttack.class);
		registerHandler(EffectMutePhisycal.class);
		registerHandler(EffectNegateMark.class);
		registerHandler(EffectParalyze.class);
		registerHandler(EffectPetrification.class);
		registerHandler(EffectRelax.class);
		registerHandler(EffectSalvation.class);
		registerHandler(EffectServitorShare.class);
		registerHandler(EffectSilentMove.class);
		registerHandler(EffectSleep.class);
		registerHandler(EffectStun.class);
		registerHandler(EffectKnockDown.class);
		registerHandler(EffectKnockBack.class);
		registerHandler(EffectFlyUp.class);
		registerHandler(EffectThrowHorizontal.class);
		registerHandler(EffectThrowUp.class);
		registerHandler(EffectTransformation.class);
		registerHandler(EffectVisualTransformation.class);
		registerHandler(EffectVitality.class);
		registerHandler(EffectShadowStep.class);

		registerHandler(EffectRestoreCP.class);
		registerHandler(EffectRestoreHP.class);
		registerHandler(EffectRestoreMP.class);

		registerHandler(EffectCPDrain.class);
		registerHandler(EffectHPDrain.class);
		registerHandler(EffectMPDrain.class);

		registerHandler(EffectAbsorbDamageToEffector.class); // абсорбирует часть дамага к еффектора еффекта
		registerHandler(EffectAbsorbDamageToMp.class); // абсорбирует часть дамага в мп
		registerHandler(EffectAbsorbDamageToSummon.class); // абсорбирует часть дамага к сумону

		registerHandler(EffectArmorBreaker.class);
		registerHandler(EffectDamageOnSkillUse.class);

		// Offlike Effects

		// Consume Effects
		registerHandler(c_mp.class);
		registerHandler(c_mp_by_level.class);

		// Instant Effects
		registerHandler(i_add_hate.class);
		registerHandler(i_add_soul.class);
		registerHandler(i_align_direction.class);
		registerHandler(i_call_random_skill.class);
		registerHandler(i_call_skill.class);
		registerHandler(i_dispel_all.class);
		registerHandler(i_dispel_by_category.class);
		registerHandler(i_dispel_by_slot.class);
		registerHandler(i_dispel_by_slot_myself.class);
		registerHandler(i_dispel_by_slot_probability.class);
		registerHandler(i_death_link.class);
		registerHandler(i_delete_hate.class);
		registerHandler(i_delete_hate_of_me.class);
		registerHandler(i_fishing_shot.class);
		registerHandler(i_get_agro.class);
		registerHandler(i_get_costume.class);
		registerHandler(i_get_exp.class);
		registerHandler(i_hp_drain.class);
		registerHandler(i_m_attack.class);
		registerHandler(i_my_summon_kill.class);
		registerHandler(i_p_attack.class);
		registerHandler(i_p_hit.class);
		registerHandler(i_pledge_reputation.class);
		registerHandler(i_randomize_hate.class);
		registerHandler(i_refresh_instance.class);
		registerHandler(i_reset_skill_reuse.class);
		registerHandler(i_set_skill.class);
		registerHandler(i_sp.class);
		registerHandler(i_soul_shot.class);
		registerHandler(i_spirit_shot.class);
		registerHandler(i_spoil.class);
		registerHandler(i_summon_agathion.class);
		registerHandler(i_summon_cubic.class);
		registerHandler(i_summon_soul_shot.class);
		registerHandler(i_summon_spirit_shot.class);
		registerHandler(i_target_cancel.class);
		registerHandler(i_target_me.class);
		registerHandler(i_unsummon_agathion.class);
		registerHandler(i_restore_time_restrict_field.class);


		// Permanent Effects
		registerHandler(p_attack_trait.class);
		registerHandler(p_block_buff_slot.class);
		registerHandler(p_block_chat.class);
		registerHandler(p_block_debuff.class);
		registerHandler(p_block_escape.class);
		registerHandler(p_block_move.class);
		registerHandler(p_block_party.class);
		registerHandler(p_block_target.class);
		registerHandler(p_critical_damage.class);
		registerHandler(p_defence_trait.class);
		registerHandler(p_get_item_by_exp.class);
		registerHandler(p_heal_effect.class);
		registerHandler(p_magic_critical_dmg.class);
		registerHandler(p_max_cp.class);
		registerHandler(p_max_hp.class);
		registerHandler(p_max_mp.class);
		registerHandler(p_passive.class);
		registerHandler(p_preserve_abnormal.class);
		registerHandler(p_raid_berserk.class);
		registerHandler(p_skill_critical_damage.class);
		registerHandler(p_target_me.class);
		registerHandler(p_violet_boy.class);

		// Tick Effects
		registerHandler(t_hp.class);
		registerHandler(t_hp_magic.class);

		registerHandler(damage_block_count.class);
	}

	public void registerHandler(Class<? extends EffectHandler> handlerClass)
	{
		String name = EffectHandler.getName(handlerClass);
		if(_handlerConstructors.containsKey(name))
		{
			warn("EffectHandlerHolder: Dublicate handler registered! Handler: CLASS[" + handlerClass.getSimpleName() + "], NAME[" + name + "]");
			return;
		}

		try
		{
			_handlerConstructors.put(name, handlerClass.getConstructor(new Class<?>[] { EffectTemplate.class }));
		}
		catch(Exception e)
		{
			error("EffectHandlerHolder: Error while loading handler: " + e, e);
		}
	}

	public EffectHandler makeHandler(String handlerName, EffectTemplate template)
	{
		if(StringUtils.isEmpty(handlerName))
			return new EffectHandler(template);

		Constructor<? extends EffectHandler> constructor = _handlerConstructors.get(handlerName.toLowerCase());
		if(constructor == null)
		{
			warn("EffectHandlerHolder: Not found handler: " + handlerName);
			return new EffectHandler(template);
		}

		try
		{
			return (EffectHandler) constructor.newInstance(template);
		}
		catch(Exception e)
		{
			error("EffectHandlerHolder: Error while making handler: " + e, e);
			return new EffectHandler(template);
		}
	}

	@Override
	public int size()
	{
		return _handlerConstructors.size();
	}

	@Override
	public void clear()
	{
		_handlerConstructors.clear();
	}
}
