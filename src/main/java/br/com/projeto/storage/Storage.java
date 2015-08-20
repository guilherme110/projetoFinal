package br.com.projeto.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;

import br.com.projeto.diretorio.Arquivo;

public class Storage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final AtomicInteger count = new AtomicInteger(0); 
	private String 		  nomeStorage;
	private String 		  enderecoHost;
	private int    		  portaConexao;
	private long   		  espacoLivre;
	private List<Arquivo> listaArquivos;
	private int           idServidor;
	private String	      localArmazenamento;
	
	public Storage() {
		this.idServidor = count.incrementAndGet();
	}
	
	public Storage(int idServidor, int portaConexao, long espacoLivre, 
			String localArmazenamento, List<Arquivo> listaArquivos) {
		super();
		this.portaConexao = portaConexao;
		this.espacoLivre = espacoLivre;
		this.listaArquivos = listaArquivos;
		this.idServidor = idServidor;
		this.localArmazenamento = localArmazenamento;
	}
	public String getNomeStorage() {
		return nomeStorage;
	}
	public void setNomeStorage(String nomeStorage) {
		this.nomeStorage = nomeStorage;
	}
	public String getEnderecoHost() {
		return enderecoHost;
	}
	public void setEnderecoHost(String enderecoHost) {
		this.enderecoHost = enderecoHost;
	}
	public int getPortaConexao() {
		return portaConexao;
	}
	public void setPortaConexao(int portaConexao) {
		this.portaConexao = portaConexao;
	}
	public long getEspacoLivre() {
		return espacoLivre;
	}
	public void setEspacoLivre(long espacoLivre) {
		this.espacoLivre = espacoLivre;
	}
	public List<Arquivo> getListaArquivos() {
		return listaArquivos;
	}
	public void setListaArquivos(List<Arquivo> listaArquivos) {
		this.listaArquivos = listaArquivos;
	}
	public int getIdServidor() {
		return idServidor;
	}

	public void setIdServidor(int idServidor) {
		this.idServidor = idServidor;
	}

	public String getLocalArmazenamento() {
		return localArmazenamento;
	}

	public void setLocalArmazenamento(String localArmazenamento) {
		this.localArmazenamento = localArmazenamento;
	}

	public List<String> getListaNomeArquivos() {
		List<Arquivo> listaArquivos = this.getListaArquivos();
		List<String> listaNomeArquivos = new ArrayList<String>();
		
		if (CollectionUtils.isNotEmpty(listaArquivos)) {
			for (Arquivo arquivo : listaArquivos)
				listaNomeArquivos.add(arquivo.getNomeArquivo());
			return listaNomeArquivos;
		}
		return new ArrayList<String>();
	}

	public void addListaArquivo(Arquivo arquivo) {
		List<Arquivo> listaArquivos = this.getListaArquivos();
		listaArquivos.add(arquivo);
		this.setListaArquivos(listaArquivos);
	}

	public void remListaArquivo(Arquivo arquivo) {
		List<Arquivo> listaArquivos = this.getListaArquivos();
		
		listaArquivos.remove(arquivo);
		this.setListaArquivos(listaArquivos);
		
	}
}
