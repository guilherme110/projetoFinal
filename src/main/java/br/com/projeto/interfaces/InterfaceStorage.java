package br.com.projeto.interfaces;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.storage.Storage;
import br.com.projeto.storage.StorageServico;

public interface InterfaceStorage {
	ServiceProxy estabeleceComunicacaoBFT(int idStorage);
	Storage criaStorage(int idStorage, int portaConexao, long espacoLivre, String localArmazenamento);
	StorageServico criaStorageServico(ServiceProxy KVProxy, Storage storage);
}
