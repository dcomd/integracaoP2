# Documentação Integração API terceiros 

Como integrar seu app com API de terceiros .

# Configurações
  - Adicionar o arquivo pos7api-release.arr na pasta lib do projeto Android
  - Nas dependencia dos gradle adicione o importe da lib  
     - implementation fileTree(include: ['*.aar'], dir: 'libs')
  - Para impressão adicione os pacotes da sunmi 
     -  sunmiImplementation fileTree(dir: 'sunmi-libs', include: ['*.jar', '*.aar'])
     -  sunmiImplementation 'com.sunmi:printerlibrary:1.0.7'
     -  sunmiImplementation 'com.sunmi:sunmiui:1.1.27'
  - Crie uma pasta sunmi-libs e adicione o PayLib-release-1.4.73.aar

# Enviando um Pagamento

 - Existem 6 formas de pagamento disponiveis Credito à vista, Debito, Voucher, Credito Parcelado 2 , Credito Parcelado 3.
 - Para efetuar a transação é necessario iniciar o Pos 7   POS7API transacao = new POS7API (getApplicationContext() );
 - Objeto enviado para transacionar ParamIn entrada.
   - Nesse Objeto é necessario passar os seguintes parametros
     entrada.setTrsType(1);
     entrada.setTrsAmount(String.valueOf(valorTotal)); é um inteiro
     entrada.setTrsInstallments(parcelas); Quantidade de Parcelas é um inteiro
     entrada.setTrsProduct(tipoTransacao); Credito à vista 1 , Debito 2 , Credito Parcelado com duas parcelas 1 , credito parcelado com 3 parcelas 1 , Pix 8 , Voucher 4
     entrada.setTrsInstMode(tipoParcela);  Credito à vista 0 , Debito 0 , Credito Parcelado com duas parcelas 1 , credito parcelado com 3 parcelas 2 , Pix 0 , Voucher 0
     entrada.setTrsId(idPedido);
   -  //obtem o timestamp para compor o ID da transação
      idPedido =  new SimpleDateFormat("yyMMddHHmmss").format(new Date());
     entrada.setTrsReceipt(false);
 - Após montar o objeto é necessario chamar o metodo de pagamento processTransaction que retornará ParamOut paramOut no onResult.


# Efetuando a impressão


# Verificando as ultimas transações

