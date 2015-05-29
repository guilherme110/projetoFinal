package br.com.projeto.diretorio;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ArvoreDeDiretorios implements Serializable {
	/**
	 * Default serialVersion
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Map<String, byte[]>> arvoreDeDiretorios = null;
	
	public ArvoreDeDiretorios() {
		arvoreDeDiretorios = new TreeMap<String, Map<String,byte[]>>();
	}

	public Map<String, byte[]> addDiretorio(String nomeDiretorio,
			Map<String, byte[]> estruturaDiretorio) {
		try {
			return arvoreDeDiretorios.put(nomeDiretorio, estruturaDiretorio);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, byte[]> getDiretorio(String nomeDiretorio) {
		return arvoreDeDiretorios.get(nomeDiretorio);
	}

	public void listaArquivos(List<String> listaArquivos) {
		
		for (Map.Entry<String, Map<String, byte[]>> dado : arvoreDeDiretorios.entrySet()) {
			listaArquivos.add(dado.getKey());
		}
	}
}
