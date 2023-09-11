package l2s.gameserver.botscript.voicehandler;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;

public class BotCallOut
implements IVoicedCommandHandler {
    private static final String[] COMMANDS = new String[]{"\u5185\u6302", "\u52a0\u8f7d\u5185\u6302"};
	/*\u5185\u6302 内挂	\u52a0\u8f7d\u5185\u6302 加载内挂*/

    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if (command.equals("\u5185\u6302")) {
			/*\u5185\u6302 内挂*/
            BotControlPage.mainPage(activeChar);
        } else if (command.equals("\u52a0\u8f7d\u5185\u6302")) {
			/*\u52a0\u8f7d\u5185\u6302 加载内挂*/
            activeChar.sendMessage("\u8fd9\u4e2a\u547d\u4ee4\u5df2\u7ecf\u8fc7\u671f...");
			/*\u8fd9\u4e2a\u547d\u4ee4\u5df2\u7ecf\u8fc7\u671f... 这个命令已经过期...*/
        }
        return false;
    }

    public String[] getVoicedCommandList() {
        return COMMANDS;
    }
}