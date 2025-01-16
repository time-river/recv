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
import java.util.*;

public class ERC20TransferEventImpl {
    private static final Logger logger = LoggerFactory.getLogger(ERC20TransferEventImpl.class);

    private final Blockchain mBlockchain;
    private final ERC20Meta mCoinMeta;
    private final Address mFrom;
    private final Address mTo;

    public ERC20TransferEventImpl(Blockchain blockchain, ERC20Meta coinMeta, Address from, Address to) {
        mBlockchain = blockchain;
        mCoinMeta = coinMeta;
        mFrom = from;
        mTo = to;
    }

    public Result getCompletedTransferEvents(BigInteger fromBlock) throws Exception {
        return getCompletedTransferEvents(mBlockchain.rpcUrl, fromBlock, mCoinMeta, mFrom, mTo);
    }

    /**
     * `event Transfer(address indexed from, address indexed to, uint256 value)`
     */
    static private EthFilter createEventFilter(BigInteger fromBlock, Address filterAddress)  {
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

    static public Result getCompletedTransferEvents(
            String url, BigInteger fromBlock, ERC20Meta coinMeta, Address fromAddress, Address toAddress) throws Exception {
        Result rc = new Result();
        List<ERC20TransferEvent> events = new ArrayList<>();
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

        rc.blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        List<EthLog.LogResult> logs = web3j.ethGetLogs(filter).send().getLogs();
        if (logs == null || logs.isEmpty()) {
            return rc;
        }

        for (EthLog.LogResult logResult : logs) {
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
            ERC20TransferEvent val = new ERC20TransferEvent(
                    log.getBlockNumber(), log.getTransactionHash(), from, to, coin
            );

            events.add(val);
        }

        rc.events = events.toArray(new ERC20TransferEvent[0]);
        return rc;
    }

    static public class Result {
        public BigInteger blockNumber;
        public ERC20TransferEvent[] events = new ERC20TransferEvent[0];

        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("blockNumber: ").append(blockNumber.toString());
            for (ERC20TransferEvent event : events) {
                builder.append("\n\\-").append(event.toString());
            }

            return builder.toString();
        }
    }
}
