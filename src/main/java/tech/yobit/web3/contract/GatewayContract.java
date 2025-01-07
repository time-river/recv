package tech.yobit.web3.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeEncoder;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import tech.yobit.generated.gateway.Gateway;
import tech.yobit.web3.utils.Address;
import tech.yobit.web3.utils.Blockchain;

public class GatewayContract {
    private static final Logger log = LoggerFactory.getLogger(GatewayContract.class);

    private final Blockchain mBlockchain;
    private final Gateway mGatewayContract;

    public GatewayContract(String privateKey, Blockchain blockchain) {
        mBlockchain = blockchain;

        Credentials mCredentials = Credentials.create(privateKey);
        log.info("Credentials loaded, wallet address: {}", mCredentials.getAddress());

        Web3j web3j = Web3j.build(new HttpService(blockchain.url));
        log.info("Connected to {} network", blockchain.name);

        mGatewayContract = Gateway.load(blockchain.gatewayAddress.toHex(),
                web3j, mCredentials, new DefaultGasProvider()); // TODO: fix gas
    }

    private byte[] generateSalt(String salt) {
        byte[] bytes = Numeric.hexStringToByteArray(salt);
        return Hash.sha3(bytes);
    }

    private byte[] generateInitCode(byte[] salt) {

        String result = Gateway.BINARY +
                Numeric.toHexStringNoPrefix(salt) +
                TypeEncoder.encode(mBlockchain.gatewayAddress);

        return result.getBytes();
    }

    public Address predictWalletAddress(byte[] salt) {
        byte[] initCode = generateInitCode(salt);
        byte[] address = ContractUtils.generateCreate2ContractAddress(mBlockchain.gatewayAddress.toBytes(), salt, initCode);
        return Address.fromBytes(address);
    }

    public Address predictWalletAddress(String saltString) {
        byte[] salt = generateSalt(saltString);

        return predictWalletAddress(salt);
    }

    public boolean checkWalletAddress(Address address) throws Exception {
        return mGatewayContract.wallets(address.toHex()).send();
    }

    // TODO
    public Address checkAndCreateWallet(String saltString) throws Exception {
        byte[] salt = generateSalt(saltString);
        Address walletAddress = predictWalletAddress(salt);

        boolean exist = checkWalletAddress(walletAddress);
        if (exist) {
            return walletAddress;
        }

        TransactionReceipt receipt = mGatewayContract.createWallet(salt).send();


        return "";
    }
}
