package io.bhex.saas.admin.util;

import io.bhex.base.constants.ProtoConstants;
import io.bhex.base.proto.Decimal;

import java.math.BigDecimal;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-06-02 09:50
 */
public class DecimalUtile {

    public static final int PRECISION = ProtoConstants.PRECISION;
    public static final int ROUNDMODE = ProtoConstants.ROUNDMODE;

    public static BigDecimal toBigDecimal(Decimal decimalValue, BigDecimal defaultValue) {
        try {
            return toBigDecimal(decimalValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static BigDecimal toBigDecimal(Decimal decimalValue) {
        if (null != decimalValue.getStr() && !"".equals(decimalValue.getStr().trim())) {
            return new BigDecimal(decimalValue.getStr()).setScale(PRECISION, ROUNDMODE);
        }
        return BigDecimal.valueOf(decimalValue.getUnscaledValue(),
                decimalValue.getScale()).setScale(PRECISION, ROUNDMODE);
    }

    public static Decimal fromBigDecimal(BigDecimal bigDecimalValue) {
        return Decimal.newBuilder()
                .setStr(bigDecimalValue.toPlainString())
                .setScale(bigDecimalValue.scale())
                .build();
    }
}
