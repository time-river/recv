package tech.yobit.web3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.types.ERC20Meta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @firstDepositAmount: 第一次充值最小金额
 * @minimumWithdrawalAmount: 提现最小金额
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public String firstMinimumDepositAmount;
    public String minimumWithdrawalAmount;
    public String privateKey;
    public CoinConfig[] coins;
    public BlockchainConfig[] blockchains;
    public GasTrackerConfig[] gasTrackers;

    private Map<Long, Blockchain> getBlockchains() {
        Map<Long, Blockchain> blockchains = new HashMap<>();

        for (BlockchainConfig chain : this.blockchains) {

            BlockchainName blockchain = BlockchainName.from(chain.id);
            if (chain.name == null || chain.name.isEmpty()) {
                logger.warn("blockchain {} name is empty", chain.id);
            } else if (chain.rpcUrl == null || chain.rpcUrl.isEmpty()) {
                throw new RuntimeException("blockchain " + chain.name + " RPC URL is empty");
            } else if (chain.blockExplorerUrl == null || chain.blockExplorerUrl.isEmpty()) {
                throw new RuntimeException("blockchain " + chain.name + " block explorer URL is empty");
            } else if (chain.gatewayAddress == null || chain.gatewayAddress.isEmpty()) {
                throw new RuntimeException("blockchain " + chain.name + " gateway address is empty");
            }

            Blockchain obj = new Blockchain(
                    blockchain, chain.rpcUrl, chain.blockExplorerUrl, Address.fromString(chain.gatewayAddress)
            );

            blockchains.put(chain.id, obj);
        }

        return blockchains;
    }

    public ERC20Meta[] getCoinMetaTypes() throws Exception {
        Map<Long, Blockchain> blockchains = getBlockchains();
        Set<ERC20Meta> coins = new HashSet<>();

        for (CoinConfig coin : this.coins) {
            for (CoinConfig.Blockchain chain : coin.blockchains) {
                Blockchain blockchain = blockchains.get(chain.id);
                if (blockchain == null) {
                    throw new RuntimeException("Blockchain not found: " + chain.id);
                }

                ERC20Meta obj = new ERC20Meta(
                        coin.name, coin.fullName, coin.decimals,
                        blockchain.getId(), Address.fromString(chain.coinContractAddress)
                );

                coins.add(obj);
            }
        }

        return coins.toArray(new ERC20Meta[0]);
    }

    public Blockchain[] getBlockchainTypes() {
        return getBlockchains().values().toArray(new Blockchain[0]);
    }

    public GasTrackerConfig[] getGasTrackerConfigs() {
        return gasTrackers;
    }
}
