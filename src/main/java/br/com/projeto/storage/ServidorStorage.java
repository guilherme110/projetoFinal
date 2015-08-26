package br.com.projeto.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.utils.Constantes;

/**Classe da aplicação que rodará no Storage.
 * Possuí os objetos: storage, serverSocket e KVProxy.
 * 
 * @author guilherme
 *
 */
public class ServidorStorage {
	private static Storage storage;
	private	static ServerSocket serverSocket;
	private static ServiceProxy KVProxy;
	
	/**Método principal da classe.
	 * Recebe os seguintes dados como parâmetro:
	 	** Id do storage
	 	** Porta de conexão
	 	** Espaço livre de armazenamento
	 	** Local default de armazenamento
	 * Inicializa o servidor e fica em looping aguardando novos clientes.
	 *  
	 * @param args
	 * @throws IOException
	 */
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
	private static boolean iniciaServidor(String[] args) {
		storage = new Storage(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
				Long.parseLong(args[2]), args[3] ,new ArrayList<Arquivo>());

		try {
			serverSocket = new ServerSocket(storage.getPortaConexao());
			storage.setNomeStorage(serverSocket.getInetAddress().getLocalHost().getHostName());
			storage.setEnderecoHost(serverSocket.getInetAddress().getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.out.println("Erro na inicialização do servidor storage!");
			e.printStackTrace();
			return false;
		}
		
		//cria o objeto de proxy com o Id informado como parametro
		try {
			KVProxy = new ServiceProxy(storage.getIdStorage(), "config");
			if (!enviaDadosStorage(storage))
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
	private static boolean enviaDadosStorage(Storage storage) {
		boolean res = false;
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.STORAGE_ENVIA_DADOS);
			
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
	 * Em seguida trata os dados da requisição do cliente e verifica qual operação o
	 * cliente deseja realizar.
	 * Por último realiza a operação do cliente.
	 * 
	 * @return Boolean com o status da requisição do cliente.
	 */
	private static boolean aguardaCliente() {
		Socket cliente = null;	
		boolean res = true;
	
		try {
			System.out.println("\nAguardando novo cliente...");
			cliente = serverSocket.accept();
			System.out.println("Novo cliente conectado, cliente: " + cliente.getInetAddress().getHostAddress());
			
			//Dispara thread para ler os dados do cliente.
			TrataCliente trataCliente = new TrataCliente(cliente.getReceiveBufferSize(), 
					cliente.getInputStream(), cliente.getOutputStream(), storage);
			trataCliente.run();
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return res;
	}

	/**Método para finalizar o servidor de storage
	 * Fecha o server socket
	 */
	public static void terminaServidor() {
		System.out.println("Finalizando servidor!");
		try {
			serverSocket.close();
			System.out.println("Servidor finalizado com sucesso!");
		} catch (IOException e) {
			System.out.println("Erro na finalização do servidor!");
			e.printStackTrace();
		}
	}
}
