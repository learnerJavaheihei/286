package votereward;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteApiService;
import l2s.gameserver.model.entity.votereward.VoteRewardRecord;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: l2network.eu
 **/
public class L2NetworkSite extends VoteRewardSite {
	private final String apiKey;

	public L2NetworkSite(MultiValueSet<String> parameters) {
		super(parameters);
		apiKey = parameters.getString("api_key");
	}

	@Override
	public boolean isEnabled() {
		if(StringUtils.isEmpty(apiKey))
			return false;
		return super.isEnabled();
	}

	@Override
	public boolean tryGiveRewards(Player player) {
		String serverResponse = VoteApiService.getApiResponse(String.format("https://l2network.eu/api.php?apiKey=%s&type=2&player=%s", apiKey, player.getAccountName()));
		if(serverResponse == null) {
			return false;
		}

		long voteTime;
		try {
			voteTime = TimeUnit.SECONDS.toMillis(Integer.parseInt(serverResponse.trim()));
		}
		catch (Exception e) {
			return false;
		}

		if(voteTime <= 0)
			return false;

		getLock().lock();
		try {
			VoteRewardRecord record = getRecord(player.getIP());

			long lastVoteTime = (record.getLastVoteTime() * 1000L);
			if(lastVoteTime >= voteTime)
				return false;

			record.onReceiveReward(1, voteTime);
			giveRewards(player, 1);
			return true;
		}
		finally {
			getLock().unlock();
		}
	}
}