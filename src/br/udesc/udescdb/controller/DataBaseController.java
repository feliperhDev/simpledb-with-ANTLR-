package br.udesc.udescdb.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.udesc.udescdb.model.Coluna;
import br.udesc.udescdb.model.SQLiteBaseListener;
import br.udesc.udescdb.model.SQLiteLexer;
import br.udesc.udescdb.model.SQLiteParser;
import br.udesc.udescdb.model.Tabela;

public class DataBaseController {

	private List<Observer> observers = new ArrayList<>();

	/*
	 * Referencia para o diretorio.
	 */
	private String nomeDb;
	private File tabelaMetaDados;
	private File tabelaDados;

	private SQLiteBaseListener baseListener;

	private Tabela t;

	public DataBaseController(String nomeDb) {
		this.nomeDb = nomeDb;
		try {
			this.criaMetaDado();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void attach(Observer obs) {
		observers.add(obs);
	}

	public void detach(Observer obs) {
		observers.remove(obs);
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

		this.baseListener = baseListener;
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

		t = new Tabela(colunasMetadado.length);

		try {
			if (!metadadoRegistro.isEmpty()) {

				try {
					criarTabela(nomeTabela);
				} catch (IOException e) {
					e.printStackTrace();
				}

				t.setNome(nomeTabela);

				RandomAccessFile ra = new RandomAccessFile(tabelaDados, "r");

				int TAMREG = 0;

				/*
				 * Parte do metodo que utilizo para pecorrer meus dados da tabela, e faço a soma
				 * dos tamanhos conforme o tipo de dado de cada 1 com a logica da soma de +
				 * 1byte (1 ou 0) que verifica se o campo é null ou não.
				 */
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

				long tamArquivo = tabelaDados.length();
				long numeroDeRegistros = (tamArquivo / TAMREG);
				int posicaoArquivo = 0;
				for (int i = 0; i < numeroDeRegistros; i++) {
					posicaoArquivo = (TAMREG * i);
					ra.seek(posicaoArquivo);

					t.addLinha(new ArrayList<String>());

					/*
					 * Parte do metodo em que é feita a leitura do arquivo binario e salvo os dados
					 * em um Objeto Tabela do sistema.
					 */
					for (String tipoVa : colunasValorMetadado) {
						ra.seek(posicaoArquivo);

						if (tipoVa.equals("int")) {
							if (ra.readByte() == 1) {
								String valorNull = "null";
								t.addValoresLinha(i, valorNull);
							} else {
								int intValue = ra.readInt();
								System.out.println(String.valueOf(intValue));
								t.addValoresLinha(i, String.valueOf(intValue));
							}
							posicaoArquivo += 4 + 1;

						} else if (tipoVa.equals("float")) {
							if (ra.readByte() == 1) {
								String valorNull = "null";
								t.addValoresLinha(i, valorNull);
							} else {
								String floatValue = String.valueOf(ra.readFloat());
								t.addValoresLinha(i, floatValue);
							}
							posicaoArquivo += (4 + 1);

						} else if (tipoVa.contains("char")) {
							int tamChar = 0;
							String split1 = tipoVa.split("\\(")[1];
							String split2 = split1.split("\\)")[0];
							tamChar = Integer.parseInt(split2);

							String result = "";
							if (ra.readByte() == 1) {
								result = "null";
								t.addValoresLinha(i, result);

							} else {
								byte[] charValor = new byte[tamChar];
								for (int j = 0; j < charValor.length; j++) {
									charValor[j] = ra.readByte();
									result += (char) charValor[j];
								}
								t.addValoresLinha(i, result);
							}
							posicaoArquivo += (1 + tamChar);
						}
					}
					posicaoArquivo = 0;
				}

				t.setColunas(colunasMetadado);

				t.setTamanho(TAMREG);

				ra.close();

				notifyTableChanged();

			} else {
				System.out.println("A tabela:(" + nomeTabela + ") não foi encontrada.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getLinhaTabela(int linha, int coluna) {
		String result = "";
		result = t.getValorLinha(linha, coluna);
		return result;
	}

	public void validaXml(String path) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File("udescdb.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new File(path)));

	}
	
	public void lerXmlFile(String path) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(path));
		
		Element root = doc.getDocumentElement();
		NodeList filhos = root.getChildNodes();
		
		
//		inserirNaTabela(nomeTabela, colunasInsert);
		
	}

	public void notifyTableChanged() {
		for (Observer obs : observers) {
			obs.updateTable();
		}
	}

}
