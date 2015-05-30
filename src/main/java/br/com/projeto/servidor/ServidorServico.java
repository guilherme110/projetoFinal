package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.projeto.diretorio.ArvoreDeDiretorios;

public class ServidorServico {
	
	public ServidorServico() {

	}
	
	@SuppressWarnings("unchecked")
	public byte[] criarDiretorio(ByteArrayInputStream dados, 
			ArvoreDeDiretorios arvoreDeDiretorios) throws IOException {
		String nomeDiretorio = new DataInputStream(dados).readUTF();
        ObjectInputStream objIn = new ObjectInputStream(dados);
	    Map<String, byte[]> diretorio = null;
	    try {
	    	diretorio = (Map<String, byte[]>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    Map<String, byte[]> diretorioCriado = arvoreDeDiretorios.addDiretorio(nomeDiretorio, diretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(diretorioCriado);
	    objOut.close();
	    dados.close();
	    
	    return saida.toByteArray();
	}

	public byte[] verificarDiretorio(ByteArrayInputStream dados,
			ArvoreDeDiretorios arvoreDeDiretorios) throws IOException {
		String nomeDiretorio = new DataInputStream(dados).readUTF();
	    Map<String, byte[]> table = arvoreDeDiretorios.getDiretorio(nomeDiretorio);
	    boolean tableExists = (table != null);
	    System.out.println("Table exists: " + tableExists);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    new DataOutputStream(saida).writeBoolean(tableExists);

		return saida.toByteArray();
	}

	public byte[] listaArquivos(ArvoreDeDiretorios arvoreDeDiretorios) throws IOException {
		List<String> listaArquivos = new ArrayList<String>();
		arvoreDeDiretorios.listaArquivos(listaArquivos);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(listaArquivos);
	    objOut.close();
	    
	    return saida.toByteArray();
	}

	public String atualizaCaminhoDiretorio(ByteArrayInputStream dados) {
		// TODO Auto-generated method stub
		return null;
	}
}
