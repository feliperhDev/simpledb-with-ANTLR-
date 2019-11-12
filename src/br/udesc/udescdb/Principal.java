package br.udesc.udescdb;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import br.udesc.udescdb.view.FirstFrame;
import br.udesc.udescdb.view.TelaCrud;

public class Principal {
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				FirstFrame f;
				f = new FirstFrame();
				f.setVisible(true);

			}
		});
	}
}
