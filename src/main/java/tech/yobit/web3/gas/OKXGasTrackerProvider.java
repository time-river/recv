package tech.yobit.web3.gas;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.types.GasTracker;
import tech.yobit.web3.types.OKXGasLimitRequest;
import tech.yobit.web3.types.OKXGasLimitResponse;
import tech.yobit.web3.types.OKXGasPriceResponse;
import tech.yobit.web3.utils.Constant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class OKXGasTrackerProvider implements GasTrackerProvider {
    private static final Logger logger = LoggerFactory.getLogger(OKXGasTrackerProvider.class);

    private static final String mDomain = "https://www.okx.com";
    private static OKXGasTrackerConfig mConfig;
    private static long[] mBlockchainIds;

    public OKXGasTrackerProvider(GasTrackerConfig config) {
        mConfig = (OKXGasTrackerConfig) config;
        mBlockchainIds = Arrays.stream(mConfig.blockchainIds).mapToLong(Long::parseLong).toArray();
    }

    @Override
    public boolean isSupported(long blockchainId) {
        for (long id : mBlockchainIds) {
            if (id == blockchainId) {
                return true;
            }
        }
        return false;
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

    @Override
    public GasTracker getGasTracker(long blockchainId) {
        String path = "/api/v5/wallet/pre-transaction/gas-price?chainIndex=" + blockchainId;
        GasTracker tracker = new GasTracker();

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
                logger.debug("OKXGasTrackerImpl getGasTracker: {}", rawData);
                OKXGasPriceResponse data = new Gson().fromJson(rawData, OKXGasPriceResponse.class);
                if (data.code.equals("0")) {
                    tracker = GasTracker.fromOKXGasTracker(data.data[0]);
                }
            } else if (response.body() != null)  {
                logger.warn("Failed to get gas tracker, response: {}", response.body().string());
            } else {
                logger.warn("Failed to get gas tracker, response is null");
            }
        } catch (Exception e) {
            logger.warn("Failed to get gas tracker", e);
        }

        return tracker;
    }

    private String createGasLimitRequestBody(long blockchainId, String extJson) {
        OKXGasLimitRequest form = new OKXGasLimitRequest();
        form.chainIndex = String.valueOf(blockchainId);
        form.fromAddr = Constant.ZERO_ADDRESS;
        form.toAddr = Constant.ZERO_ADDRESS;
        form.extJson.inputData = extJson;

        return new Gson().toJson(form);

    }

    @Override
    public BigInteger estimateGas(long blockchainId, String data) {
        String path = "/api/v5/wallet/pre-transaction/gas-limit";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        try {
            String timestamp = getNowISOTimestamp();
            String body = createGasLimitRequestBody(blockchainId, data);
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
            if (response.isSuccessful() && response.body()  != null) {
                String rawData = response.body().string();
                logger.debug("OKXGasTrackerImpl estimateGas: {}", rawData);
                OKXGasLimitResponse gasLimit = new Gson().fromJson(rawData, OKXGasLimitResponse.class);
                // gas limit: the 1.5 times estimation
                return new BigInteger(gasLimit.data[0].gasLimit).multiply(new BigInteger("1.25"));
            } else if (response.body() != null) {
                logger.warn("Failed to estimate gas, response: {}", response.body().string());
            } else {
                logger.warn("Failed to estimate gas, response is null");
            }
        } catch (Exception e) {
            logger.warn("Failed to estimate gas", e);
        }

        return DefaultGasProvider.GAS_LIMIT;
    }
}
