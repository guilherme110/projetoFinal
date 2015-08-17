package br.com.projeto.servidor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;

import br.com.projeto.diretorio.Arquivo;

public class Storage {
	private static final AtomicInteger count = new AtomicInteger(0); 
	private String 		  nomeStorage;
	private String 		  enderecoHost;
	private int    		  portaConexao;
	private long   		  espacoLivre;
	private List<Arquivo> listaArquivos;
	private int           idServidor;
	
	public Storage() {
		this.idServidor = count.incrementAndGet();
	}
	
	//FIXME Verificar se o storage pode ter arquivos com o mesmo nome
	public Storage(String nomeStorage, String enderecoHost, int portaConexao,
			long espacoLivre, List<Arquivo> listaArquivos) {
		super();
		this.nomeStorage = nomeStorage;
		this.enderecoHost = enderecoHost;
		this.portaConexao = portaConexao;
		this.espacoLivre = espacoLivre;
		this.listaArquivos = listaArquivos;
		this.idServidor = count.incrementAndGet();
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

	public void addListaArquivo(Arquivo novoArquivo) {
		List<Arquivo> listaArquivos = this.getListaArquivos();
		listaArquivos.add(novoArquivo);
		this.setListaArquivos(listaArquivos);
	}
}
