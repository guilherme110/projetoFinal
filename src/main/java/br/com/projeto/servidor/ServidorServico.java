package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.bftmap.BFTMapServer;
import bftsmart.demo.bftmap.MapOfMaps;

public class ServidorServico {
	
	public ServidorServico() {

	}
	
	@SuppressWarnings("unchecked")
	public byte[] criarDiretorio(ByteArrayInputStream dados, 
			MapOfMaps tableMap) throws IOException {
		String nomeDiretorio = new DataInputStream(dados).readUTF();
        ObjectInputStream objIn = new ObjectInputStream(dados);
	    Map<String, byte[]> diretorio = null;
	    try {
	    	diretorio = (Map<String, byte[]>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(BFTMapServer.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    Map<String, byte[]> diretorioCriado = tableMap.addTable(nomeDiretorio, diretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(diretorioCriado);
	    objOut.close();
	    dados.close();
	    
	    return saida.toByteArray();
	}

	public byte[] verificarDiretorio(ByteArrayInputStream dados,
			MapOfMaps tableMap) throws IOException {
		String nomeDiretorio = new DataInputStream(dados).readUTF();
	    Map<String, byte[]> table = tableMap.getTable(nomeDiretorio);
	    boolean tableExists = (table != null);
	    System.out.println("Table exists: " + tableExists);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    new DataOutputStream(saida).writeBoolean(tableExists);

		return saida.toByteArray();
	}
}
