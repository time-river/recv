package tech.yobit.web3.types;

public class Blockchain extends BlockchainMeta {
    public final Address gatewayAddress;

    public Blockchain(String name, long id, String url, Address gatewayAddress) {
        super(name, id, url);
        this.gatewayAddress = gatewayAddress;
    }

}
