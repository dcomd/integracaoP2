package br.com.ger7.gfood;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Fornece métodos para realizar impressão
 */
interface IPrint {

   /**
    * Imprime uma linha
    *
    * @param line String contendo os dados de uma linha de impressão.
    */
   void printtLine(String line) throws Exception;

   /**
    * Avança o papel até a linha de corte para última impressão realizada
    *
    * @throws Exception
    */
   void printFormFeed() throws Exception;

   /**
    * Imprime o conteúdo armazenado no buffer de impressão. O conteúdo do buffer é limpo,
    * independentemente do sucesso ou falha da operação.
    *
    * @param image Buffer contendo imagem em formato BMP.
    * @throws Exception
    */
   void printImage(Bitmap image) throws Exception;
}
