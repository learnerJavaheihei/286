package l2s.gameserver.model.actor.instances.player;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterElementalsDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ElementalElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
5160	1	u,При изменении Духа правки не сохранятся.\0	0	79	9B	B0	FF	6	6	0	0	0	0	0	a,	u,При изменении Духа правки не сохранятся.\0	a,	a,none\0
5161	1	u,<$s1> использует стихийную атаку.\0	0	79	9B	B0	FF	6	6	0	0	0	0	0	a,	a,	a,	a,none\0
5166	1	u,Дух другой стихии не отвечает условиям эволюции.\0	0	79	9B	B0	FF	6	6	0	0	0	0	0	a,	u,Дух другой стихии не отвечает условиям развития.\0	a,	a,none\0
5172	1	u,Стихия не освоена. Использовать $s1 нельзя.\0	0	79	9B	B0	FF	6	6	0	0	0	0	0	a,	u,Стихия не освоена. Использовать $s1 нельзя.\0	a,	a,none\0
**/
public class ElementalList implements Iterable<Elemental>
{
	private static final Logger _log = LoggerFactory.getLogger(ElementalList.class);

	private final Map<ElementalElement, Elemental> _elementals;

	private final Player _owner;

	public ElementalList(Player owner)
	{
		if(Config.ELEMENTAL_SYSTEM_ENABLED)
			_elementals = new TreeMap<ElementalElement, Elemental>();
		else
			_elementals = Collections.emptyMap();
		_owner = owner;
	}

	@Override
	public Iterator<Elemental> iterator()
	{
		return _elementals.values().iterator();
	}

	public int size()
	{
		return _elementals.size();
	}

	public Collection<Elemental> values()
	{
		return _elementals.values();
	}

	public void restore()
	{
		if(Config.ELEMENTAL_SYSTEM_ENABLED)
			CharacterElementalsDAO.getInstance().restore(_owner, _elementals);
	}

	public void store()
	{
		if(Config.ELEMENTAL_SYSTEM_ENABLED)
			CharacterElementalsDAO.getInstance().store(_owner, _elementals.values());
	}

	public Elemental get(int id)
	{
		return get(ElementalElement.getElementById(id));
	}

	public Elemental get(ElementalElement element)
	{
		if(!Config.ELEMENTAL_SYSTEM_ENABLED)
			return null;

		if(element != ElementalElement.NONE)
		{
			Elemental elemental = _elementals.get(element);
			if(elemental == null)
			{
				elemental = new Elemental(element);
				_elementals.put(elemental.getElement(), elemental);
			}
			return elemental;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "ElementalList[owner=" + _owner.getName() + "]";
	}
}
