package tech.yobit.web3.contract;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.*;

import java.math.BigInteger;
import java.util.*;

/**
 * 1. one blockchain one gateway
 * 2. one gateway controls multiple user
 * 3. one user multiple wallet, different wallet different blockchain
 * 4. all gateway is control by one private key
 */
public class GatewayContract {
    private static final Logger logger = LoggerFactory.getLogger(GatewayContract.class);

    private final Blockchain mBlockchain;
    private final Credentials mCredentials;
    private final ERC20Meta[] mCoinMetas;
    private final Map<Address, WalletContract> mWalletContracts = new HashMap<>();

    protected GatewayContract(@NotNull Credentials credentials, @NotNull Blockchain blockchain, @NotNull ERC20Meta[] coinMetas) {
        mCredentials = credentials;
        mBlockchain = blockchain;
        mCoinMetas = coinMetas;
    }

    @NotNull
    static private byte[] generateSalt(@NotNull String salt) {
        byte[] bytes = salt.getBytes();
        return Hash.sha256(bytes);
    }

    @NotNull
    private String generateInitCode() {
        return Wallet.BINARY + TypeEncoder.encode(Address.fromHex(mCredentials.getAddress()));
    }

    @NotNull
    private ContractAddress predictWalletAddress(@NotNull byte[] salt) {
        String initCode = generateInitCode();

        byte[] address = ContractUtils.generateCreate2ContractAddress(
                mBlockchain.gatewayAddress.toBytes(), salt, Numeric.hexStringToByteArray(initCode)
        );
        return new ContractAddress(mBlockchain.getId(), address);
    }

    @NotNull
    public ContractAddress predictWalletAddress(@NotNull String uid) {
        byte[] salt = generateSalt(uid);
        return predictWalletAddress(salt);
    }

    public boolean checkWalletAddress(@NotNull Address address) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(mBlockchain.rpcUrl));
        Gateway contract = Gateway.load(
                mBlockchain.gatewayAddress.toHex(),
                web3j, mCredentials,
                new GasProvider(mBlockchain.getId())
        );

        return contract.wallets(address.toHex()).send();
    }

    @NotNull
    private String buildCreateWalletTransactionData(byte[] salt) {
        Function function = new Function(
                Gateway.FUNC_CREATEWALLET,
                List.of(new Bytes32(salt)),
                Collections.emptyList()
        );

        return FunctionEncoder.encode(function);
    }

    @Nullable
    private BigInteger getEstimateGasLimit(BlockchainName blockchain, Address from, Address to, byte[] salt) {
        BigInteger gasLimit = GasProvider.getEstimateGas(
                blockchain,
                from.toHex(),
                to.toHex(),
                buildCreateWalletTransactionData(salt)
        );

        return gasLimit.equals(BigInteger.ZERO) ? null : gasLimit;
    }

    // salt is 32 bytes length
    @Nullable
    private ContractAddress createWallet(@NotNull byte[] salt) throws Exception {
        logger.info("createWallet in {}({}) network", mBlockchain.getName(), mBlockchain.getId());

        Web3j web3j = Web3j.build(new HttpService(mBlockchain.rpcUrl));
        BigInteger gasLimit = getEstimateGasLimit(mBlockchain.chain, Address.fromHex(mCredentials.getAddress()), mBlockchain.gatewayAddress, salt);
        if (gasLimit == null) {
            return null;
        }

        Gateway contract = Gateway.load(
                mBlockchain.gatewayAddress.toHex(),
                web3j, mCredentials,
                new GasProvider(mBlockchain.getId(), gasLimit)
        );

        TransactionReceipt tx = contract.createWallet(salt).send();

        logger.info("create wallet for {} in {}({}) network, txId {}, status {}, gas used {}",
                Numeric.toHexString(salt), mBlockchain.getName(), mBlockchain.getId(),
                tx.getTransactionHash(), tx.isStatusOK(), tx.getGasUsed()
        );

        if (!tx.isStatusOK()) {
            logger.warn("create wallet for {} failure, revert reason: {}",
                    Numeric.toHexString(salt), tx.getRevertReason());
            return null;
        }

        // the first logger is the contract creating logger generated by blockchain contract creating event
        String address = tx.getLogs().getFirst().getAddress();
        logger.info("deploy wallet contract {} for {} in {}({}) network",
                address, Numeric.toHexString(salt),
                mBlockchain.getName(), mBlockchain.getId()
        );
        return new ContractAddress(mBlockchain.getId(), address);
    }

    @Nullable
    public ContractAddress createWallet(@NotNull String uid) throws Exception {
        byte[] salt = generateSalt(uid);

        return createWallet(salt);
    }

    @NotNull
    synchronized protected WalletContract getWalletContract(@NotNull Address address, @NotNull Credentials credentials) {
        if (mWalletContracts.containsKey(address)) {
            return mWalletContracts.get(address);
        } else {
            WalletContract contract = new WalletContract(address, mBlockchain, mCoinMetas, credentials);
            mWalletContracts.put(new Address(address), contract);
            return contract;
        }
    }

    public BigInteger getLatestBlockchain() throws Exception{
        Web3j web3j = Web3j.build(new HttpService(mBlockchain.rpcUrl));
        return web3j.ethGetBlockByNumber(DefaultBlockParameterName.FINALIZED, false)
                .send()
                .getBlock()
                .getNumber();
    }
}
