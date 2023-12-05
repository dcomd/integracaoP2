package br.com.ger7.gfood;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;

public class Cardapio extends Activity {

   //lista de produtos disponíveis
   private final String PRODUTOS[] = {"X-SALADA", "X-CHURRASCO", "COCA-COLA 350ml", "GUARANÁ 350ml"};
   private final int PRECO[] = {1800, 2000, 500, 500};
   private final int IMAGEM[] = {R.drawable.xsalada, R.drawable.xchurras, R.drawable.coca, R.drawable.guarana};

   private ListView listView;
   private CardapioAdapter mAdapter;
   private ArrayList<Produto> mPedido;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.cardapio);

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_cardapio);
      setActionBar(toolbar);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setDisplayShowHomeEnabled(true);
      getActionBar().setTitle("");

      listView = (ListView) findViewById(R.id.lista_cardapio);
      mPedido = new ArrayList<>();

      for(int i = 0; i < PRODUTOS.length; i++){
         mPedido.add (new Produto(IMAGEM[i],PRODUTOS[i], PRECO[i] ));
      }

      mAdapter = new CardapioAdapter(this, mPedido);
      listView.setAdapter(mAdapter);

      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Produto prod = (Produto)adapterView.getItemAtPosition(i);
            prod.setQuantidade(prod.getQuantidade() + 1);
            mAdapter.notifyDataSetChanged();
         }
      });
   }

   @Override
   public boolean onNavigateUp() {
      onBackPressed();
      return true;
   }

   //verifica se o pedido
   private boolean verificaPedido ()
   {
      if (mAdapter != null)
      {
         for (int i=0; i< mPedido.size(); i++){
            Produto prodAux = mPedido.get(i);
            if(prodAux.getQuantidade() != 0) {
               return true;
            }
         }
      }
      return false;
   }

   //onClick do botão para confirmar pedido
   public void confirmarPedido(View view) {
      if(verificaPedido() == false){
         Toast.makeText(getApplicationContext(), R.string.pedido_vazio, Toast.LENGTH_SHORT ).show();
         return;
      }

      Intent intent = new Intent ( this, Pagamento.class);
      intent.putExtra("pedido_lista", mPedido);
      startActivity(intent);
   }
}
