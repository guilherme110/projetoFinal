package br.com.projeto.cliente;

import java.util.ArrayList;
import java.util.List;

/**Classe para objetos do tipo Cliente.
 * Está contido o id do cliente
 * diretório atual do cliente e o
 * número de storages que o usuário definiu para salvar os arquivos.
 * local de armazenamento dos arquivos recebidos através do sistema.
 * 
 * @author guilherme
 *
 */
public class Cliente {
	private int          idCliente;
	private List<String> diretorioClienteAtual;
	private int			 numeroStorages;
	private int			 fNumeroFalhas;
	private String		 localArmazenamento;
	
	public Cliente() {
		
	}
	
	
	
	public Cliente(int idCliente, int fNumeroFalhas,
			String localArmazenamento) {
		super();
		this.idCliente = idCliente;
		this.diretorioClienteAtual = new ArrayList<String>();
		this.diretorioClienteAtual.add("home");
		this.numeroStorages = 2 * fNumeroFalhas + 1;
		this.fNumeroFalhas = fNumeroFalhas;
		this.localArmazenamento = localArmazenamento;
	}



	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public List<String> getDiretorioClienteAtual() {
		return diretorioClienteAtual;
	}

	public void setDiretorioClienteAtual(List<String> diretorioClienteAtual) {
		this.diretorioClienteAtual = diretorioClienteAtual;
	}

	public int getNumeroStorages() {
		return numeroStorages;
	}

	public void setNumeroStorages(int numeroStorages) {
		this.numeroStorages = numeroStorages;
	}

	public int getfNumeroFalhas() {
		return fNumeroFalhas;
	}

	public void setfNumeroFalhas(int fNumeroFalhas) {
		this.fNumeroFalhas = fNumeroFalhas;
	}

	public String getLocalArmazenamento() {
		return localArmazenamento;
	}

	public void setLocalArmazenamento(String localArmazenamento) {
		this.localArmazenamento = localArmazenamento;
	}
	
}
