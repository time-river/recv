package tech.yobit.web3;


import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.web3.config.BlockchainConfig;
import tech.yobit.web3.config.CoinConfig;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.contract.GatewayContract;
import tech.yobit.web3.contract.GatewayManager;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;
import tech.yobit.web3.types.CoinMeta;
import tech.yobit.web3.types.ContractAddress;

public class Main {
    static String privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
    static String url = "http://127.0.0.1:8545";
    static int blockchainId = 12345;

    static Configuration genConfig(String address) {
        Configuration config = new Configuration();
        config.privateKey = privateKey;
        config.defaultUrl = url;
        BlockchainConfig blockchain = new BlockchainConfig();
        blockchain.url = null;
        blockchain.id = blockchainId;
        blockchain.gatewayAddress = address;
        blockchain.name = "TEST";
        config.blockchains = new BlockchainConfig[]{blockchain};
        config.coins = new CoinConfig[0];

        return config;
    }

    static CryptoSupport parseConfig(Configuration config) {
        CryptoSupport support = new CryptoSupport(config);

        for (Blockchain blockchain : support.getBlockchains()) {
            System.out.printf("id: %d, name: %s, url: %s, gatewayAddress: %s",
                    blockchain.id, blockchain.name, blockchain.url, blockchain.gatewayAddress
            );
        }

        return support;
    }

    static String deploy() throws Exception {
        Web3j web3j = Web3j.build(new HttpService(url));
        Credentials credentials = Credentials.create(privateKey);
        Gateway gateway = Gateway.deploy(web3j, credentials, new DefaultGasProvider()).send();
        System.out.println(gateway.getContractAddress());

        return gateway.getContractAddress();
    }

    static GatewayManager genGatewayManager(CryptoSupport support) {
        GatewayManager manager = new GatewayManager(privateKey, support.getBlockchains());
        GatewayContract contract = manager.findGatewayContract(1234);
        if (contract == null) {
            System.out.println("empty contract id 1234");
        }

        contract = manager.findGatewayContract(blockchainId);
        if (contract != null) {
            System.out.println("find contract " + blockchainId);
        }
        return manager;
    }

    static ContractAddress testGatewayContract(GatewayContract contract) throws Exception {
        String uid = "user";

        ContractAddress predicted = contract.predictWalletAddress(uid);
        System.out.println("predict address: " + predicted.toHex());

        ContractAddress address = contract.checkAndCreateWallet(uid, predicted);
        System.out.println("generate address: " + address.toHex());

        address = contract.checkAndCreateWallet(uid, predicted);
        System.out.println("generate address2: " + address.toHex());

        return address;
    }



    public static void main(String[] args) throws Exception {
        String address = deploy();
        CryptoSupport support = parseConfig(genConfig(address));

        GatewayManager manager = genGatewayManager(support);

        testGatewayContract(manager.findGatewayContract(blockchainId));
    }
}
