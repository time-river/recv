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
import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.BlockchainMeta;

public class GatewayContract {
    private static final Logger log = LoggerFactory.getLogger(GatewayContract.class);

    private final BlockchainMeta mBlockchain;
    private final Credentials mCredentials;
    private final Gateway mGatewayContract;

    public GatewayContract(String privateKey, BlockchainMeta blockchain) {
        mBlockchain = blockchain;

        mCredentials = Credentials.create(privateKey);
        log.info("Credentials loaded, address: {}", mCredentials.getAddress());

        Web3j web3j = Web3j.build(new HttpService(blockchain.url));

        // TODO: fix gas limit
        mGatewayContract = Gateway.load(
                blockchain.gatewayAddress.toHex(),
                web3j, mCredentials, new DefaultGasProvider()
        );

        log.info("Gateway {} connected to {} network",
                blockchain.gatewayAddress.toHex(), blockchain.name);
    }

    private byte[] generateSalt(String salt) {
        byte[] bytes = salt.getBytes();
        return Hash.sha256(bytes);
    }

    private String generateInitCode() {
        return Wallet.BINARY +
                TypeEncoder.encode(mBlockchain.gatewayAddress);
    }

    private Address predictWalletAddress(byte[] salt) {
        String initCode = generateInitCode();

        byte[] address = ContractUtils.generateCreate2ContractAddress(
                mBlockchain.gatewayAddress.toBytes(), salt, Numeric.hexStringToByteArray(initCode)
        );
        return Address.fromBytes(address);
    }

    public Address predictWalletAddress(String saltValue) {
        byte[] salt = generateSalt(saltValue);

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

        return null;
    }

    public WalletContract getWallet(Address address) {
        return new WalletContract(address, mCredentials, mBlockchain);
    }
}
