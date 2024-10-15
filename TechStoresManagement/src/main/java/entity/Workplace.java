package entity;

public class Workplace {
    private int id;
    private String name;
    private boolean isStore;

    public Workplace(int id, String name, boolean isStore) {
        this.id = id;
        this.name = name;
        this.isStore = isStore;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isStore() {
        return isStore;
    }
}
