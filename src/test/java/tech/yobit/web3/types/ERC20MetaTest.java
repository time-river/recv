package tech.yobit.web3.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class ERC20MetaTest {
    @Test
    public void testPositiveParseUnits() {
        BigInteger amount = ERC20Meta.parseUnits("10", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(10_000_000)), amount);

        amount = ERC20Meta.parseUnits("1", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(1_000_000)), amount);

        amount = ERC20Meta.parseUnits("0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("0.1", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(100_000)), amount);

        amount = ERC20Meta.parseUnits("0.000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(1)), amount);

        amount = ERC20Meta.parseUnits("0.0000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("0.0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("1.0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(1_000_000)), amount);

        amount = ERC20Meta.parseUnits("1.01", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(1_010_000)), amount);

        amount = ERC20Meta.parseUnits("1.0000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(1_000_000)), amount);
    }

    @Test
    public void testNegativeParseUnits() {
        BigInteger amount = ERC20Meta.parseUnits("-10", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-10_000_000)), amount);

        amount = ERC20Meta.parseUnits("-1", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-1_000_000)), amount);

        amount = ERC20Meta.parseUnits("-0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("-0.1", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-100_000)), amount);

        amount = ERC20Meta.parseUnits("-0.000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-1)), amount);

        amount = ERC20Meta.parseUnits("-0.0000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("-0.0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(0)), amount);

        amount = ERC20Meta.parseUnits("-1.0", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-1_000_000)), amount);

        amount = ERC20Meta.parseUnits("-1.01", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-1_010_000)), amount);

        amount = ERC20Meta.parseUnits("-1.0000001", 6);
        Assertions.assertEquals(new BigInteger(String.valueOf(-1_000_000)), amount);
    }

    @Test
    void testPositiveFormatUnits() {
        String amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(10_000_000)), 6);
        Assertions.assertEquals("10", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(1_000_000)), 6);
        Assertions.assertEquals("1", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(0)), 6);
        Assertions.assertEquals("0", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(100_000)), 6);
        Assertions.assertEquals("0.1", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(1)), 6);
        Assertions.assertEquals("0.000001", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(1_000_001)), 6);
        Assertions.assertEquals("1.000001", amount);
    }

    @Test
    void testNegativeFormatUnits() {
        String amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-10_000_000)), 6);
        Assertions.assertEquals("-10", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-1_000_000)), 6);
        Assertions.assertEquals("-1", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-0)), 6);
        Assertions.assertEquals("0", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-100_000)), 6);
        Assertions.assertEquals("-0.1", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-1)), 6);
        Assertions.assertEquals("-0.000001", amount);

        amount = ERC20Meta.formatUnits(new BigInteger(String.valueOf(-1_000_001)), 6);
        Assertions.assertEquals("-1.000001", amount);
    }
}
