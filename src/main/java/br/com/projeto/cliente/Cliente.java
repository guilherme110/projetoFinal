package br.com.projeto.cliente;

import bftsmart.tom.ServiceProxy;

public class Cliente {
	private int    idCliente;
	private String nomeCliente;
	private String nomeDiretorioCliente;
	private ServiceProxy conexao;
	
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

	public String getNomeDiretorioCliente() {
		return nomeDiretorioCliente;
	}

	public void setNomeDiretorioCliente(String nomeDiretorioCliente) {
		this.nomeDiretorioCliente = nomeDiretorioCliente;
	}

	public ServiceProxy getConexao() {
		return conexao;
	}

	public void setConexao(ServiceProxy conexao) {
		this.conexao = conexao;
	}
	
}
