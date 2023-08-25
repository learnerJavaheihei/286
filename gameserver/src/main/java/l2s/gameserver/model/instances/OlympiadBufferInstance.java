package l2s.gameserver.model.instances;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.Config;

/**
 * @author Bonux
**/
public class OlympiadBufferInstance extends NpcInstance
{
	private TIntHashSet buffs = new TIntHashSet();

	public OlympiadBufferInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -301)
		{
			int buffId = (int) (reply) - 1;
			if(buffId < 0 || buffId >= Olympiad.BUFFS_LIST.length)
				return;

			int[] buff = Olympiad.BUFFS_LIST[buffId];
			int id = buff[0];
			int lvl = buff[1];

			Skill skill = SkillHolder.getInstance().getSkill(id, lvl);
			Set<Creature> targets = new HashSet<Creature>();
			targets.add(player);
			if(!skill.isNotBroadcastable())
				broadcastPacket(new MagicSkillUse(this, player, id, lvl, 0, 0));
			callSkill(player, SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill), targets, true, false);
			buffs.add(id);

			showChatWindow(player, 0, false);
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return Olympiad.OLYMPIAD_HTML_PATH;
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			if(buffs.size() > 4)
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_master003.htm", firstTalk, replace);
			else if(buffs.size() > 0)
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_master002.htm", firstTalk, replace);
			else
				showChatWindow(player, Olympiad.OLYMPIAD_HTML_PATH + "olympiad_master001.htm", firstTalk, replace);
		}
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		if (Config.OLYMPIAD_CANATTACK_BUFFER)
			return true;

		return false;
	}
}