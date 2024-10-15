import java.io.*;
import java.util.*;

public class HashIndex {
    private final String indexFilePath = "dados/hash_index.idx";
    private int globalDepth;
    private List<Bucket> directory;
    private final int bucketCapacity;

    public HashIndex(int initialSize) {
        this.bucketCapacity = Math.max(1, (int)(initialSize * 0.05)); // 5% do tamanho inicial
        this.globalDepth = 1;
        this.directory = new ArrayList<>(List.of(new Bucket(1), new Bucket(1)));
    }

    private int hashFunction(int id) {
        return id % (int) Math.pow(2, globalDepth);
    }

    public void insert(int id, long position) {
        int hashKey = hashFunction(id);
        Bucket bucket = directory.get(hashKey);

        if (bucket.isFull()) {
            splitBucket(hashKey);
            hashKey = hashFunction(id); // Recalcula após o split
            bucket = directory.get(hashKey);
        }

        bucket.addEntry(id, position);
        salvar();
    }

    private void splitBucket(int hashKey) {
        Bucket bucket = directory.get(hashKey);
        int localDepth = bucket.localDepth;
        Bucket newBucket = new Bucket(localDepth + 1);
    
        List<Entry> entriesToRehash = new ArrayList<>(bucket.getEntries());
        bucket.clear();
        bucket.localDepth++;
    
        if (bucket.localDepth > globalDepth) {
            expandDirectory();
        }
    
        for (Entry entry : entriesToRehash) {
            int newHashKey = hashFunction(entry.id);
            if (newHashKey == hashKey) {
                bucket.addEntry(entry.id, entry.position);
            } else {
                newBucket.addEntry(entry.id, entry.position);
            }
        }
    
        
        int step = (int) Math.pow(2, localDepth); 
        for (int i = hashKey; i < directory.size(); i += step) {
            if (i == hashKey) {
                directory.set(i, bucket);
            } else {
                directory.set(i, newBucket);
            }
        }
    }
    

    private void expandDirectory() {
        globalDepth++;
        List<Bucket> newDirectory = new ArrayList<>(directory.size() * 2);
        for (Bucket bucket : directory) {
            newDirectory.add(bucket);
            newDirectory.add(bucket);
        }
        directory = newDirectory;
    }

    public Long search(int id) {
        int hashKey = hashFunction(id);
        Bucket bucket = directory.get(hashKey);
        return bucket.getPosition(id);
    }

    public void delete(int id) {
        int hashKey = hashFunction(id);
        Bucket bucket = directory.get(hashKey);
        bucket.removeEntry(id);
        salvar();
    }

    public void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFilePath))) {
            oos.writeObject(globalDepth);
            oos.writeObject(directory);
        } catch (IOException e) {
            System.out.println("Erro ao salvar o índice Hash Estendido: " + e.getMessage());
        }
    }

    public void carregar() {
        File file = new File(indexFilePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilePath))) {
                globalDepth = (int) ois.readObject();
                directory = (List<Bucket>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Erro ao carregar o índice Hash Estendido: " + e.getMessage());
            }
        }
    }
}

