package br.com.projeto.interfaces;

import bftsmart.tom.ServiceReplica;
import br.com.projeto.servidor.ServidorServico;

public interface InterfaceServidorMetaDados {
	ServiceReplica estabeleceComunicacaoBFT(int idServidor);
	ServidorServico criaServidorServico();
}
