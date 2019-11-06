package br.udesc.udescdb.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import br.udesc.udescdb.model.Coluna;
import br.udesc.udescdb.model.SQLiteBaseListener;
import br.udesc.udescdb.model.SQLiteLexer;
import br.udesc.udescdb.model.SQLiteParser;

public class DataBaseController {

	/*
	 * Referencia para o diretorio.
	 */
	private String nomeDb;
	private File tabelaMetaDados;
	private File tabelaDados;

	public DataBaseController(String nomeDb) {
		this.nomeDb = nomeDb;
		try {
			this.criaMetaDado();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Metodo responsavel pela intancia e controle do Listener para retirar os dados
	 * do input do usuario.
	 */
	public SQLiteBaseListener initListener(String querry) {
		CodePointCharStream inputStream = CharStreams.fromString(querry);
		SQLiteLexer lexer = new SQLiteLexer(inputStream);
		CommonTokenStream cts = new CommonTokenStream(lexer);
		SQLiteParser parser = new SQLiteParser(cts);
		parser.setBuildParseTree(true);
		ParseTree tree = parser.parse();
		ParseTreeWalker p = new ParseTreeWalker();

		SQLiteBaseListener baseListener = new SQLiteBaseListener();
		p.walk(baseListener, tree);

		return baseListener;
	}

	// Arquivo de metadados:
	/*
	 * Cria o arquivo que será usado para manter os metadados das tabelas.
	 */
	public void criaMetaDado() throws IOException {
		tabelaMetaDados = new File("C:/udescdb/" + nomeDb + "/_metadados.txt");
		if (!tabelaMetaDados.exists()) {
			tabelaMetaDados.createNewFile();
		}
	}

	/*
	 * Grava o metadado de cada tabela do banco de dados.
	 */
	public void gravaMetaDado(String nomeTabela, List<Coluna> list) {
		// Verifica se um registro igual ja existe no arquivo antes de tentar inserir.
		String dadosDoArquivo = lerMetadados();
		String nomeLinha = retornaRegistroMetaDados(dadosDoArquivo, nomeTabela);
		if (nomeLinha.isEmpty()) {
			try {
				FileWriter arq = new FileWriter(tabelaMetaDados, true);
				PrintWriter gravarArq = new PrintWriter(arq);

				String novaTabela = nomeTabela + ":";
				
				for (Iterator<Coluna> i = list.iterator(); i.hasNext();) {
					Coluna c = (Coluna) i.next();
					if (i.hasNext()) {
						novaTabela += c.getNome() + ",";
					}else
						novaTabela += c.getNome();
				}
				novaTabela += ":";

				for (Iterator<Coluna> i = list.iterator(); i.hasNext();) {
					Coluna c = (Coluna) i.next();
					if (i.hasNext()) {
						novaTabela += c.getTipo() + ",";
					}else
						novaTabela += c.getTipo();
				}
				
				gravarArq.println(novaTabela + ";");
				gravarArq.close();
				
				this.criarTabela(nomeTabela);

			} catch (Exception e) {
				System.out.println("Erro no arquivo de referencia.");
			}
		} else {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.out.println(
						"Já existe uma tabela com o nome:(" + nomeTabela + ") no database:(" + this.nomeDb + ").");
			}
		}
	}

	/*
	 * Faz a leitura dos dados do arquivo e retorna o valor em uma variavel.
	 */
	public String lerMetadados() {
		String dadosArq = "";
		try {
			FileReader arq = new FileReader(tabelaMetaDados);
			BufferedReader leituraArq = new BufferedReader(arq);
			String linha = "";

			try {
				linha = leituraArq.readLine();
				while (linha != null) {
					dadosArq += linha;
					linha = leituraArq.readLine();
				}
				arq.close();
			} catch (Exception e) {
				dadosArq = "Erro no arquivo.";
			}

		} catch (Exception e) {
			dadosArq = "Erro no arquivo.";
		}

		if (dadosArq.contains("Erro")) {
			return "Erro no arquivo.";
		} else {
			return dadosArq;
		}
	}

	/*
	 * Trabalha com a leitura dos dados de um arquivo e retorna apenas a
	 * linha/tabela conforme o filtro de nomeTabela passado por parametro.
	 */
	public String retornaRegistroMetaDados(String dadosArq, String nomeTabela) {
		String[] linhasArquivo = dadosArq.split(";");
		String nome = "";
		String linhaResultado = "";

		try {
			for (int i = 0; i < linhasArquivo.length; i++) {
				nome = linhasArquivo[i].split(":")[0];
				if (nome.equals(nomeTabela)) {
					linhaResultado = linhasArquivo[i];
					break;
				} else {
					linhaResultado = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return linhaResultado;

	}

	// Arquivo de tabela de dados:
	/*
	 * Cria o arquivo que será utilizado para os dados da tabela em especifico.
	 */
	public void criarTabela(String nomeTabela) throws IOException {
		tabelaDados = new File("C:/udescdb/" + nomeDb +"/"+ nomeTabela + ".dat");
		if (!tabelaDados.exists()) {
			tabelaDados.createNewFile();
		}
	}
	
	/*
	 * Insert das tabelas de dados.
	 */
	public void inserirNaTabela(String nomeTabela, List<String> valoresInsert) {
		String metadadoRegistro  = retornaRegistroMetaDados(lerMetadados(), nomeTabela);
		String colunas = "";
	
		String[] colunasTipo = {""};
		if (!metadadoRegistro.isEmpty()) {
			String[] dados = metadadoRegistro.split(":");
			colunas = dados[1];
			
			for (int i = 0; i < dados.length; i++) {
				colunasTipo[i] = dados[2];
			}
			
			System.out.println(colunas + " " + colunasTipo);
			
			
			
		}else {
			System.out.println("Nenhum registro de dado para ("+ nomeTabela +") foi encontrado.");
		}
		
	}

}
