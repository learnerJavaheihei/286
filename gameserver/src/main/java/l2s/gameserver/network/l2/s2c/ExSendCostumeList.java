package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.CostumesMulCollectHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExSendCostumeList extends L2GameServerPacket {
	private final Player player;
	private final CostumeList costumeList;

	public ExSendCostumeList(Player player) {
		this.player = player;
		costumeList = player.getCostumeList();
		checkSuitSkill(player);
	}
	
	public void checkSuitSkill(Player activeChar){
		List<CostumesMulCollectHolder.CostumesMulCollect> costumesMulCollects = null;
		costumesMulCollects = CostumesMulCollectHolder.getInstance().getCostumesMulCollects();
		// 先删掉技能
		int costumesSuitCounter = activeChar.getVarInt("costumes_suit_counter", 0);
		if (costumesSuitCounter > 0) {
			Optional<CostumesMulCollectHolder.CostumesMulCollect> mulCollect = costumesMulCollects.stream().filter(o -> o.getMulCollectId() == costumesSuitCounter).findFirst();
			if (mulCollect.isPresent()) {
				CostumesMulCollectHolder.CostumesMulCollect costumesMulCollect = mulCollect.get();
				costumesMulCollect.getSkills().forEach(o->activeChar.removeSkillById(o.getId()));
			}
		}
		// 重新计算套装数量
		int suitCounter = 0;

		if (costumeList!=null) {
			// 排除数量为 0 的 costume
			List<Integer> temp = new ArrayList<>();
			for (Costume costume : costumeList) {
				if (costume.getCount()>0) {
					temp.add(costume.getId());
				}
			}
			List<CostumesMulCollectHolder.CostumesSuitList> costumesSuitLists = CostumesMulCollectHolder.getInstance().getCostumesSuitLists();
			mainLoop:
			for (CostumesMulCollectHolder.CostumesSuitList costumesSuitList : costumesSuitLists) {
				for (Integer l : costumesSuitList.getCostumeId()) {
					if (!temp.contains(l)) {
						continue mainLoop;
					}
				}
				suitCounter++;
			}
		}
		if (suitCounter > 0 && costumesMulCollects!=null) {
			final int aSuitCounter = suitCounter;
			Optional<CostumesMulCollectHolder.CostumesMulCollect> mulCollect = costumesMulCollects.stream().filter(o -> o.getMulCollectId() == aSuitCounter).findFirst();
			if (mulCollect.isPresent()) {
				CostumesMulCollectHolder.CostumesMulCollect costumesMulCollect = mulCollect.get();
				for (Skill skill : costumesMulCollect.getSkills()) {
					activeChar.addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE,skill),true);
				}

				activeChar.setVar("costumes_suit_counter",suitCounter);
			}
		}
		activeChar.sendSkillList();
	}

	@Override
	protected void writeImpl() {
		writeD(costumeList.size()); //CostumeListSize
		for (Costume costume : costumeList) {
			writeD(costume.getId()); //CostumeID
			writeQ(costume.getCount()); //Amount
			writeC(costume.isFlag(Costume.IS_LOCKED)); //LockState 0 - Unlocked, 1 - Locked
			writeC(costume.isFlag(Costume.IS_NEW)); //ChangedType 0 - Normal, 1 - New,
		}
	}
}
