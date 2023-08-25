package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.CostumeTemplate;

import java.util.*;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public final class CostumesHolder extends AbstractHolder {
	private static final CostumesHolder INSTANCE = new CostumesHolder();

	public static CostumesHolder getInstance() {
		return INSTANCE;
	}

	private final Map<Integer, CostumeTemplate> costumesById = new TreeMap<>();
	private final Map<Integer, CostumeTemplate> costumesBySkillId = new HashMap<>();
	private final Map<Integer, List<CostumeTemplate>> costumesByGrade = new HashMap<>();

	public void addCostume(CostumeTemplate costumeTemplate) {
		costumesById.put(costumeTemplate.getId(), costumeTemplate);
		costumesBySkillId.put(costumeTemplate.getSkillEntry().getId(), costumeTemplate);
		costumesByGrade.computeIfAbsent(costumeTemplate.getGrade(), (l) -> new ArrayList<>()).add(costumeTemplate);
	}

	public CostumeTemplate getCostume(int id) {
		return costumesById.get(id);
	}

	public CostumeTemplate getCostumeBySkillId(int skillId) {
		return costumesBySkillId.get(skillId);
	}

	public List<CostumeTemplate> getCostumesByGrade(int grade) {
		List<CostumeTemplate> result = costumesByGrade.get(grade);
		if (result == null)
			return Collections.emptyList();
		return result;
	}

	@Override
	public int size() {
		return costumesById.size();
	}

	@Override
	public void clear() {
		costumesById.clear();
		costumesBySkillId.clear();
		costumesByGrade.clear();
	}
}
