package br.udesc.udescdb.model;

import java.util.ArrayList;
import java.util.List;

public class Tabela {
	
	private String nome;
//	private List<Coluna> colunas;
	private String[] colunas;
	
	private List< List<String> > linhas;
	private int tamanho;
	
	public Tabela() {
		
	}
	
	public Tabela(int length) {
		super();
		this.colunas = new String[length];
		this.linhas = new ArrayList< List<String> >();
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public void setColunas(String[] s) {
		this.colunas = s; 
	}
	
	public void addLinha(List<String> list) {
		this.linhas.add(list);
	}
	
	public void addValoresLinha(int index, String valor) {
		this.linhas.get(index).add(valor);
	}

	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}

	public String getNome() {
		return nome;
	}
	
	public String[] getColunas(){
		return this.colunas;
	}
	
	public String getNomeColuna(int index) {
		return this.colunas[index];
	}
	
	public List<List<String>> getLinhas() {
		return linhas;
	}
	
	public String getValorLinha(int indexLinha, int index) {
		return this.getLinhas().get(indexLinha).get(index);
	}
	
	public int getTamanho() {
		return tamanho;
	}
	
	
	
	
}
