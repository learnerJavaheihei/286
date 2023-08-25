package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.*;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.utils.PositionUtils.TargetDirection;
import l2s.gameserver.utils.SkillUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @date 13:48/10.01.2011
 */
public abstract class StatParser<H extends AbstractHolder> extends AbstractParser<H>
{
	protected StatParser(H holder)
	{
		super(holder);
	}

	protected Condition parseFirstCond(Element sub, int... arg)
	{
		List<Element> e = sub.elements();
		if(e.isEmpty())
			return null;
		Element element = e.get(0);

		return parseCond(element, arg);
	}

	protected Condition parseCond(Element element, int... arg)
	{
		String name = element.getName();
		if(name.equalsIgnoreCase("and"))
			return parseLogicAnd(element, arg);
		else if(name.equalsIgnoreCase("or"))
			return parseLogicOr(element, arg);
		else if(name.equalsIgnoreCase("not"))
			return parseLogicNot(element, arg);
		else if(name.equalsIgnoreCase("target"))
			return parseTargetCondition(element, arg);
		else if(name.equalsIgnoreCase("player"))
			return parsePlayerCondition(element, arg);
		else if(name.equalsIgnoreCase("using"))
			return parseUsingCondition(element, arg);
		else if(name.equalsIgnoreCase("zone"))
			return parseZoneCondition(element, arg);
		if(name.equalsIgnoreCase("has"))
			return parseHasCondition(element, arg);
		if(name.equalsIgnoreCase("game"))
			return parseGameCondition(element, arg);

		return null;
	}

