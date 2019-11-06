package br.udesc.udescdb.model;

public class Coluna {
	private String nome;
	private String tipo;
	
	public Coluna() {
		
	}

	public Coluna(String nome, String tipo) {
		super();
		this.nome = nome;
		this.tipo = tipo;
	}

	public String getNome() {
		return nome;
	}
	
	public String getTipo() {
		return tipo;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	

	
}
