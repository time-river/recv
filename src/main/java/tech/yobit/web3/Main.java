package tech.yobit.web3;


import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.web3.config.BlockchainConfig;
import tech.yobit.web3.config.CoinConfig;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.contract.GatewayContract;
import tech.yobit.web3.contract.GatewayManager;
import tech.yobit.web3.contract.WalletContract;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;
import tech.yobit.web3.types.CoinMeta;
import tech.yobit.web3.types.ContractAddress;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class Main {
    static String privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
    static String url = "http://127.0.0.1:8545";
    static int blockchainId = 12345;
    static CoinMeta USDTMeta = new CoinMeta(
            "USDT", "Tether USD", 6,
            blockchainId, Address.fromHex("0x7169d38820dfd117c3fa1f22a697dba58d90ba06")
    );
    static String uid = "user";

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

        ContractAddress predicted = contract.predictWalletAddress(uid);
        System.out.println("predict address: " + predicted.toHex());

        ContractAddress address = contract.checkAndCreateWallet(uid, predicted);
        System.out.println("generate address: " + address.toHex());

        address = contract.checkAndCreateWallet(uid, predicted);
        System.out.println("generate address2: " + address.toHex());

        boolean exist = contract.checkWalletAddress(address);
        System.out.println("exist: " + exist);
        return address;
    }

    static void transfer(Address to) throws  Exception {
        BigInteger amount = USDTMeta.parseUnits("1");
        Function func = new Function(
                "transfer",
                Arrays.asList(to, new Uint256(amount)),
                Collections.emptyList()
        );
        String encodedFunc = FunctionEncoder.encode(func);

        Web3j web3j = Web3j.build(new HttpService(url));
        Credentials credentials = Credentials.create(privateKey);

        //BigInteger nonce = web3j.ethGetTransactionCount(
        //        credentials.getAddress(), DefaultBlockParameterName.LATEST
        //).send().getTransactionCount();
        RawTransaction transaction = RawTransaction.createTransaction(
                null,
                web3j.ethGasPrice().send().getGasPrice(),
                BigInteger.valueOf(60000),
                USDTMeta.contractAddress.toHex(),
                encodedFunc
        );
        byte[] signMsg = TransactionEncoder.signMessage(transaction, credentials);

        EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(signMsg)).send();
        String rc = response.getTransactionHash();
        System.out.println("rcId: " + rc);
    }

    static void testWallet() {
    }

    public static void main(String[] args) throws Exception {
        String address = deploy();
        CryptoSupport support = parseConfig(genConfig(address));

        GatewayManager manager = genGatewayManager(support);

        ContractAddress address = testGatewayContract(manager.findGatewayContract(blockchainId));

        WalletContract wallet = manager.getWalletContract(blockchainId, address, uid);
    }
}
