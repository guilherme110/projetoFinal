package br.com.projeto.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.utils.Constantes;

public class StorageServico {
	private	ServerSocket serverSocket;
	private Storage storage;
	private ServiceProxy comunicacao;
	
	public StorageServico(Storage storage, ServiceProxy comunicacao) {
		super();
		this.storage = storage;
		this.comunicacao = comunicacao;
	}

	/**Método que inicializa o servidor.
	 * Primeiro inicializa o objeto storage com os dados passado como parametro.
	 * Em seguida inicializa o servidor socket, escutando na porta passada como parametro.
	 * Por último inicializa o objeto de comunicação com o servidor de metadados e 
	 * chama o método para enviar os dados para o servidor de metadados.
	 * 
	 * @param args:	0 - id storage
	 *              1 - porta de conexão
	 *              2 - espaço livre de armazenamento 
	 *              3 - local de armazenamento
	 *
	 * @return Boolean com o status da inicialização do servidor.
	 */
	@SuppressWarnings("static-access")
	public boolean iniciaServidor() {
		try {
			serverSocket = new ServerSocket(storage.getPortaConexao());
			storage.setNomeStorage(serverSocket.getInetAddress().getLocalHost().getHostName());
			storage.setEnderecoHost(serverSocket.getInetAddress().getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.out.println("Erro na inicialização do servidor storage!");
			e.printStackTrace();
			return false;
		}
		
		try {
			if (!enviaDadosStorage(storage, this.getComunicacao()))
				throw new Exception();
			System.out.println("Dados enviados para o servidor de meta dados com sucesso!");
		} catch (Exception e) {
			System.out.println("Erro de comunicação com os servidores!");
			System.exit(-1);
		}
		
		System.out.println("Servidor iniciado com sucesso!");
		System.out.println("Endereço: " + storage.getEnderecoHost());
		System.out.println("Porta: " + storage.getPortaConexao());
		System.out.println(" ");
		return true;	
	}
	
	/**Método para enviar os dados do storage para o servidor de meta dados
	 * Primeiro serializa os dados para envio ao servidor e 
	 * por último le a resposta do servidor de metadados para verificar se ocorreu tudo certo.
	 * 
	 * @param storage
	 * @return Boolean com status da solicitação
	 */
	private boolean enviaDadosStorage(Storage storage, ServiceProxy KVProxy) {
		boolean res = false;
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.STORAGE_CADASTRO_TABELASTORAGE);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(storage);
			out1.close();
			
			byte[] rep = KVProxy.invokeOrdered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    
		    res = objIn.readBoolean();
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			res = false;  
		}
		return res;
	}

	/**Método que aguarda novas requisições dos clientes.
	 * Primeiro cria-se um novo socket e aguarda novas requisições nesse socket.
	 * Em seguida cria-se uma thread para tratar a requisição do cliente.
	 * 
	 * @return Boolean com o status da requisição do cliente.
	 */
	public boolean aguardaCliente() {
		Socket cliente = null;	
		boolean res = true;
	
		try {
			System.out.println("\nAguardando novo cliente...");
			cliente = serverSocket.accept();
			System.out.println("Novo cliente conectado, cliente: " + cliente.getInetAddress().getHostAddress());
			
			//Dispara thread para ler os dados do cliente.
			TrataCliente trataCliente = new TrataCliente(cliente.getInputStream(), cliente.getOutputStream(), storage);
			Thread thread = new Thread(trataCliente);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return res;
	}

	/**Método para finalizar o servidor de storage
	 * Fecha o server socket
	 */
	public void terminaServidor() {
		System.out.println("Finalizando servidor!");
		try {
			serverSocket.close();
			System.out.println("Servidor finalizado com sucesso!");
		} catch (IOException e) {
			System.out.println("Erro na finalização do servidor!");
			e.printStackTrace();
		}
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public ServiceProxy getComunicacao() {
		return comunicacao;
	}

	public void setComunicacao(ServiceProxy comunicacao) {
		this.comunicacao = comunicacao;
	}
}
