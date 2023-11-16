package mvc.models;

import java.util.List;

public class Dictionary {
    private int number;
    private String key;
    private List<String> value;

    public Dictionary(int number, String key, List<String> value) {
        this.number = number;
        this.key = key;
        this.value = value;
    }

    public Dictionary() {

    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}

