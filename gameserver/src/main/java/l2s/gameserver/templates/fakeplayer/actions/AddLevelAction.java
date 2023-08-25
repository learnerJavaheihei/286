package l2s.gameserver.templates.fakeplayer.actions;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;

import org.dom4j.Element;

/**
 * @author Bonux
**/
public class AddLevelAction extends AbstractAction
{
	private final int _minCount, _maxCount;

	public AddLevelAction(int minCount, int maxCount, double chance)
	{
		super(chance);
		_minCount = minCount;
		_maxCount = maxCount;
	}

	@Override
	public boolean performAction(FakeAI ai)
	{
		Player player = ai.getActor();
		long exp_add = Experience.getExpForLevel(player.getLevel() + Rnd.get(_minCount, _maxCount)) - player.getExp();
		player.addExpAndSp(exp_add, 0, true);
		return true;
	}

	public static AddLevelAction parse(Element element)
	{
		int minCount = element.attributeValue("count") != null ? Integer.parseInt(element.attributeValue("count")) : Integer.parseInt(element.attributeValue("min_count"));
		int maxCount = element.attributeValue("max_count") == null ? minCount : Integer.parseInt(element.attributeValue("max_count"));
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new AddLevelAction(minCount, maxCount, chance);
	}
}