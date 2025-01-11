package tech.yobit.web3.types;

public class ContractAddress extends Address {
    public final long blockchainId;

    public ContractAddress(long blockchainId, Address address) {
        super(address);
        this.blockchainId = blockchainId;
    }

    public ContractAddress(long blockchainId, String address) {
        super(address);
        this.blockchainId = blockchainId;
    }

    public ContractAddress(long blockchainId, byte[] address) {
        super(address);
        this.blockchainId = blockchainId;
    }
}
