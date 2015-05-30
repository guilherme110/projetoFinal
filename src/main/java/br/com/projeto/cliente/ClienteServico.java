package br.com.projeto.cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import br.com.projeto.diretorio.Diretorio;
import br.com.projeto.diretorio.MapDiretorio;

public class ClienteServico {
	//FIXME Verificar como definir o nome do diretorio do cliente
	public String getDiretorioCliente(Cliente cliente) {
		return cliente.getNomeCliente();
	}
	
	public void moveDiretorio(String nomeDiretorio, Cliente cliente) {
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		if (mapDiretorio.containsKey(nomeDiretorio)) {
			try {
				mapDiretorio.moveDiretorio(nomeDiretorio, cliente);
			} catch (Exception e) {
				System.out.println("Erro ao tentar acessar o diretorio: " + nomeDiretorio);
			}
		} 
		else {
			System.out.println("Diretorio não encontrado.");
		}
	}

	public void criarDiretorio(String nomeDiretorio, Cliente cliente) {
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		if (!mapDiretorio.containsKey(nomeDiretorio)) {
			try {
				mapDiretorio.put(nomeDiretorio, new TreeMap<String,byte[]>());
				System.out.println("Diretorio criado com sucesso!");
			} catch (Exception e) {
				System.out.println("Erro na criação do diretorio!");
			}
		} 
		else {
			System.out.println("Esse diretorio já existe");
		}
	}
	
	public void listaArquivos(Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		List<String> listaArquivos = new ArrayList<String>();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getDiretorioClienteAtual().get(0));
		listaArquivos = mapDiretorio.getListaArquivos(diretorioAtual);
		
		System.out.println(" ");
		for (String arquivo : listaArquivos) {
			System.out.print(arquivo + "    ");
		}
		System.out.println(" ");
	}

	public void salvarArquivo(File arquivo, Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getDiretorioClienteAtual().get(0));
		if (mapDiretorio.salvarArquivo(diretorioAtual, arquivo) == null)
			System.out.println("Erro ao salvar o arquivo!");
		else
			System.out.println("Arquivo salvo com sucesso!");
	}
}
