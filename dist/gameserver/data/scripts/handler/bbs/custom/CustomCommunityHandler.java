package handler.bbs.custom;

import handler.bbs.ScriptsCommunityHandler;

/**
 * @author Bonux
**/
public abstract class CustomCommunityHandler extends ScriptsCommunityHandler
{
	@Override
	public void onInit()
	{
		if(BBSConfig.CUSTOM_COMMUNITY_ENABLED)
			super.onInit();
	}
}
