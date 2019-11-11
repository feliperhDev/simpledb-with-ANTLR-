package br.udesc.udescdb.view;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import br.udesc.udescdb.controller.DataBaseController;
import br.udesc.udescdb.model.Coluna;
import br.udesc.udescdb.model.SQLiteBaseListener;

public class TelaCrud extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JList<String> jList1;
	private JTextField jtfQuery;
	private JButton jbPlay;

	private DataBaseController db;
	private SQLiteBaseListener baseListener;

	public TelaCrud() {
		setTitle("UDESC-DB");
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setSize(800, 600);

//		setResizable(false);
		setLocationRelativeTo(null);

		db = new DataBaseController("db");
		initComponents();

	}

	private void initComponents() {

		JPanel jPanel1 = new JPanel();
		jtfQuery = new JTextField();

		jbPlay = new JButton("Executar");
		
		jbPlay.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
//        		String a = "create table tb_carro (ds_modelo char(20), vl_motor float, nr_portas int)";
//        		String a = "create table tb_moto (ds_modelo char(20), ds_cor char(10))";
//				String a = "insert into tb_carro (ds_modelo, vl_motor, nr_portas) values ('Fiat Uno', 1.0, 4)";
//				String a = "insert into tb_moto (ds_modelo, ds_cor) values ('Biz', 'Azul')";
        		String a = "select * from tb_carro";
//        		String a = "select * from tb_moto";
				
				baseListener = db.initListener(a);
				
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
					
					
				}

			}
		});
		
		
		jList1 = new JList<String>();

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

		add(jPanel4, BorderLayout.NORTH);
		add(new JScrollPane(jList1), BorderLayout.CENTER);

//		pack();


	}
}
