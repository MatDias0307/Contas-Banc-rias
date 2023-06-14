import java.io.IOException;

public interface Entidade {
    public int getID();
    public void setID(int idConta);
    public String getNome();
    public void setNome(String nomeConta);
    public String getCidade();
    public String getCpf();
    public byte[] toByteArray() throws IOException;
    public void fromByteArray(byte[] b) throws IOException;    
}