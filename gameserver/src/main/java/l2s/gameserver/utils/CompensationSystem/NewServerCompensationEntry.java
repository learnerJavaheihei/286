package l2s.gameserver.utils.CompensationSystem;

public class NewServerCompensationEntry {

    private String account;
    private String roleName;
    private int firstGiveCoin;
    private long firstDate;
    private int secondGiveCoin;
    private long secondDate;
    private int thirdGiveCoin;
    private long thirdDate;
    private int fourthGiveCoin;
    private long fourthDate;
    private int fifthGiveCoin;
    private long fifthDate;
    private int totalVipCoin;
    private int remain_coin;


    public NewServerCompensationEntry(){}

    public NewServerCompensationEntry(String account, String roleName, int firstGiveCoin, long firstDate, int secondGiveCoin, long secondDate, int thirdGiveCoin, long thirdDate, int fourthGiveCoin, long fourthDate, int fifthGiveCoin, long fifthDate, int totalVipCoin, int remain_coin) {
        this.account = account;
        this.roleName = roleName;
        this.firstGiveCoin = firstGiveCoin;
        this.firstDate = firstDate;
        this.secondGiveCoin = secondGiveCoin;
        this.secondDate = secondDate;
        this.thirdGiveCoin = thirdGiveCoin;
        this.thirdDate = thirdDate;
        this.fourthGiveCoin = fourthGiveCoin;
        this.fourthDate = fourthDate;
        this.fifthGiveCoin = fifthGiveCoin;
        this.fifthDate = fifthDate;
        this.totalVipCoin = totalVipCoin;
        this.remain_coin = remain_coin;
    }




    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getFirstGiveCoin() {
        return firstGiveCoin;
    }

    public void setFirstGiveCoin(int firstGiveCoin) {
        this.firstGiveCoin = firstGiveCoin;
    }

    public long getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(long firstDate) {
        this.firstDate = firstDate;
    }

    public int getSecondGiveCoin() {
        return secondGiveCoin;
    }

    public void setSecondGiveCoin(int secondGiveCoin) {
        this.secondGiveCoin = secondGiveCoin;
    }

    public long getSecondDate() {
        return secondDate;
    }

    public void setSecondDate(long secondDate) {
        this.secondDate = secondDate;
    }

    public int getThirdGiveCoin() {
        return thirdGiveCoin;
    }

    public void setThirdGiveCoin(int thirdGiveCoin) {
        this.thirdGiveCoin = thirdGiveCoin;
    }

    public long getThirdDate() {
        return thirdDate;
    }

    public void setThirdDate(long thirdDate) {
        this.thirdDate = thirdDate;
    }

    public int getFourthGiveCoin() {
        return fourthGiveCoin;
    }

    public void setFourthGiveCoin(int fourthGiveCoin) {
        this.fourthGiveCoin = fourthGiveCoin;
    }

    public long getFourthDate() {
        return fourthDate;
    }

    public void setFourthDate(long fourthDate) {
        this.fourthDate = fourthDate;
    }

    public int getFifthGiveCoin() {
        return fifthGiveCoin;
    }

    public void setFifthGiveCoin(int fifthGiveCoin) {
        this.fifthGiveCoin = fifthGiveCoin;
    }

    public long getFifthDate() {
        return fifthDate;
    }

    public void setFifthDate(long fifthDate) {
        this.fifthDate = fifthDate;
    }

    public int getTotalVipCoin() {
        return totalVipCoin;
    }

    public void setTotalVipCoin(int totalVipCoin) {
        this.totalVipCoin = totalVipCoin;
    }

    public int getRemain_coin() {
        return remain_coin;
    }

    public void setRemain_coin(int remain_coin) {
        this.remain_coin = remain_coin;
    }

}
