package miniplc0java.analyser;

public class SymbolEntry {
    String name;
    boolean isConstant;
    boolean isInitialized;
    int type; //0 void, 1 int, 2 double, 3 char
    int stackOffset;
    int functionOffset; // -1 for nonfunction

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public SymbolEntry(String name, boolean isConstant, boolean isInitialized, int type, int stackOffset) {
        this.name = name;
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.type = type;
        this.stackOffset = stackOffset;
        this.functionOffset = -1;
    }
}
