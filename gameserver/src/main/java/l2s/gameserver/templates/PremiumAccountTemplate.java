package l2s.gameserver.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.utils.Language;

import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;
/**
 * @author Bonux
**/
public class PremiumAccountTemplate extends StatTemplate
{
	public static final PremiumAccountTemplate DEFAULT_ACCOUNT_TEMPLATE = new PremiumAccountTemplate(0, StatsSet.EMPTY);

	private final int _type;

	private final int _nameColor;
	private final int _titleColor;

	private final double _expRate;
	private final double _spRate;
	private final double _adenaRate;
	private final double _dropRate;
	private final double _spoilRate;
	private final double _questDropRate;
	private final double _questRewardRate;
	private final double _fishingExpRate;
	private final double _fishingSpRate;

	private final double _dropChanceModifier;
	private final double _dropCountModifier;
	private final double _spoilChanceModifier;
	private final double _spoilCountModifier;

	private final double _enchantChanceBonus;
	private final double _craftChanceBonus;

	private final int _worldChatMinLevel;

	private final Map<Language, String> _names = new HashMap<Language, String>();
	private final List<ItemData> _giveItemsOnStart = new ArrayList<ItemData>();
	private final List<ItemData> _takeItemsOnEnd = new ArrayList<ItemData>();
	private final IntObjectMap<List<ItemData>> _fees = new TreeIntObjectMap<List<ItemData>>();
	private final List<RewardItemData> _rewards = new ArrayList<RewardItemData>();

	private SkillEntry[] _skills = SkillEntry.EMPTY_ARRAY;

	public PremiumAccountTemplate(int type, StatsSet set)
	{
		_type = type;

		String color = set.getString("name_color", null);
		_nameColor = StringUtils.isEmpty(color) ? -1 : Integer.decode("0x" + color);

		color = set.getString("title_color", null);
		_titleColor = StringUtils.isEmpty(color) ? -1 : Integer.decode("0x" + color);

		_expRate = set.getDouble("exp_rate", 1.);
		_spRate = set.getDouble("sp_rate", 1.);
		_adenaRate = set.getDouble("adena_rate", 1.);
		_dropRate = set.getDouble("drop_rate", 1.);
		_spoilRate = set.getDouble("spoil_rate", 1.);
		_questDropRate = set.getDouble("quest_drop_rate", 1.);
		_questRewardRate = set.getDouble("quest_reward_rate", 1.);
		_fishingExpRate = set.getDouble("fishing_exp_rate", 1.);
		_fishingSpRate = set.getDouble("fishing_sp_rate", 1.);

		_dropChanceModifier = set.getDouble("drop_chance_modifier", 1.);
		_dropCountModifier = set.getDouble("drop_count_modifier", 1.);
		_spoilChanceModifier = set.getDouble("spoil_chance_modifier", 1.);
		_spoilCountModifier = set.getDouble("spoil_count_modifier", 1.);

		_enchantChanceBonus = set.getDouble("enchant_chance_bonus", 0.);
		_craftChanceBonus = set.getDouble("craft_chance_bonus", 0.);

		_worldChatMinLevel = set.getInteger("world_chat_min_level", -1);
	}

