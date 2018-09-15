package xyz.spiralhalo.sherlock.util.swing;

public class TrimmedString {
    public static TrimmedString[] createArray(String[] arr, int limit){
        TrimmedString[] res = new TrimmedString[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = new TrimmedString(arr[i], limit);
        }
        return res;
    }

    private final String content;
    private final String label;

    public TrimmedString(String content, int limit) {
        this.content = content;
        int length = Math.min(content.length(), limit);
        this.label = content.substring(0, length) + (length<content.length()?"...":"");
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return label;
    }
}
