package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeCollectionSkillActive extends L2GameClientPacket {
	private int size;
	private int costume_id;
	static String[] costumeCollection =  new String[]{
			"1;59329",
			"2;59330",
			"3;59331",
			"4;59332",
			"5;59333",
			"6;59334",
			"7;59335",
			"8;59336;59341",
			"9;59337;59342",
			"10;59338;59343",
			"11;59339",
			"12;59340;59344"
	};
	static List<String> costumeSkillIds = new ArrayList<String>();
	public RequestExCostumeCollectionSkillActive(){
		load();
	}
	private static void load() {
		for (String line : costumeCollection) {
			String[] split = line.split(";");
			if (split.length==3) {
				costumeSkillIds.add(split[1]+"-"+split[2]);
				continue;
			}
			costumeSkillIds.add(split[1]);
		}
	}

	@Override
	protected boolean readImpl() {
		costume_id = readD();
		size = _buf.remaining();
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Config.EX_COSTUME_DISABLE) {
			activeChar.sendActionFailed();
			return;
		}

		int skillId = 0;
		int skillId_additional = 0;
		for (String line : costumeCollection) {
			String[] split = line.split(";");
			if (costume_id == Integer.parseInt(split[0])) {
				skillId = Integer.parseInt(split[1]);
				if (split.length == 3) {
					skillId_additional = Integer.parseInt(split[2]);
				}
				break;
			}
		}
		SkillEntry knownSkill = activeChar.getKnownSkill(skillId);
		if (knownSkill != null) {
			return;
		}
		// 刪除所有變身技能
		List<SkillEntry> costumeSkillList = new ArrayList<SkillEntry>();
		for (String costumeSkillId : costumeSkillIds) {
			if (costumeSkillId.contains("-")) {
				String[] linked = costumeSkillId.split("-");
				int getSkillId1 = Integer.parseInt(linked[0]);
				int getSkillId2 = Integer.parseInt(linked[1]);
				int [] skills = new int[]{getSkillId1, getSkillId2};
				islearned(activeChar, costumeSkillList, skills);
			}else {
				int getSkillId = Integer.parseInt(costumeSkillId);
				int [] skills = new int[]{getSkillId};
				islearned(activeChar, costumeSkillList, skills);
			}
		}
		if (costumeSkillList.size()>0) {
			costumeSkillList.forEach(skillEntry -> activeChar.removeSkill(skillEntry,true));
		}

		SkillEntry newSkill=null;
		newSkill = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, 1);
		activeChar.addSkill(newSkill,true);
		if (newSkill!=null)
			activeChar.sendMessage("激活變身套裝效果["+newSkill.getName(activeChar)+"]");
		SkillEntry newSkill_additional=null;
		if (skillId_additional>0) {
			newSkill_additional = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId_additional, 1);
			activeChar.addSkill(newSkill_additional, true);
		}

		activeChar.sendSkillList();
	}

	private void islearned(Player activeChar, List<SkillEntry> costumeSkillList, int[] skills) {
		for (int skill : skills) {
			SkillEntry costumeSkill = activeChar.getKnownSkill(skill);
			if (costumeSkill != null) {
				costumeSkillList.add(costumeSkill);
			}
		}
	}
}
