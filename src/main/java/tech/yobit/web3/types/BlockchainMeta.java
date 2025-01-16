package tech.yobit.web3.types;

public class BlockchainMeta {
    public final String name;
    public final long id;
    public final String rpcUrl;
    public final String blockExplorerUrl;

    public BlockchainMeta(String name, long id, String rpcUrl, String blockExplorerUrl) {
        this.name = name;
        this.id = id;
        this.rpcUrl = rpcUrl;
        this.blockExplorerUrl = blockExplorerUrl;
    }
}
