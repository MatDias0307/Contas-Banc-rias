import java.util.Scanner;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class mainAEDSIII {

    // função para realizar depositos
    public static void depositar(int id, float valor, ArquivoBanco<Pessoa> conta) throws Exception {
        Pessoa contaS = conta.LerConta(id);
        long pos = conta.LerPos(id);

        if (contaS != null) {
            contaS.deposito(valor);
            conta.AtualizarConta(contaS, pos);

            System.out.print("\nDepósito realizado com sucesso!\n");
        } else {
            System.out.println("\nErro ao realizar depósito!");
        }
    }

    // função para realizar saques
    public static void sacar(int id, float valor, ArquivoBanco<Pessoa> conta) throws Exception {
        Pessoa contaS = conta.LerConta(id);
        long pos = conta.LerPos(id);

        if (contaS != null) {
            contaS.saque(valor);
            conta.AtualizarConta(contaS, pos);

            System.out.print("\nSaque realizado com sucesso!\n");
        } else {
            System.out.println("\nErro ao realizar saque!");
        }
    }

    // função para realizar transferencias
    public static void realizarTransferencia(int idC, int idD, float valor, ArquivoBanco<Pessoa> conta)
            throws Exception {

        Pessoa remetente = conta.LerConta(idC);
        Pessoa destinatario = conta.LerConta(idD);

        long posR = conta.LerPos(idC);
        long posD = conta.LerPos(idD);

        // verifica se as contas não foram excluidas
        if (destinatario != null && remetente != null) {
            remetente.debitarSaldo(valor);
            conta.AtualizarConta(remetente, posR);
            destinatario.creditarSaldo(valor);
            conta.AtualizarConta(destinatario, posD);

            System.out.print("\nTransferência realizada com sucesso!\n");
        } else {
            System.out.println("\nErro ao realizar tranferência!");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        
        ArquivoBanco<Pessoa> conta = new ArquivoBanco<>(Pessoa.class.getConstructor());

        int op1 = 0;

        // loop do menu de opções
        while (op1 != 7) {

            System.out.print(
                    "\nOlá, o que você deseja realizar?\n 1- Criar conta bancária\n 2- Realizar transferência\n" +
                            " 3- Depositar\n 4- Sacar\n 5- Acessar conta\n 6- Deletar conta\n 7- Sair");
            System.out.print("\nOpção: ");
            op1 = sc.nextInt();

            if (op1 == 1) {

                System.out.print("\nNome: ");
                sc.nextLine();
                String nome = sc.nextLine();
                System.out.print("CPF: ");
                String cpf = sc.next();
                System.out.print("Cidade: ");
                String cidade = sc.next();
                
                Pessoa p = new Pessoa(nome, cpf, cidade);
                conta.criarConta(p);
                
                System.out.print("\nConta criada com sucesso!\n");

            }

            else if (op1 == 2) {

                System.out.print("\nId da conta que irá realizar a transferênria: ");
                int idC = sc.nextInt();
                System.out.print("\nId da conta que irá receber a transferênria: ");
                int idD = sc.nextInt();
                System.out.print("\nValor a ser transferido: ");
                float valor = sc.nextFloat();

                realizarTransferencia(idC, idD, valor, conta);
            }

            else if (op1 == 3) {

                System.out.print("\nId da conta que irá realizar o depósito: ");
                int id = sc.nextInt();
                System.out.print("\nValor a ser depositado: ");
                float valor = sc.nextFloat();

                depositar(id, valor, conta);
            }

            else if (op1 == 4) {

                System.out.print("\nId da conta que irá realizar o saque: ");
                int id = sc.nextInt();
                System.out.print("\nValor a ser sacado: ");
                float valor = sc.nextFloat();

                sacar(id, valor, conta);
            }

            else if (op1 == 5) {

                System.out.print("\nInsira o id da conta: ");
                int id = sc.nextInt();
                Pessoa resp = conta.LerConta(id);
                if (resp != null) {
                    System.out.println(resp);
                }
            }

            else if (op1 == 6) {

                System.out.print("\nQual o id da conta que você deseja deletar: ");
                int id = sc.nextInt();
                boolean resp = conta.DeletarConta(id);
                if (resp == true) {
                    System.out.print("\nConta deletada com sucesso!\n");
                }
            }

            else if (op1 == 7) {
                System.out.print("\nVolte sempre!");
            }

            else {
                System.out.print("\nComando inválido!");
            }
        }

        int op2 = 0;

        // ---------Lista Invertida---------
        while (op2 != 2) {
            System.out.print("\n 1- Buscar Nome/Sobrenome na lista invertida\n 2- Sair");
            System.out.print("\nOpção: ");
            op2 = sc.nextInt();

            if (op2 == 1) {
                System.out.print("\nNome/Sobrenome: ");
                sc.nextLine();
                String nome = sc.nextLine();

                conta.imprimirLista(nome);
            }

            else if (op2 == 2) {
                System.out.print("\nVolte sempre!\n");
            }

            else {
                System.out.print("\nComando inválido!");
            }
        }

        // ---------Compressao LZW---------
        System.out.println("\nDeseja comprimir arquivo de dados?\n 1- Sim\n 2- Não");
        System.out.print("Opção: ");
        int op3 = sc.nextInt();
        switch (op3) {
            case 1:
                RandomAccessFile arq4; // arquivo comprimido
                String compressaoArquivo = "contasCompressaoX.db";
                arq4 = new RandomAccessFile(compressaoArquivo, "rw");

                lzw comprimir = new lzw();

                String dados = conta.Dados(); // nome, cpf e cidade de todos registros do arquivo de dados

                String dadosComprimidos = comprimir.compressao(dados); // comprime os dados

                float tamOriginal = dados.length();
                float tamComprimido = dadosComprimidos.length();
                float porcentagem = 100 - ((tamComprimido / tamOriginal) * 100); // calcula porcentagem de ganho

                System.out.printf("\nO ganho do arquivo comprimido foi de %.2f%%\n", porcentagem);

                try {
                    arq4.seek(0);
                    arq4.writeUTF(dadosComprimidos); // escreve no arquivo contasCompressaoX os dados comprimidos
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.print("\nO arquivo de dados foi comprimido com sucesso!\n");
                arq4.close();
                break;

            case 2:
                System.out.print("\nVolte sempre!\n");
                break;
            default:
                System.out.print("\nComando inválido!\n");
        }

        // ---------Descompressao LZW---------
        System.out.println("\nDeseja descomprimir arquivo de dados?\n 1- Sim\n 2- Não");
        System.out.print("Opção: ");
        int op4 = sc.nextInt();
        switch (op4) {
            case 1:
                RandomAccessFile arq4; // arquivo comprimido
                String compressaoArquivo = "contasCompressaoX.db";
                arq4 = new RandomAccessFile(compressaoArquivo, "rw");

                lzw descomprimir = new lzw();

                try {

                    arq4.seek(0);
                    String dadosComprimidos2 = arq4.readUTF(); // le dados comprimidos do arquivo
                    String dadosDescomprimidos = descomprimir.descompressao(dadosComprimidos2); // descomprime os dados
                    arq4.seek(0);
                    arq4.writeUTF(dadosDescomprimidos); // substitui os dados comprimidos do arquivo pelos dados
                                                        // descomprimidos
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.print("\nO arquivo comprimido foi descomprimido com sucesso!\n");

                arq4.close();
                break;

            default:
                break;
        }

        // ---------Criptografia---------
        System.out.println("\nDeseja criptografar arquivo de dados?\n 1- Sim\n 2- Não");
        System.out.print("Opção: ");
        int op5 = sc.nextInt();

        switch (op5) {
            case 1:
                System.out.print("\nDigite a chave para criptografar o arquivo de dados: ");
                int chave = sc.nextInt();
                conta.CriptografarArquivo(chave);
                System.out.print("\nO arquivo de dados foi criptografado com sucesso!\n");
                break;
            case 2:
                System.out.print("\nVolte sempre!\n");
                break;
            default:
                System.out.print("\nComando inválido!\n");
                break;
        }

        int op6 = 0;

        //---------Descriptografia---------
        while (op6 != 2) {
            System.out.print("\nO que você deseja realiar?\n 1- Buscar conta no arquivo de dados criptografado\n 2- Sair");
            System.out.print("\nOpção: ");
            op6 = sc.nextInt();

            if (op6 == 1) {
                System.out.print("\nInsira o id da conta: ");
                int id = sc.nextInt();
                System.out.print("\nDigite a mesma chave que utilizou para criptografar o arquivo de dados: ");
                int chave = sc.nextInt();
                Pessoa resp = conta.DescriptografarArquivo(chave, id);
                if (resp != null) {
                    System.out.println(resp);
                }
            }

            else if (op6 == 2) {
                System.out.print("\nPrograma Finalizado!");
            }

            else {
                System.out.print("\nComando inválido!");
            }
        }

        sc.close();

    }
}

class lzw {

    public String compressao(String entradaDados) {
        HashMap<String, Integer> dicionario = new LinkedHashMap<>();
        String[] dados = (entradaDados + "").split(""); // concatena a entrada de dados e salva em um vetor
        String dadosComprimidos = "";
        ArrayList<String> aux = new ArrayList<>();
        String adicionarChar;
        String somaChar = dados[0];
        int codigo = 256;

        for (int i = 1; i < dados.length; i++) {
            adicionarChar = dados[i];

            if (dicionario.get(somaChar + adicionarChar) != null) {
                somaChar += adicionarChar; // se a combinaçao de char tiver no dicionario, soma-se o proximo char da
                                           // palavra
            }

            else {
                if (somaChar.length() > 1) {
                    aux.add(Character.toString((char) dicionario.get(somaChar).intValue())); // adiciona char no
                                                                                             // arraylist
                } else {
                    aux.add(Character.toString((char) Character.codePointAt(somaChar, 0)));
                }

                dicionario.put(somaChar + adicionarChar, codigo); // se a combinaçao de char nao tiver no dicionario,
                                                                  // soma-se o proximo char da palavra
                                                                  // e a soma deles e guardada no dicionario com seu
                                                                  // codigo
                codigo++; // aumenta codigos do dicionario
                somaChar = adicionarChar; // atualiza variavel somaChar
            }
        }

        if (somaChar.length() > 1) {
            aux.add(Character.toString((char) dicionario.get(somaChar).intValue())); // adiciona char no arraylist
        } else {
            aux.add(Character.toString((char) Character.codePointAt(somaChar, 0)));
        }

        for (String charAux : aux) {
            dadosComprimidos += charAux; // percorre o arraylist e soma todos char comprimidos na variavel
                                         // dadosComprimidos
        }

        return dadosComprimidos;
    }

    public String descompressao(String entradaDados) {
        HashMap<Integer, String> dicionario = new LinkedHashMap<>();
        String[] dados = (entradaDados + "").split(""); // concatena a entrada de dados e salva em um vetor
        String somaChar = dados[0];
        String antigaString = somaChar;
        String dadosDescomprimidos = somaChar;
        int codigo = 256;
        String stringAux = "";

        for (int i = 1; i < dados.length; i++) {
            int novaString = Character.codePointAt(dados[i], 0);
            if (novaString < 256) {
                stringAux = dados[i];
            } else {
                if (dicionario.get(novaString) != null) {
                    stringAux = dicionario.get(novaString);
                } else {
                    stringAux = antigaString + somaChar;
                }
            }
            dadosDescomprimidos += stringAux; // soma todos char descomprimidos na variavel dadosDescomprimidos
            somaChar = stringAux.substring(0, 1);
            dicionario.put(codigo, antigaString + somaChar);
            codigo++;
            antigaString = stringAux; // atualiza variavel antigaString
        }

        return dadosDescomprimidos;
    }
}

