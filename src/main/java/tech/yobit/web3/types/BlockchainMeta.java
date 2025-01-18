package tech.yobit.web3.types;

public class BlockchainMeta {
    public final BlockchainName chain;
    public final String rpcUrl;
    public final String blockExplorerUrl;

    public BlockchainMeta(BlockchainName chain, String rpcUrl, String blockExplorerUrl) {
        this.chain = chain;
        this.rpcUrl = rpcUrl;
        this.blockExplorerUrl = blockExplorerUrl;
    }

    public long getId() {
        return chain.getId();
    }

    public String getName() {
        return chain.toString();
    }
}
