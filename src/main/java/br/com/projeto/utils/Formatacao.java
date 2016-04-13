package br.com.projeto.utils;

/**Classe com métodos úteis para o projeto.
 * 
 * @author guilherme
 *
 */
public class Formatacao {
	public Formatacao() {
		
	}

	//Metodo convert um long bytes para B, kb, MB, GB e TB
	public String convertNomeBytes(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}	
