package br.udesc.udescdb.model;

import java.util.ArrayList;
import java.util.List;

public class Tabela {
	
	private String nome;
	private List<String> linhas;
	private int tamanho;
	
	public Tabela() {
		super();
		this.linhas = new ArrayList<String>();
	}

	public Tabela(String nome, List<String> linhas, int tamanho) {
		super();
		this.nome = nome;
		this.linhas = linhas;
		this.tamanho = tamanho;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void addLinha(String l) {
		this.linhas.add(l);
	}

	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}

	public String getNome() {
		return nome;
	}

	public List<String> getLinhas() {
		return linhas;
	}

	public int getTamanho() {
		return tamanho;
	}
	
	
	
	
}
