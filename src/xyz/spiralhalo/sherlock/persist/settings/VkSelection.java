package xyz.spiralhalo.sherlock.persist.settings;

public class VkSelection {

    private final String name;
    private final int value;

    public VkSelection(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
