package tech.yobit.web3.contract;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;
import tech.yobit.web3.types.ERC20Meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * one private key controls all gateway contract
 */
public class GatewayManager {
    private static final Logger logger = LoggerFactory.getLogger(GatewayManager.class);

    private final Credentials mCredentials;
    private final List<Blockchain> mBlockchains = new ArrayList<>();
    private final HashMap<Long, GatewayContract> mGatewayContracts = new HashMap<>();

    public GatewayManager(@NotNull String privateKey,
                          @NotNull Blockchain[] blockchains,
                          @NotNull ERC20Meta[] coinMetas,
                          @NotNull GasTrackerConfig[] gasTrackerConfigs) {
        GasProvider.initialize(gasTrackerConfigs);

        mCredentials = Credentials.create(privateKey);
        logger.info("Credentials loaded, address: {}", mCredentials.getAddress());

        for (Blockchain chain : blockchains) {
            List<ERC20Meta> coinMetaList = new ArrayList<>();
            for (ERC20Meta coinMeta : coinMetas) {
                if (coinMeta.contractAddress.blockchainId == chain.getId()) {
                    coinMetaList.add(coinMeta);
                }
            }

            GatewayContract contract = new GatewayContract(mCredentials, chain, coinMetaList.toArray(new ERC20Meta[0]));
            mGatewayContracts.put(chain.getId(), contract);
            mBlockchains.add(chain);
        }
    }

    @NotNull
    public Blockchain[] getSupportedBlockchainTypes() {
        return mBlockchains.toArray(new Blockchain[0]);
    }

    @Nullable
    public GatewayContract findGatewayContract(long blockchainId) {
        return mGatewayContracts.get(blockchainId);
    }

    @Nullable
    public WalletContract getWalletContract(long blockchainId, @NotNull Address address) {
        GatewayContract gatewayContract = findGatewayContract(blockchainId);
        if (gatewayContract == null) {
            return null;
        }

        return gatewayContract.getWalletContract(address, mCredentials);
    }
}
