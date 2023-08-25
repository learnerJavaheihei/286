package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.agathion.AgathionTemplate;

/**
 * @author Bonux
**/
public class Agathion extends Cubic
{
	public Agathion(Player owner, AgathionTemplate template, Skill skill)
	{
		super(owner, template, skill);
	}

	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	@Override
	public AgathionTemplate getTemplate()
	{
		return (AgathionTemplate) _template;
	}

	@Override
	public void init()
	{
		_owner.setAgathion(this);

		if(_task == null)
			_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
	}

	@Override
	public void delete()
	{
		if(_task != null)
		{
			_task.cancel(true);
			_task = null;
		}

		if(_castTask != null)
		{
			_castTask.cancel(true);
			_castTask = null;
		}

		_owner.setAgathion(null);
	}

	@Override
	public boolean isCubic()
	{
		return false;
	}

	@Override
	public boolean isAgathion()
	{
		return true;
	}
}