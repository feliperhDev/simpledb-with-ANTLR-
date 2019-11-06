package br.udesc.udescdb.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String nome;
	private List<Coluna> colunas;
	private int tamanho;
	
	public Table() {
		super();
		this.colunas = new ArrayList<Coluna>();
	}

	public Table(String nome, List<Coluna> colunas, int tamanho) {
		super();
		this.nome = nome;
		this.colunas = colunas;
		this.tamanho = tamanho;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void addColuna(Coluna c) {
		this.colunas.add(c);
	}

	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}

	public String getNome() {
		return nome;
	}

	public List<Coluna> getColunas() {
		return colunas;
	}

	public int getTamanho() {
		return tamanho;
	}
	
	
	
	
}
