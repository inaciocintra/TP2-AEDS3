import java.io.*;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "tvs.csv/tvs.csv";
    private HashIndex hashIndex;

    public SerieFileManager(int initialSize) {
        this.hashIndex = new HashIndex(initialSize);
    }

    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);

        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {

                bufferedReader.readLine(); // Ignora cabeçalho do CSV

                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);

                    long posicao = arq.getFilePointer();
                    byte[] ba = serie.toByteArray();

                    arq.writeBoolean(true);
                    arq.writeInt(ba.length);
                    arq.write(ba);

                    hashIndex.insert(serie.getId(), posicao);
                }
                hashIndex.salvar();
                System.out.println("Arquivo de dados e índice carregados com sucesso.");
            }
        } else {
            hashIndex.carregar();
            System.out.println("Índice carregado a partir do arquivo existente.");
        }
    }

    public Serie lerSerie(int id) throws IOException {
        Long posicao = hashIndex.search(id);
        if (posicao == null) {
            return null;
        }

        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
            arq.seek(posicao);
            boolean lapide = arq.readBoolean();
            int tamanhoRegistro = arq.readInt();
            byte[] ba = new byte[tamanhoRegistro];
            arq.readFully(ba);

            if (lapide) {
                Serie serie = new Serie();
                serie.fromByteArray(ba);
                return serie;
            }
        }
        return null;
    }

    public void adicionarSerie(Serie serie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            arq.seek(arq.length());
            long posicao = arq.getFilePointer();
            byte[] ba = serie.toByteArray();
            
            arq.writeBoolean(true); // Marca o registro como ativo
            arq.writeInt(ba.length);
            arq.write(ba);

            hashIndex.insert(serie.getId(), posicao); // Atualiza o índice de hash
            hashIndex.salvar(); // Salva o índice atualizado
            System.out.println("Série adicionada e índice atualizado.");
        }
    }

    public void atualizarSerie(int id, Serie novaSerie) throws IOException {
        Long posicaoAntiga = hashIndex.search(id);
        if (posicaoAntiga != null) {
            try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
                // Marca o registro antigo como inativo
                arq.seek(posicaoAntiga);
                arq.writeBoolean(false);

                // Grava o novo registro no final do arquivo
                arq.seek(arq.length());
                long novaPosicao = arq.getFilePointer();
                byte[] ba = novaSerie.toByteArray();
                
                arq.writeBoolean(true); // Marca o novo registro como ativo
                arq.writeInt(ba.length);
                arq.write(ba);

                // Atualiza o índice: remove o antigo e insere o novo
                hashIndex.delete(id); // Remove a posição antiga do índice
                hashIndex.insert(novaSerie.getId(), novaPosicao); // Insere a nova posição no índice
                hashIndex.salvar(); // Salva o índice atualizado
                System.out.println("Série atualizada e índice de hash sincronizado.");
            }
        } else {
            System.out.println("ID não encontrado para atualização.");
        }
    }

    public void excluirSerie(int id) throws IOException {
        Long posicao = hashIndex.search(id);
        if (posicao != null) {
            try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
                arq.seek(posicao);
                arq.writeBoolean(false); // Marca o registro como inativo

                hashIndex.delete(id); // Remove a entrada do índice
                hashIndex.salvar(); // Salva o índice atualizado
                System.out.println("Série excluída e índice de hash atualizado.");
            }
        } else {
            System.out.println("ID não encontrado para exclusão.");
        }
    }
}
