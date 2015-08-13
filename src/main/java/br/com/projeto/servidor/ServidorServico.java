package br.com.projeto.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.projeto.diretorio.ArvoreDiretorio;

public class ServidorServico {
	
	public ServidorServico() {
	
	}
	
	public byte[] criaDiretorio(ByteArrayInputStream dados, 
			ArvoreDiretorio ArvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		String nomeNovoDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    }
		
	    String msgRetorno = ArvoreDiretorio.addDiretorio(diretorioCliente, nomeNovoDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(msgRetorno);
	    objOut.close();
	    dados.close();
	    
	    System.out.println("Diretorio: " + nomeNovoDiretorio + "Status: " + msgRetorno);
	    return saida.toByteArray();
	}

//	public byte[] verificarDiretorio(ByteArrayInputStream dados,
//			ArvoreDeDiretorios arvoreDeDiretorios) throws IOException {
//		String nomeDiretorio = new DataInputStream(dados).readUTF();
//	    Map<String, byte[]> table = arvoreDeDiretorios.getDiretorio(nomeDiretorio);
//	    boolean tableExists = (table != null);
//	    System.out.println("Table exists: " + tableExists);
//	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
//	    new DataOutputStream(saida).writeBoolean(tableExists);
//
//		return saida.toByteArray();
//	}
//
	public byte[] listaArquivos(ByteArrayInputStream dados, ArvoreDiretorio arvoreDiretorio) throws IOException {
		List<String> listaArquivos = new ArrayList<String>();
		List<String> diretorioCliente = new ArrayList<String>();
	
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    }
		listaArquivos = arvoreDiretorio.listaArquivos(diretorioCliente);
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

	public byte[] verificaDiretorio(ByteArrayInputStream dados,
			ArvoreDiretorio arvoreDiretorio) throws IOException {
		List<String> diretorioCliente = new ArrayList<String>();
		List<String> listaAux = new ArrayList<String>();
 		boolean retorno = true;
		
		String nomeDiretorio = new DataInputStream(dados).readUTF();
		ObjectInputStream objIn = new ObjectInputStream(dados);
	    try {
	    	diretorioCliente = (List<String>) objIn.readObject();
	    } catch (ClassNotFoundException ex) {
	       Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
	    }
		listaAux = arvoreDiretorio.listaArquivos(diretorioCliente);
		retorno = listaAux.contains(nomeDiretorio);
	    ByteArrayOutputStream saida = new ByteArrayOutputStream();
	    ObjectOutputStream objOut = new ObjectOutputStream(saida);
	    objOut.writeObject(retorno);
	    objOut.close();
	    
	    System.out.println("Diretorio: " + nomeDiretorio + "   Existe: " + retorno);
	    return saida.toByteArray();
	}
}
