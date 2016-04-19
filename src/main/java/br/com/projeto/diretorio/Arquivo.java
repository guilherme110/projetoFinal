package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**Classe para objetos do tipo Arquivo.
 * Está contido o nome do arquivo, tamanho do arquivo,
 * lista de storage onde o arquivo está salvo,
 * codigo hash do arquivo em hexadecimal;
 * 
 * **/
public class Arquivo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String 		  nomeArquivo;
	private Long   		  tamanhoArquivo;
	private List<Integer> listaIdStorage;
	private String		  codigoHash;
	
	
	public Arquivo() {
		this.listaIdStorage = new ArrayList<Integer>();
	}
	
	public Arquivo(String nomeArquivo, Long tamanhoArquivo, List<Integer> listaIdStorage, 
			String codigoHash) {
		super();
		this.nomeArquivo = nomeArquivo;
		this.tamanhoArquivo = tamanhoArquivo;
		this.listaIdStorage = listaIdStorage;
		this.codigoHash = codigoHash;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}


	public Long getTamanhoArquivo() {
		return tamanhoArquivo;
	}

	public void setTamanhoArquivo(Long tamanhoArquivo) {
		this.tamanhoArquivo = tamanhoArquivo;
	}
	
	public List<Integer> getListaIdStorage() {
		return listaIdStorage;
	}

	public void setListaIdStorage(List<Integer> listaIdStorage) {
		this.listaIdStorage = listaIdStorage;
	}
	
	public void addListaIdStorage(int idStorage) {
		List<Integer> listaIdStorage = this.getListaIdStorage();
		
		listaIdStorage.add(idStorage);
		this.setListaIdStorage(listaIdStorage);
	}

	public String getCodigoHash() {
		return codigoHash;
	}

	public void setCodigoHash(String codigoHash) {
		this.codigoHash = codigoHash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((listaIdStorage == null) ? 0 : listaIdStorage.hashCode());
		result = prime * result
				+ ((nomeArquivo == null) ? 0 : nomeArquivo.hashCode());
		result = prime * result
				+ ((tamanhoArquivo == null) ? 0 : tamanhoArquivo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Arquivo other = (Arquivo) obj;
		if (listaIdStorage == null) {
			if (other.listaIdStorage != null)
				return false;
		} else if (!listaIdStorage.equals(other.listaIdStorage))
			return false;
		if (nomeArquivo == null) {
			if (other.nomeArquivo != null)
				return false;
		} else if (!nomeArquivo.equals(other.nomeArquivo))
			return false;
		if (tamanhoArquivo == null) {
			if (other.tamanhoArquivo != null)
				return false;
		} else if (!tamanhoArquivo.equals(other.tamanhoArquivo))
			return false;
		return true;
	}
}
