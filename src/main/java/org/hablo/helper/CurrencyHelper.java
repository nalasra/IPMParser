package org.hablo.helper;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
}
