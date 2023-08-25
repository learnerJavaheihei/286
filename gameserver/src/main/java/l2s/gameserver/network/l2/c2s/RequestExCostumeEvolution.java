package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeEvolution extends L2GameClientPacket {
	private int unk1, costumeId, unk3, unk4;
	private List<int[]> costumesConsume;

	@Override
	protected boolean readImpl() {
		unk1 = readD();
		costumeId = readD();
		unk3 = readD();
		unk4 = readD();
		int count = readD();
		costumesConsume = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int[] data = new int[3];
			data[0] = readD();
			data[1] = readD();
			data[2] = readD();
			costumesConsume.add(data);
		}
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
			activeChar.sendMessage(getClass().getSimpleName() + ": unk1=" + unk1 + ", costumeId=" + costumeId + ", unk3=" + unk3 + ", unk4=" + unk4 + ", costumesConsume.size()=" + costumesConsume.size());

		if (!activeChar.getCostumeList().evolutionCostume(costumeId, costumesConsume)) {
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendPacket(new ExSendCostumeList(activeChar));
	}
}
