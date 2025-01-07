package tech.yobit.web3.utils;

public class CryptoCoin {
    public final String name;
    public final String fullName;
    public final int decimal;
    public final Blockchain blockchain;
    public final Address walletAddress;

    public CryptoCoin(String name, String fullName, int decimal,
                      Blockchain blockchain, Address walletAddress) {
        this.name = name;
        this.fullName = fullName;
        this.decimal = decimal;
        this.blockchain = blockchain;
        this.walletAddress = walletAddress;
    }
}
