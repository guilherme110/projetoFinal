package br.com.projeto.testes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import br.com.projeto.cliente.Cliente;
import br.com.projeto.cliente.ClienteServico;
import br.com.projeto.diretorio.Arquivo;
import br.com.projeto.storage.Storage;
import br.com.projeto.utils.Constantes;
import br.com.projeto.utils.Estatistica;

/**Classe para testes relacionado aos serviços do Cliente.
 * 
 * @author guilherme
 *
 */
public class LatenciaCliente {

	private Estatistica estatistica;
	private ClienteServico clienteServico;
	private Cliente clienteTeste;
	private long horarioReq;
	private long horarioResp;
	
	/**Construtor default da classe
	 * 
	 */
	public LatenciaCliente(int numeroReq, Cliente cliente, ClienteServico clienteServico) {
		this.estatistica = new Estatistica(numeroReq);
		this.estatistica.clear();
		this.clienteServico = clienteServico;
		this.clienteTeste = cliente;
		this.horarioReq = 0L;
		this.horarioResp = 0L;
	}
	
	/**Método para testar o serviço de listar dados
	 * Primeiramente realiza o Warm UP para aquecer o sistema.
	 * Em seguida salva o horário da requisição, realiza a requisição e salva o horário da resposta.
	 * Armazena o resultado utilizando a biblioteca Estatistica.
	 * Por último, grava os resultados em log.
	 * 
	 */
	public void testeListarDados() {
		
		this.getEstatistica().clear();
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().listaDados(this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq(); i++) {
			
			this.setHorarioReq(System.nanoTime());
			this.getClienteServico().listaDados(this.getClienteTeste());
			this.setHorarioResp(System.nanoTime());
			this.getEstatistica().getSt().store(this.getHorarioResp() - this.getHorarioReq());
		}
		this.getEstatistica().salvaDados(Constantes.LISTA_DADOS, null);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
	}
	
	/**Método para testar o serviço de salvar arquivo
	 * Primeiramente cria um arquivo TMP para ser utilizado no teste de acordo com o arquivo escolhido
	 * Não é possível utilizar o arquivo direto em uma aplicação .jar é necessário criar um arquivo TMP.
	 * Em seguida realiza o Warm UP para aquecer o sistema.
	 * Logo salva o horário da requisição, realiza a requisição e salva o horário da resposta.
	 * Armazena o resultado utilizando a biblioteca Estatistica.
	 * Remove o arquivo do sistema.
	 * Por último, grava os resultados em log.
	 * 
	 */
	public void testeSalvarArquivo2(String nomeArquivo) {
		File arquivoTemp = null;
		try {
			arquivoTemp = File.createTempFile(nomeArquivo + "-", ".tmp");
			arquivoTemp.deleteOnExit();
			FileOutputStream out = new FileOutputStream(arquivoTemp);
			InputStream caminhoArquivo = this.getClass().getResourceAsStream("/file/" + nomeArquivo + ".txt");
			IOUtils.copy(caminhoArquivo, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Arquivo arqTemp = new Arquivo(arquivoTemp.getName(), arquivoTemp.length(), null, null);
		this.getEstatistica().clear();
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq(); i++) {	
			this.setHorarioReq(System.nanoTime());
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.setHorarioResp(System.nanoTime());
			this.getEstatistica().getSt().store(this.getHorarioResp() - this.getHorarioReq());
			
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
		}
		this.getEstatistica().salvaDados(Constantes.SALVA_ARQUIVO, arqTemp);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
	}
	
	public void testeSalvarArquivo(String nomeArquivo, int numeroReq) {
		Estatistica estatisticaMetaDados = new Estatistica(numeroReq);
		Estatistica estatisticaStorage = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		File arquivoFisicoTemp = null;
		Arquivo arquivoLogicoTemp = new Arquivo();
		List<Storage> listaStorages = new ArrayList<Storage>();
		
		try {
			arquivoFisicoTemp = File.createTempFile(nomeArquivo + "-", ".tmp");
			arquivoFisicoTemp.deleteOnExit();
			FileOutputStream out = new FileOutputStream(arquivoFisicoTemp);
			InputStream caminhoArquivo = this.getClass().getResourceAsStream("/file/" + nomeArquivo + ".txt");
			IOUtils.copy(caminhoArquivo, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivoServidorMetaDados(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		this.getEstatistica().clear();
		
		for (int i = 0; i < numeroReq; i++) {
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoServidorMetaDados(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			horarioResp = System.nanoTime();
			estatisticaMetaDados.getSt().store(horarioResp - horarioReq);
			
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			horarioResp = System.nanoTime();
			estatisticaStorage.getSt().store(horarioResp - horarioReq);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		estatisticaMetaDados.salvaDados(Constantes.SALVA_ARQUIVO, arquivoLogicoTemp);
		estatisticaStorage.salvaDados(Constantes.STORAGE_SALVA_ARQUIVO, arquivoLogicoTemp);
	}
	
	/**Método para testar o serviço de remover arquivo
	 * Primeiramente cria um arquivo TMP para ser utilizado no teste.
	 * Em seguida realiza o Warm UP para aquecer o sistema.
	 * Logo salva o arquivo para poder ser removido.
	 * Em seguida salva o horário da requisição, realiza a requisição para remover o arquivo e salva o horário da resposta.
	 * Armazena o resultado utilizando a biblioteca Estatistica.
	 * Por último, grava os resultados em log.
	 * 
	 */
	public void testeRemoverArquivo(String nomeArquivo) {
		File arquivoTemp = null;
		try {
			arquivoTemp = File.createTempFile(nomeArquivo, ".tmp");
			arquivoTemp.deleteOnExit();
			FileOutputStream out = new FileOutputStream(arquivoTemp);
			InputStream caminhoArquivo = this.getClass().getResourceAsStream("/file/" + nomeArquivo + ".txt");
			IOUtils.copy(caminhoArquivo, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Arquivo arqTemp = new Arquivo(arquivoTemp.getName(), arquivoTemp.length(), null, null);
		this.getEstatistica().clear();
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq(); i++) {	
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.getEstatistica().getSt().store(this.getHorarioResp() - this.getHorarioReq());
			
			this.setHorarioReq(System.nanoTime());
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
			this.setHorarioResp(System.nanoTime());
		}
		this.getEstatistica().salvaDados(Constantes.REMOVE_ARQUIVO, arqTemp);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
	}

	public Estatistica getEstatistica() {
		return estatistica;
	}

	public void setEstatistica(Estatistica estatistica) {
		this.estatistica = estatistica;
	}

	public ClienteServico getClienteServico() {
		return clienteServico;
	}

	public void setClienteServico(ClienteServico clienteServico) {
		this.clienteServico = clienteServico;
	}

	public Cliente getClienteTeste() {
		return clienteTeste;
	}

	public void setClienteTeste(Cliente clienteTeste) {
		this.clienteTeste = clienteTeste;
	}

	public long getHorarioReq() {
		return horarioReq;
	}

	public void setHorarioReq(long horarioReq) {
		this.horarioReq = horarioReq;
	}

	public long getHorarioResp() {
		return horarioResp;
	}

	public void setHorarioResp(long horarioResp) {
		this.horarioResp = horarioResp;
	}
}
