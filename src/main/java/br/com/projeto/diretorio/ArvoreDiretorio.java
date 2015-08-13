package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArvoreDiretorio implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Node<Object> home = new Node<Object>("home");
	private Tree<Object> arvoreDiretorio = null;
	
	public ArvoreDiretorio() {
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
	
	public List<Node<Object>> verificaNodesCliente(List<String> diretorioAtual) {
		List<Node<Object>> listaChildren = home.getChildren();
		Node<Object> nodeAux = home;
		boolean encontrou = true;
		
		for (String aux : diretorioAtual) {
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
		return listaChildren;
	}
	
	public List<String> listaArquivos(List<String> diretorioCliente) {
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
}
