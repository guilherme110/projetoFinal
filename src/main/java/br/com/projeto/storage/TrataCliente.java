package br.com.projeto.storage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;

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
	private Storage      storage;

	public TrataCliente (InputStream inputStream, 
			OutputStream outputStream, Storage storage) {
		this.canalClienteReceberDados = inputStream;
		this.canalClienteEnviarDados = outputStream;
		this.storage = storage;
	}
	
	/**Método principal da thread
	 * Primeiro le os dados enviados pelo cliente e verifica a opção selecionada
	 * pelo cliente.
	 * De acordo com a operação selecionando um método para ser realizado.
	 * 
	 */
	public void run() {
		int operacao = 0;
        Arquivo arquivo = new Arquivo();
        
		try {
		    DataInputStream dis = new DataInputStream(this.canalClienteReceberDados);
		    ObjectInputStream objIn = new ObjectInputStream(this.canalClienteReceberDados);
			operacao = dis.readInt();
        	arquivo = (Arquivo) objIn.readObject();
			
			switch (operacao) {
			case Constantes.STORAGE_SALVA_ARQUIVO:	
				if (salvaArquivo(dis, arquivo, storage.getLocalArmazenamento()))
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
			
			objIn.close();
			dis.close();
		} catch (SocketException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	/**Método que cria o arquivo no local de armazenamento do storage.
	 * Primeiro verifica o local do novo arquivo e em seguida.
	 * Em seguida utiliza a biblioteca Apache IOUtils para copiar o arquivo.
	 * 
	 * @param dis
	 * @param localArmazenamento
	 * @return Boolean com status da solicitação.
	 */
	private boolean salvaArquivo(DataInputStream dis, Arquivo arquivo, 
			String localArmazenamento) {
        try {
        	String localNovoArquivo = localArmazenamento + arquivo.getNomeArquivo();
        
        	System.out.println("Salvando arquivo:  " + arquivo.getNomeArquivo());
        	FileOutputStream fos = new FileOutputStream(localNovoArquivo);
 			if (arquivo.getTamanhoArquivo() < 2000000000)
 				IOUtils.copy(dis, fos);
 			else
 				IOUtils.copyLarge(dis, fos);
 			
 			fos.close();
 			dis.close();
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
