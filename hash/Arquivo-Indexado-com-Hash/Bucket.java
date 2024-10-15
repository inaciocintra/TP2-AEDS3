import java.io.*;
import java.util.*;

class Bucket implements Serializable {
    int localDepth;
    private final List<Entry> entries;
    private final int capacity;

    public Bucket(int localDepth) {
        this.localDepth = localDepth;
        this.capacity = 5;  // Ajuste conforme necessário
        this.entries = new ArrayList<>();
    }

    public boolean isFull() {
        return entries.size() >= capacity;
    }

    public void addEntry(int id, long position) {
        entries.add(new Entry(id, position));
    }

    public Long getPosition(int id) {
        for (Entry entry : entries) {
            if (entry.id == id) {
                return entry.position;
            }
        }
        return null;
    }

    public void removeEntry(int id) {
        entries.removeIf(entry -> entry.id == id);
    }

    public List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void clear() {
        entries.clear();
    }
}

class Entry implements Serializable {
    int id;
    long position;

    public Entry(int id, long position) {
        this.id = id;
        this.position = position;
    }
}

