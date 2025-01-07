package tech.yobit.web3.contract;

import tech.yobit.web3.utils.Address;

public class WalletContract {
    private final Address mAddress;

    public WalletContract(Address address) {
        mAddress = address;
    }

    public static WalletContract fromBase58(String address) {
        return new WalletContract(Address.fromBase58(address));
    }

    public static WalletContract fromHex(String address) {
        return new WalletContract(Address.fromHex(address));
    }

    public static WalletContract fromBytes(byte[] address) {
        return new WalletContract(new Address(address));
    }

    public String withdraw(Address to, Address cryptoAddrees, String amount) {
        String txId = "";

        return txId;
    }
}
