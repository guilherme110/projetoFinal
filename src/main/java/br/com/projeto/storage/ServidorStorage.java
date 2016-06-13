package br.com.projeto.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.interfaces.InterfaceStorage;

/**Classe da aplicação que rodará no Storage.
 * Possuí os objetos: storage, serverSocket e KVProxy.
 * 
 * @author guilherme
 *
 */
public class ServidorStorage implements InterfaceStorage{
	private static Storage storage;
	private static StorageServico storageServico;
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
		
		ServidorStorage servidorStorage = new ServidorStorage();
		servidorStorage.iniciaAplicacao(args);
		
	}
	
	public void iniciaAplicacao(String args[]) {
		try {
			KVProxy = estabeleceComunicacaoBFT(Integer.parseInt(args[0]));
		} catch (Exception e) {
			System.out.println("Erro na comunicação com o BFT-SMaRt!");
			System.exit(-1);
		}
		
		storage = criaStorage(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
				Long.parseLong(args[2]), args[3] ,new ArrayList<Arquivo>());
		storageServico = criaStorageServico(storage, KVProxy);
		
		//fica em loop enquanto aguarda novos clientes.
		if (storageServico.iniciaServidor()) {
			while(storageServico.aguardaCliente());
			storageServico.terminaServidor();
		}
	}
	@Override
	public ServiceProxy estabeleceComunicacaoBFT(int idStorage) {
		try {
			return new ServiceProxy(idStorage);
		} catch (Exception e) {
			System.out.println("Erro de comunicação com os servidores de metadados");
			System.exit(-1);
		}
		return null;
	}

	@Override
	public Storage criaStorage(int idStorage, int portaConexao,
			long espacoLivre, String localArmazenamento,
			List<Arquivo> listaArquivos) {
		return new Storage(idStorage, portaConexao, espacoLivre, localArmazenamento, listaArquivos);
	}

	@Override
	public StorageServico criaStorageServico(Storage storage,
			ServiceProxy KVProxy) {
		return new StorageServico(storage, KVProxy);
	}
}
