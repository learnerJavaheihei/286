package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesMulCollectHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Costume;
import l2s.gameserver.model.actor.instances.player.CostumeList;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.List;
import java.util.Optional;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeList extends L2GameClientPacket {
	private int size;

	@Override
	protected boolean readImpl() {
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

		activeChar.sendPacket(new ExSendCostumeList(activeChar));
		if (activeChar.getVar("costumes_suit_counter") == null) {
			int suitCounter = 0;
			List<CostumesMulCollectHolder.CostumesMulCollect> costumesMulCollects = null;
			CostumeList costumeList = activeChar.getCostumeList();
			if (costumeList!=null) {
				costumesMulCollects = CostumesMulCollectHolder.getInstance().getCostumesMulCollects();
				List<CostumesMulCollectHolder.CostumesSuitList> costumesSuitLists = CostumesMulCollectHolder.getInstance().getCostumesSuitLists();

				mainLoop:
				for (CostumesMulCollectHolder.CostumesSuitList costumesSuitList : costumesSuitLists) {
					for (Integer l : costumesSuitList.getCostumeId()) {
						if (!costumeList.contains(l)) {
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
					activeChar.sendSkillList();
					activeChar.setVar("costumes_suit_counter",suitCounter);
				}
			}
		}
	}
}
