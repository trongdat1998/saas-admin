package io.bhex.saas.admin.util;

import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.Combo2;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.util
 * @Author: ming.xu
 * @CreateDate: 2019/10/29 5:15 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
public class SymbolUtil {
    private static final Long SYMBOL_CATEGORY_COIN = 1L;

    private static final Long SYMBOL_CATEGORY_OPTION = 3L;

    private static final Long SYMBOL_CATEGORY_FUTURE = 4L;

    private static String topicTemplate = "%s";

    /**
     * @param downPriceRange
     * @param upPriceRange
     * @return
     */
    public static String priceRange(BigDecimal downPriceRange, BigDecimal upPriceRange) {
        upPriceRange = upPriceRange.abs();
        downPriceRange = BigDecimal.ZERO.subtract(downPriceRange.abs());
        return String.format("%s,%s", downPriceRange, upPriceRange);
    }

    /**
     * @param priceRangeString
     * @return v1 downPriceRange v2 upPriceRange
     */
    public static Combo2<BigDecimal, BigDecimal> priceRangeFromString(String priceRangeString) {
        String[] split = priceRangeString.split(",");
        if (split.length != 2) {
            return null;
        }
        BigDecimal downPriceRange = new BigDecimal(split[0]).abs();
        BigDecimal upPriceRange = new BigDecimal(split[1]).abs();
        return new Combo2(downPriceRange, upPriceRange);
    }

    //currency-transaction
    //financial-derivatives
    //bhop
    public static Combo2<String, String> partitionName(String quoteTokenId, Long category, Long orgId) {
        if (SYMBOL_CATEGORY_COIN.equals(category)) {
            String topicName = "CTX";
            if ("USDT".equals(quoteTokenId)) {
                topicName = "CTU";
            } else if ("BTC".equals(quoteTokenId)) {
                topicName = "CTB";
            }
            return new Combo2<>("currency-transaction", String.format(topicTemplate, topicName));
        }
        if (SYMBOL_CATEGORY_OPTION.equals(category)) {
            return new Combo2<>("financial-derivatives", String.format(topicTemplate, "FD" + orgId));
        }
        if (SYMBOL_CATEGORY_FUTURE.equals(category)) {
            return new Combo2<>("futures", String.format(topicTemplate, "FUS"));
        }
        log.warn("getPartitionName error! orgId:{},quoteTokenId:{},category:{}", orgId, quoteTokenId, category);
        throw new BizException(ErrorCode.ERROR);
    }

    public static List<Integer> convertDigitalMergedListToDumpScales(String mergedList) {
        List<Integer> dumpScales = new ArrayList<>();
        String[] mergedDigitals;
        if (mergedList.contains("，")) {
            mergedDigitals = mergedList.split("，");
        } else {
            mergedDigitals = mergedList.split(",");
        }

        for (String mergedDigital : mergedDigitals) {
            BigDecimal dumpScale = new BigDecimal(mergedDigital.trim());

            if (dumpScale.compareTo(BigDecimal.ZERO) == 0) {
                dumpScale = new BigDecimal(1);
            }

            if (dumpScale.scale() == 0) {
                long mergedInteger = dumpScale.longValue();
                double log10 = Math.log10(mergedInteger);
                dumpScale = BigDecimal.valueOf(log10).add(BigDecimal.valueOf(1));
                dumpScales.add(0 - dumpScale.intValue());
            } else {
                dumpScales.add(dumpScale.scale());
            }
        }
        return dumpScales;
    }

}
