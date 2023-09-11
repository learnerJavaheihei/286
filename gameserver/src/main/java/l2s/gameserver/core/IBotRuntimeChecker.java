package l2s.gameserver.core;

import l2s.gameserver.model.Player;

import java.util.function.Predicate;

public interface IBotRuntimeChecker extends Predicate<Player>
{
}