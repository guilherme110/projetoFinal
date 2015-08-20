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

public class ServidorServico {
	
	public ServidorServico() {
	
	}
	
	@SuppressWarnings("unchecked")
	public byte[] criaDiretorio(ByteArrayInputStream dados, 
			ArvoreDiretorio ArvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		String nomeNovoDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
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
	
	@SuppressWarnings("unchecked")
	public byte[] listaDados(ByteArrayInputStream dados, ArvoreDiretorio arvoreDiretorio) throws IOException {
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> diretorioCliente = new ArrayList<String>();
	
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    listaDados = arvoreDiretorio.listaDados(diretorioCliente);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(listaDados);
	    objOut.close();
	    
	    return saida.toByteArray();
	}

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
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
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

	public boolean salvaArquivo(Arquivo novoArquivo,
			List<String> diretorioCliente, ArvoreDiretorio arvoreDiretorio) {
		
		if (arvoreDiretorio.addArquivo(diretorioCliente, novoArquivo))
			return true;
		return false;
	}
	
	public boolean apagaArquivo(Arquivo arquivo, List<String> diretorioCliente,
			ArvoreDiretorio arvoreDiretorio) {
		if (arvoreDiretorio.remArquivo(diretorioCliente, arquivo))
			return true;
		return false;
	}

	//Atualiza o storage com o novo arquivo.
	//Atualiza a tabela de storages
	public void addArquivoTabelaStorage(Arquivo arquivo, Storage storage,
			Map<Integer, Storage> tabelaStorage) {	
		storage.addListaArquivo(arquivo);
		storage.setEspacoLivre(storage.getEspacoLivre() - arquivo.getTamanhoArquivo());
		tabelaStorage.put(storage.getIdServidor(), storage);
	}
	
	//Remove o arquivo da lista de arquivo do storage
	//Atualiza o espaço livre do storage.
	//Atualiza a tabela de storages
	public void remArquivoTabelaStorage(Arquivo arquivo, Storage storage,
			Map<Integer, Storage> tabelaStorage) {
		storage.remListaArquivo(arquivo);
		storage.setEspacoLivre(storage.getEspacoLivre() + arquivo.getTamanhoArquivo());
		tabelaStorage.put(storage.getIdServidor(), storage);
		
	}

	//Verifica se há storage com espaço disponivel para o arquivo
	//E se o storage não possui um arquivo com o mesmo nome
	public Storage buscaMelhorStorage(Arquivo arquivo,
			Map<Integer, Storage> tabelaStorage) {
		Storage melhorStorage = new Storage();
		
		for (Entry<Integer, Storage> key : tabelaStorage.entrySet()) {
			melhorStorage = key.getValue();
			if (melhorStorage.getEspacoLivre() > arquivo.getTamanhoArquivo()) {
				if(!melhorStorage.getListaNomeArquivos().contains(arquivo.getNomeArquivo()))
					return melhorStorage;	
			}
		}
		return null;
	}

	//Verifica se um arquivo existe no diretorio e retorna o arquivo
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
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
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
}
