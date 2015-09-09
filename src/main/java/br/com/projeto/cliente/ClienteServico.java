package br.com.projeto.cliente;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;

/**Classe de serviços da aplicação do cliente.
 * Cria um objeto do tipo mapDiretorio responsável pela serialização e 
 * comunicação com o servidor de metadados.
 * 
 * @author guilherme
 *
 */
public class ClienteServico {
	public MapDiretorio mapDiretorio;
	
	/**Construtor da classe.
	 * Cria o objeto de conexão com os servidores.
	 * 
	 * @param kVProxy
	 */
	public ClienteServico(ServiceProxy kVProxy) {
		mapDiretorio = new MapDiretorio(kVProxy);
	}
	
	/**Serviço de ir para outro diretório.
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

	/**Serviço que cria um novo diretório.
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

	/**Serviço que lista os dados do diretório do cliente.
	 * Chama o metódo de lista de dados dos servidores.
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

	//FIXME: Implementar mecanismo de ROOLBACK e verificar se arquivo existe no diretorio antes de salvar
	/**Serviço para salvar um no arquivo.
	 * Cria um objeto do tipo arquivo e realiza algumas formatações para pegar a extensão do arquivo.
	 * Chama o método de salva arquivo nos servidores e ele retorna uma lista com os storages a serem salvos.
	 * Caso a lista não seja vazia, serializa o arquivo a ser enviado e varre a lista
	 * para enviar o arquivo para cada storage da lista.
	 *
	 * @param arquivo físico a ser enviado
	 * @param cliente dados do cliente atual
	 */
	public void salvaArquivo(File arquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo novoArquivo = new Arquivo();
		String tipoArquivo = arquivo.getName().substring((arquivo.getName().lastIndexOf(".") + 1));
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());
		novoArquivo.setTipoArquivo(tipoArquivo);
		try {
			listaStorages = mapDiretorio.salvaArquivo(novoArquivo, cliente);
			if (CollectionUtils.isNotEmpty(listaStorages) && (listaStorages.size() == cliente.getFNumeroStorages())) {
				System.out.println("\nLendo dados do arquivo...");
				byte[] bufferArquivo = serializarArquivo(arquivo, novoArquivo);
				for (Storage storage : listaStorages) {
					Thread comunicacaoClienteStorage = new Thread (new ComunicacaoClienteStorage(storage, 
							bufferArquivo, Constantes.STORAGE_SALVA_ARQUIVO));
					comunicacaoClienteStorage.run();
				}
			} else {
				System.out.println("Erro ao salvar o arquivo nos storages!");
			}
		} catch (Exception e) {
			System.out.println("Erro ao salvar o arquivo!");
		}
	}
	
	/**Serviço para remover um arquivo.
	 * Cria um objeto do tipo arquivo.
	 * Chama o método de busca de arquivo nos servidores.
	 * Caso o arquivo exista nos servidores, chama o método para remover o arquivo nos servidores e
	 * esse método retorna uma lista com os storages a serem atualizados.
	 * Por último varre a lista e chama o método de remover o arquivo de cada storage.
	 * 
	 * @param nomeArquivo nome do arquivo a ser removido
	 * @param cliente dados do cliente
	 */
	
	public void removeArquivo(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				listaStorages = mapDiretorio.removeArquivo(arquivo, cliente.getDiretorioClienteAtual());
				if (CollectionUtils.isNotEmpty(listaStorages)) {
					for (Storage storage : listaStorages) {
						Thread comunicacaoClienteStorage = new Thread(new ComunicacaoClienteStorage(storage, 
								arquivo, Constantes.STORAGE_REMOVE_ARQUIVO));
						comunicacaoClienteStorage.run();
					}	
				} else {
					System.out.println("Erro ao verificar os storages com o arquivo!");
				}
			} catch (Exception e) {
				System.out.println("Erro ao apagar o arquivo!");
			}
		} else {
			System.out.println("Arquivo não existe nesse diretório!");
		}			
	}
	
	/**Serviço que lê um arquivo dos storages.
	 * Primeiramente verifica se o arquivo solicitado existe no diretório.
	 * Caso encontre o arquivo, busca a lista de storages que esse arquivo está salvo.
	 * Em seguida dispara uma thread para cada storage da lista, enviando os 
	 * dados do arquivo a ser lido e aguardando receber os dados do arquivo.
	 * Aguarda todas as thread's serem finalizadas e chama o método que verifica a integridade 
	 * dos dados enviados pelos Storages.
	 * Caso o arquivo esteja integro, salvo o arquivo no diretório do cliente.
	 * 
	 * @param nomeArquivo nome do arquivo a ser lido.
	 * @param cliente dados do cliente.
	 */
	public void leArquivo(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		Arquivo arquivoEncontrado = new Arquivo();
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getFNumeroStorages()];
		Thread thread[] = new Thread[cliente.getFNumeroStorages()];
		int count = 0;
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				listaStorages = mapDiretorio.buscaStorages(arquivo);
				if (CollectionUtils.isNotEmpty(listaStorages)) {
					for (Storage storage : listaStorages) {
						comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
								arquivo, Constantes.STORAGE_BUSCA_ARQUIVO);
						thread[count] = new Thread(comunicacaoClienteStorage[count]);
						thread[count].start();
						
						count++;
					}
					
					//aguarda todas as Thread's serem finalizadas.
					for (int i = 0; i < count; i++) {
						thread[i].join();
					}
					arquivoEncontrado = verificaIntegridadeDados(comunicacaoClienteStorage);
					if (arquivoEncontrado != null)
						if (salvaArquivoCliente(cliente.getLocalArmazenamento(), arquivoEncontrado))
							System.out.println("Arquivo: " + arquivo.getNomeArquivo() + "salvo com sucesso!");
						else
							System.out.println("Erro ao salvar o arquivo no diretorio do cliente!");
					else
						System.out.println("Arquivo danificado ou modificado pelo storage!");
					
				} else {
					System.out.println("Erro ao verificar os storages com o arquivo!");
				}
			} catch (Exception e) {
				System.out.println("Erro ao ler o arquivo nos storages!");
				e.printStackTrace();
			}
		} else {
			System.out.println("Arquivo não existe nesse diretório!");
		}			
	}
	
	//FIXME: Verificar como validar a integridade dos dados recebido
	/**Método que verifica a integridade dos dados transferido pelos storages e
	 * retorna o arquivo encontrado.
	 * Caso o arquivo não esteja integro, retorna null.
	 * 
	 * @param comunicacaoClienteStorage threads com dados dos arquivos recebidos pelos storages.
	 * @param arquivoEncontrado objeto do arquivo encontrado.
	 * @return arquivo recebido pelos storages.
	 */
	private Arquivo verificaIntegridadeDados(ComunicacaoClienteStorage[] comunicacaoClienteStorage) {
		Arquivo arquivoEncontrado = new Arquivo();
		
		for (ComunicacaoClienteStorage comunicacao : comunicacaoClienteStorage) {
			arquivoEncontrado = comunicacao.getArquivo();
		}
		
		return arquivoEncontrado;
	}
	
	/**Método que salva o arquivo no diretório do cliente.
	 * 
	 * @param localArmazenamento local de armazenamento de arquivos do cliente.
	 * @param arquivo dados do arquivo a ser salvo.
	 * @return Boolean de status da solicitação.
	 */
	private boolean salvaArquivoCliente(String localArmazenamento,
			Arquivo arquivo) {

		String nomeArquivoSaida = localArmazenamento + arquivo.getNomeArquivo();
        
        FileOutputStream bufferArquivoSaida = null;
        try {
        	bufferArquivoSaida = new FileOutputStream(nomeArquivoSaida);
        	bufferArquivoSaida.write(arquivo.getDadosArquivo());
        	bufferArquivoSaida.close();
        } catch (IOException e) {
			e.printStackTrace();
			return false;
        }
		return true;
	}



	/**Método que serializa os dados do arquivo a ser enviado ao storage.
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