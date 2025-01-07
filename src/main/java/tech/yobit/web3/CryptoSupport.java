package tech.yobit.web3;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.utils.Address;
import tech.yobit.web3.utils.Base58;
import tech.yobit.web3.utils.Blockchain;
import tech.yobit.web3.utils.CryptoCoin;

public class CryptoSupport {
    private static final Logger log = LoggerFactory.getLogger(CryptoSupport.class);

    static private final Map<Integer, Blockchain> mBlockchains = new HashMap<>();
    static private final Set<CryptoCoin> mCryptoCoins = new HashSet<>();
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

            Blockchain obj = new Blockchain(
                    chain.name, chain.id, url, parseAddress(chain.gatewayAddress)
            );
            mBlockchains.put(chain.id, obj);
        }

        for (Configuration.Coin coin : config.coins) {
            for (Configuration.Coin.Blockchain chain : coin.blockchains) {
                Blockchain blockchain = mBlockchains.get(chain.id);

                CryptoCoin obj = new CryptoCoin(
                        coin.name, coin.fullName, coin.decimal,
                        blockchain, parseAddress(chain.contractAddress)
                );

                mCryptoCoins.add(obj);
            }
        }

        mInitialized = true;
        log.info("CryptoSupport initialized {} coins", mSet.size());
    }

    public CryptoCoin[] getCryptoSupportInformation() {
        if (!mInitialized)  {
            log.info("CryptoSupport isn't initialized");
            return null;
        }

        return mCryptoCoins.toArray(new CryptoCoin[0]);
    }
}
