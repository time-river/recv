package tech.yobit.web3;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.utils.Base58;
import tech.yobit.web3.types.BlockchainMeta;
import tech.yobit.web3.types.CoinMeta;

public class CryptoSupport {
    private static final Logger log = LoggerFactory.getLogger(CryptoSupport.class);

    static private final Map<Integer, BlockchainMeta> mBlockchains = new HashMap<>();
    static private final Set<CoinMeta> mCryptoCoins = new HashSet<>();
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

        for (Configuration.Blockchain chain : config.blockchains) {
            String url = config.defaultUrl;

            if (!chain.url.isEmpty()) {
                url = chain.url;
            }

            BlockchainMeta obj = new BlockchainMeta(
                    chain.name, chain.id, url, parseAddress(chain.gatewayAddress)
            );
            mBlockchains.put(chain.id, obj);
        }

        for (Configuration.Coin coin : config.coins) {
            for (Configuration.Coin.Blockchain chain : coin.blockchains) {
                BlockchainMeta blockchain = mBlockchains.get(chain.id);

                CoinMeta obj = new CoinMeta(
                        coin.name, coin.fullName, coin.decimals,
                        blockchain, parseAddress(chain.coinContractAddress)
                );

                mCryptoCoins.add(obj);
            }
        }

        mInitialized = true;
        log.info("CryptoSupport initialized {} coins", mCryptoCoins.size());
    }

    public CoinMeta[] getCryptoSupportInformation() {
        if (!mInitialized)  {
            log.info("CryptoSupport isn't initialized");
            return null;
        }

        return mCryptoCoins.toArray(new CoinMeta[0]);
    }
}
