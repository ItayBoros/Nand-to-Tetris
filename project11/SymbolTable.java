import java.util.*;

public class SymbolTable {
    public enum Kind { STATIC, FIELD, ARG, VAR, NONE }

    private static class Symbol {
        String type;
        Kind kind;
        int index;

        Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    private Map<String, Symbol> classScope = new HashMap<>();
    private Map<String, Symbol> subroutineScope = new HashMap<>();
    private Map<Kind, Integer> kindCount = new EnumMap<>(Kind.class);

    public SymbolTable() {
        reset();
    }

    public void reset() {
        classScope.clear();
        subroutineScope.clear();
        kindCount.clear();
        for (Kind kind : Kind.values()) {
            if (kind != Kind.NONE) {
                kindCount.put(kind, 0);
            }
        }
    }

    public void startSubroutine() {
        subroutineScope.clear();
        kindCount.put(Kind.ARG, 0);
        kindCount.put(Kind.VAR, 0);
    }

    public void define(String name, String type, Kind kind) {
        Map<String, Symbol> scope = (kind == Kind.STATIC || kind == Kind.FIELD) ?
                classScope : subroutineScope;
        int index = kindCount.get(kind);
        scope.put(name, new Symbol(type, kind, index));
        kindCount.put(kind, index + 1);
    }

    public int varCount(Kind kind) {
        return kindCount.getOrDefault(kind, 0);
    }

    public Kind kindOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol != null) return symbol.kind;

        symbol = classScope.get(name);
        if (symbol != null) return symbol.kind;

        return Kind.NONE;
    }

    public String typeOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol != null) return symbol.type;

        symbol = classScope.get(name);
        if (symbol != null) return symbol.type;

        return null;
    }

    public int indexOf(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol != null) return symbol.index;

        symbol = classScope.get(name);
        if (symbol != null) return symbol.index;

        return -1;
    }
}