package io.bhex.saas.admin.enums;

public enum ExchangeOpType {
    CREATE_EXCHANGE(1,"create exchange");

    ExchangeOpType (int value, String desc){
        this.value = value;
        this.desc = desc;
    }



    private int value;

    private String desc;


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
