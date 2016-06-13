package br.com.projeto.interfaces;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.cliente.Cliente;
import br.com.projeto.cliente.ClienteServico;

public interface InterfaceCliente {
	ServiceProxy estabeleceComunicacaoBFT(int idCliente);
	Cliente criaCliente(int idCliente, int fNumeroFalhas, String localArmazenamento);
	ClienteServico criaClienteServico(ServiceProxy kVProxy, Cliente cliente);
}
