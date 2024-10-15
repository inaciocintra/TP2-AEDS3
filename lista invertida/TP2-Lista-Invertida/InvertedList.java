import java.io.*;
import java.util.*;

public class InvertedList {
    private final String filePath;
    private Map<String, List<Integer>> invertedList;

    public InvertedList(String filePath) {
        this.filePath = filePath;
        this.invertedList = new HashMap<>();
        carregar();
    }

    // Adiciona um ID à lista invertida para uma chave específica
    public void adicionar(String chave, int id) {
        invertedList.computeIfAbsent(chave, k -> new ArrayList<>()).add(id);
        salvar();
    }

    // Remove um ID da lista invertida para uma chave específica
    public void remover(String chave, int id) {
        List<Integer> ids = invertedList.get(chave);
        if (ids != null) {
            ids.remove((Integer) id); // Remove o ID específico
            if (ids.isEmpty()) {
                invertedList.remove(chave); // Remove a chave se estiver vazia
            }
        }
        salvar();
    }

    // Busca todos os IDs associados a uma chave específica
    public List<Integer> buscar(String chave) {
        return invertedList.getOrDefault(chave, new ArrayList<>());
    }

    // Salva a lista invertida no arquivo
    public void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(invertedList);
        } catch (IOException e) {
            System.out.println("Erro ao salvar a lista invertida: " + e.getMessage());
        }
    }

    // Carrega a lista invertida do arquivo
    @SuppressWarnings("unchecked")
    public void carregar() {
        File file = new File(filePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                invertedList = (Map<String, List<Integer>>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Erro ao carregar a lista invertida: " + e.getMessage());
            }
        }
    }
}
