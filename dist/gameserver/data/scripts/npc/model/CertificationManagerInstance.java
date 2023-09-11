package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
 **/
public class CertificationManagerInstance extends MerchantInstance {
	private static final long serialVersionUID = 1L;

	public CertificationManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onSkillLearnBypass(Player player) {
		showAcquireList(AcquireType.CERTIFICATION, player);
	}
}