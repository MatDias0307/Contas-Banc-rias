import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pessoa implements Entidade {
    private int id;
    private String nome;
    private String cpf;
    private String cidade;
    private int transferencias;
    private float saldo;

    public Pessoa(String nome, String cpf, String cidade) {
        this.nome = nome;
        this.cpf = cpf;
        this.cidade = cidade;
        transferencias = 0;
        saldo = 5000;
        this.id = -1;
    }

    public Pessoa() {
        this.nome = " ";
        this.cpf = " ";
        this.cidade = " ";
        transferencias = 0;
        saldo = 0F;
        this.id = -1;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public int getID() {
        return id;
    }

    public void setID(int idConta) {
        this.id = idConta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nomeConta) {
        this.nome = nomeConta;
    }

    public void debitarSaldo(float saldo) {
        this.saldo -= saldo; // diminuir saldo da conta
        this.transferencias++; // incrementar transferencias da conta
    }

    public void creditarSaldo(float saldo) {
        this.saldo += saldo; // aumentar saldo da conta
        this.transferencias++; // incrementar transferencias da conta
    }

    public void deposito(float saldo) {
        this.saldo += saldo; // diminuir saldo da conta
    }

    public void saque(float saldo) {
        this.saldo -= saldo; // aumentar saldo da conta
    }

    public String toString() {
        return "\nID: " + this.id + "\nNome: " + this.nome + "\nCPF: " + this.cpf + "\nCidade: " + this.cidade +
                "\nTransferencias: " + this.transferencias + "\nSaldo: R$" + this.saldo;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.cpf);
        dos.writeUTF(this.cidade);
        dos.writeInt(this.transferencias);
        dos.writeFloat(this.saldo);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.nome = dis.readUTF();
        this.cpf = dis.readUTF();
        this.cidade = dis.readUTF();
        this.transferencias = dis.readInt();
        this.saldo = dis.readFloat();
    }
}