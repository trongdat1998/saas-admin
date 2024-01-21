package io.bhex.saas.admin.enums;

/**
 * @author wangshouchao
 */

public enum RegisterOptionType {
    /**
     * all 邮箱+全部手机
     * phone 仅手机
     * email 仅邮箱
     * emailAndCnPhone 邮箱 + 中国手机
     */
    ALL(1), PHONE(2), EMAIL(3), EMAIL_CN_PHONE(4);
    private final int code;

    RegisterOptionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RegisterOptionType codeOf(int code) {
        for (RegisterOptionType optionType : values()) {
            if (optionType.code == code) {
                return optionType;
            }
        }
        return null;
    }

}
