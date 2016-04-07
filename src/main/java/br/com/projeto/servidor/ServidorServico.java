package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.storage.Storage;

/**Classe de serviços do objeto Servidor
 * 
 * @author guilherme
 *
 */
public class ServidorServico {
	
	public ServidorServico() {
	
	}
	
	/**Serviço para criar um novo diretório.
	 * Primeiro le os dados do cliente e monta o objeto nome do novo diretorio e diretório do cliente.
	 * Em seguida chama o metódo para adicionar o novo diretório na arvore de diretório.
	 * Por último monta os dados de saida para o cliente.
	 * 
	 * @param dados do cliente.
	 * @param ArvoreDiretorio hierarquia de diretórios.
	 * @return mensagem de retorno para o cliente.
	 * @throws IOException.
	 */
	@SuppressWarnings("unchecked")
	public byte[] criaDiretorio(ByteArrayInputStream dados, ArvoreDiretorio ArvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		String nomeNovoDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    }
		
	    String msgRetorno = ArvoreDiretorio.addDiretorio(diretorioCliente, nomeNovoDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(msgRetorno);
	    objOut.close();
	    dados.close();
	    
	    System.out.println("Diretorio: " + nomeNovoDiretorio + "   Status: " + msgRetorno);
	    return saida.toByteArray();
	}
	
	/**Serviço para remover um  diretório.
	 * Primeiro le os dados do cliente e monta o objeto nome do novo diretorio e diretório do cliente.
	 * Em seguida chama o metódo para adicionar o novo diretório na arvore de diretório.
	 * Por último monta os dados de saida para o cliente.
	 * 
	 * @param dados do cliente.
	 * @param ArvoreDiretorio hierarquia de diretórios.
	 * @return mensagem de retorno para o cliente.
	 * @throws IOException.
	 */
	@SuppressWarnings("unchecked")
	public byte[] removeDiretorio(ByteArrayInputStream dados, ArvoreDiretorio ArvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		String nomeDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    }
		
	    String msgRetorno = ArvoreDiretorio.remDiretorio(diretorioCliente, nomeDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(msgRetorno);
	    objOut.close();
	    dados.close();
	    
	    System.out.println("Diretorio: " + nomeDiretorio + "   Status: " + msgRetorno);
	    return saida.toByteArray();
	}
	
	/**Serviço que lista os dados do diretório do cliente.
	 * Primeiro le os dados do cliente e monta o objeto diretório do cliente.
	 * Em seguida chama o metodo que busca a lista de dados do diretório.
	 * Por último monta os dados de saida para o cliente.
	 * 
	 * @param dados do cliente.
	 * @param arvoreDiretorio.
	 * @return lista de dados do diretório.
	 * @throws IOException.
	 */
	@SuppressWarnings("unchecked")
	public byte[] listaDados(ByteArrayInputStream dados, ArvoreDiretorio arvoreDiretorio) throws IOException {
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> diretorioCliente = new ArrayList<String>();
	
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    listaDados = arvoreDiretorio.listaDados(diretorioCliente);
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
	 * 
	 * @param dados do cliente.
	 * @param arvoreDiretorio.
	 * @return Boolean com o status da solicitação.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public byte[] verificaDiretorio(ByteArrayInputStream dados,
			ArvoreDiretorio arvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		List<String> listaAux = new ArrayList<String>();
 		boolean retorno = true;
		
		String nomeDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    }
		listaAux = arvoreDiretorio.listaDiretorios(diretorioCliente);
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
	public boolean salvaArquivo(Arquivo novoArquivo,
			List<String> diretorioCliente, ArvoreDiretorio arvoreDiretorio) {
		
		if (arvoreDiretorio.addArquivo(diretorioCliente, novoArquivo))
			return true;
		return false;
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
	public boolean apagaArquivo(Arquivo arquivo, List<String> diretorioCliente,
			ArvoreDiretorio arvoreDiretorio) {
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
	public void addArquivoTabelaStorage(Arquivo arquivo, List<Storage> listaStorages,
			Map<Integer, Storage> tabelaStorage) {	
		
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
	public void remArquivoTabelaStorage(Arquivo arquivo, List<Storage> listaStorages,
			Map<Integer, Storage> tabelaStorage) {
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
			int numeroStorages, Arquivo arquivo, Map<Integer, Storage> tabelaStorage) {
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
	 * 
	 * @param dados do cliente
	 * @param arvoreDiretorio
	 * @return Res e Arquivo encontrado
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public byte[] buscaArquivo(ByteArrayInputStream dados,
			ArvoreDiretorio arvoreDiretorio) throws IOException {
		Arquivo arquivo = null;
		boolean res = false;
		List<String> diretorioCliente = new ArrayList<String>();
		
		String nomeArquivo = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    
	    arquivo = arvoreDiretorio.buscaArquivo(nomeArquivo, diretorioCliente);
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
	 * 
	 * @param dados do storage a ser adicionado
	 * @param tabelaStorage
	 * @return Boolean com a situação da solicitação
	 * @throws IOException
	 */
	public byte[] salvaStorage(ByteArrayInputStream dados,
			Map<Integer, Storage> tabelaStorage) throws IOException {
		boolean res = false;
		Storage novoStorage = new Storage();
		
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	novoStorage = (Storage) objIn.readObject();
	    	tabelaStorage.put(novoStorage.getIdStorage(), novoStorage);
	    	res = true;
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	       res = false;
	    }
	   
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeBoolean(res);
	    objOut.close();
	    
	    System.out.println("Storage: " + novoStorage.getNomeStorage() + " salvo com sucesso!");
	    return saida.toByteArray();
	}

	/**Serviço que busca a lista de storages onde o arquivo está salvo.
	 * Le o arquivo de entrada e varre sua lista de id de storage salvo.
	 * Para cada idStorage, verifica o storage na tabela de storages e monta a lista de saida.
	 * 
	 * @param dados arquivo para verificar os storages onde está salvo
	 * @param tabelaStorage 
	 * @return listaStorages contem os storages onde o arquivo está salvo
	 * @throws IOException
	 */
	public byte[] buscaStorages(ByteArrayInputStream dados,
			Map<Integer, Storage> tabelaStorage) throws IOException {
		List<Storage> listaStorages = new ArrayList<Storage>();
		Storage storage = new Storage();
		Arquivo arquivo = new Arquivo();
				
		ObjectInputStream objIn = new ObjectInputStream(dados);
		try {
			arquivo = (Arquivo) objIn.readObject();
			for (int idStorage : arquivo.getListaIdStorage()) {
				storage = tabelaStorage.get(idStorage);
				listaStorages.add(storage);
			}
		} catch (ClassNotFoundException ex) {
	       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	       return null;
		}
		  
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(listaStorages);
	    objOut.close();
	    
	    System.out.println("Storages encontrados: " + listaStorages.size());
	    return saida.toByteArray();
	}
}
