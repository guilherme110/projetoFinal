package br.com.projeto.cliente;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import br.com.projeto.diretorio.Diretorio;
import br.com.projeto.diretorio.MapDiretorio;

public class ClienteServico {
	//FIXME Verificar como definir o nome do diretorio do cliente
	public String getDiretorioCliente(Cliente cliente) {
		return cliente.getNomeCliente();
	}

	public void salvarArquivo(File arquivo, Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getNomeDiretorioCliente());
		if (mapDiretorio.salvarArquivo(diretorioAtual, arquivo) == null)
			System.out.println("Erro ao salvar o arquivo!");
		else
			System.out.println("Arquivo salvo com sucesso!");
	}

	public void criarDiretorio(String nomeDiretorio, Cliente cliente) {
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		//if (!mapDiretorio.containsKey(nomeDiretorio))
			try {
				mapDiretorio.put(nomeDiretorio, new TreeMap<String,byte[]>());
				System.out.println("Diretorio criado com sucesso!");
			} catch (Exception e) {
				System.out.println("Erro na criação do diretorio!");
			}
			
		//else
			//System.out.println("Esse diretorio já existe");
	}
	
	public void listarArquivos(Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getNomeDiretorioCliente());
		mapDiretorio.get(diretorioAtual);
	}

}
