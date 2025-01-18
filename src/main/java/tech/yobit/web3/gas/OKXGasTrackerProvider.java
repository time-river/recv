package tech.yobit.web3.gas;

import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.types.*;
import tech.yobit.web3.types.Address;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OKXGasTrackerProvider implements GasTrackerProvider {
    private static final Logger logger = LoggerFactory.getLogger(OKXGasTrackerProvider.class);

    private static final Set<BlockchainName> mSupportedBlockchains = Set.of(
            BlockchainName.ETHEREUM,
            BlockchainName.POLYGON,
            BlockchainName.BNB_SMART_CHAIN,
            BlockchainName.SEPOLIA
    );
    private static final String mDomain = "https://www.okx.com";
    private static OKXGasTrackerConfig mConfig = null;

    public OKXGasTrackerProvider(GasTrackerConfig config) {
        mConfig = (OKXGasTrackerConfig) config;
    }

    @NotNull
    @Override
    public String getName() {
        return OKXGasTrackerProvider.class.getSimpleName();
    }

    @Override
    public boolean isSupported(BlockchainName blockchain) {
        return mConfig != null && mSupportedBlockchains.contains(blockchain);
    }

    // refer: https://www.okx.com/zh-hans/web3/build/docs/waas/rest-authentication
    private String generateSignature(String method, String path, String timestamp, String body) throws NoSuchAlgorithmException, InvalidKeyException {
        String message = timestamp + method + path + body;
        Mac sha256Mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(mConfig.secretKey.getBytes(), "HmacSHA256");
        sha256Mac.init(secretKeySpec);
        byte[] signData = sha256Mac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(signData);
    }

    private String getNowISOTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return Instant.now().atZone(ZoneId.of("UTC")).format(formatter);
    }

    @NotNull
    private GasTracker toGasTracker(OKXGasPriceResponse.DataItem data) {
        GasTracker gasTracker = new GasTracker();

        gasTracker.normalGasPrice = data.normal;
        gasTracker.minGasPrice = data.min;
        gasTracker.maxGasPrice = data.max;
        gasTracker.supportEIP1559 = data.supportEip1559;
        if (gasTracker.supportEIP1559) {
            gasTracker.safePriorityFee = data.eip1559Protocol.safePriorityFee;
            gasTracker.proposePriorityFee = data.eip1559Protocol.proposePriorityFee;
            gasTracker.fastPriorityFee = data.eip1559Protocol.fastPriorityFee;
            gasTracker.suggestBaseFee = data.eip1559Protocol.suggestBaseFee;
        }

        return gasTracker;
    }

    @Nullable
    @Override
    public GasTracker getGasTracker(BlockchainName blockchain) {
        logger.info("request gas tracker for blockchain: {}({})", blockchain, blockchain.getId());

        if (mConfig == null) {
            return null;
        }

        String path = "/api/v5/wallet/pre-transaction/gas-price?chainIndex=" + blockchain.getId();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        try {
            String timestamp = getNowISOTimestamp();
            String signature = generateSignature("GET", path, timestamp, "");

            Request request = new Request.Builder()
                    .url(mDomain + path)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("OK-ACCESS-KEY", mConfig.apiKey)
                    .addHeader("OK-ACCESS-SIGN", signature)
                    .addHeader("OK-ACCESS-TIMESTAMP", timestamp)
                    .addHeader("OK-ACCESS-PASSPHRASE", mConfig.passphrase)
                    .addHeader("OK-ACCESS-PROJECT", mConfig.project)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String rawData = response.body().string();
                logger.debug("getGasTracker: {}", rawData);
                OKXGasPriceResponse data = new Gson().fromJson(rawData, OKXGasPriceResponse.class);
                if (data.code.equals("0")) {
                    GasTracker tracker = toGasTracker(data.data[0]);
                    if (tracker.supportEIP1559 == blockchain.isSupportedEIP1559()) {
                        return tracker;
                    } else {
                        logger.warn("EIP1559 support has problem for {} in OKX", blockchain);
                    }
                } else {
                    logger.info("Something wrong: {}", data.msg);
                }
            } else if (response.body() != null) {
                logger.warn("Failed to get gas tracker, response: {}", response.body().string());
            } else {
                logger.warn("Failed to get gas tracker, response is null");
            }
        } catch (Exception e) {
            logger.warn("Failed to get gas tracker", e);
        }

        return null;
    }

    @NotNull
    private String createGasLimitRequestBody(long blockchainId, @NotNull String from, @NotNull String to, @NotNull String extJson) {
        OKXGasLimitRequest form = new OKXGasLimitRequest();
        form.chainIndex = String.valueOf(blockchainId);
        form.fromAddr = from;
        form.toAddr = to;
        form.extJson = new OKXGasLimitRequest.ExtJSON();
        form.extJson.inputData = extJson;

        return new Gson().toJson(form);
    }

    @Override
    @Nullable
    public BigInteger estimateGas(BlockchainName blockchain, @NotNull String from, @NotNull String to, @NotNull String data) {
        if (mConfig == null) {
            return null;
        }

        String path = "/api/v5/wallet/pre-transaction/gas-limit";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        try {
            String timestamp = getNowISOTimestamp();
            String body = createGasLimitRequestBody(blockchain.getId(), from, to, data);
            String signature = generateSignature("POST", path, timestamp, body);

            Request request = new Request.Builder()
                    .url(mDomain + path)
                    .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("OK-ACCESS-KEY", mConfig.apiKey)
                    .addHeader("OK-ACCESS-SIGN", signature)
                    .addHeader("OK-ACCESS-TIMESTAMP", timestamp)
                    .addHeader("OK-ACCESS-PASSPHRASE", mConfig.passphrase)
                    .addHeader("OK-ACCESS-PROJECT", mConfig.project)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                logger.warn("Failed to estimate gas, response is failure");
                return null;
            }

            String rawData = response.body().string();
            logger.debug("EstimateGas: {}", rawData);
            OKXGasLimitResponse gasLimit = new Gson().fromJson(rawData, OKXGasLimitResponse.class);
            // error msg, e.g.
            // {
            //  "code" : "81451",
            //  "msg" : "node return failed, {"code":1,"msg":"node result error when get estimateGas error chain=SEPOLIA code=-32000 message=execution reverted"}",
            //  "data" : [ ]
            // }
            if (!gasLimit.code.equals("0")) {
                logger.warn("Failed to estimate gas, response: {}", rawData);
                return BigInteger.ZERO;
            }

            // gas limit: the 1.25 times estimation
            BigInteger base = new BigInteger(gasLimit.data[0].gasLimit);
            return base.multiply(new BigInteger("5")).divide(new BigInteger("4"));
        } catch (Exception e) {
            logger.warn("Failed to estimate gas", e);
        }

        return null;
    }
}
