package tech.yobit.web3.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.crypto.Credentials;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;

/**
 *  one private key controls all gateway contract
 */
public class GatewayManager {
    private static final Logger log = LoggerFactory.getLogger(GatewayManager.class);

    private final Credentials mCredentials;
    private final List<Blockchain> mBlockchains = new ArrayList<>();
    private final HashMap<Integer, GatewayContract> mGatewayContracts = new HashMap<>();

    public GatewayManager(String privateKey, Blockchain[] blockchains) {
        mCredentials = Credentials.create(privateKey);
        log.info("Credentials loaded, address: {}", mCredentials.getAddress());

        for (Blockchain chain : blockchains) {
            GatewayContract contract = new GatewayContract(mCredentials, chain);
            mGatewayContracts.put(chain.id, contract);
            mBlockchains.add(chain);
        }
    }

    public Blockchain[] getSupportedBlockchainTypes() {
        return mBlockchains.toArray(new Blockchain[0]);
    }

    public GatewayContract findGatewayContract(int blockchainId) {
        return mGatewayContracts.get(blockchainId);
    }

    public WalletContract getWalletContract(int blockchainId, Address address, String uid) {
        GatewayContract gatewayContract = findGatewayContract(blockchainId);
        if (gatewayContract == null) {
            return null;
        }

        return gatewayContract.getWalletContract(address, uid, mCredentials);
    }
}
