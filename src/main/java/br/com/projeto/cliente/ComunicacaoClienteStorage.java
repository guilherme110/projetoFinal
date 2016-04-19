package br.com.projeto.cliente;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

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
	private File	arquivoFisico;
	private int		operacao;
	private InputStream inputStream;
	private String  localArmazenamento;
	
	public ComunicacaoClienteStorage(Storage storageDestino, Arquivo arquivoDestino, int operacaoCliente, 
			String localArmazenamento) {
		this.storage = storageDestino;
		this.arquivo = arquivoDestino;
		this.operacao = operacaoCliente;
		this.localArmazenamento = localArmazenamento;
	}
	
	public ComunicacaoClienteStorage(Storage storageDestino, File arquivoFisico, Arquivo dadosArquivo, 
			int operacaoCliente) {
		this.storage = storageDestino;
		this.arquivoFisico = arquivoFisico;
		this.arquivo = dadosArquivo;
		this.operacao = operacaoCliente;
	}
	
	public ComunicacaoClienteStorage() {
	
	}
	
	/**Método principal da thread.
	 * Chama um método de acordo com a opção setada pelo cliente.
	 * 
	 */
	@Override
	public void run() {
		switch (this.operacao) {
		case Constantes.STORAGE_SALVA_ARQUIVO:
			if(enviaArquivoStorage(this.storage, this.arquivoFisico, this.arquivo))
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
		case Constantes.STORAGE_BUSCA_ARQUIVO:
			if(buscaArquivoStorage(this.storage, this.arquivo, this.localArmazenamento))
				System.out.println("Arquivo transferido do storage com sucesso!");
			else
				System.out.println("Erro ao transferir o arquivo do storage: " + this.storage.getNomeStorage());
			break;
		default:
			break;
		}
	}

	/**Serviço que envia um novo arquivo para o storage.
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage.
	 * Utiliza a bibliote Apache IOUtils para enviar o arquivo aos storages.
	 * Arquivos maiores que 2Gb devem usar um método específico para envio.
	 * 
	 * @param storage dados do storage
	 * @param arquivoFisico dados físico do arquivo a ser salvo
	 * @param novoArquivo objeto do arquivo a ser enviado para o storage
	 * @return Boolean se ocorreu tudo certo na transação
	 */
	private boolean enviaArquivoStorage(Storage storage, File arquivoFisico, Arquivo arquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket socketCliente = null;
		try {
			socketCliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
			DataOutputStream dos = new DataOutputStream(socketCliente.getOutputStream());
			ObjectOutputStream outObj = new ObjectOutputStream(socketCliente.getOutputStream());
			
			dos.writeInt(Constantes.STORAGE_SALVA_ARQUIVO);
			outObj.writeObject(arquivo);
			
			System.out.println("Enviando arquivo para o storage: " + nomeStorage);
			FileInputStream fis = new FileInputStream(arquivoFisico);
			if (arquivoFisico.length() < 2000000000)
				IOUtils.copy(fis, dos);
			else
				IOUtils.copyLarge(fis, dos);
			fis.close();
			dos.close();
			socketCliente.close();
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
	 * @param arquivo a ser buscado do storage
	 * @return Boolean que indica que a transação ocorreu certo ou errado
	 */
	private boolean removeArquivoStorage(Storage storage, Arquivo arquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket socketCliente = null;
		try {
			socketCliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
		    //monta os dados a ser enviado ao storage (Arquivo e a opção de remover)
		    DataOutputStream dos = new DataOutputStream(socketCliente.getOutputStream());
			ObjectOutputStream outObj = new ObjectOutputStream(socketCliente.getOutputStream());
			
			System.out.println("Removendo arquivo...");
			dos.writeInt(Constantes.STORAGE_REMOVE_ARQUIVO);
			outObj.writeObject(arquivo);
		   
			outObj.close();
	        dos.close();
	        socketCliente.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	
	/** Serviço para buscar um arquivo no storage.
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage.
	 * Em seguida envia os dados ao storage, com a opção de Buscar Arquivo e 
	 * aguarda o storage enviar a resposta.
	 * Por último chama o método que recebe os dados do arquivo do storage.
	 * 
	 * @param storage dados do storage.
	 * @param arquivo a ser buscado no storage.
	 * @param localArmazenamento local de armazenamento do arquivo.
	 * @return Boolean que indica que a transação ocorreu certo ou errado.
	 */
	public boolean buscaArquivoStorage(Storage storage, Arquivo arquivo, String localArmazenamento) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket socketCliente = null;
		try {
			socketCliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
		    //monta os dados a ser enviado ao storage (Arquivo e a opção de remover)
		    DataOutputStream dos = new DataOutputStream(socketCliente.getOutputStream());
			ObjectOutputStream outObj = new ObjectOutputStream(socketCliente.getOutputStream());
			
			System.out.println("Buscando arquivo no storage...");
			dos.writeInt(Constantes.STORAGE_BUSCA_ARQUIVO);
			outObj.writeObject(arquivo);
	        
	        //recebe os dados do arquivo do storage
	        System.out.println("Recebendo dados do storage...");
	        if (!recebeArquivoStorage(socketCliente.getInputStream(), storage, arquivo, localArmazenamento)) {
	        	System.out.println("Erro ao receber o arquivo do storage!");
	        	return false;
	        }
	        
	        outObj.close();
	        dos.close();
	        socketCliente.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	/**Método que recebe o arquivo do storage.
	 * Cria o local do novo arquivo com um nome temporario, onde é o id do storage mais
	 * o nome do arquivo.
	 * Utiliza a biblioteca IOUtils para salvar o arquivo temporario.
	 * Arquivos com mais de 2Gb devem usar um método específico.
	 * 
	 * @param is input de dados do cliente.
	 * @param storage dados do storage do arquivo.
	 * @param arquivo dados do arquivo a ser baixado.
	 * @param localArmazenamento local default de armazenamento do client.e
	 * @return boolean com status da solicitação.
	 */
	private boolean recebeArquivoStorage(InputStream is,
			Storage storage, Arquivo arquivo, String localArmazenamento) {
		String localNovoArquivo = localArmazenamento +  storage.getIdStorage() + arquivo.getNomeArquivo();
		
		try {
        	FileOutputStream fos = new FileOutputStream(localNovoArquivo);
 			if (arquivo.getTamanhoArquivo() < 2000000000)
 				IOUtils.copy(is, fos);
 			else
 				IOUtils.copyLarge(is, fos);
 			
 			fos.close();
 			is.close();
        } catch (IOException e) {
			e.printStackTrace();
			return false;
        }
		return true;
	}

	public Arquivo getArquivo() {
		return arquivo;
	}

	public void setArquivo(Arquivo arquivo) {
		this.arquivo = arquivo;
	}
	
	public File getArquivoFisico() {
		return arquivoFisico;
	}

	public void setArquivoFisico(File arquivoFisico) {
		this.arquivoFisico = arquivoFisico;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
}
