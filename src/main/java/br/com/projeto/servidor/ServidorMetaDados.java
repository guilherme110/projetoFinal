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
import br.com.projeto.interfaces.InterfaceServidorMetaDados;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;
import br.com.projeto.utils.Estatistica;

/**Classe do objeto Servidor de metadados.
 * Contêm o id do servidor, a arvore de diretorio,
 * o objeto servidor serviço e o objeto da tabela de storage.
 *
 */
public class ServidorMetaDados extends DefaultSingleRecoverable implements InterfaceServidorMetaDados {
	private int 	idServidor;
	private boolean mensurarTestes;
	private long	throughputMeasurementStartTime;
    private int 	iteracoes;
    private int 	intervalo;
    private double 	maxTp;
	//ArvoreDiretorio arvoreDiretorio;
	ServidorServico servidorServico;
	//Map<Integer, Storage> tabelaStorage;
	
	/**Construtor da classe, recebe o id do servidor, passando como
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
		//arvoreDiretorio = criaArvoreDiretorio();
		servidorServico = criaServidorServico();
		//tabelaStorage =  criaTabelaStorage();
		
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
	@SuppressWarnings("unchecked")
	@Override
	public byte[] appExecuteOrdered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            List<String> diretorioCliente = new ArrayList<String>();
			ObjectInputStream objIn = new ObjectInputStream(dados);
            
            //Calcula o throughput caso tenha sido selecionada a mensuração de dados.
            if (this.isMensurarTestes()) {
            	calculaThroughPut();
			}
        	
			switch (comando) {
			case Constantes.CRIA_DIRETORIO:
			    try {
			    	diretorioCliente = (List<String>) objIn.readObject();
					String nomeNovoDiretorio = new DataInputStream(dados).readUTF();
			    	resposta = servidorServico.criaDiretorio(diretorioCliente, nomeNovoDiretorio);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
				break;
			case Constantes.REMOVE_DIRETORIO:
			    try {
			    	diretorioCliente = (List<String>) objIn.readObject();
			    	String nomeDiretorio = new DataInputStream(dados).readUTF();
					resposta = servidorServico.removeDiretorio(diretorioCliente, nomeDiretorio);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
				break;	
			case Constantes.SALVA_ARQUIVO:
				try {
			    	Arquivo novoArquivo = (Arquivo) objIn.readObject();
			    	diretorioCliente = (List<String>) objIn.readObject();
			    	Integer numeroStorages = (Integer) objIn.readObject();

					resposta = servidorServico.salvaArquivo(novoArquivo, diretorioCliente, numeroStorages);
			    } catch (ClassNotFoundException ex) {
				    Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;	
			case Constantes.REMOVE_ARQUIVO:
				try {
			       	Arquivo arquivo = (Arquivo) objIn.readObject();
			    	diretorioCliente = (List<String>) objIn.readObject();
					resposta = servidorServico.removeArquivo(arquivo, diretorioCliente);
			    } catch (ClassNotFoundException ex) {
				    Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;
			case Constantes.STORAGE_CADASTRO_TABELASTORAGE:
			    try {
			    	Storage novoStorage = (Storage) objIn.readObject();
					resposta = servidorServico.salvaStorage(novoStorage);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
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
	@SuppressWarnings("unchecked")
	@Override
	public byte[] executeUnordered(byte[] dadosCliente, MessageContext msgCtx) {
		byte[] resposta = null;
		List<String> diretorioCliente = new ArrayList<String>();
		
		//Calcula o throughput caso tenha sido selecionada a mensuração de dados.
        if (this.isMensurarTestes()) {
        	calculaThroughPut();
		}
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
			ObjectInputStream objIn = new ObjectInputStream(dados);
            
            switch (comando) {
			case Constantes.VERIFICA_DIRETORIO:
			    try {
			    	diretorioCliente = (List<String>) objIn.readObject();
			    	String nomeDiretorio = new DataInputStream(dados).readUTF();
					resposta = servidorServico.verificaDiretorio(diretorioCliente, nomeDiretorio);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
				break;
			case Constantes.BUSCA_ARQUIVO:
			    try {
			    	diretorioCliente = (List<String>) objIn.readObject();
			    	String nomeArquivo = new DataInputStream(dados).readUTF();
			    	resposta = servidorServico.buscaArquivo(diretorioCliente, nomeArquivo);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
				break;
			case Constantes.LISTA_DADOS:			
			    try {
			    	diretorioCliente = (List<String>) objIn.readObject();
					resposta = servidorServico.listaDados(diretorioCliente);
			    } catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
			    }
				break;
			case Constantes.BUSCA_STORAGES_ARQUIVO:
				try {
					Arquivo arquivo = (Arquivo) objIn.readObject();
					resposta = servidorServico.buscaListaStorages(arquivo);
				} catch (ClassNotFoundException ex) {
			       Logger.getLogger(ServidorMetaDados.class.getName()).log(Level.SEVERE, null, ex);
				}
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
            servidorServico.setArvoreDiretorio((ArvoreDiretorio) in.readObject());
            servidorServico.setTabelaStorage((Map<Integer, Storage>) in.readObject());
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
            out.writeObject(servidorServico.getArvoreDiretorio());
            out.writeObject(servidorServico.getTabelaStorage());
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
	
	@Override
	public ServiceReplica estabeleceComunicacaoBFT(int idServidor) {
		return new ServiceReplica(idServidor, this, this);
	}

	@Override
	public ServidorServico criaServidorServico() {
		return new ServidorServico();
	}
}
