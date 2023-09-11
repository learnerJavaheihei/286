package handler.pccoupon;

import l2s.gameserver.handler.pccoupon.IPCCouponHandler;
import l2s.gameserver.handler.pccoupon.PCCouponHandler;
import l2s.gameserver.listener.script.OnInitScriptListener;

/**
 * @author Bonux
**/
public abstract class ScriptPCCouponHandler implements IPCCouponHandler, OnInitScriptListener
{
	@Override
	public void onInit()
	{
		PCCouponHandler.getInstance().registerHandler(this);
	}
}
