package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;

/**Classe do objeto Servidor de metadados
 * Contêm o id do servidor, a arvore de diretorio,
 * o objeto servidor serviço e o objeto da tabela de storage.
 *
 */
public class Servidor extends DefaultSingleRecoverable {
	private int 	idServidor;
	ArvoreDiretorio arvoreDiretorio;
	ServidorServico servidorServico;
	Map<Integer, Storage> tabelaStorage;
	
	/**Construtor da classe, recebe o id do servidor, passado como
	 * argumento ao inicializar a classe.
	 * Inicializa os outros objetos do servidor.
	 * Inicializa a comunicação via BFT-Smart
	 * 
	 * @param idServidor
	 */
	public Servidor(int idServidor) {		
		this.idServidor = idServidor;
		arvoreDiretorio = new ArvoreDiretorio();
		servidorServico = new ServidorServico();
		tabelaStorage =  new HashMap<Integer,Storage>();
		new ServiceReplica(idServidor, this, this);
	}

	/**Método de inicialização do servidor
	 * Recebe como argumento o id do servidor
	 * 
	 * @param args id do servidor
	 */
	public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Necessário passar o <id do Servidor>");
            System.exit(-1);
        }
        new Servidor(Integer.parseInt(args[0]));
	}
	
	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart
	 * appExecuteOrdered são métodos que são realizados de forma ordenada
	 * De acordo com a opção solicitada pelo mapDiretorio, um serviço e executado
	 * 
	 * @param dadosCliente stream de dados vindo do cliente
	 * @param msgCtx dados do BFT-Smart
	 * 
	 * @return resposta para o cliente
	 */
	@Override
	public byte[] appExecuteOrdered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
			switch (comando) {
			case Constantes.CRIA_DIRETORIO:
				resposta = servidorServico.criaDiretorio(dados, arvoreDiretorio);
				break;
			case Constantes.SALVA_ARQUIVO:
				resposta = opcaoSalvaArquivo(dados);
				break;	
			case Constantes.APAGA_ARQUIVO:
				resposta = opcaoRemoveArquivo(dados);
				break;
			case Constantes.STORAGE_ENVIA_DADOS:
				resposta = servidorServico.salvaStorage(dados, tabelaStorage);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			System.out.println("Erro na leitura dos dados do cliente: " + e.getMessage());
		}
		
		return resposta;
	}

	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart
	 * appExecuteUnordered são métodos que são realizados de forma desordenada
	 * De acordo com a opção solicitada pelo mapDiretorio, um serviço e executado
	 * 
	 * @param dadosCliente stream de dados vindo do cliente
	 * @param msgCtx dados do BFT-Smart
	 * 
	 * @return resposta para o cliente
	 */
	@Override
	public byte[] executeUnordered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
            switch (comando) {
			case Constantes.VERIFICA_DIRETORIO:
				resposta = servidorServico.verificaDiretorio(dados, arvoreDiretorio);
				break;
			case Constantes.BUSCA_ARQUIVO:
				resposta = servidorServico.buscaArquivo(dados, arvoreDiretorio);
				break;
			case Constantes.LISTA_DADOS:
				resposta = servidorServico.listaDados(dados, arvoreDiretorio);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na leitura dos dados do cliente: " + e.getMessage());
		}
		
		return resposta;
	}

	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart
	 * Cria um snapshot da situação atual do objeto arvore diretorio
	 * 
	 * @param state
	 */
	@Override
    public void installSnapshot(byte[] state) {
        try {             
            // serialize to byte array and return
            ByteArrayInputStream bis = new ByteArrayInputStream(state);
            ObjectInput in = new ObjectInputStream(bis);
            arvoreDiretorio = (ArvoreDiretorio) in.readObject();
            in.close();
            bis.close();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart
	 * Pega o snapshot da situação atual do objeto arvore diretorio
	 * 
	 * @return new byte
	 */
	@Override
	public byte[] getSnapshot() {
		try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(arvoreDiretorio);
            out.flush();
            bos.flush();
            out.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            return new byte[0];
        }   
	}
	
	/**Método para salvar um novo arquivo no servidor de metadados
	 * Primeiro le os dados de entrada do cliente e monta o objeto novoArquivo e diretorioCliente
	 * Depois verifica o melhor storage para salvar o arquivo
     * Caso encontre um storage, atualiza o novo arquivo com o id do storage, 
     * Chama o serviço para atualiza os dados da arvore de diretorio e
     * chama o serviço para atualiza os dados da tabela de storage.
     * Por ultimo atualiza os dados do objeto de saida dadosSaida com os dados do storage encontrado
     * 
	 * @param dados do cliente
	 * @return dados do storage utilizado
	 */
	@SuppressWarnings("unchecked")
	public byte[] opcaoSalvaArquivo(ByteArrayInputStream dados) {
		Storage melhorStorage 	      = new Storage();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
		List<String> dadosSaida 	  = new ArrayList<String>();
		List<String> diretorioCliente = new ArrayList<String>();
		ObjectInputStream objIn;
		ObjectOutputStream objOut;
		Arquivo novoArquivo           = new Arquivo();
		
		//le os dados de entrada
		try {
	    	objIn = new ObjectInputStream(dados);
	    	novoArquivo = (Arquivo) objIn.readObject();
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	    	dadosSaida.add("false");
			dadosSaida.add("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    	ex.printStackTrace(); 
	    } catch (IOException ex) {
	    	dadosSaida.add("false");
			dadosSaida.add("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace(); 
		}
		
		melhorStorage = servidorServico.buscaMelhorStorage(novoArquivo, tabelaStorage);
		if (melhorStorage != null) {
			System.out.println("Encontrado melhor storage: " + melhorStorage.getNomeStorage());
			novoArquivo.setIdStorage(melhorStorage.getIdServidor());
			if (servidorServico.salvaArquivo(novoArquivo, diretorioCliente, arvoreDiretorio)) {
				servidorServico.addArquivoTabelaStorage(novoArquivo, melhorStorage, tabelaStorage);	
				dadosSaida.add("true");
				dadosSaida.add("Melhor Storage: " + melhorStorage.getNomeStorage());
				dadosSaida.add(melhorStorage.getEnderecoHost());
				dadosSaida.add(Integer.toString(melhorStorage.getPortaConexao()));
				System.out.println("Nome do arquivo salvo: " + novoArquivo.getNomeArquivo());
				System.out.println("Tabela de Storage atualizada!");
			} else {
				dadosSaida.add("false");
				dadosSaida.add("Nome de arquivo existente nesse diretório!");
				System.out.println("Nome de arquivo existente nesse diretório!");
			}
		} else {
			dadosSaida.add("false");
			dadosSaida.add("Não há espaço nos storages ou o arquivo já está salvo em todos os Storages!"); 
			System.out.println("Não há espaço nos storages ou o arquivo já está salvo em todos os Storages!");
		}

		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(dadosSaida);
			objOut.close();
		} catch (IOException ex) {
			dadosSaida.add("false");
			dadosSaida.add("Erro na escrita da saída dos dados"); 
			Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
		return saida.toByteArray();
	}
	
	/**Método para remover um arquivo do servidor de metadados
	 * Primeiro le os dados de entrada do cliente e monta o objeto arquivo e diretorioCliente
	 * Depois verifica se o arquivo existe nesse diretório, caso exista apaga ele da arvoreDiretorio
	 * Em seguida verifica o storage onde o arquivo está salvo e remove ele do storage e
	 * atualiza a tabela de storage.
     * Por ultimo atualiza os dados do objeto de saida dadosSaida com os dados do storage atualizado
     * 
	 * @param dados do cliente
	 * @return dados do storage atualizado
	 */
	@SuppressWarnings("unchecked")
	private byte[] opcaoRemoveArquivo(ByteArrayInputStream dados) {
		Storage storage			      = new Storage();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
		List<String> dadosSaida 	  = new ArrayList<String>();
		List<String> diretorioCliente = new ArrayList<String>();
		ObjectInputStream objIn;
		ObjectOutputStream objOut;
		Arquivo arquivo		          = new Arquivo();
		
		//le os dados de entrada
		try {
	    	objIn = new ObjectInputStream(dados);
	    	arquivo = (Arquivo) objIn.readObject();
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	    	dadosSaida.add("false");
			dadosSaida.add("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    	ex.printStackTrace(); 
	    } catch (IOException ex) {
	    	dadosSaida.add("false");
			dadosSaida.add("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace(); 
		}
		
		if (servidorServico.apagaArquivo(arquivo, diretorioCliente, arvoreDiretorio)) {
			storage = tabelaStorage.get(arquivo.getIdStorage());
			servidorServico.remArquivoTabelaStorage(arquivo, storage, tabelaStorage);	
			dadosSaida.add("true");
			dadosSaida.add("Storage: " + storage.getNomeStorage());
			dadosSaida.add(storage.getEnderecoHost());
			dadosSaida.add(Integer.toString(storage.getPortaConexao()));
			System.out.println("Nome do arquivo apagado: " + arquivo.getNomeArquivo());
			System.out.println("Tabela de Storage atualizada!");
		} else {
			dadosSaida.add("false");
			dadosSaida.add("Nome de arquivo não existe nesse diretório!");
			System.out.println("Nome de arquivo não existe nesse diretório!");
		}
	
		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(dadosSaida);
			objOut.close();
		} catch (IOException ex) {
			dadosSaida.add("false");
			dadosSaida.add("Erro na escrita da saída dos dados"); 
			Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
		return saida.toByteArray();
	}

	public int getIdServidor() {
		return idServidor;
	}


	public void setIdServidor(int idServidor) {
		this.idServidor = idServidor;
	}
}
