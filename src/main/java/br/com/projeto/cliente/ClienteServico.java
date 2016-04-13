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
	 * Valida o nome do novo diretório e chama o metódo de criação de diretório nos servidores.
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

	//FIXME: Implementar mecanismo de ROOLBACK e verificar se arquivo existe no diretorio antes de salvar
	/**Serviço para salvar um no arquivo.
	 * Cria um objeto do tipo arquivo e realiza algumas formatações para pegar a extensão do arquivo.
	 * Chama o método de salva arquivo nos servidores e ele retorna uma lista com os storages a serem salvos.
	 * Caso a lista não seja vazia, serializa o arquivo a ser enviado e varre a lista
	 * disparando uma thread para cada storage que o arquivo vai ser salvo.
	 * Calcula o tempo de resposta da requisição.
	 *
	 * @param arquivo físico a ser enviado
	 * @param cliente dados do cliente atual
	 */
	public void salvaArquivo(File arquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo novoArquivo = new Arquivo();
		ComunicacaoClienteStorage comunicacaoClienteStorage[] = new ComunicacaoClienteStorage[cliente.getNumeroStorages()];
		Thread thread[] = new Thread[cliente.getNumeroStorages()];
		int count = 0;
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());

		try {
			listaStorages = mapDiretorio.salvaArquivo(novoArquivo, cliente);
			if (CollectionUtils.isNotEmpty(listaStorages) && (listaStorages.size() == cliente.getNumeroStorages())) {		
				for (Storage storage : listaStorages) {
					comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
							arquivo, novoArquivo, Constantes.STORAGE_SALVA_ARQUIVO);
					thread[count] = new Thread(comunicacaoClienteStorage[count]);
					thread[count].start();
					count++;
				}
				
				//aguarda todas as Thread's serem finalizadas e
				//salva os dados estatisticos.
				for (int i = 0; i < count; i++)
					thread[i].join();
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
	 * Calcula o tempo de resposta da requisição.
	 * Por último varre a lista e dispara um thread para remover o arquivo de cada storage.
	 * 
	 * @param nomeArquivo nome do arquivo a ser removido
	 * @param cliente dados do cliente
	 */
	
	public void removeArquivo(String nomeArquivo, Cliente cliente) {
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
	
	/**Serviço que lê um arquivo dos storages.
	 * Primeiramente verifica se o arquivo solicitado existe no diretório.
	 * Caso encontre o arquivo, busca a lista de storages que esse arquivo está salvo.
	 * Em seguida dispara uma thread para cada storage da lista, enviando os 
	 * dados do arquivo a ser lido e aguardando receber os dados do arquivo.
	 * Aguarda todas as thread's serem finalizadas e chama o método que verifica a integridade 
	 * dos arquivos enviados pelos storages.
	 * Caso o arquivo esteja integro, salvo o arquivo no diretório do cliente no próprio método 
	 * que verifica a integridade do arquivo.
	 * Calcula o tempo de resposta da requisição.
	 * 
	 * @param nomeArquivo nome do arquivo a ser lido.
	 * @param cliente dados do cliente.
	 */
	public void baixaArquivo(String nomeArquivo, Cliente cliente) {
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
					ArrayList<String> listaLocalNovoArquivo = new ArrayList<String>();
					for (Storage storage : listaStorages) {
						String localNovoArquivo = cliente.getLocalArmazenamento() + 
								storage.getIdStorage() + arquivo.getNomeArquivo();
						comunicacaoClienteStorage[count] = new ComunicacaoClienteStorage(storage, 
								arquivo, Constantes.STORAGE_BUSCA_ARQUIVO, cliente.getLocalArmazenamento());
						thread[count] = new Thread(comunicacaoClienteStorage[count]);
						
						thread[count].start();
						listaLocalNovoArquivo.add(localNovoArquivo);
						count++;
					}
					
					//aguarda todas as Thread's serem finalizadas.
					for (int i = 0; i < count; i++) {
						thread[i].join();
					}
					
					System.out.println("Verificando integridade do arquivo...");
					if (verificaIntegridadeDados(arquivo, listaLocalNovoArquivo, cliente)) {
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
	 * @param listaLocalNovoArquivo lista com o local dos arquivos baixados dos storages.
	 * @param cliente dados do cliente.
	 * @return boolean com status da verificação.
	 */
	private boolean verificaIntegridadeDados(Arquivo arquivo, ArrayList<String> listaLocalNovoArquivo,
			Cliente cliente) {
		int numeroFValido = cliente.getNumeroStorages() - cliente.getfNumeroFalhas();
		int arquivosIguais = 1;
		int comparacaoAux[][] = new int [listaLocalNovoArquivo.size()] [listaLocalNovoArquivo.size()];
		File arquivoFisico = null;
		
		try {
			for (int i = 0; i < listaLocalNovoArquivo.size(); i++) {
				String arquivoAux1 = listaLocalNovoArquivo.get(i);
				
				for (int j = 0; j < listaLocalNovoArquivo.size(); j++) {
					if (i != j) {
						String arquivoAux2 = listaLocalNovoArquivo.get(j);
						
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
						} else if (comparacaoAux[j][i] == ARQUIVOS_IGUAIS) {
							arquivosIguais++;
						}
						
						if (arquivosIguais == numeroFValido) {	
							arquivoFisico = new File(arquivoAux1);
							if (arquivoFisico.exists()) {
								File novoNome = new File(cliente.getLocalArmazenamento() + arquivo.getNomeArquivo());
								arquivoFisico.renameTo(novoNome);
								deletaArquivosTemporarios(listaLocalNovoArquivo);
								
								fis1.close();
								fis2.close();
							}
							
							return true;
						}
						fis1.close();
						fis2.close();
					}
				}
				arquivosIguais = 1;
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}

	/**
	 * Método para deletar os arquivos temporários
	 * @param listaLocalNovoArquivo lista  de arquivos temporários
	 */
	private void deletaArquivosTemporarios(
			ArrayList<String> listaLocalNovoArquivo) {
		// TODO Auto-generated method stub
		
		for (String localArquivoTmp : listaLocalNovoArquivo) {
			File arqTmp = new File(localArquivoTmp);
			if(arqTmp.exists()) {
				arqTmp.delete();
			}
		}
	
	}
	
}