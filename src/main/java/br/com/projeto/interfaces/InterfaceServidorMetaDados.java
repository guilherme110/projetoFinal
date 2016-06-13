package br.com.projeto.interfaces;

import java.util.Map;

import bftsmart.tom.ServiceReplica;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.servidor.ServidorServico;
import br.com.projeto.storage.Storage;

public interface InterfaceServidorMetaDados {
	ServiceReplica estabeleceComunicacaoBFT(int idServidor);
	ArvoreDiretorio criaArvoreDiretorio();
	Map<Integer, Storage> criaTabelaStorage();
	ServidorServico criaServidorServico();
}
