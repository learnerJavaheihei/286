package l2s.gameserver.templates.agathion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux (bonuxq@gmail.com)
 * @date 27.07.2019
 **/
public class AgathionData {
	private final Map<Integer, AgathionEnchantData> enchants = new HashMap<>();

	public AgathionData() {
		//
	}

	public void addEnchant(AgathionEnchantData enchant) {
		enchants.put(enchant.getLevel(), enchant);
	}

	public AgathionEnchantData getEnchant(int enchantLevel) {
		return enchants.get(enchantLevel);
	}
}
