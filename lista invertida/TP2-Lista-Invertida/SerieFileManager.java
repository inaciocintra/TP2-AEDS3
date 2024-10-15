import java.io.*;
import java.util.*;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final InvertedList languageList;
    private final InvertedList companyList;

    public SerieFileManager() {
        this.languageList = new InvertedList("dados/language_inverted_list.idx");
        this.companyList = new InvertedList("dados/company_inverted_list.idx");
    }

    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);

        // Verificar se o arquivo de dados já existe; se não, criar e carregar do CSV
        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader("tvs.csv/tvs.csv"));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {

                bufferedReader.readLine(); // Ignora cabeçalho do CSV

                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);

                    long posicao = arq.getFilePointer();
                    byte[] ba = serie.toByteArray();

                    arq.writeBoolean(true); // Marca o registro como ativo
                    arq.writeInt(ba.length);
                    arq.write(ba);

                    // Atualiza as listas invertidas para o novo registro
                    languageList.adicionar(serie.getLanguage(), serie.getId());
                    for (String company : serie.getCompanies()) {
                        companyList.adicionar(company, serie.getId());
                    }
                }
                // Salva as listas invertidas após a carga
                languageList.salvar();
                companyList.salvar();
                System.out.println("Arquivo de dados e listas invertidas carregados a partir do CSV.");
            } catch (FileNotFoundException e) {
                System.out.println("Arquivo CSV não encontrado: " + e.getMessage());
            }
        } else {
            // Caso o arquivo de dados já exista, apenas carrega as listas invertidas
            languageList.carregar();
            companyList.carregar();
            System.out.println("Arquivo de dados e listas invertidas carregados a partir do arquivo existente.");
        }
    }

    public Serie lerSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
            arq.seek(0); // Início do arquivo
            while (arq.getFilePointer() < arq.length()) {
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        return serie;
                    }
                }
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

            // Atualiza as listas invertidas
            languageList.adicionar(serie.getLanguage(), serie.getId());
            for (String company : serie.getCompanies()) {
                companyList.adicionar(company, serie.getId());
            }
            System.out.println("Série adicionada e listas invertidas atualizadas.");
        }
    }

    public void atualizarSerie(int id, Serie novaSerie) throws IOException {
        Serie serieAntiga = lerSerie(id);
        if (serieAntiga != null) {
            excluirSerie(id); // Exclui a série antiga
            adicionarSerie(novaSerie); // Adiciona a nova série
            System.out.println("Série atualizada e listas invertidas sincronizadas.");
        } else {
            System.out.println("ID não encontrado para atualização.");
        }
    }

    public void excluirSerie(int id) throws IOException {
        Serie serie = lerSerie(id);
        if (serie != null) {
            try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
                arq.seek(0);
                while (arq.getFilePointer() < arq.length()) {
                    long posicao = arq.getFilePointer();
                    boolean lapide = arq.readBoolean();
                    int tamanhoRegistro = arq.readInt();

                    if (lapide) {
                        byte[] ba = new byte[tamanhoRegistro];
                        arq.readFully(ba);
                        Serie serieAtual = new Serie();
                        serieAtual.fromByteArray(ba);
                        if (serieAtual.getId() == id) {
                            arq.seek(posicao);
                            arq.writeBoolean(false); // Marca o registro como inativo

                            // Remove das listas invertidas
                            languageList.remover(serieAtual.getLanguage(), id);
                            for (String company : serieAtual.getCompanies()) {
                                companyList.remover(company, id);
                            }
                            System.out.println("Série excluída e listas invertidas atualizadas.");
                            return;
                        }
                    } else {
                        arq.skipBytes(tamanhoRegistro);
                    }
                }
            }
        } else {
            System.out.println("ID não encontrado para exclusão.");
        }
    }

    // Busca IDs de séries por linguagem e companhia em uma mesma pesquisa
    public List<Integer> buscarPorLinguagemECompanhia(String linguagem, String companhia) {
        List<Integer> idsPorLinguagem = languageList.buscar(linguagem);
        List<Integer> idsPorCompanhia = companyList.buscar(companhia);
        
        // Intersecção dos IDs
        idsPorLinguagem.retainAll(idsPorCompanhia);
        return idsPorLinguagem;
    }
}
