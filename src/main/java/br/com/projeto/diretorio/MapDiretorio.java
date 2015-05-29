package br.com.projeto.diretorio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.bftmap.BFTMap;
import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.tom.ServiceProxy;
import br.com.projeto.utils.Constantes;

public class MapDiretorio implements Map<String, Map<String,byte[]>>{
	private ServiceProxy conexao;
	ByteArrayOutputStream out = null;
    
	public MapDiretorio(ServiceProxy conexao) {
		this.setConexao(conexao);
	}
	
	public byte[] salvarArquivo(Diretorio diretorio, File arquivo) {
		try {
			out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(4);
			dos.writeUTF(diretorio.getNomeDiretorio());
			dos.writeUTF("1");
			dos.writeUTF(arquivo.getName());
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			return rep;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(BFTMap.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}

	}
	
	public boolean containsKey(String key) {
		try {
			out = new ByteArrayOutputStream();
			byte[] rep;
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.VERIFICA_DIRETORIO);
			dos.writeUTF((String) key);
			rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
			boolean res = new DataInputStream(in).readBoolean();
			return res;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(BFTMap.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getListaArquivos(Object key) {
		try {
			out = new ByteArrayOutputStream();
			byte[] rep;
	        List<String> listaArquivos = null;
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.LISTA_ARQUIVOS);
			rep = this.getConexao().invokeUnordered(out.toByteArray());
			ByteArrayInputStream in = new ByteArrayInputStream(rep);
	        ObjectInputStream objIn = new ObjectInputStream(in);
		    try {
		    	listaArquivos = (List<String>) objIn.readObject();
		    } catch (ClassNotFoundException ex) {
		       Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
		    }
			return listaArquivos;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(BFTMap.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,byte[]> put(String key, Map<String,byte[]> value) {
		try {
			out = new ByteArrayOutputStream();
			
			DataOutputStream dos = new DataOutputStream(out); 
			dos.writeInt(Constantes.CRIA_DIRETORIO); 
			dos.writeUTF(key);
			ObjectOutputStream  out1 = new ObjectOutputStream(out) ;
			out1.writeObject(value);
			out1.close();
			byte[] rep = this.getConexao().invokeOrdered(out.toByteArray());
			ByteArrayInputStream bis = new ByteArrayInputStream(rep) ;
			ObjectInputStream in = new ObjectInputStream(bis) ;
			Map<String,byte[]> table = (Map<String,byte[]>) in.readObject();
			in.close();
			return table;

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			Logger.getLogger(BFTMap.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			Logger.getLogger(BFTMap.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
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

}
