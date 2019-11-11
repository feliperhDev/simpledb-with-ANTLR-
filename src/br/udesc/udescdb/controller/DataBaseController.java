package br.udesc.udescdb.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
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
import br.udesc.udescdb.model.Tabela;

public class DataBaseController {

	/*
	 * Referencia para o diretorio.
	 */
	private String nomeDb;
	private File tabelaMetaDados;
	private File tabelaDados;

	private Tabela t;

	public DataBaseController(String nomeDb) {
		this.nomeDb = nomeDb;
		try {
			this.criaMetaDado();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Tabela getTabela() {
		return this.t;
	}

	public void setTabelaDados(File dados) {
		this.tabelaDados = dados;
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

				novaTabela = nomeTabela + ":";

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

				criarTabela(nomeTabela);

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
	public File criarTabela(String nomeTabela) throws IOException {
		tabelaDados = new File("C:/udescdb/" + nomeDb + "/" + nomeTabela + ".dat");
		if (!tabelaDados.exists()) {
			tabelaDados.createNewFile();
		}
		return tabelaDados;
	}

	/*
	 * Insert das tabelas de dados.
	 */
	public void inserirNaTabela(String nomeTabela, List<Coluna> colunasInsert) {
		try {
			// Valores utilizados para validas os dados do metadado.
			String metadadoRegistro = retornaRegistroMetaDados(lerMetadados(), nomeTabela);
			String[] dados = metadadoRegistro.split(":");
			String[] colunasMetadado = dados[1].split(",");
			String[] colunasValorMetadado = dados[2].split(",");

			List<String> valoresInsert = new ArrayList<String>();

			/*
			 * Controla os dados de insert, colocando null para os nomes de colunas que não
			 * forem passados no insert, e verificando se os nomes da coluna correspondem ao
			 * registro do Metadados.
			 */
			if (!metadadoRegistro.isEmpty() && !colunasInsert.isEmpty()) {
				while (colunasMetadado.length != valoresInsert.size()) {
					for (String valoresDoMetadado : colunasMetadado) {
						for (Iterator<Coluna> i = colunasInsert.iterator(); i.hasNext();) {
							Coluna c = (Coluna) i.next();
							if (c.getNome().equals(valoresDoMetadado)) {
								valoresInsert.add(c.getValor());
								break;
							} else if (!i.hasNext()) {
								valoresInsert.add(null);
							}
						}
					}
				}

				try {
					criarTabela(nomeTabela);
				} catch (IOException e) {
					e.printStackTrace();
				}

				/*
				 * Logica para escrita dos registros de uma tabela.
				 */
				DataOutputStream out = new DataOutputStream(new FileOutputStream(tabelaDados, true));
				int count2 = 0;
				for (String valor : valoresInsert) {
					String tipo = colunasValorMetadado[count2];

					if (tipo.equals("int")) {
						if (valor == null) {
							out.write(1); // 1 se for nulo
							byte[] valorNulo = new byte[4];
							for (int i = 0; i < valorNulo.length; i++) {
								out.write(valorNulo[i]);
							}
						} else {
							out.write(0); // 0 para valor não null
							int resul = Integer.parseInt(valor);
							out.writeInt(resul);
						}
					} else if (tipo.equals("float")) {
						if (valor == null) {
							out.write(1); // 1 se for nulo
							byte[] valorNulo = new byte[4];
							for (int i = 0; i < valorNulo.length; i++) {
								out.write(valorNulo[i]);
							}
						} else {
							out.write(0); // 0 para valor não null
							float resul = Float.parseFloat(valor);
							out.writeFloat(resul);
						}
					} else if (tipo.contains("char")) {
						String split1 = tipo.split("\\(")[1];
						String split2 = split1.split("\\)")[0];
						int tamChar = Integer.parseInt(split2);

						if (valor == null) {
							out.write(1); // 1 se for nulo
							byte[] valorNulo = new byte[tamChar];
							for (int i = 0; i < valorNulo.length; i++) {
								out.write(valorNulo[i]);
							}
						} else {
							out.write(0); // 0 para valor não null
							char[] aResult = Arrays.copyOf(valor.toCharArray(), tamChar);
							for (int i = 0; i < aResult.length; i++) {
								out.write(aResult[i]);
							}
						}
					}
					count2++;
				}
//				String novaLinha = System.getProperty("line.separator");
//				out.writeBytes(novaLinha);
				out.close();
			} else {
				System.out.println("A tabela:(" + nomeTabela + ") não foi encontrada.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listarDaTabela(String nomeTabela) {
		String metadadoRegistro = retornaRegistroMetaDados(lerMetadados(), nomeTabela);

		String[] dados = metadadoRegistro.split(":");
		String[] colunasMetadado = dados[1].split(",");
		String[] colunasValorMetadado = dados[2].split(",");

		t = new Tabela();

		try {
			if (!metadadoRegistro.isEmpty()) {

				try {
					criarTabela(nomeTabela);
				} catch (IOException e) {
					e.printStackTrace();
				}

				t.setNome(nomeTabela);
				RandomAccessFile ra = new RandomAccessFile(tabelaDados, "r");
//				DataInputStream in = new DataInputStream(new FileInputStream(tabelaDados));
				

				int TAMREG = 0; 
				
				int count = 0;

				for (String tipoDoValorColuna : colunasValorMetadado) {
					if (tipoDoValorColuna.equals("int")) {
						TAMREG += 4 + 1; // + 1byte é referente a um byte que eu gravo se o valor é null(1) ou não(0)
					} else if (tipoDoValorColuna.equals("float")) {
						TAMREG += 4 + 1;
					} else if (tipoDoValorColuna.contains("char")) {
						int tamChar = 0;
						String split1 = tipoDoValorColuna.split("\\(")[1];
						String split2 = split1.split("\\)")[0];
						tamChar = Integer.parseInt(split2);

						TAMREG += tamChar + 1;
					}
				}
				t.setTamanho(TAMREG);

				long tamArquivo = tabelaDados.length();
				long numeroDeRegistros = (tamArquivo / TAMREG);
				
				for (int i = 0; i < numeroDeRegistros; i++) {
					ra.seek(TAMREG * i);
					for (String tipoVa : colunasValorMetadado) {
						if (tipoVa.equals("int")) {
							byte verificaValorNull = ra.readByte();
							if (verificaValorNull == 1) {
								System.out.println("RETORNO VARIAVEL COM O TEXTO NULL");
							}else {
								System.out.println(ra.readInt());
							}
							
						} else if (tipoVa.equals("float")) {
							byte verificaValorNull = ra.readByte();
							if (verificaValorNull == 1) {
								System.out.println("RETORNO VARIAVEL COM O TEXTO NULL");
							}else {
								String test = String.valueOf(ra.readFloat());
								System.out.println(test);
							}
							
						} else if (tipoVa.contains("char")) {
							int tamChar = 0;
							String split1 = tipoVa.split("\\(")[1];
							String split2 = split1.split("\\)")[0];
							tamChar = Integer.parseInt(split2);
							
							byte verificaValorNull = ra.readByte();
							if (verificaValorNull == 1) {
								System.out.println("RETORNO VARIAVEL COM O TEXTO NULL");
							}else {
								byte[] charValor = new byte[tamChar];
								for (int j = 0; j < charValor.length; j++) {
									charValor[j] = ra.readByte();
								}
								for (byte b : charValor) {
									System.out.print((char)b);
								}
							}
						}
					}
				}
				

				ra.close();

			} else {
				System.out.println("A tabela:(" + nomeTabela + ") não foi encontrada.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
