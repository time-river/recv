package tech.yobit.web3.types;

public class BlockchainMeta {
    public final String name;
    public final long id;
    public final String url;

    public BlockchainMeta(String name, long id, String url) {
        this.name = name;
        this.id = id;
        this.url = url;
    }
}
