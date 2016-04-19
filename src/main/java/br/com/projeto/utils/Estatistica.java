package br.com.projeto.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import bftsmart.tom.util.Storage;
import br.com.projeto.diretorio.Arquivo;

/**Classe que cálcula e salva os dados de uma determinada requisição.
 * Utiliza a biblioteca log4j para trabalhar com Log's.
 * 
 * @author guilherme
 *
 */
public class Estatistica {
	/**Método para adicionar propiedades no servidor da aplicação.
	 * current.date: Propiedade para pegar a data atual.
	 */
	static{
	    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
	    System.setProperty("current.date", dateFormat.format(new Date()));
	}
	
	private Storage st;
	private int numeroReq;
	private Logger logger;
	
	/**Construtor da classe.
	 * Cria o objeto logger e o objeto st, para trabalhar com a classe
	 * Storage de estatisticas do projeto bftSmart.
	 * 
	 * @param numeroReq numero de requisição.
	 */
	public Estatistica(int numeroReq) {
		this.numeroReq = numeroReq;
		this.st = new Storage(numeroReq);
		this.logger = Logger.getLogger(Estatistica.class);
	}
	
	/**Método que monta os dados para serem salvos no log.
	 * Dependendo da operação, um cabeçalho é montado para o log.
	 * Por fim realiza calculos de média da requisição.
	 * 
	 * @param operacao requisição do cliente.
	 * @param arquivo dados de um arquivo qualquer.
	 * @param horarioReq tempo de inicio da requisição.
	 * @param horarioResp tempo final da requisição.
	 */
	public void salvaDados(int operacao, Arquivo arquivo) {
		StringBuilder mensagemSaida = new StringBuilder();
		Formatacao format = new Formatacao();
		
		switch (operacao) {
		case Constantes.SALVA_ARQUIVO:
			mensagemSaida.append("Operacao: Salvar arquivo\n");
			mensagemSaida.append("Nome Arquivo: " + arquivo.getNomeArquivo() + "\n");
			mensagemSaida.append("Tamanho do Arquivo: " + format.convertNomeBytes(arquivo.getTamanhoArquivo(), true) + "\n");	
			break;
		case Constantes.REMOVE_ARQUIVO:
			mensagemSaida.append("Operacao: Remover arquivo\n");
			mensagemSaida.append("Nome Arquivo: " + arquivo.getNomeArquivo() + "\n");
			mensagemSaida.append("Tamanho do Arquivo: " + format.convertNomeBytes(arquivo.getTamanhoArquivo(), true) + "\n");	
			break;
		case Constantes.LISTA_DADOS:
			mensagemSaida.append("Operacao: Listar dados\n");
			break;
		case Constantes.BUSCA_ARQUIVO:
			mensagemSaida.append("Operacao: Busca e baixa arquivo\n");
			mensagemSaida.append("Nome Arquivo: " + arquivo.getNomeArquivo() + "\n");
			mensagemSaida.append("Tamanho do Arquivo: " + format.convertNomeBytes(arquivo.getTamanhoArquivo(), true) + "\n");	
			break;
		default:
			break;
		}
		
		mensagemSaida.append("Average time for " + this.getNumeroReq() + " executions (-10%) = " + this.st.getAverage(true) / 1000 + " us\n");
		mensagemSaida.append("Standard desviation for " + this.getNumeroReq() + " executions (-10%) = " + this.st.getDP(true) / 1000 + " us\n");
		mensagemSaida.append("Average time for " + this.getNumeroReq() + " executions (all samples) = " + this.st.getAverage(false) / 1000 + " us\n");
		mensagemSaida.append("Standard desviation for " + this.getNumeroReq() + " executions (all samples) = " + this.st.getDP(false) / 1000 + " us\n");
		mensagemSaida.append("Maximum time for " + this.getNumeroReq() + " executions (all samples) = " + this.st.getMax(false) / 1000 + " us\n");
		
		this.logger.info(mensagemSaida);
	}
	
	/**Método que limpa os dados estatisticos.
	 * 
	 */
	public void clear() {
		this.setSt(new Storage(this.getNumeroReq()));
	}
		
	public Storage getSt() {
		return st;
	}
	public void setSt(Storage st) {
		this.st = st;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public int getNumeroReq() {
		return numeroReq;
	}

	public void setNumeroReq(int numeroReq) {
		this.numeroReq = numeroReq;
	}
}