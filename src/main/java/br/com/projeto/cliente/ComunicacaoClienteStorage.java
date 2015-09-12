package br.com.projeto.cliente;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

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
	private volatile int value;
	
	public ComunicacaoClienteStorage(Storage storageDestino, Arquivo arquivoDestino, int operacaoCliente) {
		this.storage = storageDestino;
		this.arquivo = arquivoDestino;
		this.operacao = operacaoCliente;
	}
	
	public ComunicacaoClienteStorage(Storage storageDestino, File arquivoFisico, Arquivo dadosArquivo, int operacaoCliente) {
		this.storage = storageDestino;
		this.arquivoFisico = arquivoFisico;
		this.arquivo = dadosArquivo;
		this.operacao = operacaoCliente;
	}
	
	
	public ComunicacaoClienteStorage() {
	
	}
	
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
			if(buscaArquivoStorage(this.storage, this.arquivo))
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
		
		Socket cliente = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
			DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
			ObjectOutputStream outObj = new ObjectOutputStream(cliente.getOutputStream());
			
			dos.writeInt(Constantes.STORAGE_SALVA_ARQUIVO);
			outObj.writeObject(arquivo);
			
			System.out.println("Enviando arquivo para o storage: " + nomeStorage);
			FileInputStream fis = new FileInputStream(arquivoFisico);
			if (arquivoFisico.length() < 1000000000)
				IOUtils.copy(fis, dos);
			else
				IOUtils.copyLarge(fis, dos);
			
			fis.close();
			dos.close();
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
	 * @param arquivo a ser buscado do storage
	 * @return Boolean que indica que a transação ocorreu certo ou errado
	 */
	private boolean removeArquivoStorage(Storage storage, Arquivo arquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		
		Socket cliente = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
		    //monta os dados a ser enviado ao storage (Arquivo e a opção de remover)
		    DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
			ObjectOutputStream outObj = new ObjectOutputStream(cliente.getOutputStream());
			
			System.out.println("Removendo arquivo...");
			dos.writeInt(Constantes.STORAGE_REMOVE_ARQUIVO);
			outObj.writeObject(arquivo);
		   
			outObj.close();
	        dos.close();
	        cliente.close();
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
	 * Por último lê os dados enviados pelo storage e verifica se recebeu dados do arquivo.
	 * Caso tenha recebido dados do arquivo, seta o objeto Arquivo, que será lido pelo cliente.
	 * 
	 * @param storage dados do storage
	 * @param arquivo a ser buscado no storage
	 * @return Boolean que indica que a transação ocorreu certo ou errado
	 */
	private boolean buscaArquivoStorage(Storage storage, Arquivo arquivo) {
		String hostStorage = storage.getEnderecoHost();
		String nomeStorage = storage.getNomeStorage();
		int portaStorage = storage.getPortaConexao();
		Arquivo arquivoEntrada = null;
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		BufferedInputStream bufferEntrada = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao storage: " + nomeStorage); 
		    
		    //abre buffer de comunicação com o storage
		    bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
		    bufferEntrada = new BufferedInputStream(cliente.getInputStream());
		    
	        ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream ous;
			ous = new ObjectOutputStream(bao);
			ous.writeInt(Constantes.STORAGE_BUSCA_ARQUIVO);
			ous.writeObject(arquivo);
	       
			//envia os dados do arquivo para o storage
			System.out.println("Buscando arquivo...");
	        bufferSaida.write(bao.toByteArray());
	        bufferSaida.flush();
	        
	        //recebe os dados do arquivo do storage
	        System.out.println("Recebendo dados do storage...");
	        arquivoEntrada = recebeArquivoStorage(bufferEntrada, cliente.getReceiveBufferSize());
	        
	        bufferEntrada.close();
	        bufferSaida.close();
	        cliente.close();
	        
	        if (arquivoEntrada != null) 
	        	this.setArquivo(arquivoEntrada);
	        else 
	        	return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	private Arquivo recebeArquivoStorage(InputStream canalDadosCliente,
			int tamanhoBufferEntrada) {
		byte[] dadosEntrada = null;
        ByteArrayInputStream in = null;
        ObjectInputStream objIn = null;
        Arquivo arquivoEntrada = new Arquivo();
        
		try {
			dadosEntrada = new byte[tamanhoBufferEntrada];
			BufferedInputStream bf = new BufferedInputStream(canalDadosCliente);
			bf.read(dadosEntrada);
	
		    in = new ByteArrayInputStream(dadosEntrada);
		    objIn = new ObjectInputStream(in);
	        
		    //Le a opção do cliente e os dados do arquivo enviado
		    arquivoEntrada = (Arquivo) objIn.readObject();
	        
	        in.close();
	        objIn.close();
		} catch (SocketException e2) {
			e2.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return arquivoEntrada;
	}

	public int getValue() {
		return value;
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
}
