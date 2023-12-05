package br.com.ger7.gfood;

import java.io.Serializable;

public class Produto implements Serializable {

   private int mImagem;
   private String mNome;
   private int mValor;
   private int mQuantidade;

   //construtor
   public Produto(int mImagem, String mNome, int mValor) {
      this.mImagem = mImagem;
      this.mNome = mNome;
      this.mValor = mValor;
      this.mQuantidade = 0;
   }

   public int getImagem() {
      return mImagem;
   }

   public void setImagem(int mImagem) {
      this.mImagem = mImagem;
   }

   public String getNome() {
      return mNome;
   }

   public void setNome(String mNome) {
      this.mNome = mNome;
   }

   public int getValor() {
      return mValor;
   }

   public void setValor(int mValor) {
      this.mValor = mValor;
   }

   public int getQuantidade() {
      return mQuantidade;
   }

   public void setQuantidade(int mQuantidade) {
      this.mQuantidade = mQuantidade;
   }
}

