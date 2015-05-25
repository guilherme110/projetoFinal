package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.demo.bftmap.MapOfMaps;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import br.com.projeto.utils.Constantes;

public class Servidor extends DefaultSingleRecoverable {
	private int idServidor;
	MapOfMaps tableMap = null;
	
	public Servidor(int idServidor) {
		this.idServidor = idServidor;
		
		tableMap = new MapOfMaps();
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
		ServidorServico servidorServido = new ServidorServico();
		
		try {
			ByteArrayInputStream dados = new ByteArrayInputStream(dadosCliente);
            int comando = new DataInputStream(dados).readInt();
            
            switch (comando) {
			case Constantes.CRIAR_DIRETORIO:
				resposta = servidorServido.criarDiretorio(dados, tableMap);
			default:
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return resposta;
	}

	@Override
	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void installSnapshot(byte[] state) {
		// TODO Auto-generated method stub
		
	}

	//pegar a atual situação dos diretorios
	@Override
	public byte[] getSnapshot() {
		 try {
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            ObjectOutput out = new ObjectOutputStream(bos);
	            out.writeObject(tableMap);
	            out.flush();
	            bos.flush();
	            out.close();
	            bos.close();
	            return bos.toByteArray();
	        } catch (IOException ex) {
	            Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
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
