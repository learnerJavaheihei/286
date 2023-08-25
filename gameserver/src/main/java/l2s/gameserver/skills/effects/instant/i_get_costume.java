package l2s.gameserver.skills.effects.instant;

import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExCostumeUseItem;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;
import l2s.gameserver.templates.CostumeTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public final class i_get_costume extends i_abstract_effect {
	private final List<CostumeTemplate> costumeTemplates = new ArrayList<>();

	public i_get_costume(EffectTemplate template) {
		super(template);

		int[] ids = StringArrayUtils.stringToIntArray(template.getParams().getString("id", String.valueOf((int) template.getValue())), "[\\s,;]+");
		for (int id : ids) {
			CostumeTemplate costume = CostumesHolder.getInstance().getCostume(id);
			if (costume != null)
				costumeTemplates.add(costume);
		}

		int[] grades = StringArrayUtils.stringToIntArray(template.getParams().getString("grade", ""), "[\\s,;]+");
		for (int grade : grades) {
			costumeTemplates.addAll(CostumesHolder.getInstance().getCostumesByGrade(grade));
		}
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected) {
		return !Config.EX_COSTUME_DISABLE && effected.isPlayer() && !costumeTemplates.isEmpty();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected) {
		Player player = effected.getPlayer();
		if (player == null)
			return;

		int locationId = player.getLocationId();
		Set<CostumeTemplate> availableCostumes = new HashSet<>();
		for (CostumeTemplate costumeTemplate : costumeTemplates) {
			if (costumeTemplate.getLocationId() != -1 && costumeTemplate.getLocationId() != locationId)
				continue;

			availableCostumes.add(costumeTemplate);
		}

		if (availableCostumes.isEmpty()) {
			player.sendPacket(new ExCostumeUseItem(false, 0)); // TODO: Нужен ли он здесь?
			return;
		}

		CostumeTemplate costumeTemplate = Rnd.get(new ArrayList<>(availableCostumes));
		player.getCostumeList().add(costumeTemplate);
		player.sendPacket(new ExCostumeUseItem(true, costumeTemplate.getId())); // TODO: Нужен ли он здесь?
		player.sendPacket(new ExSendCostumeList(player));
	}
}