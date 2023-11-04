package org.hablo.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOCurrency;

public class CurrencyHelper {
    private CurrencyHelper() {
    }

    private static final Map<Integer, Currency> numericCurrencyMap = new HashMap<>();

    static {
        Set<Currency> set = Currency.getAvailableCurrencies();
        for (Currency currency : set) {
            numericCurrencyMap.put(currency.getNumericCode(), currency);
        }
    }

    /**
     * This method will only be used in case we want to locate Currency from ISO field 49, 50, 51
     */
    public static Currency getInstance(Integer code) {
        return numericCurrencyMap.get(code);
    }
    public static Currency getInstance(String code) {
        return numericCurrencyMap.get(Integer.parseInt(code));
    }

    public static String formatDecimal(BigDecimal d) {
        String r = "";
        try {
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            df.setRoundingMode(RoundingMode.UNNECESSARY);
            String pattern = "#,##0.### CR;#,##0.### DR";
            df.applyPattern(pattern);
            r = df.format(d);
            return StringUtils.leftPad(r, 15, " ");
        } catch (ArithmeticException ae){
            System.err.println("Unable to format: " + d);
        }
        return StringUtils.leftPad(d.toString(), 15, " ");
    }

    public static BigDecimal convertAmountToDecimal(String amount, String currency) {
        BigDecimal bd;
        try {
            return ISOCurrency.parseFromISO87String(amount, currency);
        } catch (IllegalArgumentException ex) {
            bd = BigDecimal.ZERO;
            if (currency.equals("157")) {
                String a = amount.substring(0, amount.length() - 2);
                String b = amount.substring(amount.length() - 2);
                bd = new BigDecimal(a);
                bd = bd.setScale(Integer.parseInt(b));
            }
        }
        return bd;
    }
}
