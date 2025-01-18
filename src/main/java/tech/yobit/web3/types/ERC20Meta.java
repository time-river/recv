package tech.yobit.web3.types;

import org.jetbrains.annotations.NotNull;

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

    public static BigInteger parseUnits(String value, int decimals) {
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
        if (decimals == 0) {
            if (Math.round(Double.parseDouble("." + fraction)) == 1) {
                integer = new BigInteger(integer).add(BigInteger.ONE).toString();
            }
            fraction = "";
        } else if (fraction.length() > decimals) {
            String left = fraction.substring(0, decimals - 1);
            String unit = fraction.substring(decimals - 1, decimals);
            String right = fraction.substring(decimals);

            long rounded = Math.round(Double.parseDouble(unit + "." + right));
            if (rounded > 9) {
                fraction = new BigInteger(left).add(BigInteger.ONE) + "0";
                fraction = fraction.substring(fraction.length() - decimals);
            } else {
                fraction = left + rounded;
            }

            if (fraction.length() > decimals) {
                fraction = fraction.substring(1);
                integer = new BigInteger(integer).add(BigInteger.ONE).toString();
            }

            fraction = fraction.substring(0, decimals);
        } else {
            fraction = fraction + "0".repeat(decimals - fraction.length());
        }

        return new BigInteger((negative ? "-" : "") + integer + fraction);
    }

    @NotNull
    public static String formatUnits(BigInteger value, int decimals) {
        String display = value.toString();

        boolean negative = display.startsWith("-");
        if (negative) {
            display = display.substring(1);
        }

        if (decimals >= display.length()) {
            int padLength = decimals - display.length();
            display = "0".repeat(padLength) + display;
        }

        String integer = display.length() > decimals ? display.substring(0, display.length() - decimals) : "0";
        String fraction = display.length() > decimals ? display.substring(display.length() - decimals) : display;

        fraction = fraction.replaceAll("0+$", "");

        return (negative ? "-" : "") + integer + (fraction.isEmpty() ? "" : "." + fraction);
    }

    public BigInteger parseUnits(String value) {
        return parseUnits(value, this.decimals);
    }

    @NotNull
    public String formatUnits(BigInteger value) {
        return formatUnits(value, this.decimals);
    }
}
