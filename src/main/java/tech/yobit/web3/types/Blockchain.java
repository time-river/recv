package tech.yobit.web3.types;

public class Blockchain extends BlockchainMeta {
    public final Address gatewayAddress;

    public Blockchain(BlockchainName blockchain, String rpcUrl, String blockExplorerUrl, Address gatewayAddress) {
        super(blockchain, rpcUrl, blockExplorerUrl);
        this.gatewayAddress = gatewayAddress;
    }

}