	protected Condition parseLogicAnd(Element n, int... arg)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement, arg));
		}

		if(cond._conditions == null || cond._conditions.length == 0)
			error("Empty <and> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicOr(Element n, int... arg)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement, arg));
		}

		if(cond._conditions == null || cond._conditions.length == 0)
			error("Empty <or> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicNot(Element n, int... arg)
	{
		for(Object element : n.elements())
			return new ConditionLogicNot(parseCond((Element) element, arg));
		error("Empty <not> condition in " + getCurrentFileName());
		return null;
	}

	protected Condition parseTargetCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("is_pet_feed"))
				cond = joinAnd(cond, new ConditionTargetPetFeed(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("type"))
				cond = joinAnd(cond, new ConditionTargetType(value));
			else if(name.equalsIgnoreCase("aggro"))
				cond = joinAnd(cond, new ConditionTargetAggro(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("mobId"))
				cond = joinAnd(cond, new ConditionTargetMobId(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("race"))
				cond = joinAnd(cond, new ConditionTargetRace(value));
			else if(name.equalsIgnoreCase("npc_class"))
				cond = joinAnd(cond, new ConditionTargetNpcClass(value));
			else if(name.equalsIgnoreCase("playerRace"))
				cond = joinAnd(cond, new ConditionTargetPlayerRace(value));
			else if(name.equalsIgnoreCase("forbiddenClassIds"))
				cond = joinAnd(cond, new ConditionTargetForbiddenClassId(value.split(";")));
			else if(name.equalsIgnoreCase("playerSameClan"))
				cond = joinAnd(cond, new ConditionTargetClan(value));
			else if(name.equalsIgnoreCase("castledoor"))
				cond = joinAnd(cond, new ConditionTargetCastleDoor(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("direction"))
				cond = joinAnd(cond, new ConditionTargetDirection(TargetDirection.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("percentHP"))
				cond = joinAnd(cond, new ConditionTargetPercentHp(Double.parseDouble(value)));
			else if(name.equalsIgnoreCase("percentMP"))
				cond = joinAnd(cond, new ConditionTargetPercentMp(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("percentCP"))
				cond = joinAnd(cond, new ConditionTargetPercentCp(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("hasBuffId"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuffId(id, level));
			}
			else if(name.equalsIgnoreCase("has_abnormal_type"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st.nextToken().trim().toUpperCase());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuff(at, level));
			}
			else if(name.equalsIgnoreCase("hasForbiddenSkill"))
				cond = joinAnd(cond, new ConditionTargetHasForbiddenSkill(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("min_distance"))
				cond = joinAnd(cond, new ConditionTargetMinDistance(Integer.parseInt(value)));
		}

		return cond;
	}

	protected Condition parseZoneCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("type"))
				cond = joinAnd(cond, new ConditionZoneType(value));
			else if(name.equalsIgnoreCase("name"))
				cond = joinAnd(cond, new ConditionZoneName(value));
		}

		return cond;
	}

	protected Condition parseHasCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("skill"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionHasSkill(id, level));
			}
		}

		return cond;
	}

	protected Condition parseGameCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("night"))
				cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, Boolean.valueOf(value)));
		}

		return cond;
	}

	protected Condition parsePlayerCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("residence"))
			{
				String[] st = value.split(";");
				cond = joinAnd(cond, new ConditionPlayerResidence(Integer.parseInt(st[1]), ResidenceType.valueOf(st[0].toUpperCase())));
			}
			else if(name.equalsIgnoreCase("classId"))
				cond = joinAnd(cond, new ConditionPlayerClassId(value.split(",")));
			else if(name.equalsIgnoreCase("olympiad"))
				cond = joinAnd(cond, new ConditionPlayerOlympiad(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("instance_zone"))
				cond = joinAnd(cond, new ConditionPlayerInstanceZone(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("is_clan_leader"))
				cond = joinAnd(cond, new ConditionPlayerIsClanLeader(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("is_hero"))
				cond = joinAnd(cond, new ConditionPlayerIsHero(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("is_chaotic"))
				cond = joinAnd(cond, new ConditionPlayerIsChaotic(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("race"))
				cond = joinAnd(cond, new ConditionPlayerRace(value));
			else if(name.equalsIgnoreCase("sex"))
				cond = joinAnd(cond, new ConditionPlayerSex(Sex.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("castle_type"))
				cond = joinAnd(cond, new ConditionPlayerCastleType(ResidenceSide.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("max_level"))
				cond = joinAnd(cond, new ConditionPlayerMaxLevel(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("min_clan_level"))
				cond = joinAnd(cond, new ConditionPlayerMinClanLevel(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("avail_max_sp"))
				cond = joinAnd(cond, new ConditionPlayerMaxSP(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("minLevel"))
				cond = joinAnd(cond, new ConditionPlayerMinLevel(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("class_type"))
				cond = joinAnd(cond, new ConditionPlayerClassType(SubClassType.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("isFlagged"))
				cond = joinAnd(cond, new ConditionPlayerFlagged(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("damage"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				double min = Double.parseDouble(st.nextToken().trim());
				double max = Integer.MAX_VALUE;
				if(st.hasMoreTokens())
					max = parseTableNumber(st.nextToken().trim(), arg).doubleValue();
				cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(min, max));
			}
			else if(name.equalsIgnoreCase("quest_state"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int questId = parseTableNumber(st.nextToken().trim(), arg).intValue();
				int condId = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerQuestState(questId, condId));
			}
			else if(name.equalsIgnoreCase("min_pledge_rank"))
				cond = joinAnd(cond, new ConditionPlayerMinPledgeRank(PledgeRank.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("summon_siege_golem"))
				cond = joinAnd(cond, new ConditionPlayerSummonSiegeGolem());
			else if(name.equalsIgnoreCase("maxPK"))
				cond = joinAnd(cond, new ConditionPlayerMaxPK(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("resting"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("moving"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.MOVING, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("running"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("standing"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("flying"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("flyingTransform"))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING_TRANSFORM, Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("percentHP"))
				cond = joinAnd(cond, new ConditionPlayerPercentHp(Double.parseDouble(value)));
			else if(name.equalsIgnoreCase("percentMP"))
				cond = joinAnd(cond, new ConditionPlayerPercentMp(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("percentCP"))
				cond = joinAnd(cond, new ConditionPlayerPercentCp(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("clan_leader_online"))
				cond = joinAnd(cond, new ConditionPlayerClanLeaderOnline(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("riding"))
				cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.valueOf(value.toUpperCase())));
			else if(name.equalsIgnoreCase("hasBuffId"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasBuffId(id, level));
			}
			else if(name.equalsIgnoreCase("has_abnormal_type"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st.nextToken().trim().toUpperCase());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasBuff(at, level));
			}
			else if(name.equalsIgnoreCase("has_summon_id"))
				cond = joinAnd(cond, new ConditionPlayerHasSummonId(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("can_transform"))
				cond = joinAnd(cond, new ConditionPlayerCanTransform(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("can_untransform"))
				cond = joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.valueOf(value)));
			else if(name.equalsIgnoreCase("agathion"))
				cond = joinAnd(cond, new ConditionPlayerAgathion(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("can_learn_skill"))
			{
				StringTokenizer st = new StringTokenizer(value, "-");
				int id = parseTableNumber(st.nextToken().trim(), arg).intValue();
				int level = 1;
				if(st.hasMoreTokens())
					level = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerCanLearnSkill(id, level));
			}
			else if(name.toLowerCase().startsWith("can_get_costume")) {
				int[] values = StringArrayUtils.stringToIntArray(value, "[\\s,;]+");
				boolean grade = name.equalsIgnoreCase("can_get_costume_grade");
				cond = joinAnd(cond, new ConditionPlayerCanGetCostume(values, grade));
			}
			else if(name.equalsIgnoreCase("min_light_souls"))
			{
				int soulsCount = parseTableNumber(value, arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerSoulsCount(0, soulsCount));
			}
			else if(name.equalsIgnoreCase("min_dark_souls"))
			{
				int soulsCount = parseTableNumber(value, arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerSoulsCount(1, soulsCount));
			}
		}

		return cond;
	}

	protected Condition parseUsingCondition(Element element, int... arg)
	{
		Condition cond = null;
		for(Iterator<Attribute> iterator = element.attributeIterator(); iterator.hasNext();)
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseTableString(attribute.getValue(), arg);
			if(name.equalsIgnoreCase("slotitem"))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if(st.hasMoreTokens())
					enchant = parseTableNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if(name.equalsIgnoreCase("kind") || name.equalsIgnoreCase("weapon"))
			{
				long mask = 0;
				StringTokenizer st = new StringTokenizer(value, ",");
				tokens: while(st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for(WeaponType wt : WeaponType.VALUES)
					{
						if(wt.toString().equalsIgnoreCase(item))
						{
							mask |= wt.mask();
							continue tokens;
						}
					}

					for(ArmorType at : ArmorType.VALUES)
					{
						if(at.toString().equalsIgnoreCase(item))
						{
							mask |= at.mask();
							continue tokens;
						}
					}

					error("Invalid item kind: \"" + item + "\" in " + getCurrentFileName());
				}
				if(mask != 0)
					cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if(name.equalsIgnoreCase("skill"))
				cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(value)));
			else if(name.equalsIgnoreCase("armor"))
				cond = joinAnd(cond, new ConditionUsingArmor(ArmorType.valueOf(value.toUpperCase())));
		}
		return cond;
	}

	protected Condition joinAnd(Condition cond, Condition c)
	{
		if(cond == null)
			return c;
		if(cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}

	protected void parseFor(Element forElement, StatTemplate template, int... arg)
	{
		for(Iterator<Element> iterator = forElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			final String elementName = element.getName().toLowerCase();
			if(elementName.equals("add"))
				attachFunc(element, 0x40, template, "Add", arg);
			else if(elementName.equals("set"))
				attachFunc(element, 0x80, template, "Set", arg);
			else if(elementName.equals("sub"))
				attachFunc(element, 0x40, template, "Sub", arg);
			else if(elementName.equals("mul"))
				attachFunc(element, 0x30, template, "Mul", arg);
			else if(elementName.equals("div"))
				attachFunc(element, 0x30, template, "Div", arg);

				// Временный изжоп, до переписывания системы статтов под ПТС.
			else if(elementName.equalsIgnoreCase("stat_effect")) {
				Stats stat = Stats.valueOfXml(parseTableString(element.attributeValue("name")));
				StatsSet params = new StatsSet();
				params.set("mode", StatModifierType.valueOfXml(parseTableString(parseString(element, "type", StatModifierType.DIFF.toString()), arg)));
				params.set("value", parseTableNumber(parseString(element, "value", "0"), arg).doubleValue());
				for(Element setElement : element.elements("set")) {
					params.set(setElement.attributeValue("name"), parseTableString(setElement.attributeValue("value"), arg));
				}
				attachFunc(element, stat, template, "New", params, arg);
			}
			else if(elementName.startsWith("p_"))
			{
				if(elementName.equals("p_attack_trait")) {
					Stats stat = Stats.valueOfXml("attack_trait_" + parseTableString(element.attributeValue("name")));
					attachFunc(element, stat, template, "Add", new StatsSet(), arg);
				}
				else if(elementName.equals("p_defence_trait")) {
					Stats stat = Stats.valueOfXml("defence_trait_" + parseTableString(element.attributeValue("name")));
					attachFunc(element, stat, template, "AddTraitDefence", new StatsSet(), arg);
				}
				else if(elementName.startsWith("p_elemental_"))
				{
					String elementalElementName = parseTableString(element.attributeValue("element"), arg);
					StatsSet params = new StatsSet();
					Stats stat = Stats.valueOfXml(elementName.replaceFirst("^p_", elementalElementName + "_"));
					if(elementName.equals("p_elemental_exp_rate")) {
						params.set("mode", StatModifierType.PER);
						attachFunc(element, stat, template, "New", params, arg);
					}
					else {
						StatModifierType type = StatModifierType.valueOfXml(parseTableString(element.attributeValue("type"), arg));
						params.set("mode", type);
						attachFunc(element, stat, template, "New", params, arg);
					}
				}
				else if(elementName.equals("p_vampiric_attack") || elementName.equals("p_mp_vampiric_attack"))
				{
					Stats stat = elementName.equals("p_vampiric_attack") ? Stats.VAMPIRIC_ATTACK : Stats.MP_VAMPIRIC_ATTACK;
					StatsSet params = StatsSet.simpleStatsSet("chance", parseTableNumber(element.attributeValue("chance"), arg).doubleValue());
					attachFunc(element, stat, template, "Absorb", params, arg);
				}
				else
				{
					StatsSet params = new StatsSet();
					Stats stat = Stats.valueOfXml(elementName.replaceFirst("^p_", ""));
					StatModifierType type = StatModifierType.valueOfXml(parseTableString(element.attributeValue("type"), arg));
					params.set("mode", type);
					attachFunc(element, stat, template, "New", params, arg);
				}
			}
		}
	}

	protected void parseTriggers(Element f, StatTemplate triggerable, int... arg)
	{
		for(Iterator<Element> iterator = f.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			int id = parseTableNumber(element.attributeValue("id"), arg).intValue();
			int level = parseTableNumber(element.attributeValue("level"), arg).intValue();

			if(id <= 0 || level <= 0)
				continue;

			TriggerType t = TriggerType.valueOf(parseTableString(element.attributeValue("type"), arg));
			double chance = element.attributeValue("chance") == null ? 100D : parseTableNumber(element.attributeValue("chance"), arg).doubleValue();
			boolean increasing = element.attributeValue("increasing") != null && parseTableBoolean(element.attributeValue("increasing"));
			int delay = element.attributeValue("delay") != null ? (parseTableNumber(element.attributeValue("delay"), arg).intValue() * 1000) : 0;
			boolean cancel = element.attributeValue("cancel_effects_on_remove") != null && parseTableBoolean(element.attributeValue("cancel_effects_on_remove"));
			String args = element.attributeValue("args") != null ? element.attributeValue("args") : "";

			TriggerInfo trigger = new TriggerInfo(id, level, t, chance, increasing, delay, cancel, args);

			Condition condition = parseFirstCond(element, arg);
			if(condition != null)
				trigger.addCondition(condition);

			triggerable.addTrigger(trigger);
		}
	}

	protected void attachFunc(Element n, Stats stat, StatTemplate template, String name, StatsSet params, int... arg)
	{
		params.set("stat", stat);
		params.set("function", name);
		params.set("condition", parseFirstCond(n, arg));
		String valueStr = n.attributeValue("value");
		if(valueStr != null)
			params.set("value", parseTableNumber(valueStr, arg).doubleValue());
		template.attachFunc(FuncTemplate.makeTemplate(params));
	}

	protected void attachFunc(Element n, int defaultOrder, StatTemplate template, String name, int... arg)
	{
		StatsSet params = new StatsSet();
		Stats stat = Stats.valueOfXml(n.attributeValue("stat"));
		String order = n.attributeValue("order");
		params.set("order", order == null ? defaultOrder : parseTableNumber(order, arg).intValue());
		attachFunc(n, stat, template, name, params, arg);
	}

	private final static Pattern TABLE_PATTERN = Pattern.compile("((?!;|:| |-).*?)((;|:| |-)|$)", Pattern.DOTALL);

	protected final Object parseTableValue(Object object, int... arg)
	{
		if(object == null)
			return null;

		String value = String.valueOf(object);
		if(value.isEmpty())
			return object;

		if(value.contains("#"))
		{
			String temp;
			StringBuilder sb = new StringBuilder();
			Matcher m = TABLE_PATTERN.matcher(value);
			while(m.find())
			{
				temp = m.group(1);
				if(temp == null || temp.isEmpty())
					continue;

				if(temp.charAt(0) == '#')
					sb.append(getTableValue(temp, arg));
				else
					sb.append(temp);

				temp = m.group(2);
				if(temp == null || temp.isEmpty())
					continue;

				sb.append(temp);
			}
			return sb.toString();
		}

		return object;
	}

	protected final String parseTableString(Object object, int... arg)
	{
		object = parseTableValue(object, arg);
		return String.valueOf(object);
	}

	protected final boolean parseTableBoolean(Object object, int... arg)
	{
		return Boolean.parseBoolean(parseTableString(object, arg));
	}

	protected final Number parseTableNumber(String value, int... arg)
	{
		value = parseTableString(value, arg);

		try
		{
			if(value.equalsIgnoreCase("max"))
				return Double.POSITIVE_INFINITY;

			if(value.equalsIgnoreCase("min"))
				return Double.NEGATIVE_INFINITY;

			if(value.indexOf('.') == -1)
			{
				int radix = 10;
				if(value.length() > 2 && value.substring(0, 2).equalsIgnoreCase("0x"))
				{
					value = value.substring(2);
					radix = 16;
				}
				return Integer.valueOf(value, radix);
			}
			return Double.valueOf(value);
		}
		catch(NumberFormatException e)
		{
			warn("Error while parsing number: " + value, e);
			return null;
		}
	}

	protected abstract Object getTableValue(String name, int... arg);
}
