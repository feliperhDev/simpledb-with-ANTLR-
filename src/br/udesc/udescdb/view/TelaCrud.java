package br.udesc.udescdb.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import br.udesc.udescdb.controller.DataBaseController;
import br.udesc.udescdb.controller.Observer;
import br.udesc.udescdb.model.SQLiteBaseListener;


public class TelaCrud extends JFrame implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class TabelaModel extends javax.swing.table.AbstractTableModel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getColumnName(int column) {
			return db.getTabela().getNomeColuna(column);
		}
		
		@Override
		public int getColumnCount() {
			return db.getTabela().getColunas().length;
		}

		@Override
		public int getRowCount() {
			return db.getTabela().getLinhas().size();
		}

		@Override
		public Object getValueAt(int linha, int coluna) {
			return db.getLinhaTabela(linha, coluna);
		}
		
	}
	
	private JTable tabela;
	private TabelaModel tabelaModel;
	private JTextField jtfQuery;
	private JButton jbPlay;
	private JButton jbXml;

	private DataBaseController db;

	public TelaCrud(String nomeDb) {
		setTitle("UDESC-DB");
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setSize(800, 280);
		setLocationRelativeTo(null);

		db = new DataBaseController(nomeDb);
		db.attach(this);
		tabelaModel = new TabelaModel();
		initComponents();

	}

	private void initComponents() {

		JPanel jPanel1 = new JPanel();
		jtfQuery = new JTextField();

		jbPlay = new JButton("Executar");
		jbPlay.addActionListener(new java.awt.event.ActionListener() {
			

			public void actionPerformed(java.awt.event.ActionEvent evt) {
        		
        		SQLiteBaseListener baseListener;
        		baseListener = db.initListener(jtfQuery.getText());
				
				//CREATE TABLE
				if(baseListener.getComando() == 1) {
					db.gravaMetaDado(baseListener.getNomeTabela(), baseListener.getListColunas());
					
				}
				
				//INSERT INTO
				if(baseListener.getComando() == 2) {
					db.inserirNaTabela(baseListener.getNomeTabela(), baseListener.getColunasInsert());
					
				}
				
				//SELECT * FROM 
				if(baseListener.getComando() == 3) {
					db.listarDaTabela(baseListener.getNomeTabela());
					initTableModel();		
				}
      
        		

			}
		});
		
		jbXml = new JButton("Carregar XML");
		jbXml.addActionListener(new java.awt.event.ActionListener() {
		
			private File xmlFile;

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				final JFileChooser xml = new JFileChooser("Carregar XML");		
				
				int returnValue = xml.showOpenDialog(null);
				boolean flag = true;
				
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					xmlFile = xml.getSelectedFile();
					System.out.println(xmlFile.getAbsolutePath());
					try { 
						db.validaXml(xmlFile.getAbsolutePath()); 
					} catch (SAXException e) {
						flag = false;
					} catch (IOException e) {
						flag = false;
					}
					
					System.out.println("Validado : " + flag);
					if(flag == true) {
						try {
							db.lerXmlFile(xmlFile.getAbsolutePath());
						} catch (ParserConfigurationException | SAXException | IOException e) {
							e.printStackTrace();
						}
					}else {
						JOptionPane.showMessageDialog(new Frame(),"Erro ao validar o xsd.");
					}
				}
				
				
			}
		});
		
		tabela = new JTable();
		
		jPanel1.add(new JLabel("Query: "));
		jPanel1.add(jtfQuery);

		jPanel1.setLayout(new SpringLayout());
		SpringUtilities.makeCompactGrid(jPanel1, 1, 2, 4, 4, 10, 10);

		JPanel jPanel2 = new JPanel();
		jPanel2.add(jbPlay);

		JPanel jPanel4 = new JPanel();
		jPanel4.setLayout(new BorderLayout());
		jPanel4.add(jPanel1, BorderLayout.CENTER);
		jPanel4.add(jPanel2, BorderLayout.SOUTH);
		
		JPanel jPanel5 = new JPanel();
		jPanel5.setLayout(new BorderLayout());
		jPanel5.add(jbXml,BorderLayout.CENTER);
		
		add(jPanel4, BorderLayout.NORTH);
		add(tabela, BorderLayout.CENTER);
		add(jPanel5, BorderLayout.SOUTH);

//		pack();
	}
	
	private void initTableModel() {
		tabela.setModel(this.tabelaModel);
	}


	@Override
	public void updateTable() {
		tabelaModel.fireTableStructureChanged();
		
	}
}
