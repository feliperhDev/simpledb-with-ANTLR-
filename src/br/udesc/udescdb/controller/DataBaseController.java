package br.udesc.udescdb.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.sun.javafx.collections.MappingChange.Map;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import br.udesc.udescdb.model.Coluna;
import br.udesc.udescdb.model.SQLiteBaseListener;
import br.udesc.udescdb.model.SQLiteLexer;
import br.udesc.udescdb.model.SQLiteParser;
import br.udesc.udescdb.model.Table;

public class DataBaseController {

	/*
	 * Referencia para o diretorio.
	 */
	private String nomeDb;
	private File tabelaMetaDados;
	private File tabelaDados;

	private Table t;

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
		// Aqui faço o filtro para saber se retorna alguma coisa com o nome de tabela
		// especifico para gravar o input ou nao.
		String linhaDoArquivo = retornaRegistroMetaDados(dadosDoArquivo, nomeTabela);
		if (linhaDoArquivo.isEmpty()) {
			try {
				FileWriter arq = new FileWriter(tabelaMetaDados, true);
				PrintWriter gravarArq = new PrintWriter(arq);

				String novaTabela = "";
				char[] tamanhoNome = {};

				if (nomeTabela.length() > 20) {
					tamanhoNome = Arrays.copyOf(nomeTabela.toCharArray(), 20);
					novaTabela = tamanhoNome + ":";
				} else {
					novaTabela = nomeTabela + ":";
				}

				for (Iterator<Coluna> i = list.iterator(); i.hasNext();) {
					Coluna c = (Coluna) i.next();
					if (i.hasNext()) {
						novaTabela += c.getNome() + ",";
					} else
						novaTabela += c.getNome();
				}
				novaTabela += ":";

				for (Iterator<Coluna> i = list.iterator(); i.hasNext();) {
					Coluna c = (Coluna) i.next();
					if (i.hasNext()) {
						novaTabela += c.getValor() + ",";
					} else
						novaTabela += c.getValor();
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
		tabelaDados = new File("C:/udescdb/" + nomeDb + "/" + nomeTabela + ".dat");
		if (!tabelaDados.exists()) {
			tabelaDados.createNewFile();
		}
	}

	/*
	 * Insert das tabelas de dados.
	 */
	public void inserirNaTabela(String nomeTabela, List<Coluna> colunasInsert) {
		try {
			String metadadoRegistro = retornaRegistroMetaDados(lerMetadados(), nomeTabela);
			String[] dados = metadadoRegistro.split(":");
			String[] colunasMetadado = dados[1].split(",");
			String[] colunasValorMetadado = dados[2].split(",");

			List<String> valoresInsert = new ArrayList<String>();

			int posicao = 0;
			if (!metadadoRegistro.isEmpty() && !colunasInsert.isEmpty()) {

				for (String colunaMetaDadoNome : colunasMetadado) {
					Coluna c = colunasInsert.get(posicao);
					if (c.getNome().equals(colunaMetaDadoNome)) {
						valoresInsert.add(c.getValor());
						if (colunasMetadado.length != colunasInsert.size()) {
							posicao = 0;
						} else {
							posicao++;
						}
					} else {
						valoresInsert.add(null);
						if (colunasMetadado.length != colunasInsert.size()) {
							posicao = 0;
						} else {
							posicao++;
						}
					}

				}

//				int testesss = 0;
//				for (String s : valoresInsert) {
//					System.out.println(s + " " + colunasValorMetadado[testesss]);
//					testesss++;
//				}

				try {
					criarTabela(nomeTabela);
				} catch (IOException e) {
					e.printStackTrace();
				}

				DataOutputStream out = new DataOutputStream(new FileOutputStream(tabelaDados, true));
				int count2 = 0;
				for (String valor : valoresInsert) {
					String tipo = colunasValorMetadado[count2];
					if (valor == null) {
						String newValor = "null";
						char[] valorVazio = newValor.toCharArray();
						out.write(1);
						for (int i = 0; i < valorVazio.length; i++) {
							out.write(valorVazio[i]);
						}
					} else {
						if (tipo.equals("int")) {
							int resul = Integer.parseInt(valor);
							out.writeInt(resul);
							count2++;
						} else if (tipo.equals("float")) {
							float resul = Float.parseFloat(valor);
							out.writeFloat(resul);
							count2++;
						} else if (tipo.contains("char")) {
							String split1 = tipo.split("\\(")[1];
							String split2 = split1.split("\\)")[0];
							int tamChar = Integer.parseInt(split2);
							char[] aResult = Arrays.copyOf(valor.toCharArray(), tamChar);
							for (int i = 0; i < aResult.length; i++) {
								out.write(aResult[i]);
							}
							count2++;
						} else {
							throw new Exception("Valores de insert invalido.");
						}

					}

				}
				String novaLinha = System.getProperty("line.separator");
				out.writeBytes(novaLinha); 
				out.close();

			} else {
				System.out.println("A tabela:(" + nomeTabela + ") não foi encontrada.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void validaValorParaGravar(String valor, String[] tipoColuna, DataOutputStream out) {
//		try {
//			
//		
//	}
}
