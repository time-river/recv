package tech.yobit.web3.utils;

public class Blockchain {
    public String name;
    public int id;
    public String url;
    public Address gatewayAddress;

    public Blockchain(String name, int id, String url, Address gatewayAddress) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.gatewayAddress = gatewayAddress;
    }
}
