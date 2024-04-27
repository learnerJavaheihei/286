package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.CostumeTemplate;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeExtract extends L2GameClientPacket {
	private int unk1, costumeId, unk3, unk4;

	@Override
	protected boolean readImpl() {
		unk1 = readH();
		costumeId = readD();
		unk3 = readD();
		unk4 = readD();
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

		if (activeChar.isGM())
			activeChar.sendMessage(getClass().getSimpleName() + ": unk1=" + unk1 + ", costumeId=" + costumeId + ", unk3=" + unk3 + ", unk4=" + unk4);

		if (!activeChar.getCostumeList().extractCostume(costumeId)) {
			activeChar.sendActionFailed();
			return;
		}
		CostumeTemplate costumeTemplate = CostumesHolder.getInstance().getCostume(costumeId);
		if (costumeTemplate!=null)
			activeChar.removeSkill(costumeTemplate.getSkillEntry().getId(),true);

		activeChar.sendPacket(new ExSendCostumeList(activeChar));
	}
}
