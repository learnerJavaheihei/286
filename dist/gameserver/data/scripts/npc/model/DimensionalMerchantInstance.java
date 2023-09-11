package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.DimensionalMerchantUtils;

public class DimensionalMerchantInstance extends MerchantInstance {
	public DimensionalMerchantInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public String getHtmlDir(String filename, Player player) {
		return DimensionalMerchantUtils.DM_HTML_FILE_PATH;
	}

	@Override
	public String getHtmlFilename(int val, Player player) {
		String filename;
		if (val == 0 || val == 1)
			filename = "e_premium_manager001.htm";
		else if (val <= 9)
			filename = "e_premium_manager00" + val + ".htm";
		else
			filename = "e_premium_manager0" + val + ".htm";
		return filename;
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state) {
		if (DimensionalMerchantUtils.onMenuSelect(this, player, ask, reply, state))
			return;
		super.onMenuSelect(player, ask, reply, state);
	}
}
