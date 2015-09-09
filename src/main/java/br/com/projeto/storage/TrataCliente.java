package br.com.projeto.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.utils.Constantes;

/**Classe que trata as requisições dos clientes com threads
 * 
 * @author guilherme
 *
 */
public class TrataCliente implements Runnable {
	private InputStream  canalClienteReceberDados;
	private OutputStream canalClienteEnviarDados;
	private int          tamanhoBufferEntrada;
	private Storage      storage;

	public TrataCliente(int receiveBufferSize, InputStream inputStream, 
			OutputStream outputStream, Storage storage) {
		this.canalClienteReceberDados = inputStream;
		this.canalClienteEnviarDados = outputStream;
		this.tamanhoBufferEntrada = receiveBufferSize;
		this.storage = storage;
	}
	
	/**Método principal da thread
	 * 
	 */
	public void run() {
		List<Object> listaDadosCliente = new ArrayList<Object>();
		Arquivo arquivo = new Arquivo();
		int operacao = 0;
		
		System.out.println("Lendo os dados do cliente...");
		listaDadosCliente = trataDadosCliente(this.canalClienteReceberDados, this.tamanhoBufferEntrada);
		operacao = (Integer) listaDadosCliente.get(0);
		arquivo = (Arquivo) listaDadosCliente.get(1);
		
		//realiza a operação do cliente
		realizaOperacaoCliente(arquivo, operacao);
	}

