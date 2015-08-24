package br.com.projeto.cliente;

import java.util.List;

/**Classe para objetos do tipo Cliente
 * Está contido o id do cliente, nome do cliente,
 * e o diretório atual do cliente.
 * 
 * @author guilherme
 *
 */
public class Cliente {
	private int          idCliente;
	private String       nomeCliente;
	private List<String> diretorioClienteAtual;
	
	public Cliente() {
		
	}
	
	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public List<String> getDiretorioClienteAtual() {
		return diretorioClienteAtual;
	}

	public void setDiretorioClienteAtual(List<String> diretorioClienteAtual) {
		this.diretorioClienteAtual = diretorioClienteAtual;
	}
	
}
