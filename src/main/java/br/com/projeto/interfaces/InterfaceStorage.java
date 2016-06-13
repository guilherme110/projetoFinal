package br.com.projeto.interfaces;

import java.util.List;

import bftsmart.tom.ServiceProxy;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.storage.Storage;
import br.com.projeto.storage.StorageServico;

public interface InterfaceStorage {
	ServiceProxy estabeleceComunicacaoBFT(int idStorage);
	Storage criaStorage(int idStorage, int portaConexao, long espacoLivre, String localArmazenamento, List<Arquivo> listaArquivos);
	StorageServico criaStorageServico(Storage storage, ServiceProxy KVProxy);
}
