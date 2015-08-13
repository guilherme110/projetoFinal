package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.demo.bftmap.MapOfMaps;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import br.com.projeto.diretorio.ArvoreDiretorio;
import br.com.projeto.utils.Constantes;

public class Servidor extends DefaultSingleRecoverable {
	private int 	   idServidor;
	ArvoreDiretorio arvoreDiretorio      = null;
	
	public Servidor(int idServidor) {
		this.idServidor = idServidor;
		
		arvoreDiretorio = new ArvoreDiretorio();
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
		ServidorServico servidorServico = new ServidorServico();
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
            switch (comando) {
			case Constantes.CRIA_DIRETORIO:
				resposta = servidorServico.criaDiretorio(dados, arvoreDiretorio);
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
		ServidorServico servidorServico = new ServidorServico();
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
            switch (comando) {
			case Constantes.VERIFICA_DIRETORIO:
				resposta = servidorServico.verificaDiretorio(dados, arvoreDiretorio);
				break;
			case Constantes.LISTA_ARQUIVOS:
				resposta = servidorServico.listaArquivos(dados, arvoreDiretorio);
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

	public int getIdServidor() {
		return idServidor;
	}


	public void setIdServidor(int idServidor) {
		this.idServidor = idServidor;
	}
}
