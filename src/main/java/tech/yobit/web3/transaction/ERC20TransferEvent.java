package tech.yobit.web3.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;
import tech.yobit.web3.types.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ERC20TransferEvent {
    private static final Logger logger = LoggerFactory.getLogger(ERC20TransferEvent.class);

    private final Blockchain mBlockchain;
    private final ERC20Meta mCoinMeta;
    private final Address mFrom;
    private final Address mTo;

    public ERC20TransferEvent(Blockchain blockchain, ERC20Meta coinMeta, Address from, Address to) {
        mBlockchain = blockchain;
        mCoinMeta = coinMeta;
        mFrom = from;
        mTo = to;
    }

    /**
     * `event Transfer(address indexed from, address indexed to, uint256 value)`
     */
    static private EthFilter createEventFilter(BigInteger fromBlock, Address filterAddress) {
        Event event = new Event(
                "Transfer",
                Arrays.asList(
                        TypeReference.create(org.web3j.abi.datatypes.Address.class, true),
                        TypeReference.create(org.web3j.abi.datatypes.Address.class, true),
                        TypeReference.create(Uint256.class, false)
                )
        );

        return new EthFilter(
                new DefaultBlockParameterNumber(fromBlock),
                DefaultBlockParameterName.FINALIZED,
                filterAddress.toHex()
        ).addSingleTopic(EventEncoder.encode(event));
    }

    static public ERC20TransferEventResult getCompletedTransferEvents(
            String url, BigInteger fromBlock, ERC20Meta coinMeta, Address fromAddress, Address toAddress) throws Exception {
        ERC20TransferEventResult rc = new ERC20TransferEventResult();
        List<tech.yobit.web3.types.ERC20TransferEvent> events = new ArrayList<>();
        EthFilter filter = createEventFilter(fromBlock, coinMeta.contractAddress);

        if (fromAddress != null && toAddress != null) {
            filter.addSingleTopic(TypeEncoder.encode(fromAddress));
            filter.addSingleTopic(TypeEncoder.encode(toAddress));
        } else if (fromAddress != null) {
            filter.addSingleTopic(TypeEncoder.encode(fromAddress));
        } else if (toAddress != null) {
            filter.addNullTopic();
            filter.addSingleTopic(TypeEncoder.encode(toAddress));
        }

        Web3j web3j = Web3j.build(new HttpService(url));

        rc.blockNumber = web3j.ethGetBlockByNumber(DefaultBlockParameterName.FINALIZED, false)
                .send()
                .getBlock()
                .getNumber();
        List<EthLog.LogResult> logResults = web3j.ethGetLogs(filter).send().getLogs();
        if (logResults == null || logResults.isEmpty()) {
            return rc;
        }

        for (EthLog.LogResult logResult : logResults) {
            if (!(logResult instanceof EthLog.LogObject)) {
                logger.warn("Unexpected result type: {}, required LogObject", logResult.get());
                break;
            }

            Log log = ((EthLog.LogObject) logResult).get();
            List<String> topics = log.getTopics();
            // topics = { event_signature, from, to }
            Address from = Address.fromHex(topics.get(1));
            Address to = Address.fromHex(topics.get(2));

            String value = log.getData().substring(2); // strip `0x` prefix
            Coin coin = new Coin(new BigInteger(value, 16), coinMeta);
            tech.yobit.web3.types.ERC20TransferEvent val = new tech.yobit.web3.types.ERC20TransferEvent(
                    log.getBlockNumber(), log.getTransactionHash(), from, to, coin
            );

            if (rc.blockNumber.compareTo(log.getBlockNumber()) < 0) {
                rc.blockNumber = log.getBlockNumber();
            }

            events.add(val);
        }

        rc.events = events.toArray(new tech.yobit.web3.types.ERC20TransferEvent[0]);
        return rc;
    }

    public ERC20TransferEventResult getCompletedTransferEvents(BigInteger fromBlock) throws Exception {
        return getCompletedTransferEvents(mBlockchain.rpcUrl, fromBlock, mCoinMeta, mFrom, mTo);
    }
}
