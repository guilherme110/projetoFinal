package br.com.projeto.cliente;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.utils.Constantes;

/**Classe de serviços da aplicação do cliente
 * Cria um objeto do tipo mapDiretorio responsável pela serialização e 
 * comunicação com o servidor de metadados
 * 
 * @author guilherme
 *
 */
public class ClienteServico {
	public MapDiretorio mapDiretorio;
	
	/**Construtor da classe
	 * Cria o objeto de conexão com os servidores.
	 * @param kVProxy
	 */
	public ClienteServico(ServiceProxy kVProxy) {
		mapDiretorio = new MapDiretorio(kVProxy);
	}
	
	/**Serviço de ir para outro diretório
	 * Se o nome do diretorio que o cliente que ir é .. significa que ele que voltar um diretório, logo:
	 	** verifica o tamanho ou a quantidade de diretório que o cliente está
	 	** se o tamanho for 1 é por que ele está na pasta raiz (home)
	 	** se for maior que 1 remove o último diretório do lista de diretorioClienteAtual
	 * Se o diretório que o cliente que ir é diferente é diferente de ..
	 * chama o método de verificação de diretório nos servidores, caso o diretório seja 
	 * encontrado, adiciona o nome do novo diretório na lista de diretorio atual do cliente.
	 * 
	 * @param nomeDiretorio
	 * @param cliente
	 */
	public void moveDiretorio(String nomeDiretorio, Cliente cliente) {
		List<String> diretorioCliente = cliente.getDiretorioClienteAtual();
		int tamanhoDiretorioAtual = 0;
		
		if (("..".equalsIgnoreCase(nomeDiretorio))) {
			tamanhoDiretorioAtual = cliente.getDiretorioClienteAtual().size();
			if (tamanhoDiretorioAtual > 1)
				cliente.getDiretorioClienteAtual().remove(tamanhoDiretorioAtual - 1);
		} else {
			if (mapDiretorio.verificaDiretorio(nomeDiretorio, diretorioCliente)) {
				diretorioCliente.add(nomeDiretorio);
				cliente.setDiretorioClienteAtual(diretorioCliente);
			} else {
				System.out.println("Diretorio não encontrado.");
			}
		}
	}

	/**Serviço que cria um novo diretório
	 * Valida o nome do novo diretório e chama o metódo de criação de diretório nos servidores.
	 * 
	 * @param nomeNovoDiretorio
	 * @param cliente
	 */
	public void criaDiretorio(String nomeNovoDiretorio, Cliente cliente) {
		String msgSaida = "";
		
		try {
			if (StringUtils.isEmpty(nomeNovoDiretorio)) {
				msgSaida = "Necessário informar um nome para o diretório";
			} else if (nomeNovoDiretorio.charAt(0) == '.' || nomeNovoDiretorio.charAt(0) == ' ') {
				msgSaida = "O nome da pasta não pode começar com '.' ou espaço!";
			} else {
				msgSaida = mapDiretorio.criaDiretorio(nomeNovoDiretorio, cliente.getDiretorioClienteAtual());
			}
		} catch (Exception e) {
			msgSaida = "Erro na criação do diretorio!";
		}
		System.out.println(msgSaida);
	}

	/**Serviço que lista os dados do diretório do cliente
	 * Chama o metódo de lista de dados dos servidores
	 * Recebe como resposta duas listas:
	 	** posição 0: lista de arquivos
	 	** posição 1: lista de diretórios
	 * Imprime as duas listas na tela do cliente.
	 * 
	 * @param cliente
	 */
	public void listaDados(Cliente cliente) {
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> listaArquivos = new ArrayList<String>();
		List<String> listaDiretorios = new ArrayList<String>();
		
		listaDados = mapDiretorio.getListaDados(cliente.getDiretorioClienteAtual());
		
		listaArquivos = listaDados.get(0);
		listaDiretorios = listaDados.get(1);
		
		if (CollectionUtils.isNotEmpty(listaDiretorios)) {
			System.out.println(" ");
			System.out.print("Diretorios: ");
			for (String diretorio : listaDiretorios)
				System.out.print("\"" + diretorio + "\"    ");	
		}
		
		if (CollectionUtils.isNotEmpty(listaArquivos)) {
			System.out.print("\nArquivos: ");
			for (String arquivo : listaArquivos)
				System.out.print("\"" + arquivo + "\"    ");
		}
		System.out.println(" ");
	}

	/**Serviço para salvar um no arquivo
	 * Cria um objeto do tipo arquivo e realiza algumas formatações para pegar a extensão do arquivo
	 * Chama o método de salva arquivo nos servidores e ele
	 * retorna o melhor storage para salvar o arquivo, onde:
	 	** melhorStorage index position: 0 - Status do Salvamento
		**								 1 - Mensagem de retorno
		**								 2 - endereço de host do storage
		**								 3 - porta do host do storage
	 * Primeiro salva o arquivo na arvore de diretorio e obtem o storage para ser salvo o arquivo
	 * Segundo salva o arquivo no storage encontrado.
	 * Caso ocorra algum problema ao tentar enviar o arquivo, e realizado rollback no mapDiretorio.
	 *
	 * @param arquivo
	 * @param cliente
	 */
	
