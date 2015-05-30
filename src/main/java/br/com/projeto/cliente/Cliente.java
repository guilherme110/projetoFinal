package br.com.projeto.cliente;

import java.util.List;

import bftsmart.tom.ServiceProxy;

public class Cliente {
	private int          idCliente;
	private String       nomeCliente;
	private List<String> diretorioClienteAtual;
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

	public List<String> getDiretorioClienteAtual() {
		return diretorioClienteAtual;
	}

	public void setDiretorioClienteAtual(List<String> diretorioClienteAtual) {
		this.diretorioClienteAtual = diretorioClienteAtual;
	}

	public ServiceProxy getConexao() {
		return conexao;
	}

	public void setConexao(ServiceProxy conexao) {
		this.conexao = conexao;
	}
	
}
