package tech.yobit.web3;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.BlockchainConfig;
import tech.yobit.web3.config.CoinConfig;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;
import tech.yobit.web3.utils.Base58;
import tech.yobit.web3.types.CoinMeta;

public class CryptoSupport {
    private static final Logger log = LoggerFactory.getLogger(CryptoSupport.class);

    static private final Map<Integer, Blockchain> mBlockchains = new HashMap<>();
    static private final Set<CoinMeta> mCoinMetas = new HashSet<>();
    static private boolean mInitialized = false;

    static private Address parseAddress(String str) {
        Address address;

        if (Base58.isValidBase58((str))) {
            address = Address.fromBase58(str);
        } else {
            address = Address.fromHex(str);
        }

        return address;
    }

    synchronized public static void parseConfig(Configuration config) {
        if (mInitialized) {
            log.info("CryptoSupport has been initialized");
            return;
        }

        for (BlockchainConfig chain : config.blockchains) {
            String url = config.defaultUrl;

            if (!chain.url.isEmpty()) {
                url = chain.url;
            }

            Blockchain obj = new Blockchain(
                    chain.name, chain.id, url, parseAddress(chain.gatewayAddress)
            );
            mBlockchains.put(chain.id, obj);
        }

        for (CoinConfig coin : config.coins) {
            for (CoinConfig.Blockchain chain: coin.blockchains) {
                Blockchain blockchain = mBlockchains.get(chain.id);

                CoinMeta obj = new CoinMeta(
                        coin.name, coin.fullName, coin.decimals,
                        blockchain.id, parseAddress(chain.coinContractAddress)
                );

                mCoinMetas.add(obj);
            }
        }

        mInitialized = true;
        log.info("CryptoSupport initialized {} coins, {} blockchains",
                mCoinMetas.size(), mBlockchains.size());
    }

    public CoinMeta[] getCoinMetas() {
        if (!mInitialized)  {
            log.info("CryptoSupport isn't  initialized");
            return null;
        }

        return mCoinMetas.toArray(new CoinMeta[0]);
    }

    public Blockchain[] getBlockchains() {
        if (!mInitialized)  {
            log.info("CryptoSupport isn't initialized");
            return null;
        }

        return mBlockchains.values().toArray(new Blockchain[0]);
    }
}
