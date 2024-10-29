package org.hablo;

import static org.hablo.helper.CurrencyHelper.convertAmountToDecimal;
import static org.hablo.helper.CurrencyHelper.formatDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeneralTests {


    @Test
    void test1() {
        String t = "0000000000254677";
        BigDecimal d = convertAmountToDecimal(t, "376");
        String s = formatDecimal(d);

        BigDecimal dd = d.negate(new MathContext(0));

        Assertions.assertEquals("    2,546.77 CR", s);
        Assertions.assertEquals(new BigDecimal("-2546.77"), dd);
    }

    @Test
    void test3() {
        String t = "0000000000755399";
        BigDecimal d = convertAmountToDecimal(t, "157");
        String s = formatDecimal(d);

        Assertions.assertEquals("    7,553.99 CR", s);
    }

    @Test
    void test2() {
        String t = "-6582715200.726";
        BigDecimal d = new BigDecimal(t);
        String s = formatDecimal(d);

        Assertions.assertEquals("6,582,715,200.726 DR", s);
    }
    @Test
    void test5() {
        String[] skipBins = new String[]{"12345678","123456","212121"};
        String pan = "1234568900000000";

        boolean b = Arrays.stream(skipBins).anyMatch(pan::startsWith);
        System.out.println(b);
        Assertions.assertTrue(b);
    }

    @Test
    void test6() {
        String[] skipBins = new String[]{"123456","12345689","212121"};
        String pan = "1234568900000000";

        for(String s : skipBins){
            int length = s.length();
            String binFromPan = pan.substring(0, length);

            if(binFromPan.equals(s)) {
                System.out.println("PAN belongs to SKIP BIN List");
                break;
            }
        }
    }

}