	public void salvaArquivo(File arquivo, Cliente cliente) {
		List<String> melhorStorage = new ArrayList<String>();
		Arquivo novoArquivo = new Arquivo();
		String tipoArquivo = arquivo.getName().substring((arquivo.getName().lastIndexOf(".") + 1));
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());
		novoArquivo.setTipoArquivo(tipoArquivo);
		try {
			melhorStorage = mapDiretorio.salvaArquivo(novoArquivo, cliente.getDiretorioClienteAtual());
			if ("true".equalsIgnoreCase(melhorStorage.get(0))) {
				System.out.println("\n" + melhorStorage.get(1));
				if (enviaArquivoStorage(melhorStorage, arquivo, novoArquivo)) {
					System.out.println("Arquivo enviado com sucesso!");
				} else {
					System.out.println("Erro ao enviar o arquivo " + novoArquivo.getNomeArquivo());
					melhorStorage = mapDiretorio.removeArquivo(novoArquivo, cliente.getDiretorioClienteAtual());
					if ("true".equalsIgnoreCase(melhorStorage.get(0))) {
						System.out.println("Efetuado rollback dos dados!");
					}
				}
			} else {
				System.out.println(melhorStorage.get(1));
			}
		} catch (Exception e) {
			System.out.println("Erro ao salvar o arquivo!");
		}
	}
	
	/**Serviço para remover um arquivo
	 * Cria um objeto do tipo arquivo
	 * Chama o método de busca de arquivo nos servidores
	 * Caso o arquivo exista nos servidores, chama o método para remover o arquivo nos servidores e
	 * esse método retorna o storage onde o arquivo foi removido, onde o storage:
	 	** storage index position: 0 - Status do Salvamento
		**						   1 - Mensagem de retorno
		**						   2 - endereço de host do storage
		**						   3 - porta do host do storage
	 * Por último chama o serviço de comunicação com os storages para remover
	 * o arquivo dos storages
	 * 
	 * @param nomeArquivo
	 * @param cliente
	 */
	
	public void removeArquivo(String nomeArquivo, Cliente cliente) {
		List<String> statusStorage = new ArrayList<String>();
		Arquivo arquivo = new Arquivo();
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				statusStorage = mapDiretorio.removeArquivo(arquivo, cliente.getDiretorioClienteAtual());
				if ("true".equalsIgnoreCase(statusStorage.get(0))) {
					System.out.println("\n" + statusStorage.get(1));
					if (apagaArquivoStorage(statusStorage, arquivo)) {
						System.out.println("Arquivo apagado com sucesso!");
					} else {
						System.out.println("Erro ao apagar o arquivo " + arquivo.getNomeArquivo());
					}
				} else {
					System.out.println(statusStorage.get(1));
				}
			} catch (Exception e) {
				System.out.println("Erro ao apagar o arquivo!");
			}
		} else {
			System.out.println("Arquivo não existe nesse diretório!");
		}			
	}

	/**Serviço que envia um novo arquivo para o storage
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage
	 * Chama um método para serializar os dados a ser enviado ao storage e envia os dados ao storage.
	 * 
	 * @param statusStorage dados do storage
	 * @param arquivo dados físico do arquivo a ser salvo
	 * @param novoArquivo objeto do arquivo a ser enviado para o storage
	 * @return Boolean se ocorreu tudo certo na transação
	 */
	private boolean enviaArquivoStorage(List<String> statusStorage, File arquivo, Arquivo novoArquivo) {
		String hostStorage = statusStorage.get(2);
		int portaStorage = Integer.parseInt(statusStorage.get(3));
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		byte[] bufferArquivo = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao servidor!"); 
		   
		    System.out.println("Enviando arquivo...");
	        bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
	        bufferArquivo = serializarArquivo(arquivo, novoArquivo);
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
	
	/** Serviço para apagar o arquivo no storage
	 * Primeiro cria a comunicação socket com o storage, com o host e porta
	 * informado pelos dados do storage
	 * Monta os dados a ser enviado ao storage e envia os dados ao storage.
	 * Seta a opção de remover o arquivo do storage.
	 * 
	 * @param statusStorage dados do storage
	 * @param arquivo a ser removido no storage
	 * @return Boolean que indica que a transação ocorreu certo ou errado
	 */
	private boolean apagaArquivoStorage(List<String> statusStorage, Arquivo arquivo) {
		String hostStorage = statusStorage.get(2);
		int portaStorage = Integer.parseInt(statusStorage.get(3));
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao servidor!"); 
		    
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
			System.out.println("Erro ao tentar remover o arquivo!");
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	

	/**Método que serializa os dados do arquivo a ser enviado ao storage
	 * Setá a opção de salvar arquivo para o storage.
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
		   ous.writeInt(Constantes.STORAGE_SALVA_ARQUIVO);
		   ous.writeObject(novoArquivo);
		
		   return bao.toByteArray();
	    } catch (IOException e) {
	       e.printStackTrace();
	    }
	    return null;
	}
}