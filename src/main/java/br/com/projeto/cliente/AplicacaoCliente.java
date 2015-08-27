package br.com.projeto.cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.utils.Formatacao;


/**Classe principal da Aplicação do Cliente
 * 
 * @author guilherme
 *
 */
public class AplicacaoCliente {
	private static ClienteServico clienteServico;
	private static Cliente cliente;
	private static ServiceProxy KVProxy;
	
	/**Método inicial da aplicação
	 * 
	 * @param args[0] Id para o proxy de comunicação com o servidor
	 * de meta dados e args[1] numero de storages que o cliente deseja
	 * que o arquivo seja salvo.
	 */
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Necessário passar o <process id>");
			System.exit(-1);
		}
		criaCliente(args[0], Integer.parseInt(args[1]));
		carregaTela();
		opcoesCliente();
	}

	/**Método que cria o objeto cliente, o objeto clienteServico
	 * e cria a comunicação com os servidores de metadados.
	 * KVProxy é o serviço de proxy de comunicação com os servidores de metadados.
	 * 
	 * @param idCliente id do cliente.
	 * @param FNumeroStorages (2 * FNumeroStorages + 1) número de storages a serem utilizados.
	 */	
	public static void criaCliente(String idCliente, int FNumeroStorages) {
		cliente = new Cliente();
		
		cliente.setIdCliente(Integer.parseInt(idCliente));
		cliente.setNomeCliente(idCliente);
		cliente.setDiretorioClienteAtual(new ArrayList<String>());
		cliente.getDiretorioClienteAtual().add("home");
		cliente.setFNumeroStorages(2 * FNumeroStorages + 1);
		
		try {
			KVProxy = new ServiceProxy(cliente.getIdCliente(), "config");
			clienteServico = new ClienteServico(KVProxy);
		} catch (Exception e) {
			System.out.println("Erro de comunicação com os servidores!");
			System.exit(-1);
		}
		
	}
	
	/**Método que carrega as opções em tela para o cliente.
	 * 
	 */
	public static void carregaTela() {
		System.out.println(" ---------- Lista de comandos do sistema ----------");
		System.out.println("cd -> movimentar entre as pastas [nome da pasta]");
		System.out.println("mk -> criar diretorio [nome do diretorio]");
		System.out.println("sv -> salvar arquivo [caminho do arquivo]");
		System.out.println("rm -> remover arquivo [caminho do arquivo]");
		System.out.println("ls -> listar arquivos e diretorios");
		System.out.println("exit -> sair do programa");
	}

	/**Método que finaliza a aplicação do cliente.
	 * Fecha a conexão com o servidor de metadados e o leitor de dados.
	 * 
	 * @param leitor de dados do cliente
	 */
	public static void finalizaAplicacao(Scanner leitor) {
		System.out.println("Fechando conexões!");
		try {
			KVProxy.close();
		} catch (Exception e) {
			System.out.println("Erro ao tentar encerrar o servidor!");
		}
		leitor.close();
		System.exit(0);
	}
	
	/**Método que carrega o menu de opções para o cliente.
	 * De acordo com a opção escolhida um método é chamado.
	 */
	private static void opcoesCliente() {
		Scanner leitor = new Scanner(System.in);
		String comando;
		String opcao = "";
		String diretorioAtual = buscaDiretorioAtual(cliente.getDiretorioClienteAtual());
		
		System.out.println(" ");
		System.out.print("Comando " + diretorioAtual + " -> ");
		comando = leitor.nextLine();
		if (!comando.isEmpty())
			opcao = comando.split(" ")[0];
		
		switch (opcao) {
		case "cd":
			opcaoMoveDiretorio(comando, leitor);
			break;
		case "mk":
			opcaoCriaDiretorio(comando, leitor);
			break;
		case "sv":
			opcaoSalvaArquivo(comando, leitor);
			break;
		case "rm":
			opcaoRemoveArquivo(comando, leitor);
		case "ls":
			opcaoListaDados();
			break;
		case "exit":
			finalizaAplicacao(leitor);
			break;
		default:
			System.out.println("comando invalido!");
			break;
		}
		
		//Chama o metodo de opções novamente
		opcoesCliente();
	}

	/**Método que busca o diretorio atual do cliente.
	 * Formata o nome para ser apresentado na tela.
	 * 
	 * @param diretorioClienteAtual dados do diretório atual do cliente.
	 * @return String com o nome do diretorio formatado.
	 */
	private static String buscaDiretorioAtual(List<String> diretorioClienteAtual) {
		String diretorioAtual = "";
		
		for (String nomeDiretorio : diretorioClienteAtual) {
			diretorioAtual = diretorioAtual + "/" + nomeDiretorio;
		}
		return diretorioAtual;
	}

	/**Método de opcao para ir para um outro diretorio.
	 * Caso o nome do diretorio não seja informado solicita o nome dele.
	 * Por último chama o serviço para ir a outro diretório.
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoMoveDiretorio(String dadosLeitura, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = dadosLeitura.split(" ")[1];
			nomeDiretorio = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.nextLine();
		}
		
		clienteServico.moveDiretorio(nomeDiretorio, cliente);
		
	}

	/**Método de opcao para criar um novo diretorio.
	 * Caso o nome do diretorio não seja informado solicita o nome dele.
	 * Por último chama o serviço de criar arquivo.
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoCriaDiretorio(String dadosLeitura, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = dadosLeitura.split(" ")[1];
			nomeDiretorio = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.nextLine();
		}
		clienteServico.criaDiretorio(nomeDiretorio, cliente);
	}

	/**Método da opcao de salvar um arquivo.
	 * Caso o caminho para o arquivo não seja informado, solicita o nome dele.
	 * Apresenta para o cliente os dados do arquivo e uma confirmação.
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoSalvaArquivo(String dadosLeitura, Scanner leitor) {
		File arquivoEntrada;
		Formatacao format = new Formatacao();
		String parametro;
		
		try { 
			parametro = dadosLeitura.split(" ")[1];
			parametro = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
			arquivoEntrada = new File(parametro);
		} catch (Exception e) {
			System.out.print("Insira o local do arquivo: ");
			arquivoEntrada = new File(leitor.nextLine());
		}
		if(arquivoEntrada.exists()) {
			System.out.println("Nome do arquivo   : " + arquivoEntrada.getName());
			System.out.println("Tamanho do arquivo: " + format.convertNomeBytes(arquivoEntrada.length()));
			System.out.println("Salvar arquivo (S/N): ");
			System.out.print("opção: ");
			String opcao = leitor.next();
			if (opcao.equalsIgnoreCase("S")){
				clienteServico.salvaArquivo(arquivoEntrada, cliente);
			}
		} else {
			System.out.println("Arquivo não encontrado.");
		}
	}
	
	/**Método de opção para remover um arquivo.
	 * Caso o nome do arquivo não seja informado, solicita o nome.
	 * Por último chama o serviço para remover o arquivo.
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoRemoveArquivo(String dadosLeitura, Scanner leitor) {
		String nomeArquivo;
		
		try { 
			nomeArquivo = dadosLeitura.split(" ")[1];
			nomeArquivo = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o local do arquivo: ");
			nomeArquivo = leitor.nextLine();
		}
		clienteServico.removeArquivo(nomeArquivo, cliente);
	}
	
	/**Método de opção de lista os dados do diretório.
	 * Chama o serviço de listar o diretório.
	 */
	private static void opcaoListaDados() {
		clienteServico.listaDados(cliente);
	}
}
