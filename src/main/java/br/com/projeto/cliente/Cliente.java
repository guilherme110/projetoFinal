package br.com.projeto.cliente;

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
	private int			 FNumeroStorages;
	private String		 localArmazenamento;
	
	public Cliente() {
		
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

	public int getFNumeroStorages() {
		return FNumeroStorages;
	}

	public void setFNumeroStorages(int fNumeroStorages) {
		FNumeroStorages = fNumeroStorages;
	}

	public String getLocalArmazenamento() {
		return localArmazenamento;
	}

	public void setLocalArmazenamento(String localArmazenamento) {
		this.localArmazenamento = localArmazenamento;
	}
	
}
