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

import org.apache.commons.collections4.CollectionUtils;

import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;
import br.com.projeto.utils.Estatistica;

/**Classe do objeto Servidor de metadados.
 * Contêm o id do servidor, a arvore de diretorio,
 * o objeto servidor serviço e o objeto da tabela de storage.
 *
 */
public class ServidorMetaDados extends DefaultSingleRecoverable {
	private int 	idServidor;
	private boolean mensurarTestes;
	private long	throughputMeasurementStartTime;
    private int 	iteracoes;
    private int 	intervalo;
    private double 	maxTp;
	ArvoreDiretorio arvoreDiretorio;
	ServidorServico servidorServico;
	Map<Integer, Storage> tabelaStorage;
	
	/**Construtor da classe, recebe o id do servidor, passado como
	 * argumento ao inicializar a classe.
	 * Inicializa os outros objetos do servidor.
	 * Inicializa a comunicação via BFT-Smart.
	 * 
	 * @param idServidor
	 * @param mensurarTestes
	 * @param numeroIntervalo 
	 */
	public ServidorMetaDados(int idServidor, boolean mensurarTestes, int numeroIntervalo) {		
		this.idServidor = idServidor;
		this.mensurarTestes = mensurarTestes;
		arvoreDiretorio = new ArvoreDiretorio();
		servidorServico = new ServidorServico();
		tabelaStorage =  new HashMap<Integer,Storage>();
		
		//dados estatisticos
		throughputMeasurementStartTime = System.currentTimeMillis();
		intervalo = numeroIntervalo;
		maxTp = 0;
		iteracoes = 1;
		
		new ServiceReplica(idServidor, this, this);
	}

