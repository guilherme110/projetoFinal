package br.com.projeto.cliente;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.diretorio.MapDiretorio;
import br.com.projeto.utils.Constantes;

public class ClienteServico {
	public MapDiretorio mapDiretorio;
	
	//FIXME Verificar como definir o nome do diretorio do cliente
	public String getDiretorioCliente(Cliente cliente) {
		return cliente.getNomeCliente();
	}
	
	//Cria o objeto de conexão com os servidores.
	public ClienteServico(Cliente cliente) {
		mapDiretorio = new MapDiretorio(cliente.getConexao());
	}
	
	public void moveDiretorio(String nomeDiretorio, Cliente cliente) {
		List<String> diretorioCliente = cliente.getDiretorioClienteAtual();
		int tamanhoDiretorioAtual = 0;
		
		if (("..".equalsIgnoreCase(nomeDiretorio))) {
			tamanhoDiretorioAtual = cliente.getDiretorioClienteAtual().size();
			if (tamanhoDiretorioAtual > 1)
				cliente.getDiretorioClienteAtual().remove(tamanhoDiretorioAtual - 1);
		} else {
			if (mapDiretorio.verificaDiretorio(nomeDiretorio, diretorioCliente)) {
				diretorioCliente.add(nomeDiretorio);
				cliente.setDiretorioClienteAtual(diretorioCliente);
			} else {
				System.out.println("Diretorio não encontrado.");
			}
		}
	}

	public void criaDiretorio(String nomeNovoDiretorio, Cliente cliente) {
		String msgSaida = "";
		
		try {
			if (StringUtils.isEmpty(nomeNovoDiretorio)) {
				msgSaida = "Necessário informar um nome para o diretório";
			} else if (nomeNovoDiretorio.charAt(0) == '.' || nomeNovoDiretorio.charAt(0) == ' ') {
				msgSaida = "O nome da pasta não pode começar com '.' ou espaço!";
			} else {
				msgSaida = mapDiretorio.put(nomeNovoDiretorio, cliente.getDiretorioClienteAtual());
			}
		} catch (Exception e) {
			msgSaida = "Erro na criação do diretorio!";
		}
		System.out.println(msgSaida);
	}

	
	public void listaDados(Cliente cliente) {
		ArrayList<List<String>> listaDados = new ArrayList<List<String>>();
		List<String> listaArquivos = new ArrayList<String>();
		List<String> listaDiretorios = new ArrayList<String>();
		
		listaDados = mapDiretorio.getListaDados(cliente.getDiretorioClienteAtual());
		
		//a posição é importante uma vez que os arquivos são salvos na primeira posição.
		//e os diretorios são gravados na segunda posição da listaDados.
		listaArquivos = listaDados.get(0);
		listaDiretorios = listaDados.get(1);
		
		if (CollectionUtils.isNotEmpty(listaDiretorios)) {
			System.out.println(" ");
			System.out.print("Diretorios: ");
			for (String diretorio : listaDiretorios)
				System.out.print("\"" + diretorio + "\"    ");	
		}
		
		if (CollectionUtils.isNotEmpty(listaArquivos)) {
			System.out.print("\nArquivos: ");
			for (String arquivo : listaArquivos)
				System.out.print("\"" + arquivo + "\"    ");
		}
		System.out.println(" ");
	}

	/*StatusStorage index position  0 - Status do Salvamento
									1 - Mensagem de retorno
									2 - endereço de host do storage
									3 - porta do host do storage
	Primeiro salva o arquivo na arvore de diretorio e obtem o storage para ser salvo o arquivo
	Segundo salva o arquivo no storage encontrado.
	Caso ocorra algum problema ao tentar enviar o arquivo, e realizado rollback no mapDiretorio.
	*/
	public void salvaArquivo(File arquivo, Cliente cliente) {
		List<String> statusStorage = new ArrayList<String>();
		Arquivo novoArquivo = new Arquivo();
		String tipoArquivo = arquivo.getName().substring((arquivo.getName().lastIndexOf(".") + 1));
		
		novoArquivo.setNomeArquivo(arquivo.getName());
		novoArquivo.setTamanhoArquivo(arquivo.length());
		novoArquivo.setTipoArquivo(tipoArquivo);
		try {
			statusStorage = mapDiretorio.salvaArquivo(novoArquivo, cliente.getDiretorioClienteAtual());
			if ("true".equalsIgnoreCase(statusStorage.get(0))) {
				System.out.println("\n" + statusStorage.get(1));
				if (enviaArquivoStorage(statusStorage, arquivo, novoArquivo)) {
					System.out.println("Arquivo enviado com sucesso!");
				} else {
					System.out.println("Erro ao enviar o arquivo " + novoArquivo.getNomeArquivo());
					statusStorage = mapDiretorio.apagaArquivo(novoArquivo, cliente.getDiretorioClienteAtual());
					if ("true".equalsIgnoreCase(statusStorage.get(0))) {
						System.out.println("Efetuado rollback dos dados!");
					}
				}
			} else {
				System.out.println(statusStorage.get(1));
			}
		} catch (Exception e) {
			System.out.println("Erro ao salvar o arquivo!");
		}
	}
	
