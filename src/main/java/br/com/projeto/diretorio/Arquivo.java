package br.com.projeto.diretorio;

import java.io.Serializable;
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
}
