package br.com.ger7.gfood;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardapioAdapter extends ArrayAdapter {

   private Context mContext;
   private List<Produto> listaProduto = new ArrayList<>();

   public CardapioAdapter(@NonNull Context context, ArrayList<Produto> list) {
      super(context, 0 , list);
      mContext = context;
      listaProduto = list;
   }

   @NonNull
   @Override
   public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      View listItem = convertView;
      if(listItem == null)
         listItem = LayoutInflater.from(mContext).inflate(R.layout.lista_cardapio,parent,false);

      Produto produto = listaProduto.get(position);

      ImageView imagem = (ImageView)listItem.findViewById(R.id.imagemProduto);
      imagem.setImageResource(produto.getImagem());

      TextView nome = (TextView) listItem.findViewById(R.id.nomeProduto);
      nome.setText(produto.getNome());

      TextView valor = (TextView) listItem.findViewById(R.id.valorProduto);

      String strUnit = Integer.toString(produto.getValor());
      strUnit = new StringBuilder(strUnit).insert(strUnit.length()-2, ".").toString();
      valor.setText("R$ " + strUnit);

      TextView qtde = (TextView)listItem.findViewById(R.id.qtdeProduto);
      qtde.setText(Integer.toString(produto.getQuantidade()));

      return listItem;
   }
}