	/*StatusStorage index position: 0 - Status do Salvamento
									1 - Mensagem de retorno
									2 - endereço de host do storage
									3 - porta do host do storage
	*/
	public void apagaArquivo(String nomeArquivo, Cliente cliente) {
		List<String> statusStorage = new ArrayList<String>();
		Arquivo arquivo = new Arquivo();
		
		arquivo = mapDiretorio.buscaArquivo(nomeArquivo, cliente.getDiretorioClienteAtual());
		if (arquivo != null) {
			try {
				statusStorage = mapDiretorio.apagaArquivo(arquivo, cliente.getDiretorioClienteAtual());
				if ("true".equalsIgnoreCase(statusStorage.get(0))) {
					System.out.println("\n" + statusStorage.get(1));
					if (apagaArquivoStorage(statusStorage, arquivo)) {
						System.out.println("Arquivo apagado com sucesso!");
					} else {
						System.out.println("Erro ao apagar o arquivo " + arquivo.getNomeArquivo());
					}
				} else {
					System.out.println(statusStorage.get(1));
				}
			} catch (Exception e) {
				System.out.println("Erro ao apagar o arquivo!");
			}
		} else {
			System.out.println("Arquivo não existe nesse diretório!");
		}			
	}

	/*StatusStorage index position: 0 - Status do Salvamento
									1 - Mensagem de retorno
									2 - endereço de host do storage
									3 - porta do host do storage
	*/
	private boolean enviaArquivoStorage(List<String> statusStorage, File arquivo, Arquivo novoArquivo) {
		String hostStorage = statusStorage.get(2);
		int portaStorage = Integer.parseInt(statusStorage.get(3));
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		byte[] bufferArquivo = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao servidor!"); 
		   
		    System.out.println("Enviando arquivo...");
	        bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
	        bufferArquivo = serializarArquivo(arquivo, novoArquivo);
	        bufferSaida.write(bufferArquivo);
	        bufferSaida.flush();
	        bufferSaida.close();
		    cliente.close();
		} catch (Exception e) {
			System.out.println("Erro no envio do arquivo!");
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	/*StatusStorage index position: 0 - Status do Salvamento
		1 - Mensagem de retorno
		2 - endereço de host do storage
		3 - porta do host do storage
	*/
	private boolean apagaArquivoStorage(List<String> statusStorage, Arquivo arquivo) {
		String hostStorage = statusStorage.get(2);
		int portaStorage = Integer.parseInt(statusStorage.get(3));
		
		Socket cliente = null;
		BufferedOutputStream bufferSaida = null;
		try {
			cliente = new Socket(hostStorage, portaStorage);
		    System.out.println("O cliente se conectou ao servidor!"); 
		    
		    //monta os dados a ser enviado ao storage (Arquivo e a opção de remover)
		    bufferSaida = new BufferedOutputStream(cliente.getOutputStream());
	        ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream ous;
			ous = new ObjectOutputStream(bao);
			ous.writeObject(arquivo);
			ous.writeInt(Constantes.STORAGE_REMOVE_ARQUIVO);
	       
			System.out.println("Removendo arquivo...");
	        bufferSaida.write(bao.toByteArray());
	        bufferSaida.flush();
	        bufferSaida.close();
		    cliente.close();
		} catch (Exception e) {
			System.out.println("Erro ao tentar remover o arquivo!");
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	

	//Serializa o arquivo para ser enviado.
	//Passa um parametro de opção de salvar arquivo
	private byte[] serializarArquivo(File arquivo, Arquivo novoArquivo){
		FileInputStream fis;
		
        try {
           //le os dados do arquivo, armazena os dados do arquivo no objeto
    	   byte[] conteudoByte = new byte[(int) arquivo.length()];
           fis = new FileInputStream(arquivo);
           fis.read(conteudoByte);
           fis.close();
           novoArquivo.setDadosArquivo(conteudoByte);
           
           //converte em byte o objeto arquivo para ser enviado
           ByteArrayOutputStream bao = new ByteArrayOutputStream();
		   ObjectOutputStream ous;
		   ous = new ObjectOutputStream(bao);
		   ous.writeObject(novoArquivo);
		   ous.writeInt(Constantes.STORAGE_SALVA_ARQUIVO);
		   return bao.toByteArray();
	    } catch (IOException e) {
	       e.printStackTrace();
	    }
	    return null;
	}
}