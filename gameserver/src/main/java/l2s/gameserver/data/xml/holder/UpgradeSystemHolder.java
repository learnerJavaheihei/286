package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeData;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 02.04.2019
 * Developed for L2-Scripts.com
 **/
public final class UpgradeSystemHolder extends AbstractHolder {
	private static UpgradeSystemHolder INSTANCE = new UpgradeSystemHolder();

	public static UpgradeSystemHolder getInstance() {
		return INSTANCE;
	}

	private final Map<Integer, NormalUpgradeData> normalUpgradeDatas = new HashMap<>();
	private final Map<Integer, RareUpgradeData> rareUpgradeDatas = new HashMap<>();

	public void addNormalUpgradeData(NormalUpgradeData data)
	{
		normalUpgradeDatas.put(data.getId(), data);
	}

	public NormalUpgradeData getNormalUpgradeData(int id) {
		return normalUpgradeDatas.get(id);
	}

	public void addRareUpgradeData(RareUpgradeData data)
	{
		rareUpgradeDatas.put(data.getId(), data);
	}

	public RareUpgradeData getRareUpgradeData(int id) {
		return rareUpgradeDatas.get(id);
	}

	@Override
	public void log() {
		info(String.format("loaded %d normal upgrade data(s) count.", normalUpgradeDatas.size()));
		info(String.format("loaded %d rare upgrade data(s) count.", rareUpgradeDatas.size()));
	}

	@Override
	public int size() {
		return normalUpgradeDatas.size() + rareUpgradeDatas.size();
	}

	@Override
	public void clear() {
		normalUpgradeDatas.clear();
		rareUpgradeDatas.clear();
	}
}
