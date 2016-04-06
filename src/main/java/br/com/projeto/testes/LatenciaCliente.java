package br.com.projeto.testes;

import java.io.File;

import br.com.projeto.cliente.Cliente;
import br.com.projeto.cliente.ClienteServico;
import br.com.projeto.diretorio.Arquivo;
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
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {
			this.getClienteServico().listaDados(this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {
			
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
	 * Primeiramente realiza o Warm UP para aquecer o sistema.
	 * Em seguida salva o horário da requisição, realiza a requisição e salva o horário da resposta.
	 * Armazena o resultado utilizando a biblioteca Estatistica.
	 * Por último, grava os resultados em log.
	 * 
	 */
	public void testeSalvarArquivo(String caminhoArquivo) {
		File arquivoTemp = new File(caminhoArquivo);
		Arquivo arqTemp = new Arquivo(arquivoTemp.getName(), arquivoTemp.length(), null, null);
		this.getEstatistica().clear();
		
		//Warm UP
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {	
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
	
	/**Método para testar o serviço de remover arquivo
	 * Primeiramente realiza o Warm UP para aquecer o sistema.
	 * Em seguida salva o horário da requisição, realiza a requisição e salva o horário da resposta.
	 * Armazena o resultado utilizando a biblioteca Estatistica.
	 * Por último, grava os resultados em log.
	 * 
	 */
	public void testeRemoverArquivo(String caminhoArquivo) {
		File arquivoTemp = new File(caminhoArquivo);
		Arquivo arqTemp = new Arquivo(arquivoTemp.getName(), arquivoTemp.length(), null, null);
		this.getEstatistica().clear();
		
		//Warm UP
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {
			this.getClienteServico().salvaArquivo(arquivoTemp, this.getClienteTeste());
			this.getClienteServico().removeArquivo(arquivoTemp.getName(), this.getClienteTeste());
		}
		
		for (int i = 0; i < this.getEstatistica().getNumeroReq() / 2; i++) {	
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
