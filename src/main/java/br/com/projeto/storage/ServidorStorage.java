package br.com.projeto.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import br.com.projeto.diretorio.Arquivo;

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
		Arquivo novoArquivo = new Arquivo();
		
		try {
			System.out.println("\n\nAguardando novo cliente...");
			cliente = storage.getSocket().accept();
			System.out.println("Novo cliente conectado, cliente: " + cliente.getInetAddress().getHostAddress());
			
			//le os dados de entrada do cliente
			System.out.println("Lendo os dados do cliente...");
			novoArquivo = trataDadosCliente(cliente);
            
            //salva o arquivo
			System.out.println("Salvando arquivo:  " + novoArquivo.getNomeArquivo());
			if (salvaArquivo(novoArquivo, storage.getLocalArmazenamento()))
				System.out.println("Arquivo " + novoArquivo.getNomeArquivo() + " salvo com sucesso!");
			else
				System.out.println("Erro no salvamento do arquivo: " + novoArquivo.getNomeArquivo());
			cliente.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
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

	private static Arquivo trataDadosCliente(Socket cliente) {
		byte[] objetoEntrada = null;
		Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
    	
		try {
			objetoEntrada = new byte[cliente.getReceiveBufferSize()];
			BufferedInputStream bf = new BufferedInputStream(cliente.getInputStream());
			bf.read(objetoEntrada);
	
		    bis = new ByteArrayInputStream(objetoEntrada);
	        ois = new ObjectInputStream(bis);
	        obj = ois.readObject();
	        bis.close();
	        ois.close();
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        return (Arquivo) obj; 
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
