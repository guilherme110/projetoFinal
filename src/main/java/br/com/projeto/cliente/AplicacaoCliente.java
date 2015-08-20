package br.com.projeto.cliente;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.utils.Formatacao;

public class AplicacaoCliente {
	private static ClienteServico clienteServico;
	private static Cliente cliente;
	private static ServiceProxy KVProxy;
	
	public static void main(String[] args) throws IOException {
		if(args.length < 1) {
			System.out.println("Necessário passar o <process id>");
			System.exit(-1);
		}
		criaCliente(args[0]);
		carregaTela();
		opcoesCliente();
	}

	//FIXME Verificar como passar o nome do cliente
	//Cria o objeto cliente e o objeto clienteServico		
	public static void criaCliente(String idCliente) {
		cliente = new Cliente();
		
		cliente.setIdCliente(Integer.parseInt(idCliente));
		cliente.setNomeCliente(idCliente);
		cliente.setDiretorioClienteAtual(new ArrayList<String>());
		cliente.getDiretorioClienteAtual().add("home");
		
		try {
			KVProxy = new ServiceProxy(cliente.getIdCliente(), "config");
			cliente.setConexao(KVProxy);
			clienteServico = new ClienteServico(cliente);
		} catch (Exception e) {
			System.out.println("Erro de comunicação com os servidores!");
			System.exit(-1);
		}
		
	}
	
	public static void carregaTela() {
		System.out.println(" ---------- Lista de comandos do sistema ----------");
		System.out.println("cd -> movimentar entre as pastas [nome da pasta]");
		System.out.println("mk -> criar diretorio [nome do diretorio]");
		System.out.println("sv -> salvar arquivo [caminho do arquivo]");
		System.out.println("rm -> remover arquivo [caminho do arquivo]");
		System.out.println("ls -> listar arquivos e diretorios");
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
			opcaoApagaArquivo(comando, leitor);
		case "ls":
			opcaoListaDados(comando, leitor);
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

	private static String buscaDiretorioAtual(List<String> diretorioClienteAtual) {
		String diretorioAtual = "";
		
		for (String nomeDiretorio : diretorioClienteAtual) {
			diretorioAtual = diretorioAtual + "/" + nomeDiretorio;
		}
		return diretorioAtual;
	}

	private static void opcaoMoveDiretorio(String comando, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = comando.split(" ")[1];
			nomeDiretorio = comando.substring(comando.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.nextLine();
		}
		
		clienteServico.moveDiretorio(nomeDiretorio, cliente);
		
	}

	private static void opcaoCriaDiretorio(String comando, Scanner leitor) {
		String nomeDiretorio;
		
		try {
			nomeDiretorio = comando.split(" ")[1];
			nomeDiretorio = comando.substring(comando.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o nome do diretorio: ");
			nomeDiretorio = leitor.nextLine();
		}
		clienteServico.criaDiretorio(nomeDiretorio, cliente);
	}

	private static void opcaoSalvaArquivo(String comando, Scanner leitor) {
		File arquivoEntrada;
		Formatacao format = new Formatacao();
		String parametro;
		
		try { 
			parametro = comando.split(" ")[1];
			parametro = comando.substring(comando.indexOf(" ") + 1);
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
	
	private static void opcaoApagaArquivo(String comando, Scanner leitor) {
		String nomeArquivo;
		
		try { 
			nomeArquivo = comando.split(" ")[1];
			nomeArquivo = comando.substring(comando.indexOf(" ") + 1);
		} catch (Exception e) {
			System.out.print("Insira o local do arquivo: ");
			nomeArquivo = leitor.nextLine();
		}
		clienteServico.apagaArquivo(nomeArquivo, cliente);
	}
	
	private static void opcaoListaDados(String comando, Scanner leitor) {
		clienteServico.listaDados(cliente);
	}
}
