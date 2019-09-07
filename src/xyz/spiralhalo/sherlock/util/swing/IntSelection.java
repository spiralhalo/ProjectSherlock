package xyz.spiralhalo.sherlock.util.swing;

public class IntSelection {

    private final String name;
    private final int value;

    public IntSelection(String name, int value) {
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
