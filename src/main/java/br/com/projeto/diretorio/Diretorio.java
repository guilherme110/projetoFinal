package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class Diretorio implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String 				nomeDiretorio;
	private int    				totalArquivos;
	private List<Arquivo>		listaArquivos;
	
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
	public List<Arquivo> getListaArquivos() {
		return listaArquivos;
	}
	public void setListaArquivos(List<Arquivo> listaArquivos) {
		this.listaArquivos = listaArquivos;
	}
	public List<String> getNomeArquivos() {
		List<Arquivo> listaArquivos = this.getListaArquivos();
		List<String> listaNomeArquivos = new ArrayList<String>();
		
		if (CollectionUtils.isNotEmpty(listaArquivos)) {
			for (Arquivo arquivo : listaArquivos)
				listaNomeArquivos.add(arquivo.getNomeArquivo());
			return listaNomeArquivos;
		}
		return new ArrayList<String>();
	}
	public void addArquivo(Arquivo novoArquivo) {
		List<Arquivo> listaArquivos = this.getListaArquivos();
	
		if (CollectionUtils.isEmpty(listaArquivos))
			listaArquivos = new ArrayList<Arquivo>();
		
		listaArquivos.add(novoArquivo);
		this.setListaArquivos(listaArquivos);
	}
}
