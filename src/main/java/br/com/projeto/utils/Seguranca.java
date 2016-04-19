package br.com.projeto.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;

public class Seguranca {
private static final String ALGORITMO_HASH = "SHA-256";
	

	/**Método que gera um código HASH em Hexadecimal de um buffer de arquivo.
	 * 
	 * @param inputArquivo buffer de bytes do arquivo.
	 * @return código Hash em hexadecimal
	 */
	public static String geraHashArquivo(FileInputStream inputArquivo) {
		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance(ALGORITMO_HASH);
			byte messageDigest[] = algorithm.digest(IOUtils.toByteArray(inputArquivo));
		 
			StringBuilder hexString = new StringBuilder();
			for (byte b : messageDigest) {
				hexString.append(String.format("%02X", 0xFF & b));
			}
			return hexString.toString();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
