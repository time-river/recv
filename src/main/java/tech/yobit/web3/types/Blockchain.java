package tech.yobit.web3.types;

public class Blockchain extends BlockchainMeta {
    public final Address gatewayAddress;

    public Blockchain(String name, long id, String rpcUrl, String blockExplorerUrl, Address gatewayAddress) {
        super(name, id, rpcUrl, blockExplorerUrl);
        this.gatewayAddress = gatewayAddress;
    }

}
