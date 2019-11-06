package br.udesc.udescdb;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import br.udesc.udescdb.view.TelaCrud;

public class Principal {
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Escolha um nome para o banco de dados: ");
		String bdNome = "db"; //sc.next(); 
		System.out.println(bdNome);
		Path p1 = Paths.get("C:/" + "udescdb/"+bdNome+"/"); //aqui eu passo o nome do banco de dados que o usuario vai escolher
		if (!Files.exists(p1)) {
			Files.createDirectories(p1);
		}
		
		TelaCrud tela = new TelaCrud();
		tela.setVisible(true);
		
	}
}
