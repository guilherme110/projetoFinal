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

public class Servidor extends DefaultSingleRecoverable {
	private int 	idServidor;
	ArvoreDiretorio arvoreDiretorio;
	ServidorServico servidorServico;
	Map<Integer, Storage> tabelaStorage;
	
	//FIXME Verificar como um novo storage irá se cadastrar na lista de storage
	public Servidor(int idServidor) {		
		this.idServidor = idServidor;
		arvoreDiretorio = new ArvoreDiretorio();
		servidorServico = new ServidorServico();
		tabelaStorage =  new HashMap<Integer,Storage>();
		new ServiceReplica(idServidor, this, this);
	}


	public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Necessário passar o <id do Servidor>");
            System.exit(-1);
        }
        new Servidor(Integer.parseInt(args[0]));
	}
	
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
				resposta = opcaoApagaArquivo(dados);
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

	//pegar a atual situação dos diretorios
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
		
	    /*caso encontre um storage:
	    atualiza o novo arquivo com o id do storage 
	    atualiza os dados da tabela de storage
	    atualiza os dados da arvore de diretorio
	    atualiza os dados do objeto de saida dadosSaida.*/
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
	

	@SuppressWarnings("unchecked")
	private byte[] opcaoApagaArquivo(ByteArrayInputStream dados) {
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
		
	    /*apaga o arquivo na arvoreDiretorio e verifica se o arquivo existe  nesse diretorio
	    verifica o storage onde o arquivo está salvo
	    atualiza a tabela de storage
	    atualiza os dados do objeto de saida dadosSaida.*/
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
