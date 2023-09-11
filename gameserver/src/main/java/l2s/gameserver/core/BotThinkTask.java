package l2s.gameserver.core;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;

public final class BotThinkTask implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(BotThinkTask.class);
	private final SoftReference actor;

	/* member class not found */
	class Task
	{
	}

	public BotThinkTask(Player actor)
	{
		this.actor = new SoftReference(actor);
	}

	public void run()
	{
		Player actor;
		BotConfig config;
		actor = (Player) this.actor.get();
		if(actor == null)
			return;
		config = BotEngine.getInstance().getBotConfig(actor);
        if(BotEngine.getInstance().getRunTimeChecker().test(actor))
        {
            if(!config.isAbort())
                ThreadPoolManager.getInstance().schedule(this, 1000L);
            else
            if(actor != null)
                BotEngine.getInstance().stopTask(actor);
            if(actor != null)
                config.releaseMemory(actor);
            return;
        }
		try
		{
			for(IBotActionHandler actionHandler : BotActionHandler.getInstance().getData().values())
			{
		        if(actionHandler.test(actor))
		        {
					if(!config.isAbort())
					{
						ThreadPoolManager.getInstance().schedule(this, 1000L);
						return;
					}
					else
					{
						if(actor != null)
							BotEngine.getInstance().stopTask(actor);
						if(actor != null)
							config.releaseMemory(actor);
					}				
		        }
			}
		}
		catch(Exception e)
		{
			config.setAbort(true, "内挂启动失败，请联系管理员检查");
			LOG.error("内挂执行过程中出现异常:", e);
		}

		if(!config.isAbort())
		{
			ThreadPoolManager.getInstance().schedule(this, 1000L);
			return;
		}
		else
		{
			if(actor != null)
				BotEngine.getInstance().stopTask(actor);
			if(actor != null)
				config.releaseMemory(actor);
		}		
	}

	public static boolean checkSkillMpCost(Player actor, SkillEntry skill)
	{
		return actor.getCurrentMp() >= skill.getTemplate().getMpConsume();
	}
}