	public int getType()
	{
		return _type;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public int getTitleColor()
	{
		return _titleColor;
	}

	public double getExpRate()
	{
		return _expRate;
	}

	public double getSpRate()
	{
		return _spRate;
	}

	public double getAdenaRate()
	{
		return _adenaRate;
	}

	public double getDropRate()
	{
		return _dropRate;
	}

	public double getSpoilRate()
	{
		return _spoilRate;
	}

	public double getQuestDropRate()
	{
		return _questDropRate;
	}

	public double getQuestRewardRate()
	{
		return _questRewardRate;
	}

	public double getFishingExpRate()
	{
		return _fishingExpRate;
	}

	public double getFishingSpRate()
	{
		return _fishingSpRate;
	}

	public double getDropChanceModifier()
	{
		return _dropChanceModifier;
	}

	public double getDropCountModifier()
	{
		return _dropCountModifier;
	}

	public double getSpoilChanceModifier()
	{
		return _spoilChanceModifier;
	}

	public double getSpoilCountModifier()
	{
		return _spoilCountModifier;
	}

	public double getEnchantChanceBonus()
	{
		return _enchantChanceBonus;
	}

	public double getCraftChanceBonus()
	{
		return _craftChanceBonus;
	}

	public int getWorldChatMinLevel()
	{
		return _worldChatMinLevel;
	}

	public void addName(Language lang, String name)
	{
		_names.put(lang, name);
	}

	public String getName(Language lang)
	{
		String name = _names.get(lang);
		if(name == null)
		{
			Language secondLang = lang;
			do
			{
				if(secondLang == secondLang.getSecondLanguage())
					break;

				if(!Config.AVAILABLE_LANGUAGES.contains(secondLang))
					break;

				secondLang = secondLang.getSecondLanguage();
				name = _names.get(secondLang);
			}
			while(name == null);

			if(name == null)
			{
				for(Language l : Language.VALUES)
				{
					if(!Config.AVAILABLE_LANGUAGES.contains(l))
						continue;

					if((name = _names.get(l)) != null)
						break;
				}
			}
		}
		if(name == null)
			return "Type: " + getType();
		return name;
	}

	public void addGiveItemOnStart(ItemData item)
	{
		_giveItemsOnStart.add(item);
	}

	public List<ItemData> getGiveItemsOnStart()
	{
		return _giveItemsOnStart;
	}

	public void addTakeItemOnEnd(ItemData item)
	{
		_takeItemsOnEnd.add(item);
	}

	public List<ItemData> getTakeItemsOnEnd()
	{
		return _takeItemsOnEnd;
	}

	public void addFee(int delay, ItemData item)
	{
		List<ItemData> items = _fees.get(delay);
		if(items == null)
		{
			items = new ArrayList<ItemData>();
			_fees.put(delay, items);
		}
		items.add(item);
	}

	public int[] getFeeDelays()
	{
		return _fees.keySet().toArray();
	}

	public List<ItemData> getFeeItems(int delay)
	{
		List<ItemData> items = _fees.get(delay);
		if(items == null)
			return null;
		return items;
	}

	public void attachSkill(SkillEntry skill)
	{
		_skills = ArrayUtils.add(_skills, skill);
	}

	public SkillEntry[] getAttachedSkills()
	{
		return _skills;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	public void addReward(RewardItemData reward)
	{
		_rewards.add(reward);
	}

	public List<RewardItemData> getRewards()
	{
		return _rewards;
	}

	public void onAdd(Player player)
	{
		double currentHpRatio = player.getCurrentHpRatio();
		double currentMpRatio = player.getCurrentMpRatio();
		double currentCpRatio = player.getCurrentCpRatio();

		player.addTriggers(this);
		player.getStat().addFuncs(getStatFuncs());

		SkillEntry[] skills = getAttachedSkills();
		for(SkillEntry skill : skills)
			player.addSkill(skill);

		if(skills.length > 0)
			player.sendSkillList();

		player.setCurrentHp(player.getMaxHp() * currentHpRatio, false);
		player.setCurrentMp(player.getMaxMp() * currentMpRatio);
		player.setCurrentCp(player.getMaxCp() * currentCpRatio);

		player.updateStats();
	}

	public void onRemove(Player player)
	{
		double currentHpRatio = player.getCurrentHpRatio();
		double currentMpRatio = player.getCurrentMpRatio();
		double currentCpRatio = player.getCurrentCpRatio();

		player.getStat().removeFuncsByOwner(this);
		player.removeTriggers(this);

		SkillEntry[] skills = getAttachedSkills();
		for(SkillEntry skill : skills)
			player.removeSkill(skill);

		if(skills.length > 0)
			player.sendSkillList();

		player.setCurrentHp(player.getMaxHp() * currentHpRatio, false);
		player.setCurrentMp(player.getMaxMp() * currentMpRatio);
		player.setCurrentCp(player.getMaxCp() * currentCpRatio);

		player.updateStats();
	}
}