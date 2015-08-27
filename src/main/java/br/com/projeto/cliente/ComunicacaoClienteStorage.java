package br.com.projeto.cliente;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;

/**Classe responsável por enviar dados ao storage de forma
 * paralela com thread's.
 * 
 * @author guilherme
 *
 */
public class ComunicacaoClienteStorage implements Runnable{
	private Storage storage;
	private Arquivo arquivo;
	private byte[]  bufferArquivo;
	private int		operacao;
	
	public ComunicacaoClienteStorage(Storage storageDestino, Arquivo arquivoDestino, int operacaoCliente) {
		this.storage = storageDestino;
		this.arquivo = arquivoDestino;
		this.operacao = operacaoCliente;
	}
	
	public ComunicacaoClienteStorage(Storage storageDestino, byte[] bufferNovoArquivo, int operacaoCliente) {
		this.storage = storageDestino;
		this.bufferArquivo = bufferNovoArquivo;
		this.operacao = operacaoCliente;
	}
	
	@Override
	public void run() {
		switch (this.operacao) {
		case Constantes.STORAGE_SALVA_ARQUIVO:
			if(enviaArquivoStorage(this.storage, this.bufferArquivo))
				System.out.println("Arquivo enviado com sucesso!");
			else
				System.out.println("Erro ao enviar o arquivo para o storage: " + this.storage.getNomeStorage());
			break;
		case Constantes.STORAGE_REMOVE_ARQUIVO:
			if(removeArquivoStorage(this.storage, this.arquivo))
				System.out.println("Arquivo removido com sucesso!");
			else
				System.out.println("Erro ao remover o arquivo do storage: " + this.storage.getNomeStorage());
			break;
		default:
			break;
		}
	}
	
	/**Serviço que envia um novo arquivo para o storage.
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage.
	 * Chama um método para serializar os dados a ser enviado ao storage e envia os dados ao storage..
	 * 
	 * @param storage dados do storage
	 * @param arquivo dados físico do arquivo a ser salvo
	 * @param novoArquivo objeto do arquivo a ser enviado para o storage
	 * @return Boolean se ocorreu tudo certo na transação
	 */
	private boolean enviaArquivoStorage(Storage storage, byte[] bufferArquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		   
		    System.out.println("Enviando arquivo...");
	        bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
	        bufferSaida.write(bufferArquivo);
	        bufferSaida.flush();
	        bufferSaida.close();
		    cliente.close();
		} catch (Exception e) {
			System.out.println("Erro no envio do arquivo!");
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	/** Serviço para remover um arquivo do storage.
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage.
	 * Monta os dados a ser enviado ao storage e envia os dados ao storage.
	 * Seta a opção de remover o arquivo do storage.
	 * 
	 * @param storage dados do storage
	 * @param arquivo a ser removido no storage
	 * @return Boolean que indica que a transação ocorreu certo ou errado
	 */
	private boolean removeArquivoStorage(Storage storage, Arquivo arquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
		    //monta os dados a ser enviado ao storage (Arquivo e a opção de remover)
		    bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
	        ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream ous;
			ous = new ObjectOutputStream(bao);
			ous.writeInt(Constantes.STORAGE_REMOVE_ARQUIVO);
			ous.writeObject(arquivo);
	       
			System.out.println("Removendo arquivo...");
	        bufferSaida.write(bao.toByteArray());
	        bufferSaida.flush();
	        bufferSaida.close();
		    cliente.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}
}
