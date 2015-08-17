package br.com.projeto.cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;

public class ClienteServico {
	public MapDiretorio mapDiretorio;
	
	//FIXME Verificar como definir o nome do diretorio do cliente
	public String getDiretorioCliente(Cliente cliente) {
		return cliente.getNomeCliente();
	}
	
	//Cria o objeto de conexão com os servidores.
	public ClienteServico(Cliente cliente) {
		mapDiretorio = new MapDiretorio(cliente.getConexao());
	}
	
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

	public void criaDiretorio(String nomeNovoDiretorio, Cliente cliente) {
		String msgSaida = "";
		
		try {
			if (StringUtils.isEmpty(nomeNovoDiretorio)) {
				msgSaida = "Necessário informar um nome para o diretório";
			} else if (nomeNovoDiretorio.charAt(0) == '.' || nomeNovoDiretorio.charAt(0) == ' ') {
				msgSaida = "O nome da pasta não pode começar com '.' ou espaço!";
			} else {
				msgSaida = mapDiretorio.put(nomeNovoDiretorio, cliente.getDiretorioClienteAtual());
			}
		} catch (Exception e) {
			msgSaida = "Erro na criação do diretorio!";
		}
		System.out.println(msgSaida);
	}

	
	public void listaDados(Cliente cliente) {
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> listaArquivos = new ArrayList<String>();
		List<String> listaDiretorios = new ArrayList<String>();
		
		listaDados = mapDiretorio.getListaDados(cliente.getDiretorioClienteAtual());
		
		//a posição é importante uma vez que os arquivos são salvos na primeira posição.
		//e os diretorios são gravados na segunda posição da listaDados.
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
				System.out.print(arquivo + "    ");
		}
		System.out.println(" ");
	}

	/*StatusStorage index position  0 - Status do Salvamento
									1 - Mensagem de retorno
									2 - endereço de host do storage
									3 - porta do host do storage
	*/
	public void salvaArquivo(File arquivo, Cliente cliente) {
		List<String> statusStorage = new ArrayList<String>();
		Arquivo novoArquivo = new Arquivo();
		String tipoArquivo = arquivo.getName().substring((arquivo.getName().lastIndexOf(".") + 1));
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());
		novoArquivo.setTipoArquivo(tipoArquivo);
		try {
			statusStorage = mapDiretorio.salvarArquivo(novoArquivo, cliente.getDiretorioClienteAtual());
			if ("true".equalsIgnoreCase(statusStorage.get(0))) {
				System.out.println(statusStorage.get(1));
				System.out.println("Endereço de Host: " + statusStorage.get(2));
				System.out.println("Porta do Host: " + statusStorage.get(3));
			} else {
				System.out.println(statusStorage.get(1));
			}
		} catch (Exception e) {
			System.out.println("Erro ao salvar o arquivo!");
		}
	}
}