	/**Método que trata os dados da requisição do cliente.
	 * Primeiro le os dados da requisição do cliente.
	 * Em seguida cria-se uma lista de dados para retorno.
	 * Monta a seguinte saida dadosCliente onde:
	 	** 0 - Opção do cliente
	  	** 1 - Dados do arquivo enviado				
	 * @param cliente
	 * @return
	 */
	public static List<Object> trataDadosCliente(InputStream canalDadosCliente, int tamanhoBufferEntrada) {
		byte[] dadosEntrada = null;
        ByteArrayInputStream in = null;
        ObjectInputStream objIn = null;
    	List<Object> listaDadosCliente = new ArrayList<Object>();
        
		try {
			dadosEntrada = new byte[tamanhoBufferEntrada];
			BufferedInputStream bf = new BufferedInputStream(canalDadosCliente);
			bf.read(dadosEntrada);
	
		    in = new ByteArrayInputStream(dadosEntrada);
		    objIn = new ObjectInputStream(in);
	        
		    //Le a opção do cliente e os dados do arquivo enviado
		    listaDadosCliente.add((Integer) objIn.readInt());
		    listaDadosCliente.add((Arquivo) objIn.readObject());
	        
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
		return listaDadosCliente;
	}
	
	/**Método para realizar a operação desejada do cliente.
	 * Verifica qual operação o cliente deseja realizar e chama o método responsavel pela operação.
	 * 
	 * @param arquivo
	 * @param operacao
	 * @return Boolean com o status da operação.
	 */
	private void realizaOperacaoCliente(Arquivo arquivo, int operacao) {
		switch (operacao) {
		case Constantes.STORAGE_SALVA_ARQUIVO:	
			System.out.println("Salvando arquivo:  " + arquivo.getNomeArquivo());
			if (salvaArquivo(arquivo, storage.getLocalArmazenamento()))
				System.out.println("Arquivo " + arquivo.getNomeArquivo() + " salvo com sucesso!");
			else
				System.out.println("Erro no salvamento do arquivo: " + arquivo.getNomeArquivo());
			break;
		case Constantes.STORAGE_REMOVE_ARQUIVO:
			System.out.println("Removendo o arquivo:  " + arquivo.getNomeArquivo());
			if (removeArquivo(arquivo, storage.getLocalArmazenamento()))
				System.out.println("Arquivo " + arquivo.getNomeArquivo() + " removido com sucesso!");
			else
				System.out.println("Erro ao tentar remover o arquivo: " + arquivo.getNomeArquivo());
			break;
		case Constantes.STORAGE_BUSCA_ARQUIVO:
			String localArquivo = storage.getLocalArmazenamento() + arquivo.getNomeArquivo();
			File arquivoEncontrado = new File(localArquivo);
			
			if (arquivoEncontrado.exists()) {
				System.out.println("Arquivo encontrado, enviando arquivo:  " + arquivo.getNomeArquivo());
				if(enviaArquivoCliente(arquivoEncontrado, arquivo))
					System.out.println("Arquivo " + arquivo.getNomeArquivo() + " enviado com sucesso!");
				else
					System.out.println("Erro ao tentar enviar o arquivo: " + arquivo.getNomeArquivo());
			} else {
				System.out.println("Arquivo não encontrado, dados do arquivo: " + localArquivo);
			}
			break;
		default:
			System.out.println("Opção inválida do cliente!");
			break;
		}
	}
	
	/**Método que envia dados ao cliente.
	 * 
	 * @param arquivoEncontrado dados do arquivo físico a ser enviado.
	 * @param arquivo dados do objeto arquivo a ser enviado.
	 * @return Boolean com status da solicitação.
	 */
	private boolean enviaArquivoCliente(File arquivoEncontrado, Arquivo arquivo) {
		byte[] bufferArquivo = serializarArquivo(arquivoEncontrado, arquivo);
		BufferedOutputStream bufferSaida = null;
		
		try {
			bufferSaida = new BufferedOutputStream(this.canalClienteEnviarDados);
	        bufferSaida.write(bufferArquivo);
	        bufferSaida.flush();
	        bufferSaida.close();
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**Método que cria o arquivo no local de armazenamento do storage.
	 * Primeiro verifica o local do novo arquivo e em seguida.
	 * cria o objeto de buffer para escravar os dados do arquivo.
	 * O objeto buffer de saida lê os dados que o cliente enviou e escreve no local de armazenamento.
	 * 
	 * @param novoArquivo
	 * @param localArmazenamento
	 * @return Boolean com status da solicitação.
	 */
	private boolean salvaArquivo(Arquivo novoArquivo,
			String localArmazenamento) {
		String localNovoArquivo = storage.getLocalArmazenamento() + novoArquivo.getNomeArquivo();
        
        FileOutputStream bufferArquivoSaida = null;
        try {
        	bufferArquivoSaida = new FileOutputStream(localNovoArquivo);
        	bufferArquivoSaida.write(novoArquivo.getDadosArquivo());
        	bufferArquivoSaida.close();
        } catch (IOException e) {
			e.printStackTrace();
			return false;
        }
		return true;
	}
	
	/**Método para remover um arquivo do storage.
	 * Verifica o local de armazenamento, juntamento com o nome do arquivo a ser removido.
	 * Por último verifica se o arquivo existe, caso exista, remove o mesmo.
	 *  
	 * @param dadosArquivo
	 * @param localArmazenamento
	 * @return Boolean com status da solicitação
	 */
	private boolean removeArquivo(Arquivo dadosArquivo,
			String localArmazenamento) {
		String localArquivo = storage.getLocalArmazenamento() + dadosArquivo.getNomeArquivo();
        
        File arquivo = new File(localArquivo);
		if(arquivo.exists()) {
			arquivo.delete();
			return true;
		} else {
			System.out.println("Arquivo não encontrado!");
			return false;
		}
        
	}

	/**Método que serializa os dados do arquivo a ser enviado ao cliente.
	 * 
	 * @param arquivo dados físico do arquivo a ser enviado
	 * @param novoArquivo objeto do arquivo a ser enviado
	 * @return bytes do arquivo serializado a ser enviado
	 */
	private byte[] serializarArquivo(File arquivo, Arquivo novoArquivo){
		FileInputStream fis;
		
        try {
           //le os dados do arquivo, armazena os dados do arquivo no objeto
    	   byte[] conteudoByte = new byte[(int) arquivo.length()];
           fis = new FileInputStream(arquivo);
           fis.read(conteudoByte);
           fis.close();
           novoArquivo.setDadosArquivo(conteudoByte);
           
           //converte em byte o objeto arquivo para ser enviado
           ByteArrayOutputStream bao = new ByteArrayOutputStream();
		   ObjectOutputStream ous;
		   ous = new ObjectOutputStream(bao);
		   ous.writeObject(novoArquivo);
		
		   return bao.toByteArray();
	    } catch (IOException e) {
	       e.printStackTrace();
	    }
	    return null;
	}
}
