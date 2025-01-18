package tech.yobit.web3.contract;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Coin;
import tech.yobit.web3.types.ContractAddress;
import tech.yobit.web3.types.ERC20Meta;
import tech.yobit.web3.utils.SetupTest;

import java.math.BigInteger;

public class WalletContractTest {
    private static final Logger logger = LoggerFactory.getLogger(WalletContractTest.class);

    private static Configuration config;
    private static GatewayManager manager;
    private static WalletContract wallet;
    private static final Address address = Address.fromHex("0x94F9652C6B7981BD821D88025163146979D1527E");

    @BeforeAll
    public static void init() {
        config = SetupTest.buildConfig();

        // Initialize gas provider
        GasProvider.initialize(config.gasTrackers);

        try {
            manager = new GatewayManager(
                    config.privateKey,
                    config.getBlockchainTypes(),
                    config.getCoinMetaTypes()
            );
            wallet = manager.getWalletContract(11_155_111, address);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testGetContractAddress() {
        ContractAddress address = wallet.getContractAddress();
        logger.info("Wallet contract address: {}", address.toHex());
    }

    @Test
    public void testCoinMeta() {
        ERC20Meta[] coins = wallet.getCoinMetaTypes();
        logger.info("Wallet has {} coins", coins.length);
        Assertions.assertNotEquals(0, coins.length);

        wallet.updateCoinMeta(coins[0]);
        ERC20Meta coin = wallet.findCoinMeta(coins[0].contractAddress);
        Assertions.assertNotNull(coin);
    }

    @Test
    public void testGetCoinBalance() {
        ERC20Meta[] coins = wallet.getCoinMetaTypes();
        try {
            BigInteger amount = wallet.getCoinBalance(coins[0].contractAddress);
            Assertions.assertNotNull(amount);
            logger.info("Coin {} balance: {}", coins[0].name, Coin.formatUnits(amount, coins[0].decimals));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testWithdrawCoin() {
        ERC20Meta[] coins = wallet.getCoinMetaTypes();
        Coin coin = new Coin("1", coins[0]);
        try {
            String txId = wallet.withdraw(wallet.getContractAddress(), coin);
            Assertions.assertNotNull(txId);
            logger.info("Withdraw(Coin) {}({}) to {}, txId {}", coin.value, coin.formatUnits(), wallet.getContractAddress().toHex(), txId);

            Coin fakeCoin = new Coin("1",
                    new ERC20Meta(
                            "Fake",
                            "Fake",
                            18,
                            1,
                            ContractAddress.fromHex("0x0000000000000000000000000000000000000000"))
            );
            Assertions.assertNull(wallet.withdraw(wallet.getContractAddress(), fakeCoin));

        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testWithdrawBigInteger() {
        ERC20Meta[] coins = wallet.getCoinMetaTypes();
        Coin coin = new Coin("1", coins[0]);
        try {
            String txId = wallet.withdraw(wallet.getContractAddress(), coin.value, coin.contractAddress);
            Assertions.assertNotNull(txId);
            logger.info("Withdraw(BigInteger) {}({}) to {}, txId {}", coin.value, coin.formatUnits(), wallet.getContractAddress().toHex(), txId);

            Coin fakeCoin = new Coin("1",
                    new ERC20Meta(
                            "Fake",
                            "Fake",
                            18,
                            1,
                            ContractAddress.fromHex("0x0000000000000000000000000000000000000000"))
            );
            Assertions.assertNull(wallet.withdraw(wallet.getContractAddress(), fakeCoin.value, fakeCoin.contractAddress));

        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testWithdrawString() {
        ERC20Meta[] coins = wallet.getCoinMetaTypes();
        Coin coin = new Coin("1", coins[0]);
        try {
            String txId = wallet.withdraw(wallet.getContractAddress(), coin.formatUnits(), coin.contractAddress);
            Assertions.assertNotNull(txId);
            logger.info("Withdraw(String) {}({}) to {}, txId {}", coin.value, coin.formatUnits(), wallet.getContractAddress().toHex(), txId);

            Coin fakeCoin = new Coin("1",
                    new ERC20Meta(
                            "Fake",
                            "Fake",
                            18,
                            1,
                            ContractAddress.fromHex("0x0000000000000000000000000000000000000000"))
            );
            Assertions.assertNull(wallet.withdraw(wallet.getContractAddress(), fakeCoin.value.toString(), fakeCoin.contractAddress));

        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
