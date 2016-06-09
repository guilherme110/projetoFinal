package br.com.projeto.diretorio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.cliente.Cliente;
import br.com.projeto.servidor.ServidorMetaDados;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;

/**Classe do objeto mapDiretorio responsável pelos métodos de comunicação com o servidor
 * de metadados.
 * 
 * @author guilherme
 *
 */
public class MapDiretorio {
	private ServiceProxy conexao;
	ByteArrayOutputStream out = null;
    
	/**Construtor que setá a conexão com o servidor de metadados.
	 * 
	 * @param conexao
	 */
	public MapDiretorio(ServiceProxy conexao) {
		this.setConexao(conexao);
	}
	
	/**Método que salva um arquivo no servidor de metadados.
	 * Serializa os parametros arquivo, diretorio do cliente e o número de storages a serem salvos.
	 * Chama o método inokeOrdered para enviar os parametros para o servidor de metadados.
	 * Por última serializa o objeto de retorno listaStorages com os storages a serem atualizados.
	 * 
	 * @param novoArquivo arquivo a ser salvo.
	 * @param diretorioCliente dados do diretório do cliente.
	 * @return listaStorages lista com os storages a serem atualizados.
	 */
	@SuppressWarnings("unchecked")
	public List<Storage> salvaArquivo(Arquivo novoArquivo, Cliente cliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.SALVA_ARQUIVO);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(novoArquivo);
			out1.writeObject(cliente.getDiretorioClienteAtual());
			out1.writeObject(cliente.getNumeroStorages());
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	listaStorages = (List<Storage>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
		       return null;
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;  
		}
		return listaStorages;
	}
	
	/**Método que remove um arquivo do servidor de metadados.
	 * Serializa os parametros arquivo e diretorio do cliente para serem enviados.
	 * Chama o método inokeOrdered para enviar os parametros para o servidor de metadados.
	 * Por última serializa o objeto de retorno listaStorages com os storages a serem atualizados.
	 * 
	 * @param arquivo arquivo a ser removido
	 * @param diretorioCliente dados do diretório do cliente
	 * @return listaStorages lista de storages a serem atualizados
	 */
	@SuppressWarnings("unchecked")
	public List<Storage> removeArquivo(Arquivo arquivo, List<String> diretorioCliente) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.REMOVE_ARQUIVO);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(arquivo);
			out1.writeObject(diretorioCliente);
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	listaStorages = (List<Storage>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return listaStorages;
	}
	
	public String removeDiretorio(String nomeDiretorio, List<String> diretorioCliente) {
		String msgSaida = "";
		try {
			out = new ByteArrayOutputStream();
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.REMOVE_DIRETORIO); 
			dos.writeUTF(nomeDiretorio);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(diretorioCliente);
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream bis = new ByteArrayInputStream(rep) ;
			ObjectInputStream in = new ObjectInputStream(bis) ;
			msgSaida = (String) in.readObject();
			in.close();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			msgSaida = "Erro na exclusão do diretorio!";
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			msgSaida = "Erro na exclusão do diretorio!";
		}
		return msgSaida;
	}
	
	@SuppressWarnings("unchecked")
	public List<Storage> buscaStorages(Arquivo arquivo) {
		List<Storage> listaStorages = new ArrayList<Storage>();
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.BUSCA_STORAGES_ARQUIVO);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(arquivo);
			out1.close();
			
			byte[] rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	listaStorages = (List<Storage>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return listaStorages;
	}
	
	/**Método que busca a lista de dados do diretório do cliente no servidor de metadados
	 * Serializa o parametro diretorio do cliente para ser enviado
	 * Chama o método inokeUnordered para enviar os parametros para o servidor de metadados
	 * Por última serializa o objeto de retorno listaDados com os dados do diretório do cliente.
	 * 
	 * @param diretorioCliente
	 * @return Lista com os dados do diretório do cliente.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<List<String>> getListaDados(List<String> diretorioCliente) {
		try {
			out = new ByteArrayOutputStream();
			byte[] rep;
	        ArrayList<List<String>> listaDados = null;
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.LISTA_DADOS);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(diretorioCliente);
			out1.close();
			
			rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	listaDados = (ArrayList<List<String>>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
		    }
			return listaDados;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	/**Método que cria um novo diretorio no servidor de metadados
	 * Serializa os parametros nome do diretorio e diretorio do cliente para serem enviados
	 * Chama o método inokeOrdered para enviar os parametros para o servidor de metadados
	 * Por última serializa a mensagem de status da operação de retorno.
	 * 
	 * @param novoDiretorio
	 * @param diretorioCliente
	 * @return MEnsagem de status da solicitação
	 */
	public String criaDiretorio(String novoDiretorio, List<String> diretorioCliente) {
		String msgSaida = "";
		try {
			out = new ByteArrayOutputStream();
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.CRIA_DIRETORIO); 
			dos.writeUTF(novoDiretorio);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(diretorioCliente);
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream bis = new ByteArrayInputStream(rep) ;
			ObjectInputStream in = new ObjectInputStream(bis) ;
			msgSaida = (String) in.readObject();
			in.close();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			msgSaida = "Erro na criação do diretorio!";
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			msgSaida = "Erro na criação do diretorio!";
		}
		return msgSaida;
	}
	
	/**Método que busca um arquivo no servidor de metadados
	 * Serializa os parametros nome do arquivo e diretorio do cliente para serem enviados
	 * Chama o método inokeOrdered para enviar os parametros para o servidor de metadados
	 * Por última serializa o objeto de retorno que são:
	 	** res: contem o status da solicitação 
	 	** arquivo: arquivo encontrado, caso res == true
	 * 
	 * @param nomeArquivo
	 * @param diretorioCliente
	 * @return Status da solicitação e o objeto Arquivo encontrado
	 */
	public Arquivo buscaArquivo(String nomeArquivo,
			List<String> diretorioCliente) {
		Arquivo arquivo = null;
		boolean res = false;
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.BUSCA_ARQUIVO);
			dos.writeUTF((String) nomeArquivo);

			ObjectOutputStream outObject = new ObjectOutputStream(out) ;
			outObject.writeObject(diretorioCliente);
			outObject.close();
			
			byte[] rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    
	        //caso não exista o arquivo o primeiro parametro da resposta (res) é falso
	        try {
	        	res = (boolean) objIn.readObject();
		        if (res == false)
		        	return null;
		    	arquivo = (Arquivo) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return arquivo;
	}
	
	/**Método que verifica um diretório no servidor de metadados
	 * Serializa os parametros nome do diretorio e diretorio do cliente para serem enviados
	 * Chama o método inokeOrdered para enviar os parametros para o servidor de metadados
	 * Por última serializa o objeto de retorno boolean, com o status da solicitação
	 * 
	 * @param nomeDiretorio
	 * @param diretorioCliente
	 * @return Boolean caso encontre ou não o diretório no diretório do cliente
	 */
	public boolean verificaDiretorio(String nomeDiretorio,
			List<String> diretorioCliente) {
		boolean saida = true;
		
		try {
			out = new ByteArrayOutputStream();
			byte[] rep;
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.VERIFICA_DIRETORIO);
			dos.writeUTF((String) nomeDiretorio);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(diretorioCliente);
			out1.close();
			
			rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream bis = new ByteArrayInputStream(rep) ;
			ObjectInputStream in = new ObjectInputStream(bis) ;
			saida = (boolean) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			saida = false;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			saida = false;
		}
		return saida;
	}

	public ServiceProxy getConexao() {
		return conexao;
	}

	public void setConexao(ServiceProxy conexao) {
		this.conexao = conexao;
	}

}
