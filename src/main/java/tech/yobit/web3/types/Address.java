package tech.yobit.web3.types;

import org.web3j.utils.Numeric;
import tech.yobit.web3.utils.Base58;

// TODO: support base58, non-160 bit address
public class Address extends org.web3j.abi.datatypes.Address {

    private Address(int bitSize, byte[] bytesValue) {
        super(bitSize, Numeric.toBigInt(bytesValue));
    }

    private Address(int bitSize, String hexValue) {
        super(bitSize, Numeric.toBigInt(hexValue));
    }

    public Address(String hexValue) {
        this(DEFAULT_LENGTH, hexValue);
    }

    public Address(byte[] bytesValue) {
        this(DEFAULT_LENGTH, bytesValue);
    }

    public Address(Address address) {
        super(address.toUint());
    }

    public static Address fromBase58(String base58) {
        byte[] address = Base58.decode(base58);
        return new Address(address);
    }

    public static Address fromHex(String hex) {
        byte[] address = Numeric.hexStringToByteArray(hex);
        return new Address(address);
    }

    public static Address fromBytes(byte[] bytes) {
        return new Address(bytes);
    }

    public String toHex() {
        return toString();
    }

    public byte[] toBytes() {
        return Numeric.hexStringToByteArray(toString());
    }

    public String toBase58() {
        return Base58.encode(toBytes());
    }
}
