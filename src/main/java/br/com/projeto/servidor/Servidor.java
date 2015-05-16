package br.com.projeto.servidor;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

public class Servidor extends DefaultSingleRecoverable {
	private int idServidor;
	
	public Servidor(int idServidor) {
		this.idServidor = idServidor;
		
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
	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void installSnapshot(byte[] state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getSnapshot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getIdServidor() {
		return idServidor;
	}


	public void setIdServidor(int idServidor) {
		this.idServidor = idServidor;
	}

}
