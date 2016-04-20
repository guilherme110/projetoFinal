package br.com.projeto.cliente;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;
import br.com.projeto.utils.Seguranca;

/**Classe de serviços da aplicação do cliente.
 * Cria um objeto do tipo mapDiretorio responsável pela serialização e 
 * comunicação com o servidor de metadados.
 * 
 * @author guilherme
 *
 */
public class ClienteServico {
	public MapDiretorio mapDiretorio;
	private final int ARQUIVOS_NAO_COMPARADOS = 0;
	private final int ARQUIVOS_IGUAIS = 1;
	private final int ARQUIVOS_DIFERENTES = 2;
	
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
		
	
	/**Serviço que remove um diretório.
	 * Valida o nome do novo diretório e chama o metódo de criação de diretório nos servidThread thread[] = new Thread[cliente.getNumeroStorages()];ores.
	 * 
	 * @param nomeNovoDiretorio
	 * @param cliente
	 */
	public void removeDiretorio(String nomeDiretorio, Cliente cliente) {
		String msgSaida = "";
		
		try {
			if (StringUtils.isEmpty(nomeDiretorio)) {
				msgSaida = "Necessário informar um diretório";
			} else if (nomeDiretorio.charAt(0) == '.' || nomeDiretorio.charAt(0) == ' ') {
				msgSaida = "O nome da pasta não pode começar com '.' ou espaço!";
			} else {
				msgSaida = mapDiretorio.removeDiretorio(nomeDiretorio, cliente.getDiretorioClienteAtual());
			}
		} catch (Exception e) {
			msgSaida = "Erro na exclusão do diretorio!";
		}
		System.out.println(msgSaida);
	}
	

	/**Serviço que lista os dados do diretório do cliente.
	 * Chama o metódo de lista de dados dos servidores.
	 * Calcula o tempo de resposta da requisição.
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

	//FIXME: Implementar mecanismo de ROOLBACK em caso de erro de envio do arquivo para os Storages
	/**Serviço para salvar um no arquivo.
	 * Verifica se o arquivo já existe no diretorio atual do cliente
	 * Caso não exista, salva primeiramente o arquivo no servidor de meta dados
	 * Em caso de sucesso (lista de melhores storages não vazia) salva os arquivos nos storages
	 *
	 * @param arquivo físico a ser enviado
	 * @param cliente dados do cliente atual
	 */
	public void salvaArquivo(File arquivoFisico, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivoLogico = new Arquivo();
			
		if (mapDiretorio.buscaArquivo(arquivoFisico.getName(), cliente.getDiretorioClienteAtual()) == null) {
			salvaArquivoServidorMetaDados(arquivoLogico, cliente, arquivoFisico, listaStorages);
			if (CollectionUtils.isNotEmpty(listaStorages) && (listaStorages.size() == cliente.getNumeroStorages())) {
				salvaArquivoStorage(listaStorages, arquivoLogico, arquivoFisico, cliente);
			} else {
				System.out.println("Erro ao salvar o arquivo no servidor de meta dados!");
			}
		} else {
			System.out.println("\nJá existe um arquivo com esse nome, nesse diretorio!");
		}
			
	}
	
	/**Método para salvar o arquivo no servidor de meta dados
	 * Seta os atributos do objeto do arquivo.
	 * Em seguida gera o código hash, a ser usado no arquivo
	 * Chama o método de salva arquivo nos servidores e ele retorna uma lista com os storages a serem salvos.
	 * 
	 * @param novoArquivo arquivo lógico a ser salvo
	 * @param cliente dados do cliente da aplicação
	 * @param arquivo arquivo físico a ser salvo
	 * @param listaStorages lista com os melhores storages
	 */
	public void salvaArquivoServidorMetaDados(Arquivo novoArquivo, Cliente cliente, 
			File arquivo, List<Storage> listaStorages) {
		List<Storage> listaStoragesTemp = new ArrayList<Storage>();
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());
		
