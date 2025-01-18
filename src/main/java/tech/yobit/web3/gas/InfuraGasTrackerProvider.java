package tech.yobit.web3.gas;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.InfuraGasTrackerConfig;
import tech.yobit.web3.types.*;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InfuraGasTrackerProvider implements GasTrackerProvider {
    private static final Logger logger = LoggerFactory.getLogger(InfuraGasTrackerProvider.class);

    private static final int GWEI_FACTOR = 9;
    private static final Set<BlockchainName> mSupportedBlockchains = Set.of(
            BlockchainName.ETHEREUM,
            BlockchainName.BNB_SMART_CHAIN,
            BlockchainName.SEPOLIA
    );
    private static InfuraGasTrackerConfig mConfig = null;

    public InfuraGasTrackerProvider(GasTrackerConfig config) {
        mConfig = (InfuraGasTrackerConfig) config;
    }

    @NotNull
    @Override
    public String getName() {
        return InfuraGasTrackerProvider.class.getSimpleName();
    }

    @Override
    public boolean isSupported(BlockchainName blockchain) {
        return mConfig != null && mSupportedBlockchains.contains(blockchain);
    }

    @NotNull
    private GasTracker toGasTracker(InfuraGasPriceResponse data) {
        InfuraGasPriceResponse.PriceLevel low = data.low;
        InfuraGasPriceResponse.PriceLevel medium = data.medium;
        InfuraGasPriceResponse.PriceLevel high = data.high;
        GasTracker tracker = new GasTracker();

        tracker.minGasPrice = ERC20Meta.parseUnits(low.suggestedMaxFeePerGas, GWEI_FACTOR);
        tracker.normalGasPrice = ERC20Meta.parseUnits(medium.suggestedMaxFeePerGas, GWEI_FACTOR);
        tracker.maxGasPrice = ERC20Meta.parseUnits(high.suggestedMaxFeePerGas, GWEI_FACTOR);

        tracker.supportEIP1559 = true;
        tracker.safePriorityFee = ERC20Meta.parseUnits(low.suggestedMaxPriorityFeePerGas, GWEI_FACTOR);
        tracker.proposePriorityFee = ERC20Meta.parseUnits(medium.suggestedMaxPriorityFeePerGas, GWEI_FACTOR);
        tracker.fastPriorityFee = ERC20Meta.parseUnits(high.suggestedMaxPriorityFeePerGas, GWEI_FACTOR);
        tracker.suggestBaseFee =
                ERC20Meta.parseUnits(low.suggestedMaxFeePerGas, GWEI_FACTOR)
                        .subtract(ERC20Meta.parseUnits(low.suggestedMaxPriorityFeePerGas, GWEI_FACTOR))
                        .add(
                                ERC20Meta.parseUnits(medium.suggestedMaxFeePerGas, GWEI_FACTOR)
                                        .divide(ERC20Meta.parseUnits(high.suggestedMaxPriorityFeePerGas, GWEI_FACTOR))
                        ).divide(new BigInteger("2"));

        return tracker;
    }

    @Override
    @Nullable
    public GasTracker getGasTracker(BlockchainName blockchain) {
        logger.info("request gas tracker for blockchain: {}({})", blockchain, blockchain.getId());

        if (mConfig == null) {
            return null;
        }

        String auth = Base64.getEncoder().encodeToString((mConfig.apiKey + ":" + mConfig.apiKeySecret).getBytes());
        String url = "https://gas.api.infura.io/networks/" + blockchain.getId() + "/suggestedGasFees";
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Basic " + auth)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String rawData = response.body().string();
                logger.debug("getGasTracker: {}", rawData);
                InfuraGasPriceResponse data = new Gson().fromJson(rawData, InfuraGasPriceResponse.class);
                return toGasTracker(data);
            } else {
                logger.warn("Something wrong: {}", response.code());
            }
        } catch (Exception e) {
            logger.error("Failed to get gas tracker", e);
        }

        return null;
    }

    @Override
    @Nullable
    public BigInteger estimateGas(BlockchainName blockchain, @NotNull String from, @NotNull String to, @NotNull String data) {
        return null;
    }
}
