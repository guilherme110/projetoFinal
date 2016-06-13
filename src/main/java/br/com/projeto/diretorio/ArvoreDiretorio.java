package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**Classe do objeto de hierarquia de diretórios.
 * 
 * @author guilherme
 *
 */
public class ArvoreDiretorio implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static Node<Object> home;
	private Tree<Object> arvoreDiretorio;
	
	/**Método que cria o objeto arvoreDiretorio.
	 * Cria o diretório raiz "home"
	 */
	public ArvoreDiretorio() {
		Diretorio diretorioRaiz = new Diretorio();
		diretorioRaiz.setNomeDiretorio("home");
		
		home = new Node<Object>("home");
		home.setData(diretorioRaiz);
		arvoreDiretorio = new Tree<Object>(home);
	}

	/**Método que adiciona um novo diretório.
	 * Primeiro cria um novo diretório.
	 * Segundo verifica o diretório do cliente, para pegar o diretório atual do cliente.
	 * Por último verifica se o nome do novo diretório já existe, caso não exista,
	 * cria o novo diretório.
	 * 
	 * @param diretorioCliente lista com o diretório do cliente.
	 * @param nomeNovoDiretorio
	 * @return uma mensagem de status da solicitação.
	 */
	public String addDiretorio(List<String> diretorioCliente, String nomeNovoDiretorio) {
		boolean encontrou = false;
		String msgSaida = "";
		Diretorio novoDiretorio = new Diretorio();
		novoDiretorio.setNomeDiretorio(nomeNovoDiretorio);
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
	
	/**Método que remove um novo diretório.
	 * Primeiro cria um novo diretório.
	 * Segundo verifica o diretório do cliente, para pegar o diretório atual do cliente.
	 * Por último verifica se o nome do novo diretório já existe, caso não exista,
	 * cria o novo diretório.
	 * 
	 * @param diretorioCliente lista com o diretório do cliente.
	 * @param nomeNovoDiretorio
	 * @return uma mensagem de status da solicitação.
	 */
	public String remDiretorio(List<String> diretorioCliente, String nomeDiretorio) {
		boolean encontrou = false;
		int indiceDiretorio;
		String msgSaida = "";
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> listaArquivos = new ArrayList<String>();
		List<String> listaDiretorios = new ArrayList<String>();
		
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
			for (Node<Object> nodeFilho : listaChildren) {
				Diretorio diretorioAux = (Diretorio) nodeFilho.getData();
				if (diretorioAux.getNomeDiretorio().equalsIgnoreCase(nomeDiretorio)) {
					diretorioCliente.add(nomeDiretorio);
					listaDados = listaDados(diretorioCliente);
					listaArquivos = listaDados.get(0);
					listaDiretorios = listaDados.get(1);
					if(listaArquivos.size() > 0 || listaDiretorios.size() > 0){
						msgSaida = "O diretorio informado possui arquivos ou outros diretorios por isso não pode ser excluído!";
						return msgSaida;
					}
					indiceDiretorio = listaChildren.indexOf(nodeFilho);
					nodeAux.removeChildAt(indiceDiretorio);
					msgSaida = "Diretorio excluido com sucesso!";
					return msgSaida;
				}else{
					msgSaida = "Diretorio informado não existe.";
				}
			}
			return msgSaida;
	}
	
	/**Método que adiciona um arquivo em um diretório.
	 * Primeiro verifica o diretório atual do cliente.
	 * Segundo verifica a lista de arquivos do diretório atual do cliente.
	 * Por último verifica se na lista de arquivos, existe algum arquivo com esse nome
	 * caso não exista, adiciona o novo arquivo.
	 * 
	 * @param diretorioCliente lista do diretório atual do cliente.
	 * @param arquivo a ser adicionado.
	 * @return Boolean de status da solicitação.
	 */
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
	
	/**Método que remove um arquivo de um diretório.
	 * Primeiro verifica o diretório atual do cliente.
	 * Segundo verifica a lista de arquivos do diretório atual do cliente.
	 * Por último verifica se na lista de arquivos, existe algum arquivo com esse nome
	 * caso exista, remove o arquivo.
	 * 
	 * @param diretorioCliente
	 * @param arquivo a ser removido
	 * @return Boolean de status da solicitação.
	 */
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
	
		
	
	/**Método que verifica se o diretorio existe em uma lista de nodes.
	 * 
	 * @param listaDiretorio
	 * @param nomeDiretorio
	 * @return Boolean de status da solicitação.
 	 */
	public boolean verificaDiretorio(List<Node<Object>> listaDiretorio, String nomeDiretorio) {
		for (Node<Object> nodeFilho : listaDiretorio) {
			Diretorio diretorioAux = (Diretorio) nodeFilho.getData();
			if (diretorioAux.getNomeDiretorio().equalsIgnoreCase(nomeDiretorio)) {
				return true;
			}
		}
		return false;
	}
	
	/**Metódo que verifica o diretorio do cliente atual.
	 * Primeira pega a raiz da hierarquia do diretório.
	 * Segundo percorre a raiz de acordo com os nomes da lista do diretório atual do cliente.
	 * 
	 * @param diretorioCliente
	 * @return Diretorio atual do cliente.
	 */
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
	
	/**Método que lista os diretórios do diretorio atual do cliente.
	 * 
	 * @param diretorioCliente
	 * @return List<String> com o nome dos diretórios do cliente.
	 */
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
	
	/**Método que lista os dados do diretório do cliente.
	 * Primeiro cria-se duas lista, uma conterá o nome dos diretórios e a outra o nome dos arquivos.
	 * Depois verifica-se o diretório atual do cliente.
	 * A partir do diretório atual le o nome dos diretórios e o nome dos arquivos.
	 * 
	 * @param diretorioCliente
	 * @return ArrayList<List<String>> que contém o nome dos diretórios e o nome dos arquivos.
	 */
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

	/**Método que busta um arquivo no diretório do cliente.
	 * Primeiro verifica-se o diretório atual do cliente.
	 * Segundo lista todos os arquivos do diretório.
	 * Por último verifica-se se o nome de algum arquivo e igual ao arquivo procurado.
	 * 
	 * @param nomeArquivo a ser buscado
	 * @param diretorioCliente
	 * @return Arquivo caso encontre o mesmo
	 */
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
	
	public Tree<Object> getArvoreDiretorio() {
		return arvoreDiretorio;
	}

	public void setArvoreDiretorio(Tree<Object> arvoreDiretorio) {
		this.arvoreDiretorio = arvoreDiretorio;
	}
}