		try {
			FileInputStream inputArquivo = new FileInputStream(arquivo);
			novoArquivo.setCodigoHash(Seguranca.geraHashArquivo(inputArquivo));
			inputArquivo.close();
			listaStoragesTemp = mapDiretorio.salvaArquivo(novoArquivo, cliente);
			
			listaStorages.clear();
			listaStorages.addAll(listaStoragesTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Método para salvar o arquivo nos storages.
	 * Varre a lista de storages disparando uma thread para cada storage que o arquivo vai ser salvo.
	 * 
	 * @param novoArquivo arquivo lógico a ser salvo
	 * @param cliente dados do cliente da aplicação
	 * @param arquivo arquivo físico a ser salvo
	 * @param listaStorages lista com os melhores storages
	 */
	public void salvaArquivoStorage(List<Storage> listaStorages, Arquivo novoArquivo, 
			File arquivo, Cliente cliente) {
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getNumeroStorages()];
		Thread thread[] = new Thread[cliente.getNumeroStorages()];
		int count = 0;
		
		try {
			for (Storage storage : listaStorages) {
				comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
						arquivo, novoArquivo, Constantes.STORAGE_SALVA_ARQUIVO);
				thread[count] = new Thread(comunicacaoClienteStorage[count]);
				thread[count].start();
				count++;
			}
			
			//aguarda todas as Thread's serem finalizadas
			for (int i = 0; i < count; i++) {
				thread[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**Serviço para remover um arquivo.
	 * Cria um objeto do tipo arquivo.
	 * Chama o método de busca de arquivo nos servidores.
	 * Caso o arquivo exista nos servidores, chama o método para remover o arquivo nos servidores e
	 * esse método retorna uma lista com os storages a serem atualizados.
	 * Calcula o tempo de resposta da requisição.
	 * Por último varre a lista e dispara um thread para remover o arquivo de cada storage.
	 * 
	 * @param nomeArquivo nome do arquivo a ser removido
	 * @param cliente dados do cliente
	 */
	public void removeArquivo2(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getNumeroStorages()];
		Thread thread[] = new Thread[cliente.getNumeroStorages()];
		int count = 0;
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				listaStorages = mapDiretorio.removeArquivo(arquivo, cliente.getDiretorioClienteAtual());
				if (CollectionUtils.isNotEmpty(listaStorages)) {
					for (Storage storage : listaStorages) {
						comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
								arquivo, Constantes.STORAGE_REMOVE_ARQUIVO, cliente.getLocalArmazenamento());
						thread[count] = new Thread(comunicacaoClienteStorage[count]);
						thread[count].start();
						count++;
					}	
					
					//aguarda todas as Thread's serem finalizadas e
					//salva os dados estatisticos.
					for (int i = 0; i < count; i++) {
						thread[i].join();
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
	
	public void removeArquivo(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			removeArquivoServidorMetaDados(arquivo, cliente, listaStorages);
			if (CollectionUtils.isNotEmpty(listaStorages) && (listaStorages.size() == cliente.getNumeroStorages())) {
				removeArquivoStorage(listaStorages, arquivo, cliente);
			} else {
				System.out.println("Erro ao remover o arquivo do servidor de meta dados!");
			}
		} else {
			System.out.println("\nNão existe esse arquivo nesse diretorio!");
		}
			
	}
	
	private void removeArquivoServidorMetaDados(Arquivo arquivo,
			Cliente cliente, List<Storage> listaStorages) {
		List<Storage> listaStoragesTemp = new ArrayList<Storage>();
		
		listaStoragesTemp = mapDiretorio.removeArquivo(arquivo, cliente.getDiretorioClienteAtual());
		listaStorages.clear();
		listaStorages.addAll(listaStoragesTemp);
	}

	private void removeArquivoStorage(List<Storage> listaStorages,
			Arquivo arquivo, Cliente cliente) {
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getNumeroStorages()];
		Thread thread[] = new Thread[cliente.getNumeroStorages()];
		int count = 0;
		
		try {
			for (Storage storage : listaStorages) {
				comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
						arquivo, Constantes.STORAGE_REMOVE_ARQUIVO, cliente.getLocalArmazenamento());
				thread[count] = new Thread(comunicacaoClienteStorage[count]);
				thread[count].start();
				count++;
			}
			
			//aguarda todas as Thread's serem finalizadas
			for (int i = 0; i < count; i++) {
				thread[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao remover o arquivo dos storages!");
		}
	}

	/**Serviço que lê um arquivo dos storages utilizando hash.
	 * Primeiramente verifica se o arquivo solicitado existe no diretório.
	 * Caso encontre o arquivo, busca a lista de storages que esse arquivo está salvo.
	 * Em seguida baixa o arquivo de cada um dos storages da lista, baixando um de cada vez
	 * Compara o hash do arquivo baixado, com o hash esperado
	 * 		- Caso os hash's sejam iguais, chama a função trataArquivos para salvas o arquivo e remove os arquivos temporários
	 * 		- Caso sejam diferentes, baixa o próximo arquivo, até o final da lista de storages.
	 * 
	 * @param nomeArquivo nome do arquivo a ser lido.
	 * @param cliente dados do cliente.
	 */
	public boolean baixaArquivoHash(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		ComunicacaoClienteStorage comunicacaoClienteStorage = new ComunicacaoClienteStorage();
		ArrayList<String> listaLocalArquivosTemp = new ArrayList<String>();
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				listaStorages = mapDiretorio.buscaStorages(arquivo);
				if (CollectionUtils.isNotEmpty(listaStorages)) {
					for (Storage storage : listaStorages) {
						comunicacaoClienteStorage.buscaArquivoStorage(storage, arquivo, cliente.getLocalArmazenamento());
						String localArquivoTemp = cliente.getLocalArmazenamento() + 
								storage.getIdStorage() + arquivo.getNomeArquivo();
						listaLocalArquivosTemp.add(localArquivoTemp);
						
						FileInputStream inputArquivo = new FileInputStream(localArquivoTemp);
						if (arquivo.getCodigoHash().equals(Seguranca.geraHashArquivo(inputArquivo))) {
							trataArquivos(listaLocalArquivosTemp, cliente.getLocalArmazenamento(), 
										arquivo.getNomeArquivo(), localArquivoTemp);	
							
							System.out.println("Arquivo recebido sem modificações.");
							System.out.println("Arquivo: " + arquivo.getNomeArquivo() + " salvo com sucesso!");
							inputArquivo.close();
							return true;
						}
						inputArquivo.close();	
					}
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
		deletaArquivosTemporarios(listaLocalArquivosTemp);
		return false;
	}
	
	
	/**Serviço que lê um arquivo dos storages utilizando thread's.
	 * Primeiramente verifica se o arquivo solicitado existe no diretório.
	 * Caso encontre o arquivo, busca a lista de storages que esse arquivo está salvo.
	 * Em seguida dispara uma thread para cada storage da lista, enviando os 
	 * dados do arquivo a ser lido e aguardando receber os dados do arquivo.
	 * Aguarda todas as thread's serem finalizadas e chama o método que verifica a integridade 
	 * dos arquivos enviados pelos storages.
	 * Caso o arquivo esteja integro, salvo o arquivo no diretório do cliente no próprio método 
	 * que verifica a integridade do arquivo.
	 * 
	 * @param nomeArquivo nome do arquivo a ser lido.
	 * @param cliente dados do cliente.
	 */
	public void baixaArquivoThread(String nomeArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivo = new Arquivo();
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getNumeroStorages()];
		Thread thread[] = new Thread[cliente.getNumeroStorages()];
		int count = 0;
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				listaStorages = mapDiretorio.buscaStorages(arquivo);
				if (CollectionUtils.isNotEmpty(listaStorages)) {
					ArrayList<String> listaLocalArquivosTemp = new ArrayList<String>();
					for (Storage storage : listaStorages) {
						String localNovoArquivo = cliente.getLocalArmazenamento() + 
								storage.getIdStorage() + arquivo.getNomeArquivo();
						comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
								arquivo, Constantes.STORAGE_BUSCA_ARQUIVO, cliente.getLocalArmazenamento());
						thread[count] = new Thread(comunicacaoClienteStorage[count]);
						
						thread[count].start();
						listaLocalArquivosTemp.add(localNovoArquivo);
						count++;
					}
					
					//aguarda todas as Thread's serem finalizadas.
					for (int i = 0; i < count; i++) {
						thread[i].join();
					}
					
					System.out.println("Verificando integridade do arquivo...");
					if (verificaIntegridadeDados(arquivo, listaLocalArquivosTemp, cliente)) {
						System.out.println("Arquivo recebido sem modificações.");
						System.out.println("Arquivo: " + arquivo.getNomeArquivo() + " salvo com sucesso!");
					} else {
						System.out.println("Arquivo danificado ou modificado pelo storage!");
					}
					
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
	
	/**Método que verifica a integridade do arquivo.
	 * Varre a lista de local do novo arquivo, para pegar cada arquivo que foi transferido do storage.
	 * Compara o primeiro arquivo com o restante da lista de arquivos.
	 * Caso o arquivo seja igual ao numero de storages que o arquivo foi salvo menos o numero de falhas aceitos,
	 * o arquivo está integro.
	 * Caso contrario, busca o próximo arquivo e varre novamente a lista.
	 * Implementada uma matriz para não comparar arquivos que anteriormente já foram comparados.
	 * 		- 0: arquivos não comparados
	 * 		- 1: arquivos iguais
	 * 		- 2: arquivos diferentes
	 * No fim apaga os arquivos temporários.
	 * 
	 * @param arquivo dados do arquivo a ser verificado.
	 * @param listaLocalArquivosTemp lista com o local dos arquivos baixados dos storages.
	 * @param cliente dados do cliente.
	 * @return boolean com status da verificação.
	 */
	private boolean verificaIntegridadeDados(Arquivo arquivo, ArrayList<String> listaLocalArquivosTemp,
			Cliente cliente) {
		int numeroFValido = cliente.getNumeroStorages() - cliente.getfNumeroFalhas();
		int arquivosIguais = 1;
		int comparacaoAux[][] = new int [listaLocalArquivosTemp.size()] [listaLocalArquivosTemp.size()];

		try {
			for (int i = 0; i < listaLocalArquivosTemp.size(); i++) {
				String arquivoAux1 = listaLocalArquivosTemp.get(i);
				
				for (int j = 0; j < listaLocalArquivosTemp.size(); j++) {
					if (i != j) {
						String arquivoAux2 = listaLocalArquivosTemp.get(j);
						
						FileInputStream fis1 = new FileInputStream(arquivoAux1);
						FileInputStream fis2 = new FileInputStream(arquivoAux2);
						
						if (comparacaoAux[j][i] == ARQUIVOS_NAO_COMPARADOS) {
							if (IOUtils.contentEquals(fis1, fis2)) {
								comparacaoAux[i][j] = ARQUIVOS_IGUAIS;
								comparacaoAux[j][i] = ARQUIVOS_IGUAIS;
								arquivosIguais++;
							} else {
								comparacaoAux[i][j] = ARQUIVOS_DIFERENTES;
								comparacaoAux[j][i] = ARQUIVOS_DIFERENTES;
							}
							fis1.close();
							fis2.close();
						} else if (comparacaoAux[j][i] == ARQUIVOS_IGUAIS) {
							arquivosIguais++;
						}
						
						if (arquivosIguais == numeroFValido) {	
							trataArquivos(listaLocalArquivosTemp, cliente.getLocalArmazenamento(), 
									arquivo.getNomeArquivo(), arquivoAux1);	
							return true;
						}
					}
				}
				arquivosIguais = 1;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		deletaArquivosTemporarios(listaLocalArquivosTemp);
		return false;
	}

	/**Método para tratar os arquivos temporários
	 * 
	 * @param listaLocalArquivosTemp lista com os arquivos temporários
	 * @param localArmazenamento local de armazenamento, dos arquivos temporários
	 * @param nomeArquivo nome do arquivo tratado
	 * @param localArquivoIntegro local de salvamento do arquivo integro
	 */
	private void trataArquivos(ArrayList<String> listaLocalArquivosTemp,
			String localArmazenamento, String nomeArquivo, String localArquivoIntegro) {
		File arquivoIntegro = new File(localArquivoIntegro);
		File novoNome = new File(localArmazenamento + nomeArquivo);
		
		if (arquivoIntegro.exists()) {
			arquivoIntegro.renameTo(novoNome);
			deletaArquivosTemporarios(listaLocalArquivosTemp);
		}
	}

	/**
	 * Método para deletar os arquivos temporários
	 * @param listaLocalArquivosTemp lista  de arquivos temporários
	 */
	private void deletaArquivosTemporarios(
			ArrayList<String> listaLocalArquivosTemp) {
		// TODO Auto-generated method stub
		
		for (String localArquivoTmp : listaLocalArquivosTemp) {
			File arqTmp = new File(localArquivoTmp);
			if(arqTmp.exists()) {
				arqTmp.delete();
			}
		}
	
	}
	
}