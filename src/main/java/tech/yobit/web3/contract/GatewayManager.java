package tech.yobit.web3.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Blockchain;

/**
 *  one private key controls all gateway contract
 */
public class GatewayManager {
    private static final Logger log = LoggerFactory.getLogger(GatewayManager.class);

    private final String mPrivateKey;
    private final List<Blockchain> mBlockchains = new ArrayList<>();
    private final HashMap<Integer, GatewayContract> mGatewayContracts = new HashMap<>();

    public GatewayManager(String privateKey, Blockchain[] blockchains) {
        mPrivateKey = privateKey;
        mBlockchains.addAll(List.of(blockchains));
    }

    public boolean updateGatewayContract(int blockchainId, Address address) {
        Blockchain blockchain = null;

        for (Blockchain chain : mBlockchains) {
            if (chain.id == blockchainId) {
                blockchain = chain;
                break;
            }
        }
        if (blockchain == null) {
            log.warn("blockchainId {} don't support", blockchainId);
            return false;
        }

        if (mGatewayContracts.containsKey(blockchainId)) {
            log.warn("Gateway contract {} already exists in blockchain {}", address.toHex(), blockchainId);
            return false;
        }

        GatewayContract contract = new GatewayContract(mPrivateKey, blockchain);
        mGatewayContracts.put(blockchainId, contract);
        return true;
    }

    public GatewayContract findGatewayContract(int blockchainId) {
        return mGatewayContracts.get(blockchainId);
    }
}
