package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class ArvoreDiretorio implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Node<Object> home;
	private Tree<Object> arvoreDiretorio;
	
	public ArvoreDiretorio() {
		Diretorio diretorioRaiz = new Diretorio();
		diretorioRaiz.setTotalArquivos(0);
		diretorioRaiz.setNomeDiretorio("home");
		
		home = new Node<Object>("home");
		home.setData(diretorioRaiz);
		arvoreDiretorio = new Tree<Object>(home);
	}

	public String addDiretorio(List<String> diretorioCliente, String nomeNovoDiretorio) {
		boolean encontrou = false;
		String msgSaida = "";
		Diretorio novoDiretorio = new Diretorio();
		novoDiretorio.setNomeDiretorio(nomeNovoDiretorio);
		novoDiretorio.setTotalArquivos(0);
		Node<Object> novoNode = null;
		Node<Object> nodeAux = home;
		
		//verifica o diretorio do cliente
		List<Node<Object>> listaChildren = home.getChildren();
		for (String aux : diretorioCliente) {
			for (Node<Object> nodeFilho : listaChildren) {
				Diretorio diretorioFilho = (Diretorio) nodeFilho.getData();
				if (diretorioFilho.getNomeDiretorio().equalsIgnoreCase(aux)) {
					nodeAux = nodeFilho;
					encontrou = true;
				}
			}
			if (encontrou == true) {
				listaChildren = nodeAux.getChildren();
				encontrou = false;
			}
		}
		
		if(!verificaDiretorio(listaChildren, nomeNovoDiretorio)) {
			novoNode = new Node<Object>(nomeNovoDiretorio);
			novoNode.setData(novoDiretorio);
			nodeAux.addChild(novoNode);
			msgSaida = "Diretorio criado com sucesso!";
		} else {
			msgSaida = "Já existe uma pasta com esse nome!";
		}
		return msgSaida;
	}
	
	//Adiciona um arquivo no diretorio atual do cliente.
	public boolean addArquivo(List<String> diretorioCliente, Arquivo arquivo) {
		Diretorio    diretorioAtual  = (Diretorio) home.getData();
		List<String> listaArquivos   = new ArrayList<String>();
		
		//verifica o diretório atual do cliente.
		diretorioAtual = verificaDiretorioAtual(diretorioCliente);
		
		//verifica se o diretorio já possuí um arquivo com esse nome
		listaArquivos = diretorioAtual.getNomeArquivos();
		if (!listaArquivos.contains(arquivo.getNomeArquivo())) {
			diretorioAtual.addArquivo(arquivo);
			return true;
		}
		
		return false;
	}
	
	//Remove um arquivo do diretorio atual do cliente.
	public boolean remArquivo(List<String> diretorioCliente, Arquivo arquivo) {
		Diretorio    diretorioAtual  = (Diretorio) home.getData();
		List<String> listaArquivos   = new ArrayList<String>();
		
		//verifica o diretório atual do cliente.
		diretorioAtual = verificaDiretorioAtual(diretorioCliente);
		
		//verifica se o diretorio possuí um arquivo com esse nome
		listaArquivos = diretorioAtual.getNomeArquivos();
		if (listaArquivos.contains(arquivo.getNomeArquivo())) {
			diretorioAtual.remArquivo(arquivo);
			return true;
		}
		
		return false;
	}
	
		
	
	//Verifica se o diretorio existe em uma lista de nodes.
	public boolean verificaDiretorio(List<Node<Object>> listaDiretorio, String nomeDiretorio) {
		for (Node<Object> nodeFilho : listaDiretorio) {
			Diretorio diretorioAux = (Diretorio) nodeFilho.getData();
			if (diretorioAux.getNomeDiretorio().equalsIgnoreCase(nomeDiretorio)) {
				return true;
			}
		}
		return false;
	}
	
	//Metódo que verifica o diretorio do cliente atual.
	private Diretorio verificaDiretorioAtual(List<String> diretorioCliente) {
		boolean      encontrou     = false;
		Node<Object> nodeAux       = home;
		Diretorio    diretorioAux  = (Diretorio) home.getData();
		
		List<Node<Object>> listaChildren = home.getChildren();
		for (String aux : diretorioCliente) {
			for (Node<Object> nodeFilho : listaChildren) {
				Diretorio diretorioFilho = (Diretorio) nodeFilho.getData();
				if (diretorioFilho.getNomeDiretorio().equalsIgnoreCase(aux)) {
					nodeAux = nodeFilho;
					encontrou = true;
				}
			}
			if (encontrou == true) {
				diretorioAux = (Diretorio) nodeAux.getData();
				listaChildren = nodeAux.getChildren();
				encontrou = false;
			}
		}
		
		return diretorioAux;
	}

	public List<String> listaDiretorios(List<String> diretorioCliente) {
		boolean encontrou = false;
		List<String> listaSaida = new ArrayList<String>();
		Node<Object> nodeAux = home;
		Diretorio diretorioAux = new Diretorio();
		List<Node<Object>> listaChildren = home.getChildren();
	
		for (String aux : diretorioCliente) {
			for (Node<Object> nodeFilho : listaChildren) {
				Diretorio diretorioFilho = (Diretorio) nodeFilho.getData();
				if (diretorioFilho.getNomeDiretorio().equalsIgnoreCase(aux)) {
					nodeAux = nodeFilho;
					encontrou = true;
				}
			}
			if (encontrou == true) {
				listaChildren = nodeAux.getChildren();
				encontrou = false;
			}
		}
		for (Node<Object> node : listaChildren) {
			diretorioAux = (Diretorio) node.getData();
			listaSaida.add(diretorioAux.getNomeDiretorio());
		}
		return listaSaida;
	}
	
	
	//Lista arquivos e diretorios
	public ArrayList<List<String>> listaDados(List<String> diretorioCliente) {
		boolean encontrou = false;
		List<String> listaDiretorios = new ArrayList<String>();
		ArrayList<List<String>> listaSaida = new ArrayList<List<String>>();
		Node<Object> nodeAux = home;
		Diretorio diretorioAux = (Diretorio) home.getData();
		List<Node<Object>> listaChildren = home.getChildren();
	
		diretorioAux = verificaDiretorioAtual(diretorioCliente);
		for (String aux : diretorioCliente) {
			for (Node<Object> nodeFilho : listaChildren) {
				Diretorio diretorioFilho = (Diretorio) nodeFilho.getData();
				if (diretorioFilho.getNomeDiretorio().equalsIgnoreCase(aux)) {
					nodeAux = nodeFilho;
					encontrou = true;
				}
			}
			if (encontrou == true) {
				diretorioAux = (Diretorio) nodeAux.getData();
				listaChildren = nodeAux.getChildren();
				encontrou = false;
			}
		}
		
		//adiciona os arquivos do último diretorio encontrado.
		listaSaida.add(diretorioAux.getNomeArquivos());
		
		//verifica os diretorios do último node(Diretorio) visitado
		//adiciona a lista com os ultimos diretórios encontrado.
		for (Node<Object> node : listaChildren) {
			diretorioAux = (Diretorio) node.getData();
			listaDiretorios.add(diretorioAux.getNomeDiretorio());
		}
		listaSaida.add(listaDiretorios);
		
		return listaSaida;
	}

	public Tree<Object> getArvoreDiretorio() {
		return arvoreDiretorio;
	}

	public void setArvoreDiretorio(Tree<Object> arvoreDiretorio) {
		this.arvoreDiretorio = arvoreDiretorio;
	}

	public Arquivo buscaArquivo(String nomeArquivo, List<String> diretorioCliente) {
		Diretorio    diretorioAtual  = (Diretorio) home.getData();
		List<Arquivo> listaArquivos   = new ArrayList<Arquivo>();
		
		//verifica o diretório atual do cliente.
		diretorioAtual = verificaDiretorioAtual(diretorioCliente);
		
		//verifica se o diretorio possuí um arquivo com esse nome.
		listaArquivos = diretorioAtual.getListaArquivos();
		if(CollectionUtils.isNotEmpty(listaArquivos)) {
			for(Arquivo arquivo: listaArquivos) {
				if(arquivo.getNomeArquivo().equalsIgnoreCase(nomeArquivo))
					return arquivo;
			}
		}
	
		return null;
	}
}
