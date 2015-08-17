package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	
	public boolean addArquivo(List<String> diretorioCliente,
			Arquivo novoArquivo, ArvoreDiretorio arvoreDiretorio2) {
		boolean      encontrou     = false;
		Node<Object> novoNode      = null;
		Node<Object> nodeAux       = home;
		Diretorio    diretorioAux  = (Diretorio) home.getData();
		List<String> listaArquivos = new ArrayList<String>();
		
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
				diretorioAux = (Diretorio) nodeAux.getData();
				listaChildren = nodeAux.getChildren();
				encontrou = false;
			}
		}
		
		listaArquivos = diretorioAux.getNomeArquivos();
		if (!listaArquivos.contains(novoArquivo.getNomeArquivo())) {
			novoNode = new Node<Object>(novoArquivo.getNomeArquivo());
			novoNode.setData(novoArquivo);
			nodeAux.addChild(novoNode);
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
}
