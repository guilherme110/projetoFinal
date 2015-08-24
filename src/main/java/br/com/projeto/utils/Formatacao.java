package br.com.projeto.utils;

import java.text.DecimalFormat;

/**Classe com métodos úteis para o projeto
 * 
 * @author guilherme
 *
 */
public class Formatacao {
	public Formatacao() {
		
	}
	
	//Metodo convert um long bytes para B, kb, MB, GB e TB
	public String convertNomeBytes(long size) {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}	
