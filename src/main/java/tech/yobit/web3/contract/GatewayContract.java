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
import tech.yobit.web3.types.ContractAddress;

/**
 * 1. one gateway multiple wallet
 * 2. one user multiple wallet
 * 3. different wallet different user, or different blockchain
 * 4. all gateway is control by one private key
 */
public class GatewayContract {
    private static final Logger log = LoggerFactory.getLogger(GatewayContract.class);

    private final BlockchainMeta mBlockchain;
    private final Web3j mWeb3j;
    private final Credentials mCredentials;
    private final Gateway mGatewayContract;

    // don't need to record WalletContract own to it maybe unlimited

    public GatewayContract(String privateKey, BlockchainMeta blockchain) {
        mBlockchain = blockchain;

        mCredentials = Credentials.create(privateKey);
        log.info("Credentials loaded, address: {}", mCredentials.getAddress());

        mWeb3j = Web3j.build(new HttpService(blockchain.url));
        mGatewayContract = Gateway.load(
                blockchain.gatewayAddress.toHex(),
                mWeb3j, mCredentials, new DefaultGasProvider() // TODO: fix gas limit
        );

        log.info("Gateway {} connected to {}({}) network",
                blockchain.gatewayAddress.toHex(), blockchain.name, blockchain.id);
    }

    private byte[] generateSalt(String salt) {
        byte[] bytes = salt.getBytes();
        return Hash.sha256(bytes);
    }

    private String generateInitCode() {
        Address args = Address.fromHex(mCredentials.getAddress());
        return Wallet.BINARY + TypeEncoder.encode(args);
    }

    private ContractAddress predictWalletAddress(byte[] salt)  {
        String initCode = generateInitCode();

        byte[] address = ContractUtils.generateCreate2ContractAddress(
                mBlockchain.gatewayAddress.toBytes(), salt, Numeric.hexStringToByteArray(initCode)
        );
        return new ContractAddress(mBlockchain.id, address);
    }

    public ContractAddress predictWalletAddress(String uid)  {
        byte[] salt = generateSalt(uid);
        return predictWalletAddress(salt);
    }

    public boolean checkWalletAddress(Address address) throws Exception {
        return mGatewayContract.wallets(address.toHex()).send();
    }

    /*
     *  1. return wallet address if wallet already exists
     *  2. create then return wallet address if wallet don't exist
     */
    public ContractAddress checkAndCreateWallet(String uid) throws Exception {
        byte[] salt = generateSalt(uid);
        ContractAddress walletAddress = predictWalletAddress(salt);

        boolean exist = checkWalletAddress(walletAddress);
        if (exist) {
            return walletAddress;
        }

        TransactionReceipt tx = mGatewayContract.createWallet(salt).send();
        log.info("create wallet in {}({}), txId {}, status {}, gas used {}",
                mBlockchain.name, mBlockchain.id,
                tx.getTransactionHash(), tx.isStatusOK(), tx.getGasUsed()
        );

        if (tx.isStatusOK()) {
            tx.getLogs().forEach(msg -> {
                msg.getTopics().forEach(topic -> {
                    log.info("topic: {}", topic);
                });
            });
            return walletAddress;
        } else {
            return null;
        }
    }

    public WalletContract getWalletContract(Address address) {
        return new WalletContract(address, mBlockchain, mCredentials);
    }
}
