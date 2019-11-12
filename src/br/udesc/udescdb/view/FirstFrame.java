package br.udesc.udescdb.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class FirstFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TelaCrud tl;
	
	public FirstFrame() {
		setSize(400, 165);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
//		pack();

		initComponents();
	}

	private void initComponents() {

		getContentPane().setLayout(new BorderLayout());

		JPanel newPanel = new JPanel();
		JPanel newPanel2 = new JPanel();
		JPanel newPanel4 = new JPanel();
		
		JLabel lb = new JLabel("UDESC-DB");
		lb.setFont(new Font("Courier New", Font.ITALIC, 30));
		lb.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel p1 = new JLabel("Nome: ");
		JTextField tx1 = new JTextField(10);

		JButton btnStartGame = new JButton("Criar database");
		btnStartGame.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				String bdNome = tx1.getText(); // sc.next();
				Path p1 = Paths.get("C:/" + "udescdb/" + bdNome + "/"); // aqui eu passo o nome do banco de dados que o usuario

				if (!Files.exists(p1)) {
					try {
						Files.createDirectories(p1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				tl = new TelaCrud(bdNome);

				tl.setVisible(true);
				setVisible(false);

			}
		});
		
		newPanel.add(lb);
		newPanel2.add(p1);
		newPanel2.add(tx1);
		newPanel4.add(btnStartGame);

		getContentPane().add(newPanel, BorderLayout.PAGE_START);
		getContentPane().add(newPanel2);

		getContentPane().add(newPanel4, BorderLayout.PAGE_END);
	}

}