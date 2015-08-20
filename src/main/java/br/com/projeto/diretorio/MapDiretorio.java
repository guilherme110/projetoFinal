package br.com.projeto.diretorio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.servidor.Servidor;
import br.com.projeto.utils.Constantes;

public class MapDiretorio implements Map<String, Map<String,byte[]>>{
	private ServiceProxy conexao;
	ByteArrayOutputStream out = null;
    
	public MapDiretorio(ServiceProxy conexao) {
		this.setConexao(conexao);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> salvaArquivo(Arquivo novoArquivo, List<String> diretorioCliente) {
		List<String> statusStorage = new ArrayList<String>();
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.SALVA_ARQUIVO);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(novoArquivo);
			out1.writeObject(diretorioCliente);
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	statusStorage = (List<String>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			statusStorage.add("false");
			statusStorage.add("Erro no salvamento do arquivo!");  
		}
		return statusStorage;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> apagaArquivo(Arquivo arquivo, List<String> diretorioCliente) {
		List<String> statusStorage = new ArrayList<String>();
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			
			dos.writeInt(Constantes.APAGA_ARQUIVO);
			
			ObjectOutputStream out1 = new ObjectOutputStream(out) ;
			out1.writeObject(arquivo);
			out1.writeObject(diretorioCliente);
			out1.close();
			
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	statusStorage = (List<String>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			statusStorage.add("false");
			statusStorage.add("Erro ao tentar apagar o arquivo!");  
		}
		return statusStorage;
	}
	
	public boolean containsKey(String key) {
		boolean res = true;
		try {
			out = new ByteArrayOutputStream();
			byte[] rep;
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.VERIFICA_DIRETORIO);
			dos.writeUTF((String) key);
			
			rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
			res = new DataInputStream(in).readBoolean();
			
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			res = false;
		}
		return res;
	}
	
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
		       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
		    }
			return listaDados;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	public String put(String key, List<String> list) {
		String msgSaida = "";
		try {
			out = new ByteArrayOutputStream();
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.CRIA_DIRETORIO); 
			dos.writeUTF(key);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(list);
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
		       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(MapDiretorio.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return arquivo;
	}
	
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
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, byte[]> get(Object key) {
		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, Map<String, byte[]>>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Map<String, byte[]>> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, byte[]> remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Map<String, byte[]>> values() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceProxy getConexao() {
		return conexao;
	}

	public void setConexao(ServiceProxy conexao) {
		this.conexao = conexao;
	}

	@Override
	public Map<String, byte[]> put(String key, Map<String, byte[]> value) {
		// TODO Auto-generated method stub
		return null;
	}

}
