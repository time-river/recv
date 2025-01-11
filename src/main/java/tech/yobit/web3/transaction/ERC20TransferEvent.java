package tech.yobit.web3.transaction;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Coin;
import tech.yobit.web3.types.ERC20Meta;
import tech.yobit.web3.types.ERC20TransferEventMessage;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class ERC20TransferEvent {
    private Logger log = LoggerFactory.getLogger(ERC20TransferEvent.class);

    private final String mUrl;
    private final BigInteger mFromBlock;
    private final ERC20Meta mERC20Meta;
    private final Set<Address> mFromAddress = new CopyOnWriteArraySet<>();
    private final Set<Address> mToAddress = new CopyOnWriteArraySet<>();
    private final Callback mCallback;

    public ERC20TransferEvent(String url, BigInteger fromBlock, ERC20Meta meta, Callback callback)  {
        mUrl = url;
        mFromBlock = fromBlock;
        mERC20Meta = meta;
        mCallback = callback;
    }

    public void addFromAddress(Address address) {
        mFromAddress.add(address);
    }

    public void addToAddress(Address address) {
        mToAddress.add(address);
    }

    /**
     * `event Transfer(address indexed from, address indexed to, uint256 value)`
     */
    static public EthFilter createEventFilter(BigInteger fromBlock, Address filterAddress)  {
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

    public void start() {
        EthFilter filter = createEventFilter(mFromBlock, mERC20Meta.contractAddress);
        Web3j web3j = Web3j.build(new HttpService(mUrl));
        Disposable watch = web3j.ethLogFlowable(filter).subscribe(log -> {
            List<String> topics = log.getTopics();
            // topics = { event_signature, from, to }
            Address from = Address.fromHex(topics.get(1));
            Address to = Address.fromHex(topics.get(2));

            if (mFromAddress.contains(from) || mToAddress.contains(to)) {
                String value = log.getData();
                Coin coin = new Coin(new BigInteger(value, 16), mERC20Meta);
                ERC20TransferEventMessage msg = new ERC20TransferEventMessage(
                        log.getBlockNumber(), log.getTransactionHash(), from, to, coin
                );
                mCallback.onReceived(msg);
            }
        });
    }

    static public ERC20TransferEventMessage[] getCompletedTransferEvents(
            String url, BigInteger fromBlock, ERC20Meta coinMeta, Address fromAddress, Address toAddress) throws Exception {
        List<ERC20TransferEventMessage> rc = new ArrayList<>();
        EthFilter filter = createEventFilter(fromBlock, coinMeta.contractAddress);

        /* ensure the same class when `equals` */
        if (fromAddress != null) {
           // filter.addOptionalTopics(null, fromAddress.toHex());
            fromAddress = Address.fromBytes(fromAddress.toBytes());
        }
        if (toAddress != null) {
            //filter.addOptionalTopics(null, null, toAddress.toHex());
            toAddress = Address.fromBytes(toAddress.toBytes());
        }

        Web3j web3j = Web3j.build(new HttpService(url));
        List<EthLog.LogResult> logs = web3j.ethGetLogs(filter).send().getLogs();
        if (logs.isEmpty()) {
            return null;
        }

        for (EthLog.LogResult data : logs) {
            Log log = (Log)data;
            List<String> topics = log.getTopics();
            // topics = { event_signature, from, to }
            Address from = Address.fromHex(topics.get(1));
            Address to = Address.fromHex(topics.get(2));

            if (from.equals(fromAddress) || to.equals(toAddress)) {
                String value = log.getData().substring(2); // strip `0x` prefix
                Coin coin = new Coin(new BigInteger(value, 16), coinMeta);
                ERC20TransferEventMessage msg = new ERC20TransferEventMessage(
                        log.getBlockNumber(), log.getTransactionHash(), from, to, coin
                );

                rc.add(msg);
            }
        }

        return rc.toArray(new ERC20TransferEventMessage[0]);
    }

    static public interface Callback {
        void onReceived(ERC20TransferEventMessage msg);
    }
}
