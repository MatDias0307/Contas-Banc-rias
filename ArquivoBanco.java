
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.*;

public class ArquivoBanco<T extends Entidade> {

  protected RandomAccessFile arq; // arquivo de dados
  protected final String contasArquivo = "contas.db";
  protected Constructor<T> construtor;
  protected RandomAccessFile arq2; // arquivo de indices
  protected final String indicesArquivo = "indices.db";
  protected RandomAccessFile arq3; // arquivo lista invertida
  protected final String listaInvertida = "lista.db";

  public ArquivoBanco(Constructor<T> construtor) {
    this.construtor = construtor; // construtor Pessoa(Entidade)
    try {
      arq = new RandomAccessFile(contasArquivo, "rw");
      arq2 = new RandomAccessFile(indicesArquivo, "rw");
      arq3 = new RandomAccessFile(listaInvertida, "rw");
      if (arq.length() <= 0) {
        arq.writeInt(0); // escrever 0 no arquivo de dados vazios
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void inserirLista(String nome, int id) {

    String[] result = nome.split(" "); // separa nome/sobrenome

    try {
      for (int i = 0; i < result.length; i++) {
        long pos = this.verificarLista(result[i]); // retorna -1 se o nome/sobrenome não está na lista e se estiver
                                                   // retorna a posição dele

        if (pos == -1) {

          long length = arq3.length();
          arq3.seek(length);

          arq3.writeChar(' '); // lapide
          arq3.writeUTF(result[i]); // escreve nome/sobrenome
          arq3.writeInt(id); // primeira vez que nome/sobrenome aparece, então escreve o id da conta da
                             // pessoa no id1
          arq3.writeInt(0); // id2 zerado
          arq3.writeInt(0); // id3 zerado
        } else {
          arq3.seek(pos);

          long posLap = arq3.getFilePointer();
          arq3.readChar(); // lapide
          arq3.readUTF(); // nome/sobrenome
          arq3.readInt(); // id1

          long posId2 = arq3.getFilePointer();
          int id2 = arq3.readInt(); // id2
          long posId3 = arq3.getFilePointer();
          int id3 = arq3.readInt(); // id3

          if (id2 == 0) {
            arq3.seek(posId2); // vai pra posição do id2
            arq3.writeInt(id); // segunda vez que nome/sobrenome aparece, então escreve o id da conta com nome
                               // repetido no id2
          } else if (id2 != 0 && id3 == 0) {
            arq3.seek(posId3); // vai pra posição do id3
            arq3.writeInt(id); // terceira vez que nome/sobrenome aparece, então escreve o id da conta com nome
                               // repetido no id3
            arq3.seek(posLap); // vai pra posição da lapide
            arq3.writeChar('*'); // * para indicar que os 3 id estão cheios
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public long verificarLista(String nome) {
    try {

      arq3.seek(0);

      // se o arquivo estiver vazio retorna -1
      if (arq3.length() == 0) {
        return -1;
      }

      while (arq3.getFilePointer() < arq3.length()) {
        long pos = arq3.getFilePointer();
        char lapide = arq3.readChar(); // lapide
        String nomePessoa = arq3.readUTF(); // nome/sobrenome
        arq3.readInt(); // id1
        arq3.readInt(); // id2
        arq3.readInt(); // id3
        if (nomePessoa.equals(nome) && lapide == ' ') {
          return pos; // se o nome já apareceu no arquivo retorna a posição dele
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return -1; // se o nome não apareceu no arquivo retorna -1
  }

  public void imprimirLista(String nome) {
    try {

      arq3.seek(0);

      // se o arquivo estiver vazio 
      if (arq3.length() == 0) {
        System.out.print("\nArquivo vazio!\n");
      }

      while (arq3.getFilePointer() < arq3.length()) {
        arq3.getFilePointer();
        arq3.readChar(); // lapide
        String nomePessoa = arq3.readUTF(); // nome/sobrenome
        int id1 = arq3.readInt(); // id1
        int id2 = arq3.readInt(); // id2
        int id3 = arq3.readInt(); // id3
        if (nomePessoa.equals(nome)) {
          System.out.println("'" + nome + "'" + "está nos registros: " + id1 + ", " + id2 + ", " + id3);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void inserirIndices(int id, long pos) throws IOException {
    try {
      long length = arq2.length();
      arq2.seek(length); // ponteiro no final do arquivo para escrever o indice
      arq2.writeInt(id); // id do registro
      arq2.writeLong(pos); // posiçao do registro
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void criarConta(T entidade) throws IOException {

    arq.seek(0);
    int id = arq.readInt() + 1;
    entidade.setID(id); // ler id do cabeçalho, incrementar +1 e setar ele como id do registro

    try {
      long length = arq.length();
      arq.seek(length); // ponteiro no final do arquivo para escrever o resgistro
      byte[] b = entidade.toByteArray();
      long pos = arq.getFilePointer();
      this.inserirIndices(entidade.getID(), pos);// escrever o id e posiçao do registro no arquivo de indices
      this.inserirLista(entidade.getNome(), entidade.getID());
      arq.writeChar(' '); // lápide vazia para saber se o registro é válido
      arq.writeInt(b.length); // tamanho do registro
      arq.write(b); // registro em byteArray
      System.out.print("O Id da sua conta é: " + id + "\n");
      arq.seek(0); // voltar para início do arquivo
      arq.writeInt(id++); // atualizar último id no cabeçalho
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void AtualizarConta(T atualizar, long pos) {

    try {
      arq.seek(pos);
      arq.readChar(); // leitura da lápide

      int tamAntigo = arq.readInt(); // Leitura do tamanho do registro
      byte[] baAntigo = new byte[tamAntigo];
      arq.read(baAntigo); // leitura do registro em bytes
      T objetoAntigo = construtor.newInstance();
      objetoAntigo.fromByteArray(baAntigo); // criação do objeto

      byte baNovo[] = atualizar.toByteArray();
      int tamNovo = baNovo.length; // tamanho do novo registro

      // se a conta ficar maior, outra é criada no final do arquivo
      if (tamAntigo < tamNovo) {
        arq.seek(pos);
        arq.writeChar('*'); // deleta arquivo

        long posL = arq.length();
        arq.seek(posL); // vai para o final do arquivo
        this.criarConta(atualizar);
      }

      // se a conta continuar do mesmo tamanho, o tamanho e os dados são atualizados
      else {
        arq.seek(pos);
        arq.readChar();
        arq.writeInt(baNovo.length);
        arq.write(baNovo);
      }
    }

    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean DeletarConta(int id) {

    try {
      arq2.seek(0);
      // percorrer o arquivo de indice até achar a conta desejada
      while (arq2.getFilePointer() < arq2.length()) {
        int idIndice = arq2.readInt();
        long pos = arq2.readLong();
        if (idIndice == id) {

          arq.seek(pos); // vai para a posição no arquivo
          char lapide = arq.readChar();

          if (lapide == '*') {
            break;
          } else {
            arq.seek(pos);
            arq.writeChar('*'); // lapide para excluir arquivo
          }

          return true;
        }
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.print("\nConta não encontrada ou já excluída!\n");

    return false;
  }

  // encontra id do registro no arquivo de indice e retorna o registro no arquivo
  // de dados
  public T LerConta(int id) throws Exception {
    T entidade = construtor.newInstance();

    try {
      arq2.seek(0);
      // percorrer o arquivo de indice até achar a conta desejada
      while (arq2.getFilePointer() < arq2.length()) {
        int idIndice = arq2.readInt();
        long pos = arq2.readLong();
        if (idIndice == id) {

          arq.seek(pos); // vai para a posição do registro no arquivo de dados
          char lapide = arq.readChar(); // leitura da lápide

          if (lapide == '*') {
            // System.out.print("\nConta excluída!"); //Verifica se o registro foi excluído
            break;
          }

          int tam = arq.readInt(); // Leitura do tamanho do registro
          byte[] ba = new byte[tam];
          arq.read(ba); // leitura do registro em bytes
          entidade.fromByteArray(ba); // criação do objeto

          return entidade;
        }
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.print("\nConta não encontrada ou excluída!\n");

    return null;
  }

  // encontra id do registro no arquivo de indice e retorna a posição dele no
  // arquivo de dados
  public long LerPos(int id) throws Exception {

    try {
      arq2.seek(0);
      // percorrer o arquivo de indice até achar o registro desejado
      while (arq2.getFilePointer() < arq2.length()) {
        int idIndice = arq2.readInt();
        long pos = arq2.readLong();
        if (idIndice == id) {

          return pos; // retorna posição dele
        }
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return -1;
  }

  // método para juntar nome, cpf e cidade de todos registros do arquivo de dados
  public String Dados() {
    String baseDados = "";
    try {
      arq.seek(0);
      arq.readInt(); // le id cabeçalho

      while (arq.getFilePointer() < arq.length()) {
        char lapide = arq.readChar(); // leitura da lápide

        int tam = arq.readInt(); // Leitura do tamanho do registro
        byte[] ba = new byte[tam];
        arq.read(ba); // leitura do registro em bytes
        T entidade = construtor.newInstance();
        entidade.fromByteArray(ba); // criação do objeto

        if (lapide == ' ') {
          baseDados += entidade.getNome() + " " + entidade.getCidade() + " " + entidade.getCpf() + "*"; // junta nome,
                                                                                                        // cpf e cidade
                                                                                                        // dos registros
                                                                                                        // na variavel
                                                                                                        // baseDados,
        } // '*' para separar um registro do outro
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return baseDados;
  }

  public void CriptografarArquivo(int chave) {
    try {
      arq.seek(0);
      arq.readInt(); // le id cabeçalho

      while (arq.getFilePointer() < arq.length()) {
        char lapide = arq.readChar(); // leitura da lápide

        int tam = arq.readInt(); // Leitura do tamanho do registro
        byte[] ba = new byte[tam];
        long posEntidade = arq.getFilePointer(); // posicao dos dados do registro
        arq.read(ba); // leitura do registro em bytes
        T entidade = construtor.newInstance();
        entidade.fromByteArray(ba); // criação do objeto

        long posFinal = arq.getFilePointer(); // posicao depois de ler todo registro

        if (lapide == ' ') {
          String nomeEncriptado = this.criptografar(chave, entidade.getNome()); // criptografa o nome do registro e
                                                                                // salva na variavel
          entidade.setNome(nomeEncriptado); // seta o nome criptografado na entidade
          arq.seek(posEntidade); // volta pra posicao de dados do registro
          byte baNovo[] = entidade.toByteArray();
          arq.write(baNovo); // escreve a entidade com o novo nome
          arq.seek(posFinal); // volta pra posicao depois de ler todo registro, e assim, o loop le o proximo
                              // registro
        }
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public T DescriptografarArquivo(int chave, int id) {
    try {
      arq.seek(0);
      arq.readInt(); // le id cabeçalho

      while (arq.getFilePointer() < arq.length()) {
        char lapide = arq.readChar(); // leitura da lápide

        int tam = arq.readInt(); // Leitura do tamanho do registro
        byte[] ba = new byte[tam];
        arq.read(ba); // leitura do registro em bytes
        T entidade = construtor.newInstance();
        entidade.fromByteArray(ba); // criação do objeto

        if (lapide == ' ' && entidade.getID() == id) {
          String nomeEncriptado = this.descriptografar(chave, entidade.getNome()); // descriptografa o nome do registro
                                                                                   // e salva na variavel
          entidade.setNome(nomeEncriptado); // seta o nome criptografado na entidade

          return entidade;
        }
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.print("\nConta não encontrada ou excluída!\n");

    return null;
  }

  public String criptografar(int chave, String texto) {

    StringBuilder textoCriptografado = new StringBuilder();
 
    int tamTexto = texto.length();

    //criptografa cada caracter por vez
    for (int c = 0; c < tamTexto; c++) {
      //transforma o caracter em codigo ASCII e faz a criptografia
      int letraCriptografadaASCII = ((int) texto.charAt(c)) + chave;

      //verifica se o codigo ASCII esta no limite dos caracteres imprimiveis
      while (letraCriptografadaASCII > 126)
        letraCriptografadaASCII -= 94;

      //transforma codigo ASCII criptografado em caracter ao novo texto
      textoCriptografado.append((char) letraCriptografadaASCII);
    }

    return textoCriptografado.toString();
  }

  public String descriptografar(int chave, String textoCriptografado) {

    StringBuilder textoDescritografado = new StringBuilder();

    int tamTexto = textoCriptografado.length();

    //descriptografa cada caracter por vez
    for (int c = 0; c < tamTexto; c++) {
      //transforma o caracter em codigo ASCII e faz a descriptografia
      int letraDescriptografadaASCII = ((int) textoCriptografado.charAt(c)) - chave;

      //verifica se o codigo ASCII esta no limite dos caracteres imprimiveis
      while (letraDescriptografadaASCII < 32)
        letraDescriptografadaASCII += 94;

      //transforma codigo ASCII descriptografado em caracter ao novo texto
      textoDescritografado.append((char) letraDescriptografadaASCII);
    }

    return textoDescritografado.toString();
  }
}