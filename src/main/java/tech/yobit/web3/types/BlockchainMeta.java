package tech.yobit.web3.types;

public class BlockchainMeta {
    public final String name;
    public final int id;
    public final String url;
    public final Address gatewayAddress;

    public BlockchainMeta(String name, int id, String url, Address gatewayAddress) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.gatewayAddress = gatewayAddress;
    }
}
