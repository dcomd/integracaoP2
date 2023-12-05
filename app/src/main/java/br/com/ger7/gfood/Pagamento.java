package br.com.ger7.gfood;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import ger7.com.br.pos7api.POS7API;
import ger7.com.br.pos7api.ParamIn;
import ger7.com.br.pos7api.ParamOut;

public class Pagamento extends Activity {

   private RadioGroup mProdutos;
   private TextView mResumoPedido;
   private TextView mTotal;
   private Button mBotaoTrs;

   private POS7API transacao;
   private ParamIn entrada;

   private int parcelas = 0;
   private int tipoTransacao = 0;
   private int tipoParcela = 0;
   private int valorTotal = 0;
   private String idPedido = null;
   private String pedidoComprovante = null;

   //exibe dialog customizado de acordo com os parametros da resposta
   private void trataResultado (ParamOut paramOut) {
      // custom dialog
      final Dialog dialog = new Dialog(this);
      String message = null;

      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      dialog.setContentView(R.layout.dialog);

      LinearLayout layout = (LinearLayout)dialog.findViewById(R.id.layout);

      if(paramOut != null) {
         switch (paramOut.getResponse()) {
            case 0:
               layout.setBackgroundResource(R.drawable.aprovada);
               String vias[] = paramOut.getResPrint().split("\\f");
               printReceipt(vias[1], paramOut.getResId() + paramOut.getResAuthorization());
               if (paramOut.getResProduct() == 8) {
                  message = "AUTORIZAÇÃO: " + paramOut.getResPixId();
               } else {
                  message = "AUTORIZAÇÃO: " + paramOut.getResAuthorization();
               }
               break;
            case 1:
               message = paramOut.getResDisplay();
               layout.setBackgroundResource(R.drawable.negada);
               break;
            default:
               message = paramOut.getResDisplay() + "\n" + "ERRO: " + paramOut.getResErrorCode();
               layout.setBackgroundResource(R.drawable.falha);
               break;
         }
      }else{
         layout.setBackgroundResource(R.drawable.falha);
      }

      if (message != null) {
         TextView text = (TextView) dialog.findViewById(R.id.dialog_msg);
         text.setText(message);
      }

      Button dialogButton = (Button) dialog.findViewById(R.id.dialog_btn);
      dialogButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      dialog.show();

      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialogInterface) {
            backToMain();
         }
      });
   }

   //retorna para tela inicial
   private void backToMain () {
      Intent intent = new Intent (this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
   }

   //imprime o comprovante
   private void printReceipt (final String viaCliente, final String codigo){

      Log.d("PRINT", "printReceipt");

      Thread t = new Thread(){
         @Override
         public void run() {
            try {
               Print prn = new Print(getApplicationContext());

               int id = getApplicationContext().getResources().getIdentifier("logo","drawable",
                       getApplicationContext().getPackageName());
               Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), id);
               prn.printImage(bmp);

               String[] lines = viaCliente.split("\n");
               for (String line: lines)
                  prn.printtLine(line);

               prn.printtLine("----------------------------------------");
               String[] resumoPedido = pedidoComprovante.split("\n");
               for (String line: resumoPedido)
                  prn.printtLine(line);

               prn.printFormFeed();
            }catch (Exception e){
               Log.d("PRINT", "printReceipt err=" + e.getMessage());
            }
         }
      };
      t.start();
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.pagamento);

      //obtem o arraylist do pedido
      ArrayList<Produto> pedido = (ArrayList<Produto>)getIntent().getSerializableExtra("pedido_lista");

      //instancia as classes da API de pagamento POS7API
      transacao = new POS7API (getApplicationContext() );
      entrada = new ParamIn();

      //habilita o botão back da ActionBar
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_pagamneto);
      setActionBar(toolbar);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setDisplayShowHomeEnabled(true);
      getActionBar().setTitle("");

      //obtem o timestamp para compor o ID da transação
      idPedido =  new SimpleDateFormat("yyMMddHHmmss").format(new Date());

      mBotaoTrs = (Button)findViewById(R.id.botaotrs);
      mResumoPedido = (TextView)findViewById(R.id.pedido);
      mTotal = (TextView)findViewById(R.id.total);

      //monta a textview contendo a lista do pedido
      StringBuilder resumo = new StringBuilder();
      resumo.append("PEDIDO: " + idPedido + "\n\n");
      for (int i=0; i< pedido.size(); i++){
         Produto prodAux = pedido.get(i);
         if(prodAux.getQuantidade() != 0) {
            int valorTotalProd = (prodAux.getQuantidade() * prodAux.getValor());
            valorTotal += valorTotalProd;
            String strTotal = Integer.toString(valorTotalProd);
            strTotal = new StringBuilder(strTotal).insert(strTotal.length()-2, ".").toString();
            String strUnit = Integer.toString(prodAux.getValor());
            strUnit = new StringBuilder(strUnit).insert(strUnit.length()-2, ".").toString();
            resumo.append( prodAux.getNome() + " - R$ " + strUnit + "(x" +  prodAux.getQuantidade() + ") = R$ " +  strTotal + "\n"  );
         }
      }

      mResumoPedido.setText(resumo);
      String str = Integer.toString(valorTotal);
      str = new StringBuilder(str).insert(str.length()-2, ".").toString();

      String total = "TOTAL: R$ " + str;
      mTotal.setText(total);

      //obtem através do radio group, o tipo de produto e parcelamento da transação
      pedidoComprovante = resumo.toString() + total;
      mProdutos = (RadioGroup)findViewById(R.id.rgproduto);
      mProdutos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId)
            {
               case R.id.creditoVista:
                  tipoTransacao = 1;
                  tipoParcela = 0;
                  parcelas = 0;
                  break;
               case R.id.debito:
                  tipoTransacao = 2;
                  tipoParcela = 0;
                  parcelas = 0;
                  break;
               case R.id.voucher:
                  tipoTransacao = 4;
                  tipoParcela = 0;
                  parcelas = 0;
                  break;
               case R.id.creditoParc2:
                  tipoTransacao = 1;
                  tipoParcela = 1;
                  parcelas = 2;
                  break;
               case R.id.creditoParc3:
                  tipoTransacao = 1;
                  tipoParcela = 2;
                  parcelas = 3;
                  break;
               case R.id.pix:
                  tipoTransacao = 8;
                  tipoParcela = 0;
                  parcelas = 0;
                  break;
               default:
                  tipoTransacao = 0;
                  break;
            }
         }
      });

      //botão para chamar a transação na aplicação POS7
      mBotaoTrs.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            try {
               //informa dados da transação
               entrada.setTrsType(1);
               entrada.setTrsAmount(String.valueOf(valorTotal));
               entrada.setTrsInstallments(parcelas);
               entrada.setTrsProduct(tipoTransacao);
               entrada.setTrsInstMode(tipoParcela);
               entrada.setTrsId(idPedido);
               entrada.setTrsReceipt(false);

               //chama a transação
               transacao.processTransaction(entrada, new POS7API.Pos7apiCallback() {
                  @Override
                  public void onResult(ParamOut paramOut) { //obtem os resultados da transação
                     trataResultado(paramOut);
                  }
               });
            }catch (Exception e){
               trataResultado(null);
            }
         }
      });
   }

   @Override
   public boolean onNavigateUp() {
      onBackPressed();
      return true;
   }

   //onClick do botão para cancelar o pedido
   public void cancelaPedido(View view) {
      backToMain();
   }
}
