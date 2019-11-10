package br.udesc.udescdb.model;

public class Coluna {
	private String nome;
	private String valor;
	
	public Coluna() {
		
	}

	public Coluna(String nome, String tipo) {
		super();
		this.nome = nome;
		this.valor = tipo;
	}

	public String getNome() {
		return nome;
	}
	
	public String getValor() {
		return valor;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
	

	
}
