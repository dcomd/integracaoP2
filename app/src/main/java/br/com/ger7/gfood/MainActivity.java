package br.com.ger7.gfood;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import ger7.com.br.pos7api.LastTransaction;
import ger7.com.br.pos7api.POS7API;
import ger7.com.br.pos7api.ParamIn;
import ger7.com.br.pos7api.ParamOut;
import ger7.com.br.pos7api.TransactionReport;

public class MainActivity extends AppCompatActivity {

    long idTrs;

    private void trataResultado(ParamOut paramOut) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        String message = null;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);

        if (paramOut != null) {
            switch (paramOut.getResponse()) {
                case 0:
                    layout.setBackgroundResource(R.drawable.aprovada);
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
        } else {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_adm);
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setTitle("");

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        idTrs = readLong("NSU.txt", -1);
        if (idTrs == -1) {
            Log.e("[NSU]", "Arquivo não existe, criando...");
            idTrs = 10000000;
            try {
                writeLong("NSU.txt", idTrs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                new Print(getApplicationContext());
                mDialog.dismiss();
            }
        }).start();
    }

    public void fazerPedido(View view) {
        Intent intent = new Intent(this, Cardapio.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int type = 0;
        try {
            POS7API api = new POS7API(getApplicationContext());
            String strIdTrs = lerAtualizarNSU();
            ParamIn in = new ParamIn();
            in.setTrsId(strIdTrs);

            switch (id) {
                case R.id.desfazimento:
                    //Pega a ultima transacao e verifica se foi aprovada ou nao obteve resposta do servidor
                    //Em caso afirmativo desfaz a ultima transacao, senao apenas retorna sem enviar para API.
                    LastTransaction lr = api.getLastTransaction();
                    if (lr != null) {
                        if (lr.getTrsStatus() == LastTransaction.TransactionStatus.APPROVED ||
                                lr.getTrsStatus() == LastTransaction.TransactionStatus.NO_ANSWER) {
                            in.setTrsVoidId(lr.getTrsId());
                            type = 20;
                        }
                    } else {
                        return super.onOptionsItemSelected(item);
                    }
                    break;

                case R.id.logTransacoes:
                    File logTrs = new TransactionReport().getReport();
                    readFromTransactionLog(logTrs);
                    return super.onOptionsItemSelected(item);

                case R.id.cancelar:
                    in.setTrsReceipt(true);
                    in.setMerchantPwd(false);
                    type = 2;
                    break;
                case R.id.config:
                    type = 3;
                    break;
                case R.id.param:
                    type = 4;
                    break;
                case R.id.licenca:
                    type = 5;
                    break;
                case R.id.telecarga:
                    type = 6;
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
            in.setTrsType(type);

            api.processTransaction(in, new POS7API.Pos7apiCallback() {
                @Override
                public void onResult(ParamOut paramOut) {
                    trataResultado(paramOut);
                }
            });
        } catch (Exception e) {
            trataResultado(null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    //NSU Deve ser atualizado de forma sequencial quando enviado para o POS7
    //NSU pode ser zerado, mas neste caso a API não pode gerar Desfazimento
    private String lerAtualizarNSU() throws IOException {
        String strIdTrs = String.format("%012d", idTrs);
        idTrs++;
        writeLong("NSU.txt", idTrs);
        return strIdTrs;
    }

    public void writeLong(String filename, long number) throws IOException {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(String.format("%012d", number));
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public long readLong(String filename, long valueIfNotFound) {
        long valorNSU;
        if (!new File(filename).canRead()) return valueIfNotFound;
        try {
            InputStream inputStream = this.openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                valorNSU = Long.parseLong(bufferedReader.readLine());
                inputStream.close();
                return valorNSU;
            }
            return valueIfNotFound;
        } catch (IOException ignored) {
            return valueIfNotFound;
        }
    }

    private void readFromTransactionLog(File readFrom) {
        final Dialog dialog = new Dialog(this);
        String message = null;
        String data = null;
        String valor = null;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);


        try {
            if (readFrom != null) {
                InputStream inputStream = new FileInputStream(readFrom);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    int i = 0;
                    //Recebe os campos vindos do CSV e formata e imprime data e valor para exibir mini relatório
                    //Exibe 10 entradas na tela ou até o final do arquivo
                    while ((receiveString = bufferedReader.readLine()) != null && i < 10) {
                        String tokens[] = receiveString.split(";");
                        if (tokens.length > 5) {
                            if (i > 0) {
                                data = tokens[0].substring(4, 6) + '/' + tokens[0].substring(2, 4) + '/' + tokens[0].substring(0, 2);
                                valor = "R$ " + Long.parseLong(tokens[4].substring(0, 10)) + ',' + tokens[4].substring(10);
                            } else {
                                data = tokens[0];
                                valor = '\t' + tokens[4];
                            }
                            message = stringBuilder.append(data).append("\t").append(valor).append("\n").toString();
                        } else
                            message = "Erro na leitura do arquivo!";
                        i++;
                    }
                    inputStream.close();
                }
            }else {
                    message = "Arquivo de Registros da API vazio";
                }
                if (message != null) {
                    TextView text = (TextView) dialog.findViewById(R.id.dialog_msg);
                    layout.setBackgroundResource(R.drawable.relatorio);
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

        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }
}