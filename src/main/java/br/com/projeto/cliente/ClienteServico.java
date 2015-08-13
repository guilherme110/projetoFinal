package br.com.projeto.cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.projeto.diretorio.Diretorio;
import br.com.projeto.diretorio.MapDiretorio;

public class ClienteServico {
	//FIXME Verificar como definir o nome do diretorio do cliente
	public String getDiretorioCliente(Cliente cliente) {
		return cliente.getNomeCliente();
	}
	
	public void moveDiretorio(String nomeDiretorio, Cliente cliente) {
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
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

	public void criaDiretorio(String nomeNovoDiretorio, Cliente cliente) {
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		String msgSaida = "";
		
		try {
			msgSaida = mapDiretorio.put(nomeNovoDiretorio, cliente.getDiretorioClienteAtual());
			//System.out.println("Diretorio criado com sucesso!");
		} catch (Exception e) {
			msgSaida = "Erro na criação do diretorio!";
		}
		System.out.println(msgSaida);
	}
	
	public void listaArquivos(Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		List<String> listaArquivos = new ArrayList<String>();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getDiretorioClienteAtual().get(0));
		listaArquivos = mapDiretorio.getListaArquivos(cliente.getDiretorioClienteAtual());
		
		System.out.println(" ");
		for (String arquivo : listaArquivos) {
			System.out.print(arquivo + "    ");
		}
		System.out.println(" ");
	}

	public void salvaArquivo(File arquivo, Cliente cliente) {
		Diretorio diretorioAtual = new Diretorio();
		MapDiretorio mapDiretorio = new MapDiretorio(cliente.getConexao());
		
		diretorioAtual.setNomeDiretorio(cliente.getDiretorioClienteAtual().get(0));
		if (mapDiretorio.salvarArquivo(diretorioAtual, arquivo) == null)
			System.out.println("Erro ao salvar o arquivo!");
		else
			System.out.println("Arquivo salvo com sucesso!");
	}
}
