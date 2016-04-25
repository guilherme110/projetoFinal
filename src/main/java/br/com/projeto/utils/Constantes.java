package br.com.projeto.utils;

/**Classe de constantes do projeto
 * 
 * @author guilherme
 *
 */
public class Constantes {
	/*Opções para diretórios*/
	public static final int VERIFICA_DIRETORIO = 1;
	public static final int CRIA_DIRETORIO     = 2;
	public static final int LISTA_DADOS		   = 3;
	public static final int MOVE_DIRETORIO     = 4;
	public static final int REMOVE_DIRETORIO   = 5;
	
	/*Opções para arquivos*/
	public static final int SALVA_ARQUIVO 	       = 10;
	public static final int REMOVE_ARQUIVO	       = 11;
	public static final int BUSCA_ARQUIVO		   = 12;
	public static final int BUSCA_STORAGES_ARQUIVO = 13;
	
	/*Opções para storage*/
	public static final int STORAGE_SALVA_ARQUIVO     	   = 15;
	public static final int STORAGE_REMOVE_ARQUIVO	       = 16;
	public static final int STORAGE_BUSCA_ARQUIVO		   = 17;
	public static final int STORAGE_CADASTRO_TABELASTORAGE = 18;
	
	/*Opçãlo de teste do sistema*/
	public static final int TESTE_SALVA_ARQUIVO_THREAD    		 = 1;
	public static final int TESTE_SALVA_ARQUIVO_HASH	  		 = 2;
	public static final int TESTE_REMOVE_ARQUIVO          		 = 3;
	public static final int TESTE_LEITURA_ARQUIVO_THREAD  		 = 4;
	public static final int TESTE_LEITURA_ARQUIVO_HASH    		 = 5;
	public static final int TESTE_SALVA_ARQUIVO_THREAD_METADADOS = 6;
	public static final int TESTE_SALVA_ARQUIVO_THREAD_STORAGE   = 7;
	public static final int TESTE_SALVA_ARQUIVO_HASH_METADADOS   = 8;
	public static final int TESTE_SALVA_ARQUIVO_HASH_STORAGE     = 9;
	public static final int WARM_UP_DEFAULT	                     = 50;
}
