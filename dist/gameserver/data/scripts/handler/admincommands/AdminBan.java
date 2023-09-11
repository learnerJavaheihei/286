package handler.admincommands;

import l2s.commons.ban.BanBindType;
import l2s.commons.ban.BanInfo;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.BanRequest;
import l2s.gameserver.network.authcomm.gs2as.UnbanRequest;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 11.04.2019
 * Developed for L2-Scripts.com
 **/
public class AdminBan extends ScriptAdminCommand {
	private static enum Commands {
			admin_ban,
			admin_unban,
			admin_auth_ban,
			admin_auth_unban,
			admin_game_ban,
			admin_game_unban
	};

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar) {
		if (!activeChar.getPlayerAccess().CanBan)
			return false;

		if (comm == Commands.admin_ban || comm == Commands.admin_unban) {
			activeChar.sendPacket(new HtmlMessage(5).setFile("admin/ban/ban.htm"));
		}
		else if (comm == Commands.admin_auth_ban || comm == Commands.admin_game_ban) {
			boolean auth = comm == Commands.admin_auth_ban;
			try {
				StringTokenizer st = new StringTokenizer(fullString);
				st.nextToken();

				if(st.hasMoreTokens()) {
					BanBindType bindType = BanBindType.valueOf(st.nextToken().toUpperCase());
					String charName = st.nextToken();
					int period = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1;
					TimeUnit periodType;
					if (st.hasMoreTokens()) {
						String t = st.nextToken();
						if (t.matches("(?i:(min(utes?)?|мин(уты?)?))"))
							periodType = TimeUnit.MINUTES;
						else if (t.matches("(?i:(days?|дни|дней|день))"))
							periodType = TimeUnit.DAYS;
						else
							periodType = TimeUnit.HOURS;
					} else
						periodType = TimeUnit.HOURS;
					int endTime = period == -1 ? -1 : (int) (periodType.toSeconds(period) + (System.currentTimeMillis() / 1000));
					String reason = st.hasMoreTokens() ? fullString.substring(fullString.indexOf(st.nextToken())) : "";

					if (bindType == BanBindType.LOGIN) {
						String account;
						Player player = GameObjectsStorage.getPlayer(charName);
						if (player != null) {
							account = player.getAccountName();
						} else {
							account = CharacterDAO.getInstance().getAccNameByName(charName);
						}

						if (StringUtils.isEmpty(account)) {
							activeChar.sendMessage("Not found account for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You banned account: '" + account + "'.");
						giveBan(activeChar, fullString, bindType, account, endTime, reason, auth);
					} else if (bindType == BanBindType.IP) {
						String ip;
						Player player = GameObjectsStorage.getPlayer(charName);
						if (player != null) {
							ip = player.getIP();
						} else {
							ip = CharacterDAO.getInstance().getLastIPByName(charName);
						}

						if (StringUtils.isEmpty(ip)) {
							activeChar.sendMessage("Not found IP for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You banned IP: '" + ip + "'.");
						giveBan(activeChar, fullString, bindType, ip, endTime, reason, auth);
					} else if (bindType == BanBindType.HWID) {
						String hwid;
						Player player = GameObjectsStorage.getPlayer(charName);
						if (player != null) {
							hwid = player.getHWID();
						} else {
							hwid = CharacterDAO.getInstance().getLastHWIDByName(charName);
						}

						if (StringUtils.isEmpty(hwid)) {
							activeChar.sendMessage("Not found HWID for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You banned HWID: '" + hwid + "'.");
						giveBan(activeChar, fullString, bindType, hwid, endTime, reason, auth);
					} else if (!auth && (bindType == BanBindType.PLAYER || bindType == BanBindType.CHAT)) {
						int objectId;
						Player player = GameObjectsStorage.getPlayer(charName);
						if (player != null) {
							objectId = player.getObjectId();
						} else {
							objectId = CharacterDAO.getInstance().getObjectIdByName(charName);
						}

						if (objectId <= 0) {
							activeChar.sendMessage("Not found '" + charName + "' character.");
							return true;
						}

						if (bindType == BanBindType.PLAYER)
							activeChar.sendMessage("You banned character: '" + charName + "'.");
						else
							activeChar.sendMessage("You banned character: '" + charName + "' chat.");
						giveBan(activeChar, fullString, bindType, objectId, endTime, reason, false);
					} else {
						if (auth)
							activeChar.sendMessage("USAGE: //auth_ban [type: LOGIN | IP | HWID] [char_name] [period] [period_type: MIN | HOUR | DAY] [reason]");
						else
							activeChar.sendMessage("USAGE: //game_ban [type: LOGIN | IP | HWID | PLAYER | CHAT] [char_name] [period] [period_type: MIN | HOUR | DAY] [reason]");
					}
				}

				if(auth)
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/ban/auth_ban.htm"));
				else
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/ban/game_ban.htm"));
			} catch (Exception e) {
				if (auth)
					activeChar.sendMessage("USAGE: //auth_ban [type: LOGIN | IP | HWID] [char_name] [period] [period_type: MIN | HOUR | DAY] [reason]");
				else
					activeChar.sendMessage("USAGE: //game_ban [type: LOGIN | IP | HWID | PLAYER | CHAT] [char_name] [period] [period_type: MIN | HOUR | DAY] [reason]");
			}
			return true;
		} else if (comm == Commands.admin_auth_unban || comm == Commands.admin_game_unban) {
			boolean auth = comm == Commands.admin_auth_unban;
			try {
				StringTokenizer st = new StringTokenizer(fullString);
				st.nextToken();

				if(st.hasMoreTokens()) {

					BanBindType bindType = BanBindType.valueOf(st.nextToken().toUpperCase());
					String charName = st.nextToken();

					if (bindType == BanBindType.LOGIN) {
						String account = CharacterDAO.getInstance().getAccNameByName(charName);
						if (StringUtils.isEmpty(account)) {
							activeChar.sendMessage("Not found account for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You unbanned account: '" + account + "'.");
						removeBan(activeChar, fullString, bindType, account, auth);
					} else if (bindType == BanBindType.IP) {
						String ip = CharacterDAO.getInstance().getLastIPByName(charName);
						if (StringUtils.isEmpty(ip)) {
							activeChar.sendMessage("Not found IP for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You unbanned IP: '" + ip + "'.");
						removeBan(activeChar, fullString, bindType, ip, auth);
					} else if (bindType == BanBindType.HWID) {
						String hwid = CharacterDAO.getInstance().getLastHWIDByName(charName);
						if (StringUtils.isEmpty(hwid)) {
							activeChar.sendMessage("Not found HWID for '" + charName + "' character.");
							return true;
						}

						activeChar.sendMessage("You unbanned HWID: '" + hwid + "'.");
						removeBan(activeChar, fullString, bindType, hwid, auth);
					} else if (!auth && (bindType == BanBindType.PLAYER || bindType == BanBindType.CHAT)) {
						int objectId = CharacterDAO.getInstance().getObjectIdByName(charName);
						if (objectId <= 0) {
							activeChar.sendMessage("Not found '" + charName + "' character.");
							return true;
						}

						if (bindType == BanBindType.PLAYER)
							activeChar.sendMessage("You unbanned character: '" + charName + "'.");
						else
							activeChar.sendMessage("You unbanned character: '" + charName + "' chat.");
						removeBan(activeChar, fullString, bindType, objectId, false);
					} else {
						if (auth)
							activeChar.sendMessage("USAGE: //auth_unban [type: LOGIN | IP | HWID] [char_name]");
						else
							activeChar.sendMessage("USAGE: //game_unban [type: LOGIN | IP | HWID | PLAYER | CHAT] [char_name]");
					}
				}

				if(auth)
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/ban/auth_unban.htm"));
				else
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/ban/game_unban.htm"));
			} catch (Exception e) {
				if (auth)
					activeChar.sendMessage("USAGE: //auth_unban [type: LOGIN | IP | HWID] [char_name]");
				else
					activeChar.sendMessage("USAGE: //game_unban [type: LOGIN | IP | HWID | PLAYER | CHAT] [char_name]");
			}
			return true;
		}
		return false;
	}

	private void giveBan(Player activeChar, String command, BanBindType bindType, Object bindValueObj, int endTime, String reason, boolean auth) {
		String bindValue = String.valueOf(bindValueObj);
		if (auth) {
			GameBanManager.onBan(bindType, bindValue, new BanInfo(endTime, reason), true);
			AuthServerCommunication.getInstance().sendPacket(new BanRequest(bindType, bindValue, endTime, reason));
		} else
			GameBanManager.getInstance().giveBan(bindType, bindValue, endTime, reason);

		Log.LogBan(activeChar, command, bindType, bindValueObj, endTime, reason, auth);
	}

	private void removeBan(Player activeChar, String command, BanBindType bindType, Object bindValueObj, boolean auth) {
		String bindValue = String.valueOf(bindValueObj);
		if (auth) {
			GameBanManager.onUnban(bindType, bindValue, true);
			AuthServerCommunication.getInstance().sendPacket(new UnbanRequest(bindType, bindValue));
		} else
			GameBanManager.getInstance().removeBan(bindType, bindValue);

		Log.LogUnban(activeChar, command, bindType, bindValueObj, auth);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
