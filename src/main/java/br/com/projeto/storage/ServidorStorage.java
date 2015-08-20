package br.com.projeto.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.utils.Constantes;

public class ServidorStorage {
	private static Storage storage;
	
	public static void main(String args[]) throws IOException {
		if(args.length < 1) {
			System.out.println("Necessário passar a porta de conexão, o caminho de armazenamento e o nome do storage!");
			System.exit(-1);
		}
		//fica em loop enquanto aguarda novos clientes.
		if (iniciaServidor(args)) {
			while(aguardaCliente());
			terminaServidor();
		}
	}
	
	/*Parametros de input args: 0 - nome do storage
	 * 							1 - porta de conexão
	 *                          2 - espaço livre de armazenamento
	 *                          3 - local de armazenamento */
	
	@SuppressWarnings("static-access")
	private static boolean iniciaServidor(String[] args) {
		storage = new Storage(args[0], Integer.parseInt(args[1]), Long.parseLong(args[2]), 
				new ArrayList<Arquivo>(), args[3]);
		
		try {
			storage.setSocket(new ServerSocket(storage.getPortaConexao()));
			storage.setEnderecoHost(storage.getSocket().getInetAddress().
					getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.out.println("Erro na inicialização do servidor storage!");
			e.printStackTrace();
			return false;
		}
		System.out.println("Servidor iniciado com sucesso!");
		System.out.println("Endereço: " + storage.getEnderecoHost());
		System.out.println("Porta: " + storage.getPortaConexao());
		System.out.println(" ");
		return true;	
	}
	
	private static boolean aguardaCliente() {
		Socket cliente = null;
		List<Object> dadosCliente = new ArrayList<Object>();
		Arquivo arquivo = new Arquivo();
		int opcao = 0;
		boolean res = false;
		
		try {
			System.out.println("\nAguardando novo cliente...");
			cliente = storage.getSocket().accept();
			System.out.println("Novo cliente conectado, cliente: " + cliente.getInetAddress().getHostAddress());
			
			//le os dados de entrada do cliente
			System.out.println("Lendo os dados do cliente...");
			dadosCliente = trataDadosCliente(cliente);
			opcao = (Integer) dadosCliente.get(0);
			arquivo = (Arquivo) dadosCliente.get(1);
			
			//trata a opção do cliente
			res = trataOpcaoCliente(arquivo, opcao);
			
			cliente.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return res;
	}

	//Método para tratar a opção desejada do cliente
	private static boolean trataOpcaoCliente(Arquivo arquivo, int opcao) {
		boolean res = false;
		
		switch (opcao) {
		case Constantes.STORAGE_SALVA_ARQUIVO:	
			System.out.println("Salvando arquivo:  " + arquivo.getNomeArquivo());
			if (salvaArquivo(arquivo, storage.getLocalArmazenamento())) {
				System.out.println("Arquivo " + arquivo.getNomeArquivo() + " salvo com sucesso!");
				res = true;
			} else {
				System.out.println("Erro no salvamento do arquivo: " + arquivo.getNomeArquivo());
				res = false;
			}
			break;
		case Constantes.STORAGE_REMOVE_ARQUIVO:
			System.out.println("Removendo o arquivo:  " + arquivo.getNomeArquivo());
			if (removeArquivo(arquivo, storage.getLocalArmazenamento())) {
				System.out.println("Arquivo " + arquivo.getNomeArquivo() + " removido com sucesso!");
				res = true;
			} else {
				System.out.println("Erro ao tentar remover o arquivo: " + arquivo.getNomeArquivo());
				res = false;
			}
			break;
		default:
			System.out.println("Opção inválida do cliente!");
			res = false;
			break;
		}
		
		return res;
	}

	//Cria o arquivo no local de armazenamento do storage
	private static boolean salvaArquivo(Arquivo novoArquivo,
			String localArmazenamento) {
		String localNovoArquivo = storage.getLocalArmazenamento() + novoArquivo.getNomeArquivo();
        
        FileOutputStream buffeSaida = null;
        try {
        	buffeSaida = new FileOutputStream(localNovoArquivo);
        	buffeSaida.write(novoArquivo.getDadosArquivo());
        	buffeSaida.close();
        } catch (IOException e) {
			e.printStackTrace();
			return false;
        }
		return true;
	}
	
	private static boolean removeArquivo(Arquivo dadosArquivo,
			String localArmazenamento) {
		String localArquivo = storage.getLocalArmazenamento() + dadosArquivo.getNomeArquivo();
        
        File arquivo = new File(localArquivo);
		if(arquivo.exists()) {
			arquivo.delete();
			return true;
		} else {
			System.out.println("Arquivo não encontrado!");
			return false;
		}
        
	}

	/*Le os dados do cliente e retorna uma lista de Object com os dados do cliente
	  dadosCliente: 0 - Opção do cliente
	  				1 - Dados do arquivo enviado */
					
	public static List<Object> trataDadosCliente(Socket cliente) {
		byte[] dadosEntrada = null;
        ByteArrayInputStream in = null;
        ObjectInputStream objIn = null;
    	List<Object> dadosCliente = new ArrayList<Object>();
        
		try {
			dadosEntrada = new byte[cliente.getReceiveBufferSize()];
			BufferedInputStream bf = new BufferedInputStream(cliente.getInputStream());
			bf.read(dadosEntrada);
	
		    in = new ByteArrayInputStream(dadosEntrada);
		    objIn = new ObjectInputStream(in);
	        
		    //Le a opção do cliente e os dados do arquivo enviado
		    dadosCliente.add((Integer) objIn.readInt());
		    dadosCliente.add((Arquivo) objIn.readObject());
	        
	        in.close();
	        objIn.close();
		} catch (SocketException e2) {
			e2.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return dadosCliente;
	}


	public static void terminaServidor() {
		System.out.println("Finalizando servidor!");
		try {
			storage.getSocket().close();
			System.out.println("Servidor finalizado com sucesso!");
		} catch (IOException e) {
			System.out.println("Erro na finalização do servidor!");
			e.printStackTrace();
		}
	}
}
