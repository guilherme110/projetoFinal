package br.com.projeto.cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.testes.LatenciaCliente;
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
	 * @param args[0] Id para o proxy de comunicação com o servidor de meta dados
	 * args[1]: numero de chash's que o sistema suportará
	 * args[2]: diretorio de armazenamento de arquivos baixados do sistema.
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Necessário passar o <process id>, <f do sistema> e <local de armazenamento>");
			System.exit(-1);
		}
		criaCliente(args[0], Integer.parseInt(args[1]), args[2]);
		carregaTela();
		opcoesCliente();
	}

	/**Método que cria o objeto cliente, o objeto clienteServico
	 * e cria a comunicação com os servidores de metadados.
	 * KVProxy é o serviço de proxy de comunicação com os servidores de metadados.
	 * 
	 * @param idCliente id do cliente.
	 * @param FNumeroStorages (2 * FNumeroStorages + 1) número de storages a serem utilizados.
	 * @param localArmazenamento local de armazenamento de arquivos recebidos pelo storage
	 */	
	public static void criaCliente(String idCliente, int fNumeroFalhas, String localArmazenamento) {
		cliente = new Cliente();
		
		cliente.setIdCliente(Integer.parseInt(idCliente));
		cliente.setDiretorioClienteAtual(new ArrayList<String>());
		cliente.getDiretorioClienteAtual().add("home");
		cliente.setNumeroStorages(2 * fNumeroFalhas + 1);
		cliente.setfNumeroFalhas(fNumeroFalhas);
		cliente.setLocalArmazenamento(localArmazenamento);
		
		try {
			KVProxy = new ServiceProxy(cliente.getIdCliente());
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
		System.out.println("rmd -> remover diretorio [nome do diretorio]");
		System.out.println("sv -> salvar arquivo [caminho do arquivo]");
		System.out.println("rm -> remover arquivo [caminho do arquivo]");
		System.out.println("la -> le arquivo [nome do arquivo]");
		System.out.println("ls -> listar arquivos e diretorios");
		System.out.println("ts -> teste de performance");
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
		case "rmd":
			opcaoRemoveDiretorio(comando, leitor);
			break;
		case "sv":
			opcaoSalvaArquivo(comando, leitor);
			break;
		case "rm":
			opcaoRemoveArquivo(comando, leitor);
			break;
		case "la":
			opcaoBaixaArquivo(comando, leitor);
			break;
		case "ls":
			opcaoListaDados();
			break;
		case "ts":
			opcaoTestes();
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

	/**Método de opcao para remover um novo diretorio.
	 * Caso o nome do diretorio não seja informado solicita o nome dele.
	 * Por último chama o serviço de remover diretorio.
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoRemoveDiretorio(String dadosLeitura, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = dadosLeitura.split(" ")[1];
			nomeDiretorio = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.nextLine();
		}
		clienteServico.removeDiretorio(nomeDiretorio, cliente);
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
			System.out.println("Tamanho do arquivo: " + format.convertNomeBytes(arquivoEntrada.length(), true));
			System.out.println("Salvar arquivo (S/N): ");
			System.out.print("opcao: ");
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
	
	/**Método de opção para buscar um arquivo do storage para o cliente.
	 * Caso o nome do arquivo não seja informado, solicita o nome.
	 * Por último chama o serviço para buscar o arquivo. 
	 * 
	 * @param dadosLeitura dados informado pelo cliente.
	 * @param leitor de dados do cliente.
	 */
	private static void opcaoBaixaArquivo(String dadosLeitura, Scanner leitor) {
		String nomeArquivo;
		
		try { 
			nomeArquivo = dadosLeitura.split(" ")[1];
			nomeArquivo = dadosLeitura.substring(dadosLeitura.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do arquivo: ");
			nomeArquivo = leitor.nextLine();
		}
		//clienteServico.baixaArquivoThread(nomeArquivo, cliente);
		clienteServico.baixaArquivoHash(nomeArquivo, cliente);
		
	}

	
	/**Método de opção de lista os dados do diretório.
	 * Chama o serviço de listar o diretório.
	 */
	private static void opcaoListaDados() {
		clienteServico.listaDados(cliente);
	}
	
	/**Método de opção para realizar testes no sistema.
	 * Primeiramenta solicita o numero de requisições a ser utilizado no teste.
	 * Em seguida solicita a opção de teste a ser realizado.
	 * Dependendo da opção selecionada é solicitado um arquivo para ser utilizado no teste.	 
	 */
	private static void opcaoTestes() {
		int numeroReq = 0;
		int opcaoTeste = 0;
		String caminhoArquivo;

		numeroReq = verificaNumReq();
		
		if (numeroReq != 0) {
			opcaoTeste = verificaOpcaoTeste();
			if (opcaoTeste != 0) {
				LatenciaCliente testeLatencia = new LatenciaCliente(numeroReq, cliente, clienteServico);
				
				switch (opcaoTeste) {
				case 1:
					testeLatencia.testeListarDados();
					break;
				case 2:
					caminhoArquivo = verificaOpcaoArquivo();
					if (caminhoArquivo != null) {
						testeLatencia.testeSalvarArquivo(caminhoArquivo);
					} else {
						System.out.println("Opcao invalida!");
					}
					
					break;
				case 3:
					caminhoArquivo = verificaOpcaoArquivo();
					if (caminhoArquivo != null) {
						testeLatencia.testeRemoverArquivo(caminhoArquivo);
					} else {
						System.out.println("Opcao invalida!");
					}
					
					break;
				default:
					System.out.println("Opcao invalida!");
					break;
				}
			} else {
				System.out.println("Opcao invalida!");
			}
		} else {
			System.out.println("Nuumero de requisicoes invalido!");
		}
			
	}

	/**Método para verificar o número de requisições escolhido no teste
	 * 
	 * @return o número de requisições escolhido
	 */
	@SuppressWarnings("resource")
	private static int verificaNumReq() {
		Scanner leitor = new Scanner(System.in);
		String comando;
		int numeroReq;
		
		System.out.println(" ---------- Escolha a quantidade de Requisicoes do teste ----------");	
		System.out.println(" ");
		System.out.print("Quantidade -> ");
		comando = leitor.nextLine();
		if (!comando.isEmpty()) {
			try {
				numeroReq = Integer.parseInt(comando.split(" ")[0]);
				return numeroReq;
			} catch (Exception e) {
				System.out.println("Número de Requisicoes invalido!");
			}		
		}
		return 0;
	}
	
	/**Método para verificar a opção escolhida para o teste.
	 * 
	 * @return a opção escolhida para o teste.
	 */
	@SuppressWarnings("resource")
	private static int verificaOpcaoTeste() {
		Scanner leitor = new Scanner(System.in);
		String comando;
		int opcaoTeste = 0;
		
		System.out.println(" ---------- Escolha a opcao do teste ----------");
		System.out.println("1 -> teste de leitura de arquivos e diretorios");	
		System.out.println("2 -> teste de salvamento de arquivos");	
		System.out.println("3 -> teste de remocao de arquivos");	
		System.out.println(" ");
		System.out.print("opcao -> ");
		comando = leitor.nextLine();
		if (!comando.isEmpty()) {
			try {
				opcaoTeste = Integer.parseInt(comando.split(" ")[0]);
				return opcaoTeste;
			} catch (Exception e) {
				System.out.println("Opção inválida!");
			}		
		}
		return 0;
	}
	
	/**Método para verificar o arquivo a ser escolhido no teste
	 * 
	 * @return caminho para o arquivo escolhido.
	 */
	@SuppressWarnings("resource")
	private static String verificaOpcaoArquivo() {
		Scanner leitor = new Scanner(System.in);
		String comando;
		String caminhoArquivo = null;
		int opcaoArquivo;
			
		System.out.println(" ---------- Escolha o arquivo a ser utilizado no teste ----------");
		System.out.println("1 -> arquivo pequeno (~ 1 Kb)");	
		System.out.println("2 -> arquivo medio (~ 1 Mb)");	
		System.out.println("3 -> arquivo grande (~ 10 Mb)");	
		System.out.println(" ");
		System.out.print("Arquivo -> ");
		comando = leitor.nextLine();
		if (!comando.isEmpty()) {
			try {
				opcaoArquivo = Integer.parseInt(comando.split(" ")[0]);
				switch (opcaoArquivo) {
				case 1:
					caminhoArquivo = "ArquivoPequeno";
					break;
				case 2:
					caminhoArquivo = "ArquivoMedio";
					break;
				case 3:
					caminhoArquivo = "ArquivoGrande";
					break;
				default:
					break;
				}
				return caminhoArquivo;
			} catch (Exception e) {
				System.out.println("Opção inválida!");
			}		
		}
		return null;
	}
}
