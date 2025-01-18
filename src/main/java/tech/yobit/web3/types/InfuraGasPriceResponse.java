package tech.yobit.web3.types;

public class InfuraGasPriceResponse {
    public PriceLevel low;
    public PriceLevel medium;
    public PriceLevel high;
    public String estimatedBaseFee;
    public float networkCongestion;
    public String[] latestPriorityFeeRange;
    public String[] historicalPriorityFeeRange;
    public String[] historicalBaseFeeRange;
    public String priorityFeeTrend;
    public String baseFeeTrend;

    static public class PriceLevel {
        public String suggestedMaxPriorityFeePerGas;
        public String suggestedMaxFeePerGas;
        public int minWaitTimeEstimate;
        public int maxWaitTimeEstimate;
    }
}
