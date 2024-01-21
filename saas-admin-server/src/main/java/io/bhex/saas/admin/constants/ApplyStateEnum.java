package io.bhex.saas.admin.constants;

import io.bhex.bhop.common.exception.BizException;

import static io.bhex.bhop.common.exception.ErrorCode.STATE_ERROR;

public enum ApplyStateEnum {
    APPLYING(0),
    ACCEPT(1),
    REJECT(2);

    private int state;

    ApplyStateEnum(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static ApplyStateEnum getByState(int state) throws BizException {
        for (ApplyStateEnum value : values()) {
            if (value.state == state) {
                return value;
            }
        }
        throw new BizException(STATE_ERROR);
    }
}
