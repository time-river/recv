package tech.yobit.web3.types;

public class ContractAddress extends Address {
    public final int blockchainId;

    public ContractAddress(int blockchainId, Address address)  {
        super(address);
        this.blockchainId = blockchainId;
    }

    public ContractAddress(int blockchainId, String address) {
        super(address);
        this.blockchainId = blockchainId;
    }

    public ContractAddress(int blockchainId, byte[] address) {
        super(address);
        this.blockchainId = blockchainId;
    }
}
