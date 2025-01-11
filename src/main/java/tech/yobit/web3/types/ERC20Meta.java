package tech.yobit.web3.types;

import java.math.BigInteger;

public class ERC20Meta {
    public final String name;
    public final String fullName;
    public final int decimals;
    public final ContractAddress contractAddress;

    public ERC20Meta(String name, String fullName, int decimals,
                     long blockchainId, Address coinContractAddress) {
        this.contractAddress = new ContractAddress(blockchainId, coinContractAddress);
        this.name = name;
        this.fullName = fullName;
        this.decimals = decimals;
    }

    // TODO: check
    public BigInteger parseUnits(String value) {
        if (!value.matches("^(-?)([0-9]*)\\.?([0-9]*)$")) {
            throw new IllegalArgumentException("Invalid decimal number: " + value);
        }

        String[] parts = value.split("\\.");
        String integer = parts[0];
        String fraction = parts.length > 1 ? parts[1] : "0";

        boolean negative = integer.startsWith("-");
        if (negative) {
            integer = integer.substring(1);
        }

        // trim trailing zeros.
        fraction = fraction.replaceAll("0+$", "");

        // round off if the fraction is larger than the number of decimals.
        if (this.decimals == 0) {
            if (Math.round(Double.parseDouble("." + fraction)) == 1) {
                integer = new BigInteger(integer).add(BigInteger.ONE).toString();
            }
            fraction = "";
        } else if (fraction.length() > this.decimals) {
            String left = fraction.substring(0, this.decimals - 1);
            String unit = fraction.substring(this.decimals - 1, this.decimals);
            String right = fraction.substring(this.decimals);

            long rounded = Math.round(Double.parseDouble(unit + "." + right));
            if (rounded > 9) {
                fraction = new BigInteger(left).add(BigInteger.ONE) + "0";
                fraction = fraction.substring(fraction.length() - decimals);
            } else {
                fraction = left + rounded;
            }

            if (fraction.length() > this.decimals) {
                fraction = fraction.substring(1);
                integer = new BigInteger(integer).add(BigInteger.ONE).toString();
            }

            fraction = fraction.substring(0, this.decimals);
        } else {
            fraction = fraction + "0".repeat(this.decimals - fraction.length());
        }

        return new BigInteger((negative ? "-" : "") + integer + fraction);
    }

    // TODO: check
    public String formatUnits(BigInteger value) {
        String display = value.toString();

        boolean negative = display.startsWith("-");
        if (negative) {
            display = display.substring(1);
        }

        if (this.decimals > display.length()) {
            int padLength = this.decimals - display.length();
            display = "0".repeat(padLength) + display;
        }

        String integer = display.length() > this.decimals ? display.substring(0, display.length() - this.decimals) : "0";
        String fraction = display.length() > this.decimals ? display.substring(display.length() - this.decimals) : "";

        fraction = fraction.replaceAll("0+$", "");

        return (negative ? "-" : "") + integer + (fraction.isEmpty() ? "" : "." + fraction);
    }
}
