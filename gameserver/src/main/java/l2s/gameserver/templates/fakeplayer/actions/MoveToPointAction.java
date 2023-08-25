package l2s.gameserver.templates.fakeplayer.actions;

import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import org.dom4j.Element;

/**
 * @author Bonux
 **/
public class MoveToPointAction extends MoveAction {
	private final Location _loc;
	private final int _minRange, _maxRange;

	public MoveToPointAction(Location loc, int minRange, int maxRange, double chance) {
		super(chance);
		_loc = loc;
		_minRange = minRange;
		_maxRange = maxRange;
	}

	@Override
	public boolean performAction(FakeAI ai) {
		Player player = ai.getActor();

		Location loc = Location.findPointToStay(_loc.getX(), _loc.getY(), _loc.getZ(), _minRange, _maxRange, player.getGeoIndex());
		if (loc == null)
			return false;

		if (player.getDistance(loc) > 2000 || !player.getMovement().moveToLocation(loc, 0, true)) {
			player.teleToLocation(loc, 0, 0);
		}
		return true;
	}

	public static MoveToPointAction parse(Element element) {
		Location loc = Location.parse(element);
		int minRange = element.attributeValue("range") != null ? Integer.parseInt(element.attributeValue("range")) : (element.attributeValue("min_range") != null ? Integer.parseInt(element.attributeValue("min_range")) : 0);
		int maxRange = element.attributeValue("max_range") == null ? minRange : Integer.parseInt(element.attributeValue("max_range"));
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new MoveToPointAction(loc, minRange, maxRange, chance);
	}
}