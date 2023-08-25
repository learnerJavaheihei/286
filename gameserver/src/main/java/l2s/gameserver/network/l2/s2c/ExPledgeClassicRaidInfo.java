package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.pledge.Clan;

/**
 * @author Bonux
**/
public class ExPledgeClassicRaidInfo extends L2GameServerPacket
{
	private final int _lastRaidPhase;
	private final List<Skill> _skills;

	public ExPledgeClassicRaidInfo(Player player)
	{
		Clan clan = player.getClan();
		_lastRaidPhase = clan == null ? 0 : clan.getArenaStage();
		_skills = new ArrayList<Skill>(5);
		_skills.add(SkillHolder.getInstance().getSkill(55887, 1));
		_skills.add(SkillHolder.getInstance().getSkill(55887, 2));
		_skills.add(SkillHolder.getInstance().getSkill(55887, 3));
		_skills.add(SkillHolder.getInstance().getSkill(55887, 4));
		_skills.add(SkillHolder.getInstance().getSkill(55887, 5));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_lastRaidPhase);
		writeD(_skills.size());
		for(Skill skill : _skills)
		{
			writeD(skill.getId());
			writeD(skill.getLevel());
		}
	}
}