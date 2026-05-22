package dev.kwlew.kmoney.managers.sound;

public enum SoundType {
    PAY("pay"),
    ADD("add"),
    SET("set"),
    REMOVE("remove"),
    TOP("top"),
    RELOAD("reload"),
    WITHDRAW("withdraw"),
    REDEEM("redeem");

    private final String key;

    SoundType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
