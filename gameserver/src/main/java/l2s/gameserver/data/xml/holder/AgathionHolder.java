package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.agathion.AgathionTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux
 **/
public final class AgathionHolder extends AbstractHolder {
	private static AgathionHolder _instance = new AgathionHolder();

	private final Map<Integer, AgathionTemplate> agathions = new HashMap<>();
	private final Map<Integer, AgathionTemplate> agathionsByItemId = new HashMap<>();

	public static AgathionHolder getInstance() {
		return _instance;
	}

	private AgathionHolder() {
		//
	}

	public void addAgathionTemplate(AgathionTemplate template) {
		agathions.put(template.getId(), template);
		for (int itemId : template.getItemIds())
			agathionsByItemId.put(itemId, template);
	}

	public AgathionTemplate getTemplate(int id) {
		return agathions.get(id);
	}

	public AgathionTemplate getTemplateByItemId(int itemId) {
		return agathionsByItemId.get(itemId);
	}

	@Override
	public int size() {
		return agathions.size();
	}

	@Override
	public void clear() {
		agathions.clear();
	}
}
