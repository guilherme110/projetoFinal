package br.com.projeto.servidor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.CollectionUtils;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.storage.Storage;

/**Classe de serviços do objeto Servidor
 * 
 * @author guilherme
 *
 */
public class ServidorServico {
	private ArvoreDiretorio arvoreDiretorio;
	private Map<Integer, Storage> tabelaStorage;
	
	public ServidorServico() {
		this.arvoreDiretorio = new ArvoreDiretorio();
		this.tabelaStorage = new HashMap<Integer,Storage>();
	}
	
	/**Serviço para criar um novo diretório.
	 * Primeiro le os dados do cliente e monta o objeto nome do novo diretorio e diretório do cliente.
	 * Em seguida chama o metódo para adicionar o novo diretório na arvore de diretório.
	 * Por último monta os dados de saida para o cliente.
	 * @param nomeNovoDiretorio 
	 * @param diretorioAtual 
	 * 
	 * @param dados do cliente.
	 * @param ArvoreDiretorio hierarquia de diretórios.
	 * @return mensagem de retorno para o cliente.
	 * @throws IOException.
	 */
	public byte[] criaDiretorio(List<String> diretorioAtual, String nomeNovoDiretorio) throws IOException {
	    String msgRetorno = arvoreDiretorio.addDiretorio(diretorioAtual, nomeNovoDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(msgRetorno);
	    objOut.close();
	    
	    System.out.println("Diretorio: " + nomeNovoDiretorio + "   Status: " + msgRetorno);
	    return saida.toByteArray();
	}
	
	/**Serviço para remover um  diretório.
	 * Primeiro le os dados do cliente e monta o objeto nome do novo diretorio e diretório do cliente.
	 * Em seguida chama o metódo para adicionar o novo diretório na arvore de diretório.
	 * Por último monta os dados de saida para o cliente.
	 * @param nomeDiretorio 
	 * @param diretorioAtual 
	 * 
	 * @param dados do cliente.
	 * @param ArvoreDiretorio hierarquia de diretórios.
	 * @return mensagem de retorno para o cliente.
	 * @throws IOException.
	 */
	public byte[] removeDiretorio(List<String> diretorioAtual, String nomeDiretorio) throws IOException {
	    String msgRetorno = arvoreDiretorio.remDiretorio(diretorioAtual, nomeDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(msgRetorno);
	    objOut.close();
	    
	    System.out.println("Diretorio: " + nomeDiretorio + "   Status: " + msgRetorno);
	    return saida.toByteArray();
	}
	
	/**Serviço que lista os dados do diretório do cliente.
	 * Primeiro le os dados do cliente e monta o objeto diretório do cliente.
	 * Em seguida chama o metodo que busca a lista de dados do diretório.
	 * Por último monta os dados de saida para o cliente.
	 * @param listaDados 
	 * @param diretorioAtual 
	 * 
	 * @param dados do cliente.
	 * @param arvoreDiretorio.
	 * @return lista de dados do diretório.
	 * @throws IOException.
	 */
	public byte[] listaDados(List<String> diretorioAtual, ArrayList<List<String>> listaDados) throws IOException {
	    listaDados = arvoreDiretorio.listaDados(diretorioAtual);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(listaDados);
	    objOut.close();
	    
	    return saida.toByteArray();
	}

	/**Serviço que verifica se um diretório existe no diretorio do cliente .
	 * Primeiro le os dados do cliente e monta o objeto nome do diretorio e diretório do cliente.
	 * Em seguida chama o metodo que busca a lista de diretorios do diretório do cliente e
	 * verifica se o diretorio existe nessa lista.
	 * Por último monta os dados de saida para o cliente.
	 * @param nomeDiretorio 
	 * @param diretorioAtual 
	 * 
	 * @param dados do cliente.
	 * @param arvoreDiretorio.
	 * @return Boolean com o status da solicitação.
	 * @throws IOException
	 */
	public byte[] verificaDiretorio(List<String> diretorioAtual, String nomeDiretorio) throws IOException {
		List<String> listaAux = new ArrayList<String>();
 		boolean retorno = true;
		
		listaAux = arvoreDiretorio.listaDiretorios(diretorioAtual);
		retorno = listaAux.contains(nomeDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(retorno);
	    objOut.close();
	    
	    System.out.println("Diretorio: " + nomeDiretorio + "   Existe: " + retorno);
	    return saida.toByteArray();
	}

	/**Serviço que salva um novo arquivo.
	 * Chama o metodo que salva o arquivo na arvore de diretório.
	 * Por último retorna se conseguiu salvar ou não o arquivo.
	 * 
	 * @param novoArquivo dados do novo arquivo.
	 * @param diretorioCliente dados do diretorio do cliente.
	 * @param arvoreDiretorio
	 * @return status da solicitação.
	 */
	public boolean salvaArquivoArvore(Arquivo novoArquivo,
			List<String> diretorioCliente) {
		
		if (arvoreDiretorio.addArquivo(diretorioCliente, novoArquivo))
			return true;
		return false;
	}
	
	public byte[] salvaArquivo(Arquivo novoArquivo, List<String> diretorioAtual, Integer numeroStorages) {
		List<Storage> listaStorages   = new ArrayList<Storage>();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
		ObjectOutputStream objOut;
		
		buscaListaMelhorStorage(listaStorages, numeroStorages, novoArquivo);
		if ((CollectionUtils.isNotEmpty(listaStorages)) && (numeroStorages == listaStorages.size())) {
			System.out.println("Encontrado lista com os melhores storages, tamanho da lista: " + listaStorages.size());
			if (salvaArquivoArvore(novoArquivo, diretorioAtual)) {
				adicionaArquivoTabelaStorage(novoArquivo, listaStorages);	
				System.out.println("Nome do arquivo salvo: " + novoArquivo.getNomeArquivo());
				System.out.println("Tabela de Storage atualizada!");
			} else {
				System.out.println("Nome de arquivo existente nesse diretório!");
			}
		} else if (listaStorages.size() != numeroStorages){
			System.out.println("Número de storages disponível não atende ao cliente!");
		} else {
			System.out.println("Não há espaço nos storages ou o arquivo já está salvo em todos os Storages!");
			listaStorages = null;
		}

		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(listaStorages);
			objOut.close();
		} catch (IOException ex) {
			System.out.println("Erro na escrita da saída dos dados"); 
			Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
		return saida.toByteArray();
	}
	
	/**Método para salvar um novo arquivo no servidor de metadados.
	 * Primeiro le os dados de entrada do cliente e monta o objeto novoArquivo, diretorioCliente e numeroStorages.
	 * Depois busca a lista com os melhores storages a serem utilizados para salvar o arquivo.
     * Caso a lista não seja vazia, chama o serviço para atualiza os dados da arvore de diretorio e
     * chama o serviço para atualiza os dados da tabela de storage.
     * Por ultimo serializa a lista de storages para ser enviado ao cliente.
     * 
	 * @param dados do cliente
	 * @return byte lista de storages serializada.
	 */
	
	/**Método para remover um arquivo do servidor de metadados.
	 * Primeiro le os dados de entrada do cliente e monta o objeto arquivo e diretorioCliente.
	 * Depois verifica se o arquivo existe nesse diretório, caso exista apaga ele da arvoreDiretorio.
	 * Em seguida verifica os storages onde o arquivo está salvo, remove ele de cada storage e
	 * atualiza a tabela de storage.
     * Por ultimo serializa a lista de storages para ser enviada ao cliente.
	 * @param diretorioAtual 
	 * @param arquivo 
     * 
	 * @param dados do cliente
	 * @return byte lista de storages serializada.
	 */
	public byte[] removeArquivo(Arquivo arquivo, List<String> diretorioAtual) {
		List<Storage> listaStorages	  = new ArrayList<Storage>();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
		ObjectOutputStream objOut;
		
		if (apagaArquivo(arquivo, diretorioAtual)) {
			removeArquivoTabelaStorage(arquivo, listaStorages);	
			System.out.println("Nome do arquivo apagado: " + arquivo.getNomeArquivo());
			System.out.println("Tabela de Storage atualizada!");
		} else {
			System.out.println("Nome de arquivo não existe nesse diretório!");
		}
	
		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(listaStorages);
			objOut.close();
		} catch (IOException ex) {
			System.out.println("Erro na escrita da saída dos dados"); 
			Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
		return saida.toByteArray();
	}
	
	
	
	/**Serviço que remove um arquivo.
	 * Chama o metodo que remove o arquivo da arvore de diretório.
	 * Por último retorna se conseguiu remover ou não o arquivo.
	 * 
	 * @param arquivo
	 * @param diretorioCliente
	 * @param arvoreDiretorio
	 * @return status da solicitação
	 */
	public boolean apagaArquivo(Arquivo arquivo, List<String> diretorioCliente) {
		if (arvoreDiretorio.remArquivo(diretorioCliente, arquivo))
			return true;
		return false;
	}

	/**Serviço que adiciona um novo arquivo nos storages e atualiza a tabela de storages.
	 * Varre a lista de storages a serem salvos.
	 * Depois adiciona o arquivo na lista de arquivos do storage e atualiza o espaço livre do storage.
	 * Por último atualiza a tabela de storages.
	 * 
	 * @param arquivo dados do arquivo a ser salvo.
	 * @param listaStorages dados a serem retornados ao cliente.
	 * @param tabelaStorage dados de todos os storages.
	 */
	public void adicionaArquivoTabelaStorage(Arquivo arquivo, List<Storage> listaStorages) {	
		
		for (Storage storage : listaStorages) {
			storage.addListaArquivo(arquivo);
			storage.setEspacoLivre(storage.getEspacoLivre() - arquivo.getTamanhoArquivo());
			tabelaStorage.put(storage.getIdStorage(), storage);
		}
		
	}
	
	/**Serviço que remove um arquivo de um storage.
	 * Primeiro varre a lista de storages do arquivo, para verificar em quais storages o arquivo está salvo.
	 * Em seguida remove o arquivo da lista de arquivos do storage e atualiza o espaço livre do storage.
	 * Por último adiciona o storage na lista de storages(retorno para o cliente) e atualiza a tabela de storages.
	 * 
	 * @param arquivo dados do arquivo a ser removido.
	 * @param listaStorages retorno para o cliente.
	 * @param tabelaStorage tabela com dados de todos os storages.
	 */
	public void removeArquivoTabelaStorage(Arquivo arquivo, List<Storage> listaStorages) {
		Storage storage = new Storage();
		
		for (int idStorage: arquivo.getListaIdStorage()) {
			storage = tabelaStorage.get(idStorage);
			storage.remListaArquivo(arquivo);
			storage.setEspacoLivre(storage.getEspacoLivre() + arquivo.getTamanhoArquivo());
			
			listaStorages.add(storage);
			tabelaStorage.put(storage.getIdStorage(), storage);
		}
	}

	/**Serviço que busca a lista de melhores storage para o arquivo ser salvo.
	 * Primeiro varre a tabela de storages.
	 * Em seguida verifica se o número de storages já salvo é menor que o definido pelo cliente.
	 * Então verifica se o espaço disponivel do storage e maior que o do novo arquivo.
	 * Verifica se o storage possuí algum arquivo com o mesmo nome do novo arquivo.
	 * Por último adiciona o storage na lista de storage e atualiza a lista de id de storages do arquivo a ser salvo.
	 * 
	 * @param arquivo
	 * @param tabelaStorage
	 * @return lista de storages atualizado.
	 */
	public void buscaListaMelhorStorage(List<Storage> listaStorages,
			int numeroStorages, Arquivo arquivo) {
		Storage melhorStorage = new Storage();
		int contador = 0;
		
		for (Entry<Integer, Storage> key : tabelaStorage.entrySet()) {
			melhorStorage = key.getValue();
			if (contador < numeroStorages) {
				if (melhorStorage.getEspacoLivre() > arquivo.getTamanhoArquivo()) {
					if(!melhorStorage.getListaNomeArquivos().contains(arquivo.getNomeArquivo())) {
						listaStorages.add(melhorStorage);
						arquivo.addListaIdStorage(melhorStorage.getIdStorage());
						contador ++;
					}
				}
			}
		}
	}

	/**Serviço que verifica se um arquivo existe no diretorio e retorna o arquivo.
	 * Primeiro le os dados de entrada e gera o objeto nomeArquivo e diretorioCliente.
	 * Em seguida chama o metodo que verifica se o arquivo está no diretório do cliente.
	 * Caso encontre o arquivo e retornada na resposta:
	 	** res = true
	 	** arquivo encontrado
	 * @param nomeArquivo 
	 * @param diretorioAtual 
	 * 
	 * @param dados do cliente
	 * @param arvoreDiretorio
	 * @return Res e Arquivo encontrado
	 * @throws IOException
	 */
	public byte[] buscaArquivo(List<String> diretorioAtual, String nomeArquivo) throws IOException {
		Arquivo arquivo = null;
		boolean res = false;
	   
	    arquivo = arvoreDiretorio.buscaArquivo(nomeArquivo, diretorioAtual);
	    if (arquivo != null)
	    	res = true;
	    
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(res);
	    objOut.writeObject(arquivo);
	    objOut.close();
	    
	    System.out.println("Arquivo: " + nomeArquivo + "   Existe: " + res);
	    return saida.toByteArray();
	}

	/**Serviço que adiciona um novo storage na tabela de storage.
	 * Primeiro le os dados de entrada e gera o objeto storage.
	 * Em seguida insere os dados do novo storage na tabela de storage, o id na tabela sera o id do storage.
	 * Por último gera os dados de saida para o servidor storage.
	 * @param storage 
	 * 
	 * @param dados do storage a ser adicionado
	 * @param tabelaStorage
	 * @return Boolean com a situação da solicitação
	 * @throws IOException
	 */
	public byte[] salvaStorage(Storage storage) throws IOException {
		boolean res = false;
		
	    try {
	    	tabelaStorage.put(storage.getIdStorage(), storage);
	    	res = true;
	    } catch (Exception ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	       res = false;
	    }
	   
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeBoolean(res);
	    objOut.close();
	    
	    System.out.println("Storage: " + storage.getNomeStorage() + " salvo com sucesso!");
	    return saida.toByteArray();
	}

	/**Serviço que busca a lista de storages onde o arquivo está salvo.
	 * Le o arquivo de entrada e varre sua lista de id de storage salvo.
	 * Para cada idStorage, verifica o storage na tabela de storages e monta a lista de saida.
	 * 
	 * @param arquivo arquivo para verificar os storages onde está salvo
	 * @param tabelaStorage 
	 * @return listaStorages contem os storages onde o arquivo está salvo
	 * @throws IOException
	 */
	public byte[] buscaListaStorages(Arquivo arquivo) throws IOException {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Storage storage = new Storage();
	
		for (int idStorage : arquivo.getListaIdStorage()) {
			storage = tabelaStorage.get(idStorage);
			listaStorages.add(storage);
		}
		  
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(listaStorages);
	    objOut.close();
	    
	    System.out.println("Storages encontrados: " + listaStorages.size());
	    return saida.toByteArray();
	}

	public ArvoreDiretorio getArvoreDiretorio() {
		return arvoreDiretorio;
	}

	public void setArvoreDiretorio(ArvoreDiretorio arvoreDiretorio) {
		this.arvoreDiretorio = arvoreDiretorio;
	}

	public Map<Integer, Storage> getTabelaStorage() {
		return tabelaStorage;
	}

	public void setTabelaStorage(Map<Integer, Storage> tabelaStorage) {
		this.tabelaStorage = tabelaStorage;
	}
}