	/**Método de inicialização do servidor.
	 * Recebe como argumento o id do servidor e uma flag indicando se será mensurado os testes de throughput.
	 * 
	 * @param args id do servidor.
	 */
	public static void main(String[] args){
        if(args.length < 3) {
            System.out.println("Necessário passar o <id do Servidor> <flag para mensuração de dados> <intervalo>");
            System.exit(-1);
        }
        new ServidorMetaDados(Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]), Integer.parseInt(args[2]));
	}
	
	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart
	 * appExecuteOrdered são métodos que são realizados de forma ordenada.
	 * De acordo com a opção solicitada pelo mapDiretorio, um serviço e executado.
	 * Caso a opção escolhida seja escrita é gravado o through put da operação.
	 * 
	 * @param dadosCliente stream de dados vindo do cliente.
	 * @param msgCtx dados do BFT-Smart.
	 * 
	 * @return resposta para o cliente.
	 */
	@Override
	public byte[] appExecuteOrdered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
            //Calcula o throughput caso tenha sido selecionada a mensuração de dados.
            if (this.isMensurarTestes()) {
            	calculaThroughPut();
			}
        	
			switch (comando) {
			case Constantes.CRIA_DIRETORIO:
				resposta = servidorServico.criaDiretorio(dados, arvoreDiretorio);
				break;
			case Constantes.REMOVE_DIRETORIO:
				resposta = servidorServico.removeDiretorio(dados, arvoreDiretorio);
				break;	
			case Constantes.SALVA_ARQUIVO:
				resposta = opcaoSalvaArquivo(dados);
				break;	
			case Constantes.REMOVE_ARQUIVO:
				resposta = opcaoRemoveArquivo(dados);
				break;
			case Constantes.STORAGE_CADASTRO_TABELASTORAGE:
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
	 * appExecuteUnordered são métodos que são realizados de forma desordenada.
	 * De acordo com a opção solicitada pelo mapDiretorio, um serviço e executado.
	 * 
	 * @param dadosCliente stream de dados vindo do cliente.
	 * @param msgCtx dados do BFT-Smart.
	 * 
	 * @return resposta para o cliente.
	 */
	@Override
	public byte[] executeUnordered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		
		//Calcula o throughput caso tenha sido selecionada a mensuração de dados.
        if (this.isMensurarTestes()) {
        	calculaThroughPut();
		}
		
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
			case Constantes.BUSCA_STORAGES_ARQUIVO:
				resposta = servidorServico.buscaStorages(dados, tabelaStorage);
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro na leitura dos dados do cliente: " + e.getMessage());
		}
		
		return resposta;
	}
	
	private void calculaThroughPut() {
		double tp = -1;
		Estatistica estatistica = new Estatistica();
		
		if(iteracoes % intervalo == 0) {
			System.out.println("--- Measurements after " + iteracoes + " ops (" + intervalo + " samples) ---");
			tp = (double)(intervalo*1000/(double)(System.currentTimeMillis()-throughputMeasurementStartTime));
			if ((tp > maxTp) && (Double.isFinite(tp))) {
				maxTp = tp;
			}
			
			System.out.println("Throughput = " + tp +" operations/sec (Maximum observed: " + maxTp + " ops/sec)");
			estatistica.salvaDadosThroughPut(tp, maxTp);

			throughputMeasurementStartTime = System.currentTimeMillis();
		}
		iteracoes ++;
	}

	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart.
	 * Cria um snapshot da situação atual do objeto arvore diretorio.
	 * 
	 * @param state
	 */
	@SuppressWarnings("unchecked")
	@Override
    public void installSnapshot(byte[] state) {
        try {             
            // serialize to byte array and return
            ByteArrayInputStream bis = new ByteArrayInputStream(state);
            ObjectInput in = new ObjectInputStream(bis);
            arvoreDiretorio = (ArvoreDiretorio) in.readObject();
            tabelaStorage = (Map<Integer, Storage>) in.readObject();
            in.close();
            bis.close();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	/**Método sobreescrito da classe DefaultSingleRecoverable da biblioteca BFT-Smart.
	 * Pega o snapshot da situação atual do objeto arvore diretorio.
	 * 
	 * @return new byte
	 */
	@Override
	public byte[] getSnapshot() {
		try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(arvoreDiretorio);
            out.writeObject(tabelaStorage);
            out.flush();
            bos.flush();
            out.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
            return new byte[0];
        }   
	}
	
	/**Método para salvar um novo arquivo no servidor de metadados.
	 * Primeiro le os dados de entrada do cliente e monta o objeto novoArquivo, diretorioCliente e numeroStorages.
	 * Depois busca a lista com os melhores storages a serem utilizados para salvar o arquivo.
     * Caso a lista não seja vazia, chama o serviço para atualiza os dados da arvore de diretorio e
     * chama o serviço para atualiza os dados da tabela de storage.
     * Por ultimo serializa a lista de storages para ser enviado ao cliente.
     * 
	 * @param dados do cliente
	 * @return byte lista de storages serializada.
	 */
	@SuppressWarnings("unchecked")
	public byte[] opcaoSalvaArquivo(ByteArrayInputStream dados) {
		List<Storage> listaStorages   = new ArrayList<Storage>();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
		List<String> diretorioCliente = new ArrayList<String>();
		int numeroStorages			  = 0;
		ObjectInputStream objIn;
		ObjectOutputStream objOut;
		Arquivo novoArquivo           = new Arquivo();
		
		//le os dados de entrada
		try {
	    	objIn = new ObjectInputStream(dados);
	    	novoArquivo = (Arquivo) objIn.readObject();
	    	diretorioCliente = (List<String>) objIn.readObject();
	    	numeroStorages = (Integer) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
			System.out.println("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    	ex.printStackTrace(); 
	    } catch (IOException ex) {
			System.out.println("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace(); 
		}
		
		servidorServico.buscaListaMelhorStorage(listaStorages, numeroStorages, novoArquivo, tabelaStorage);
		if ((CollectionUtils.isNotEmpty(listaStorages)) && (numeroStorages == listaStorages.size())) {
			System.out.println("Encontrado lista com os melhores storages, tamanho da lista: " + listaStorages.size());
			if (servidorServico.salvaArquivo(novoArquivo, diretorioCliente, arvoreDiretorio)) {
				servidorServico.addArquivoTabelaStorage(novoArquivo, listaStorages, tabelaStorage);	
				System.out.println("Nome do arquivo salvo: " + novoArquivo.getNomeArquivo());
				System.out.println("Tabela de Storage atualizada!");
			} else {
				System.out.println("Nome de arquivo existente nesse diretório!");
			}
		} else if (listaStorages.size() != numeroStorages){
			System.out.println("Número de storages disponível não atende ao cliente!");
		} else {
			System.out.println("Não há espaço nos storages ou o arquivo já está salvo em todos os Storages!");
			listaStorages = null;
		}

		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(listaStorages);
			objOut.close();
		} catch (IOException ex) {
			System.out.println("Erro na escrita da saída dos dados"); 
			Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
		return saida.toByteArray();
	}
	
	/**Método para remover um arquivo do servidor de metadados.
	 * Primeiro le os dados de entrada do cliente e monta o objeto arquivo e diretorioCliente.
	 * Depois verifica se o arquivo existe nesse diretório, caso exista apaga ele da arvoreDiretorio.
	 * Em seguida verifica os storages onde o arquivo está salvo, remove ele de cada storage e
	 * atualiza a tabela de storage.
     * Por ultimo serializa a lista de storages para ser enviada ao cliente.
     * 
	 * @param dados do cliente
	 * @return byte lista de storages serializada.
	 */
	@SuppressWarnings("unchecked")
	private byte[] opcaoRemoveArquivo(ByteArrayInputStream dados) {
		List<Storage> listaStorages	  = new ArrayList<Storage>();
		ByteArrayOutputStream saida   = new ByteArrayOutputStream();
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
			System.out.println("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
	    	ex.printStackTrace(); 
	    } catch (IOException ex) {
	    	System.out.println("Erro na leitura dos dados de entrada!");
	    	Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace(); 
		}
		
		if (servidorServico.apagaArquivo(arquivo, diretorioCliente, arvoreDiretorio)) {
			servidorServico.remArquivoTabelaStorage(arquivo, listaStorages, tabelaStorage);	
			System.out.println("Nome do arquivo apagado: " + arquivo.getNomeArquivo());
			System.out.println("Tabela de Storage atualizada!");
		} else {
			System.out.println("Nome de arquivo não existe nesse diretório!");
		}
	
		//monta os dados de saida
		try {
			objOut = new ObjectOutputStream(saida);
			objOut.writeObject(listaStorages);
			objOut.close();
		} catch (IOException ex) {
			System.out.println("Erro na escrita da saída dos dados"); 
			Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
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

	public boolean isMensurarTestes() {
		return mensurarTestes;
	}

	public void setMensurarTestes(boolean mensurarTestes) {
		this.mensurarTestes = mensurarTestes;
	}
}
