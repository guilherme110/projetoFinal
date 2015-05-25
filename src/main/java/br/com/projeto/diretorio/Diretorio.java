package br.com.projeto.diretorio;

import java.util.Map;


public class Diretorio {
	private String 				nomeDiretorio;
	private int    				totalArquivos;
	private Map<String, byte[]> estruturaDiretorio;
	
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
	public Map<String, byte[]> getEstruturaDiretorio() {
		return estruturaDiretorio;
	}
	public void setEstruturaDiretorio(Map<String, byte[]> estruturaDiretorio) {
		this.estruturaDiretorio = estruturaDiretorio;
	}
	
}
