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
	private ClienteServico clienteServico;
	private Cliente clienteTeste;
	
	/**Construtor default da classe
	 * 
	 */
	public LatenciaCliente(Cliente cliente, ClienteServico clienteServico) {
		this.clienteServico = clienteServico;
		this.clienteTeste = cliente;
	}
	
	/**Método para gerar um arquivo temporário, utilizado para os testes.
	 * 
	 * @param nomeArquivo nome do arquivo selecionado para os testes.
	 * @return arquivo físico temporário.
	 */
	public File geraArquivoTemp(String nomeArquivo) {
		File arquivoFisicoTemp = null;
		
		try {
			arquivoFisicoTemp = File.createTempFile(nomeArquivo + "-", ".tmp");
			arquivoFisicoTemp.deleteOnExit();
			FileOutputStream out = new FileOutputStream(arquivoFisicoTemp);
			InputStream caminhoArquivo = this.getClass().getResourceAsStream("/file/" + nomeArquivo + ".txt");
			IOUtils.copy(caminhoArquivo, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arquivoFisicoTemp;
	}
	
	/**Método para testes de salvamento de arquivos utilizando hash.
	 * Primeiramente cria-se um arquivo temporário que será utilizado nos testes.
	 * Criou-se duas variaveis para armazenar os dados estatisticos, uma armazena
	 * os dados das requisições para os servidores de metadados e a outra armazena as requisições aos storages.
	 * Em seguida realiza o warm UP para aquecer o sistema.
	 * Logo realiza os testes, salvando primeiramente os dados no servidor de meta dados e em seguida no storages.
	 * Por último, salva os resultados em log. 
	 * 
	 * @param nomeArquivo nome do arquivo escolhido para os testes.
	 * @param numeroReq número de requisições escolhido para os testes.
	 */
	public void testeSalvarArquivoHash(String nomeArquivo, int numeroReq) {
		Estatistica estatisticaMetaDados = new Estatistica(numeroReq);
		Estatistica estatisticaStorage = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivoLogicoTemp = new Arquivo();
		File arquivoFisicoTemp = geraArquivoTemp(nomeArquivo);
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivoServidorMetaDadosHash(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < numeroReq; i++) {
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoServidorMetaDadosHash(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			horarioResp = System.nanoTime();
			estatisticaMetaDados.getSt().store(horarioResp - horarioReq);
			
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			horarioResp = System.nanoTime();
			estatisticaStorage.getSt().store(horarioResp - horarioReq);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		estatisticaMetaDados.salvaDadosLatencia(Constantes.TESTE_SALVA_ARQUIVO_HASH_METADADOS, arquivoLogicoTemp);
		estatisticaStorage.salvaDadosLatencia(Constantes.TESTE_SALVA_ARQUIVO_HASH_STORAGE, arquivoLogicoTemp);
		estatisticaMetaDados.salvaTempoMaximo(estatisticaMetaDados, estatisticaStorage);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
	}
	
	/**Método para testes de salvamento de arquivos utilizando thread.
	 * Primeiramente cria-se um arquivo temporário que será utilizado nos testes.
	 * Criou-se duas variaveis para armazenar os dados estatisticos, uma armazena
	 * os dados das requisições para os servidores de metadados e a outra armazena as requisições aos storages.
	 * Em seguida realiza o warm UP para aquecer o sistema.
	 * Logo realiza os testes, salvando primeiramente os dados no servidor de meta dados e em seguida no storages.
	 * Por último, salva os resultados em log. 
	 * 
	 * @param nomeArquivo nome do arquivo escolhido para os testes.
	 * @param numeroReq número de requisições escolhido para os testes.
	 */
	public void testeSalvarArquivoThread(String nomeArquivo, int numeroReq) {
		Estatistica estatisticaMetaDados = new Estatistica(numeroReq);
		Estatistica estatisticaStorage = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivoLogicoTemp = new Arquivo();
		File arquivoFisicoTemp = geraArquivoTemp(nomeArquivo);
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivoServidorMetaDadosThread(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < numeroReq; i++) {
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoServidorMetaDadosThread(arquivoLogicoTemp, this.clienteTeste, arquivoFisicoTemp, listaStorages);
			horarioResp = System.nanoTime();
			estatisticaMetaDados.getSt().store(horarioResp - horarioReq);
			
			horarioReq = System.nanoTime();
			this.getClienteServico().salvaArquivoStorage(listaStorages, arquivoLogicoTemp, arquivoFisicoTemp, this.clienteTeste);
			horarioResp = System.nanoTime();
			estatisticaStorage.getSt().store(horarioResp - horarioReq);
			
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		estatisticaMetaDados.salvaDadosLatencia(Constantes.TESTE_SALVA_ARQUIVO_THREAD_METADADOS, arquivoLogicoTemp);
		estatisticaStorage.salvaDadosLatencia(Constantes.TESTE_SALVA_ARQUIVO_THREAD_STORAGE, arquivoLogicoTemp);
		estatisticaMetaDados.salvaTempoMaximo(estatisticaMetaDados, estatisticaStorage);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
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
	public void testeRemoverArquivo(String nomeArquivo, int numeroReq) {
		Estatistica estatistica = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		File arquivoFisicoTemp = geraArquivoTemp(nomeArquivo);
		Arquivo arquivoLogicoTemp = new Arquivo(arquivoFisicoTemp.getName(), arquivoFisicoTemp.length(), null, null);
		
		//Warm UP
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().salvaArquivoThread(arquivoFisicoTemp, this.getClienteTeste());
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < numeroReq; i++) {	
			this.getClienteServico().salvaArquivoThread(arquivoFisicoTemp, this.getClienteTeste());
			
			horarioReq = System.nanoTime();
			this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
			horarioResp = System.nanoTime();
			estatistica.getSt().store(horarioResp - horarioReq);
		}
		estatistica.salvaDadosLatencia(Constantes.TESTE_REMOVE_ARQUIVO, arquivoLogicoTemp);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
	}
	
	public void testeLeituraArquivoThread(String nomeArquivo, int numeroReq) {
		Estatistica estatisticaMetaDados = new Estatistica(numeroReq);
		Estatistica estatisticaStorage = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		File arquivoFisicoTemp = geraArquivoTemp(nomeArquivo);
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivoLogicoTemp = new Arquivo(arquivoFisicoTemp.getName(), arquivoFisicoTemp.length(), 
				new ArrayList<Integer>(), null);
		
		//Warm UP, salva o arquivo temporário para testes.
		this.getClienteServico().salvaArquivoThread(arquivoFisicoTemp, this.getClienteTeste());
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().leArquivoMetaDados(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			this.getClienteServico().leArquivoStorageThread(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);	
		}
		
		for (int i = 0; i < numeroReq; i++) {
			horarioReq = System.nanoTime();
			this.getClienteServico().leArquivoMetaDados(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			horarioResp = System.nanoTime();
			estatisticaMetaDados.getSt().store(horarioResp - horarioReq);
			
			horarioReq = System.nanoTime();
			this.getClienteServico().leArquivoStorageThread(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			horarioResp = System.nanoTime();
			estatisticaStorage.getSt().store(horarioResp - horarioReq);
		}
		this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
	
		estatisticaMetaDados.salvaDadosLatencia(Constantes.TESTE_LE_ARQUIVO_THREAD_METADADOS, arquivoLogicoTemp);
		estatisticaStorage.salvaDadosLatencia(Constantes.TESTE_LE_ARQUIVO_THREAD_STORAGE, arquivoLogicoTemp);
		estatisticaMetaDados.salvaTempoMaximo(estatisticaMetaDados, estatisticaStorage);
	
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
		
	}

	public void testeLeituraArquivoHash(String nomeArquivo, int numeroReq) {
		Estatistica estatisticaMetaDados = new Estatistica(numeroReq);
		Estatistica estatisticaStorage = new Estatistica(numeroReq);
		Long horarioReq, horarioResp = 0L;
		File arquivoFisicoTemp = geraArquivoTemp(nomeArquivo);
		List<Storage> listaStorages = new ArrayList<Storage>();
		Arquivo arquivoLogicoTemp = new Arquivo(arquivoFisicoTemp.getName(), arquivoFisicoTemp.length(), 
				new ArrayList<Integer>(), null);
		
		//Warm UP, salva o arquivo temporário para testes.
		this.getClienteServico().salvaArquivoThread(arquivoFisicoTemp, this.getClienteTeste());
		for (int i = 0; i < Constantes.WARM_UP_DEFAULT; i++) {
			this.getClienteServico().leArquivoMetaDados(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			this.getClienteServico().leArquivoStorageHash(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);	
		}
		
		for (int i = 0; i < numeroReq; i++) {
			horarioReq = System.nanoTime();
			this.getClienteServico().leArquivoMetaDados(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			horarioResp = System.nanoTime();
			estatisticaMetaDados.getSt().store(horarioResp - horarioReq);
			
			horarioReq = System.nanoTime();
			this.getClienteServico().leArquivoStorageHash(arquivoLogicoTemp, this.getClienteTeste(), listaStorages);
			horarioResp = System.nanoTime();
			estatisticaStorage.getSt().store(horarioResp - horarioReq);
		}
		this.getClienteServico().removeArquivo(arquivoFisicoTemp.getName(), this.getClienteTeste());
	
		estatisticaMetaDados.salvaDadosLatencia(Constantes.TESTE_LE_ARQUIVO_HASH_METADADOS, arquivoLogicoTemp);
		estatisticaStorage.salvaDadosLatencia(Constantes.TESTE_LE_ARQUIVO_HASH_STORAGE, arquivoLogicoTemp);
		estatisticaMetaDados.salvaTempoMaximo(estatisticaMetaDados, estatisticaStorage);
		
		for (int i = 0; i < 50; i++) {
			System.out.println("");
		}
		System.out.println("Teste efetuado com sucesso, resultado salvo em log!");
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
}
