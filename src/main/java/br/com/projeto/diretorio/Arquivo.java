package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Arquivo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nomeArquivo;
	private Long   tamanhoArquivo;
	private int    idStorage;
	private String tipoArquivo;
	private Date   dataCriacaoArquivo;
	private byte[] dadosArquivo;
	
	public Arquivo() {
		
	}
	
	public Arquivo(String nomeArquivo, Long tamanhoArquivo, int idStorage,
			String tipoArquivo) {
		super();
		this.nomeArquivo = nomeArquivo;
		this.tamanhoArquivo = tamanhoArquivo;
		this.idStorage = idStorage;
		this.tipoArquivo = tipoArquivo;
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

	public int getIdStorage() {
		return idStorage;
	}

	public void setIdStorage(int idStorage) {
		this.idStorage = idStorage;
	}

	public String getTipoArquivo() {
		return tipoArquivo;
	}

	public void setTipoArquivo(String tipoArquivo) {
		this.tipoArquivo = tipoArquivo;
	}

	public Date getDataCriacaoArquivo() {
		return dataCriacaoArquivo;
	}

	public void setDataCriacaoArquivo(Date dataCriacaoArquivo) {
		this.dataCriacaoArquivo = dataCriacaoArquivo;
	}

	public byte[] getDadosArquivo() {
		return dadosArquivo;
	}

	public void setDadosArquivo(byte[] dadosArquivo) {
		this.dadosArquivo = dadosArquivo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(dadosArquivo);
		result = prime
				* result
				+ ((dataCriacaoArquivo == null) ? 0 : dataCriacaoArquivo
						.hashCode());
		result = prime * result + idStorage;
		result = prime * result
				+ ((nomeArquivo == null) ? 0 : nomeArquivo.hashCode());
		result = prime * result
				+ ((tamanhoArquivo == null) ? 0 : tamanhoArquivo.hashCode());
		result = prime * result
				+ ((tipoArquivo == null) ? 0 : tipoArquivo.hashCode());
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
		if (!Arrays.equals(dadosArquivo, other.dadosArquivo))
			return false;
		if (dataCriacaoArquivo == null) {
			if (other.dataCriacaoArquivo != null)
				return false;
		} else if (!dataCriacaoArquivo.equals(other.dataCriacaoArquivo))
			return false;
		if (idStorage != other.idStorage)
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
		if (tipoArquivo == null) {
			if (other.tipoArquivo != null)
				return false;
		} else if (!tipoArquivo.equals(other.tipoArquivo))
			return false;
		return true;
	}
}
