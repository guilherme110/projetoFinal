package br.com.projeto.cliente;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import bftsmart.tom.ServiceProxy;

public class ServidorCliente {
	private static ClienteServico clienteServico;
	private static Cliente cliente;
	private static ServiceProxy KVProxy;
	
	public static void main(String[] args) throws IOException {
		if(args.length < 1) {
			System.out.println("Necessário passar o <process id>");
			System.exit(-1);
		}
		criarCliente(args[0]);
		carregaTela();
		opcoesCliente();
	}

	//FIXME Verificar como passar o nome do cliente
	public static void criarCliente(String idCliente) {
		cliente = new Cliente();
		clienteServico = new ClienteServico();
		
		cliente.setIdCliente(Integer.parseInt(idCliente));
		cliente.setNomeCliente(idCliente);
		cliente.setNomeDiretorioCliente(clienteServico.getDiretorioCliente(cliente));
		
		try {
			KVProxy = new ServiceProxy(cliente.getIdCliente(), "config");
			cliente.setConexao(KVProxy);
		} catch (Exception e) {
			System.out.println("Erro de comunicação com os servidore!");
			System.exit(-1);
		}
		
	}
	
	public static void carregaTela() {
		System.out.println(" ---------- Lista de comandos do sistema ----------");
		System.out.println("cd -> movimentar entre as pastas [nome da pasta]");
		System.out.println("mk -> criar diretorio [nome do diretorio]");
		System.out.println("sv -> salvar arquivo [caminho do arquivo]");
		System.out.println("ls -> listar arquivos");
		System.out.println("exit -> sair do programa");
	}
	
	public static void terminaServidor(Scanner leitor) {
		System.out.println("Fechando conexões!");
		try {
			cliente.getConexao().close();
		} catch (Exception e) {
			System.out.println("Erro ao tentar encerrar o servidor!");
		}
		leitor.close();
		System.exit(0);
	}
	
	private static void opcoesCliente() {
		Scanner leitor = new Scanner(System.in);
		String comando;
		String opcao = "";
		
		System.out.println(" ");
		System.out.print("Comando -> ");
		comando = leitor.nextLine();
		if (!comando.isEmpty())
			opcao = comando.split(" ")[0];
		
		switch (opcao) {
		case "mk":
			opcaoCriarDiretorio(comando, leitor);
			break;
		case "sv":
			opcaoSalvarArquivo(comando, leitor);
			break;
		case "ls":
			opcaoListarArquivos(comando, leitor);
			break;
		case "exit":
			terminaServidor(leitor);
			break;
		default:
			System.out.println("comando invalido!");
			break;
		}
		
		//Chama o metodo de opções novamente
		opcoesCliente();
	}
	
	private static void opcaoCriarDiretorio(String comando, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = comando.split(" ")[1];
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.next();
		}
		clienteServico.criarDiretorio(nomeDiretorio, cliente);
	}

	private static void opcaoSalvarArquivo(String comando, Scanner leitor) {
		File arquivoEntrada;
		String parametro;
		
		try {
			parametro = comando.split(" ")[1];
			arquivoEntrada = new File(parametro);
		} catch (Exception e) {
			System.out.print("Insira o local do arquivo: ");
			arquivoEntrada = new File(leitor.next());
		}
		if(arquivoEntrada.exists()) {
			System.out.println("Nome do arquivo   : " + arquivoEntrada.getName());
			System.out.println("Tamanho do arquivo: " + arquivoEntrada.length());
			System.out.println("Salvar arquivo (S-Sim / N-Não): ");
			System.out.print("opção: ");
			String teste = leitor.next();
			if (teste.equalsIgnoreCase("S")){
				clienteServico.salvarArquivo(arquivoEntrada, cliente);
			}
		} else {
			System.out.println("Arquivo não encontrado.");
		}
	}
	
	private static void opcaoListarArquivos(String comando, Scanner leitor) {
		// TODO Auto-generated method stub
		
	}
}
