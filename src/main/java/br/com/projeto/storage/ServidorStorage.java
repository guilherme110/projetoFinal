package br.com.projeto.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.utils.Constantes;

/**Classe da aplicação que rodará no Storage
 * Possuí os objetos: storage, serverSocket e KVProxy
 * 
 * @author guilherme
 *
 */
public class ServidorStorage {
	private static Storage storage;
	private	static ServerSocket serverSocket;
	private static ServiceProxy KVProxy;
	
	/**Método principal da classe
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
	
	
	/**Método que inicializa o servidor
	 * Primeiro inicializa o objeto storage com os dados passado como parametro.
	 * Em seguida inicializa o servidor socket, escutando na porta passada como parametro
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
			KVProxy = new ServiceProxy(storage.getIdServidor(), "config");
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

	/**Método que aguarda novas requisições dos clientes
	 * Primeiro cria-se um novo socket e aguarda novas requisições nesse socket.
	 * Em seguida trata os dados da requisição do cliente e verifica qual operação o
	 * cliente deseja realizar
	 * Por último realiza a operação do cliente.
	 * 
	 * @return Boolean com o status da requisição do cliente
	 */
	private static boolean aguardaCliente() {
		Socket cliente = null;
		List<Object> dadosCliente = new ArrayList<Object>();
		Arquivo arquivo = new Arquivo();
		int operacao = 0;
		boolean res = false;
		
		try {
			System.out.println("\nAguardando novo cliente...");
			cliente = serverSocket.accept();
			System.out.println("Novo cliente conectado, cliente: " + cliente.getInetAddress().getHostAddress());
			
			//le os dados de entrada do cliente
			System.out.println("Lendo os dados do cliente...");
			dadosCliente = trataDadosCliente(cliente);
			operacao = (Integer) dadosCliente.get(0);
			arquivo = (Arquivo) dadosCliente.get(1);
			
			//realiza a operação do cliente
			res = realizaOperacaoCliente(arquivo, operacao);
			
			cliente.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return res;
	}

	/**Método para realizar a operação desejada do cliente
	 * Verifica qual operação o cliente deseja realizar e chama o método responsavel pela operação
	 * 
	 * @param arquivo
	 * @param operacao
	 * @return Boolean com o status da operação
	 */
	private static boolean realizaOperacaoCliente(Arquivo arquivo, int operacao) {
		boolean res = false;
		
		switch (operacao) {
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

	/**Método que cria o arquivo no local de armazenamento do storage
	 * Primeiro verifica o local do novo arquivo e em seguida
	 * cria o objeto de buffer para escravar os dados do arquivo.
	 * O objeto buffer de saida lê os dados que o cliente enviou e escreve no local de armazenamento.
	 * 
	 * @param novoArquivo
	 * @param localArmazenamento
	 * @return Boolean com status da solicitação
	 */
	private static boolean salvaArquivo(Arquivo novoArquivo,
			String localArmazenamento) {
		String localNovoArquivo = storage.getLocalArmazenamento() + novoArquivo.getNomeArquivo();
        
        FileOutputStream bufferArquivoSaida = null;
        try {
        	bufferArquivoSaida = new FileOutputStream(localNovoArquivo);
        	bufferArquivoSaida.write(novoArquivo.getDadosArquivo());
        	bufferArquivoSaida.close();
        } catch (IOException e) {
			e.printStackTrace();
			return false;
        }
		return true;
	}
	
	/**Método para remover um arquivo do storage
	 * Verifica o local de armazenamento, juntamento com o nome do arquivo a ser removido.
	 * Por último verifica se o arquivo existe, caso exista, remove o mesmo.
	 *  
	 * @param dadosArquivo
	 * @param localArmazenamento
	 * @return Boolean com status da solicitação
	 */
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
	   */
	/**Método que trata os dados da requisição do cliente
	 * Primeiro le os dados da requisição do cliente
	 * Em seguida cria-se uma lista de dados para retorno
	 * Monta a seguinte saida dadosCliente onde:
	 	** 0 - Opção do cliente
	  	** 1 - Dados do arquivo enviado				
	 * @param cliente
	 * @return
	 */
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
