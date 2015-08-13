package br.com.projeto.diretorio;

import java.io.Serializable;

public class Diretorio implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String 				nomeDiretorio;
	private int    				totalArquivos;
	
	public String getNomeDiretorio() {
		return nomeDiretorio;
	}
	public void setNomeDiretorio(String nomeDiretorio) {
		this.nomeDiretorio = nomeDiretorio;
	}
	public int getTotalArquivos() {
		return totalArquivos;
	}
	public void setTotalArquivos(int totalArquivos) {
		this.totalArquivos = totalArquivos;
	}
}
