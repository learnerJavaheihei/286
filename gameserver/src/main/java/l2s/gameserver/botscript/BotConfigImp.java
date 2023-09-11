package l2s.gameserver.botscript;

import l2s.gameserver.core.BotConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BotConfigImp extends BotConfig
{
	private int hpMpShiftPercent = 60;
	private Map<String, Boolean> partyMemberHolder = new HashMap<String, Boolean>();
	private String leaderName = "";
	private MpHealOrder mpHealOrder = MpHealOrder.\u96e8\u9732\u5747\u6cbe;
	/*\u96e8\u9732\u5747\u6cbe 雨露均沾*/
	private int partyHealPercent = 75;
	private int partyHealSize = 3;
	private int partyHealSkillId = 0;
	private int[] autoCubic = new int[3];
	private LootType lootType = LootType.\u6307\u5b9a;
	/*\u6307\u5b9a 指定*/
	private PetTargetChoose petTargetChoose = PetTargetChoose.\u81ea\u4e3b\u9009\u602a;
	/*\u81ea\u4e3b\u9009\u602a 自主选怪*/
	private Map<String, Set<Integer>> buffSets = new HashMap<String, Set<Integer>>(5);
	private Map<String, String> buffConfig = new HashMap<String, String>(7);
	private boolean coverMember = false;
	private transient int MpHealIndex = 0;
	private boolean followAttackWhenChoosed = true;
	private int limitDefense = 0;
	private int limitEvade = 0;
	private int balancePercent = 0;
	private int balanceSize = 0;
	private int evaPercent = 0;

	public int getHpMpShiftPercent()
	{
		return this.hpMpShiftPercent;
	}

	public void setHpMpShiftPercent(int hpMpShiftPercent)
	{
		this.hpMpShiftPercent = hpMpShiftPercent;
	}

	public String getLeaderName()
	{
		return this.leaderName;
	}

	public void setLeaderName(String leaderName)
	{
		this.leaderName = leaderName;
	}

	public Map<String, Boolean> getPartyMemberHolder()
	{
		return this.partyMemberHolder;
	}

	public void setPartyMemberHolder(Map<String, Boolean> partyMemberHolder)
	{
		this.partyMemberHolder = partyMemberHolder;
	}

	public MpHealOrder getMpHealOrder()
	{
		return this.mpHealOrder;
	}

	public void setMpHealOrder(MpHealOrder mpHealOrder)
	{
		this.mpHealOrder = mpHealOrder;
	}

	public int getMpHealIndex()
	{
		return this.MpHealIndex;
	}

	public void setMpHealIndex(int mpHealIndex)
	{
		this.MpHealIndex = mpHealIndex;
	}

	public int getPartyHealSize()
	{
		return this.partyHealSize;
	}

	public void setPartyHealSize(int partyHealSize)
	{
		this.partyHealSize = partyHealSize;
	}

	public int getPartyHealSkillId()
	{
		return this.partyHealSkillId;
	}

	public void setPartyHealSkillId(int partyHealSkillId)
	{
		this.partyHealSkillId = partyHealSkillId;
	}

	public int getPartyHealPercent()
	{
		return this.partyHealPercent;
	}

	public void setPartyHealPercent(int partyHealPercent)
	{
		this.partyHealPercent = partyHealPercent;
	}

	public void setCubic(int index, int skillId)
	{
		this.autoCubic[index] = skillId;
	}

	public int[] getAutoCubic()
	{
		return this.autoCubic;
	}

	public void setAutoCubic(int[] autoCubic)
	{
		this.autoCubic = autoCubic;
	}

	public LootType getLootType()
	{
		return this.lootType;
	}

	public void setLootType(LootType lootType)
	{
		this.lootType = lootType;
	}

	public PetTargetChoose getPetTargetChoose()
	{
		return this.petTargetChoose;
	}

	public void setPetTargetChoose(PetTargetChoose petTargetChoose)
	{
		this.petTargetChoose = petTargetChoose;
	}

	public Map<String, Set<Integer>> getBuffSets()
	{
		return this.buffSets;
	}

	public void setBuffSets(Map<String, Set<Integer>> buffSets)
	{
		this.buffSets = buffSets;
	}

	public Map<String, String> getBuffConfig()
	{
		return this.buffConfig;
	}

	public void setBuffConfig(Map<String, String> buffConfig)
	{
		this.buffConfig = buffConfig;
	}

	public boolean isCoverMember()
	{
		return this.coverMember;
	}

	public void setCoverMember(boolean coverMember)
	{
		this.coverMember = coverMember;
	}

	public boolean isFollowAttackWhenChoosed()
	{
		return this.followAttackWhenChoosed;
	}

	public void setFollowAttackWhenChoosed(boolean followAttackWhenChoosed)
	{
		this.followAttackWhenChoosed = followAttackWhenChoosed;
	}

	public int getLimitEvade()
	{
		return this.limitEvade;
	}

	public void setLimitEvade(int limitEvade)
	{
		this.limitEvade = limitEvade;
	}

	public int getLimitDefense()
	{
		return this.limitDefense;
	}

	public void setLimitDefense(int limitDefense)
	{
		this.limitDefense = limitDefense;
	}

	public int getBalancePercent()
	{
		return this.balancePercent;
	}

	public void setBalancePercent(int balancePercent)
	{
		this.balancePercent = balancePercent;
	}

	public int getBalanceSize()
	{
		return this.balanceSize;
	}

	public void setBalanceSize(int balanceSize)
	{
		this.balanceSize = balanceSize;
	}

	public int getEvaPercent()
	{
		return this.evaPercent;
	}

	public void setEvaPercent(int evaPercent)
	{
		this.evaPercent = evaPercent;
	}
}