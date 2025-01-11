package tech.yobit.web3.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
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
    private static final Logger log = LoggerFactory.getLogger(GatewayManager.class);

    private final Credentials mCredentials;
    private final List<Blockchain> mBlockchains = new ArrayList<>();
    private final HashMap<Long, List<ERC20Meta>> mCoinMetas = new HashMap<>();
    private final HashMap<Long, GatewayContract> mGatewayContracts = new HashMap<>();

    public GatewayManager(String privateKey, Blockchain[] blockchains, ERC20Meta[] coinMetas) {
        mCredentials = Credentials.create(privateKey);
        log.info("Credentials loaded, address: {}", mCredentials.getAddress());

        for (Blockchain chain : blockchains) {
            GatewayContract contract = new GatewayContract(mCredentials, chain);
            mGatewayContracts.put(chain.id, contract);
            mBlockchains.add(chain);

            List<ERC20Meta> coinMetaList = new ArrayList<>();
            mCoinMetas.put(chain.id, coinMetaList);
            for (ERC20Meta coinMeta : mCoinMetas.get(chain.id)) {
                if (coinMeta.contractAddress.blockchainId == chain.id) {
                    coinMetaList.add(coinMeta);
                }
            }
        }
    }

    public Blockchain[] getSupportedBlockchainTypes() {
        return mBlockchains.toArray(new Blockchain[0]);
    }

    public GatewayContract findGatewayContract(long blockchainId) {
        return mGatewayContracts.get(blockchainId);
    }

    public WalletContract getWalletContract(long blockchainId, Address address, String uid) {
        GatewayContract gatewayContract = findGatewayContract(blockchainId);
        if (gatewayContract == null || !mCoinMetas.containsKey(blockchainId)) {
            return null;
        }

        return gatewayContract.getWalletContract(
                address, mCoinMetas.get(blockchainId).toArray(new ERC20Meta[0]),
                uid, mCredentials
        );
    }
}
