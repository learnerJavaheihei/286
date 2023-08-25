package l2s.gameserver.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.string.NpcStringHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;

import org.apache.commons.lang3.StringUtils;

/**
 * Класс хранилища и валидации ссылок в html.
 *
 * @author G1ta0
 */
public class BypassStorage
{
	public static enum BypassType
	{
		DEFAULT,
		BBS,
		ITEM;

		public static final BypassType[] VALUES = values();
	}

	private static final Pattern htmlBypass = Pattern.compile("\\s+action=\"bypass\\s+(?:-h +)?([^\"]+?)\"", Pattern.CASE_INSENSITIVE);
	private static final Pattern htmlLink = Pattern.compile("\\s+action=\"link\\s+([^\"]+?\\.html?(#[0-9]+)?)\"", Pattern.CASE_INSENSITIVE);
	private static final Pattern bbsWrite = Pattern.compile("\\s+action=\"write\\s+(\\S+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern directHtmlBypass = Pattern.compile("^(_mrsl|_diary|_match|manor_menu_select|_olympiad|pledgegame|_heroes|pcbang?).*", Pattern.DOTALL); // Убрал: learn_skill|menu_select?|talk_select|teleport_request|deposit|withdraw|deposit_pledge|withdraw_pledge|package_deposit|withdraw_items|class_change?|quest_accept?|multiclass?
	private static final Pattern directBbsBypass = Pattern.compile("^(_bbshome|_bbsgetfav|_bbsaddfav|_bbslink|_bbsloc|_bbsclan|_bbsmemo|_maillist|_friendlist).*", Pattern.DOTALL);

	private static final Pattern fstringPattern = Pattern.compile("<fstring(\\s+p[0-9]+\\s*=\\s*\"((?!>).*?)\")*\\s*>([0-9]+)<\\s*/\\s*fstring\\s*>", Pattern.CASE_INSENSITIVE);

	public static class ValidBypass
	{
		public String encryptedBypass;
		public String bypass;
		public boolean args;
		public BypassType type;

		public ValidBypass(String encryptedBypass, String bypass, boolean args, BypassType type)
		{
			this.encryptedBypass = encryptedBypass;
			this.bypass = bypass;
			this.args = args;
			this.type = type;
		}
	}

	private final Player _owner;

	private final Map<BypassType, List<ValidBypass>> _bypassesMap = new HashMap<BypassType, List<ValidBypass>>(BypassType.VALUES.length);

	public BypassStorage(Player owner)
	{
		_owner = owner;

		for(BypassType type : BypassType.VALUES)
			_bypassesMap.put(type, new CopyOnWriteArrayList<ValidBypass>());
	}

	public void parseHtml(CharSequence html, BypassType type)
	{
		parseHtml(html.toString(), type, false);
	}

	public String parseHtml(String html, BypassType type, boolean encode)
	{
		clear(type);

		if(StringUtils.isEmpty(html))
			return html;

		Matcher m = htmlBypass.matcher(html);
		while(m.find())
		{
			String bypass = m.group(1);
			//При передаче аргументов, мы можем проверить только часть команды до первого аргумента
			int i = bypass.indexOf(" $");
			if(i > 0)
				bypass = bypass.substring(0, i);

			if(encode)
			{
				ValidBypass validBypass = new ValidBypass(encodeBypass(bypass), bypass, i >= 0, type);
				addBypass(validBypass);

				html = html.replaceFirst(Pattern.quote(validBypass.bypass), validBypass.encryptedBypass);
			}
			else
				addBypass(new ValidBypass(bypass, bypass, i >= 0, type));
		}

		if(type == BypassType.BBS)
		{
			m = bbsWrite.matcher(html);
			while(m.find())
			{
				String bypass = m.group(1);
				if(encode)
				{
					ValidBypass validBypass = new ValidBypass(encodeBypass(bypass), bypass, true, type);
					addBypass(validBypass);

					html = html.replaceFirst(Pattern.quote(validBypass.bypass), validBypass.encryptedBypass);
				}
				else
					addBypass(new ValidBypass(bypass, bypass, true, type));
			}
		}

		m = htmlLink.matcher(html);
		while(m.find())
		{
			String bypass = m.group(1);
			if(encode)
			{
				ValidBypass validBypass = new ValidBypass(encodeBypass(bypass), bypass, false, type);
				addBypass(validBypass);

				html = html.replaceFirst(Pattern.quote(validBypass.bypass), validBypass.encryptedBypass);
			}
			else
				addBypass(new ValidBypass(bypass, bypass, false, type));
		}

		m = fstringPattern.matcher(html);
		while(m.find())
		{
			String npcString = null;

			int npcStringId = Integer.parseInt(m.group(3));
			GameClient client = _owner.getNetConnection();
			if(client == null) // Приоритетнее NpcString соответствующий языку клиента.
				npcString = NpcStringHolder.getInstance().getNpcString(_owner, npcStringId);
			else
				npcString = NpcStringHolder.getInstance().getNpcString(client.getLanguage(), npcStringId);

			if(npcString != null)
			{
				Matcher m2 = htmlBypass.matcher(npcString);
				while(m2.find())
				{
					String bypass = m2.group(1);
					addBypass(new ValidBypass(bypass, bypass, false, type));
				}
			}
		}
		return html;
	}

	public static String encodeBypass(String bypass)
	{
		int encoded = Rnd.get(Integer.MAX_VALUE);
		return String.valueOf(encoded);
	}

	public ValidBypass validate(String bypass)
	{
		ValidBypass ret = null;

		if(directHtmlBypass.matcher(bypass).matches())
			ret = new ValidBypass(bypass, bypass, false, BypassType.DEFAULT);
		else if(directBbsBypass.matcher(bypass).matches())
			ret = new ValidBypass(bypass, bypass, false, BypassType.BBS);
		else
		{
			String[] args = bypass.split("\\s+");
			loop: for(List<ValidBypass> bypasses : _bypassesMap.values())
			{
				for(ValidBypass bp : bypasses)
				{
					if(bp.encryptedBypass.equals(bypass))
					{
						ret = bp;
						break loop;
					}
					if(bp.args && bp.encryptedBypass.split("\\s+")[0].equals(args[0]))
					{
						if(args.length > 1)
						{
							ret = new ValidBypass(bp.encryptedBypass, bypass.replaceFirst(Pattern.quote(args[0]), bp.bypass), true, bp.type);
						}
						else
						{
							ret = bp;
						}
						break loop;
					}
				}
			}
		}

		/*if(ret != null)
		{
			clear(ret.type);
		}*/

		return ret;
	}

	private void addBypass(ValidBypass bypass)
	{
		_bypassesMap.get(bypass.type).add(bypass);
	}

	private void clear(BypassType type)
	{
		_bypassesMap.get(type).clear();
	}
}