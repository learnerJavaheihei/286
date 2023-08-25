package l2s.gameserver.network.l2.components;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.SkillInfo;
import l2s.gameserver.utils.Language;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Даный класс является обработчиком серверных интернациональных сообщений.
 */
public class CustomMessage implements IBroadcastPacket
{
	private static interface IArgument
	{
		public String toString(Language lang);
	}

	private static class StringArgument implements IArgument
	{
		private final String _text;

		public StringArgument(String text)
		{
			_text = text;
		}

		@Override
		public String toString(Language lang)
		{
			return _text;
		}
	}

	private static class CustomMessageArgument implements IArgument
	{
		private final CustomMessage _customMessage;

		public CustomMessageArgument(CustomMessage customMessage)
		{
			_customMessage = customMessage;
		}

		@Override
		public String toString(Language lang)
		{
			return _customMessage.toString(lang);
		}
	}

	private static class ItemNameArgument implements IArgument
	{
		private final int _itemId;

		public ItemNameArgument(int itemId)
		{
			_itemId = itemId;
		}

		@Override
		public String toString(Language lang)
		{
			String name = ItemNameHolder.getInstance().getItemName(lang, _itemId);
			if(name == null)
				return "null";
			return name;
		}
	}

	private static class SkillNameArgument implements IArgument
	{
		private final int _skillId;
		private final int _skillLvl;

		public SkillNameArgument(int skillId, int skillLvl)
		{
			_skillId = skillId;
			_skillLvl = skillLvl;
		}

		@Override
		public String toString(Language lang)
		{
			String name = SkillNameHolder.getInstance().getSkillName(lang, _skillId, _skillLvl);
			if(name == null)
				return "null";
			return name;
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(CustomMessage.class);

	private static final String[] PARAMS = {"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}"};

	private String _address;
	private List<IArgument> _args;

	public CustomMessage(String address)
	{
		_address = address;
	}

	public CustomMessage addString(String text)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new StringArgument(text));
		return this;
	}

	public CustomMessage addNumber(int i)
	{
		return addString(String.valueOf(i));
	}

	public CustomMessage addNumber(long l)
	{
		return addString(String.valueOf(l));
	}

	public CustomMessage addCustomMessage(CustomMessage msg)
	{
		if(msg != this) // Иначе будет рекурсия.
		{
			if(_args == null)
				_args = new ArrayList<IArgument>();
			_args.add(new CustomMessageArgument(msg));
		}
		return this;
	}

	public CustomMessage addItemName(int itemId)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new ItemNameArgument(itemId));
		return this;
	}

	public CustomMessage addSkillName(int skillId, int skillLvl)
	{
		if(_args == null)
			_args = new ArrayList<IArgument>();
		_args.add(new SkillNameArgument(skillId, skillLvl));
		return this;
	}

	public CustomMessage addSkillName(SkillInfo skillInfo)
	{
		return addSkillName(skillInfo.getDisplayId(), skillInfo.getDisplayLevel());
	}
	
	public String toString(Player player)
	{
		return toString(player.getLanguage());
	}

	public String toString(Language lang)
	{
		TextStringBuilder msg = null;

		String text = StringsHolder.getInstance().getString(_address, lang);
		if(text != null)
		{
			msg = new TextStringBuilder(text);

			if(_args != null)
			{
				for(int i = 0; i < _args.size(); i++)
				{
					msg.replaceFirst(PARAMS[i], _args.get(i).toString(lang));
				}
			}
		}

		if(StringUtils.isEmpty(msg))
		{
			_log.warn("CustomMessage: string: " + _address + " not found for lang: " + lang + "!");
			return StringUtils.EMPTY;
		}

		return msg.toString();
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		return new SystemMessage(SystemMsg.S1).addString(toString(player));
	}
}