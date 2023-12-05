package br.com.ger7.gfood;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;

class Print implements IPrint {

   private static final String TAG = "[Print]";
   private Context mContext;
   private boolean terminouImpri = false;
   private static SunmiPrinterService sunmiPrinterService;
   private static boolean mModuloInic;

   public Print (Context ctx) {
      this.mContext = ctx;

      try {
         bindPrintService();
      } catch (Exception e) {
         /* Apenas avisa e tenta uma próxima vez na primeira tentativa de impressão. */
         Log.w(TAG, "[Print] Erro ao obter instância de impressão na inicialização.");
         Log.w(TAG, "[Print] Exception: " + e);
         this.sunmiPrinterService = null;
      }
   }

   @Override
   public void printtLine(String line) throws Exception {
      try {
         if (mModuloInic) {
            terminouImpri = false;

            /* verifica estado da impressora antes de prosseguir. */
            int estado = sunmiPrinterService.updatePrinterState();
            Log.d(TAG, "estado: " + estado);
            switch (estado){
               case 1: /* The printer works normally */
               case 2: /* Preparing printer */
                  break;
               case 4: /* Out of paper */
                  return;
               case 5: /* Overheated */
               case 3: /* Abnormal communication */
               case 6: /* Open the lid */
               case 7: /* The paper cutter is abnormal */
               case 8: /* The paper cutter has been recovered */
               case 9: /* No black mark has been detected */
                  return;
            }
            sunmiPrinterService.enterPrinterBuffer(true);
            sunmiPrinterService.setAlignment(0, null);
            sunmiPrinterService.setFontSize(18, null);
            while (line.length() < 42) {
               line = line.concat(" ");
            }
            sunmiPrinterService.printText(line, null);
            sunmiPrinterService.exitPrinterBufferWithCallback(true, new InnerResultCallbcak() {
               @Override
               public void onRunResult(boolean isSuccess) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + isSuccess);
                  terminouImpri = true;
               }

               @Override
               public void onReturnString(String result) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + result);
                  terminouImpri = true;
               }

               @Override
               public void onRaiseException(int code, String msg) throws RemoteException {
                  Log.e(TAG, "exit Exception Callback: " + msg + " Código:" + code);
                  terminouImpri = true;
               }

               @Override
               public void onPrintResult(int code, String msg) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + msg + " Código:" + code);
                  terminouImpri = true;
               }

            });

            while (!terminouImpri) {
               /* Aguarda SDK */
               SystemClock.sleep(100);
            }
         }
      } catch (Exception e){
         throw new Exception(e);
      }
   }

   @Override
   public void printFormFeed() throws Exception {
      try {
         if (mModuloInic) {
            sunmiPrinterService.enterPrinterBuffer(true);
            sunmiPrinterService.lineWrap(8, null);
            sunmiPrinterService.exitPrinterBuffer(true);
         }
      } catch (Exception e){
         throw new Exception(e);
      }
   }

   @Override
   public void printImage(Bitmap image) throws Exception {
      try {
         if (mModuloInic) {
            terminouImpri = false;

            /* verifica estado da impressora antes de prosseguir. */
            int estado = sunmiPrinterService.updatePrinterState();
            Log.d(TAG, "estado: " + estado);
            switch (estado){
               case 1: /* The printer works normally */
               case 2: /* Preparing printer */
                  break;
               case 4: /* Out of paper */
                  return;
               case 5: /* Overheated */
               case 3: /* Abnormal communication */
               case 6: /* Open the lid */
               case 7: /* The paper cutter is abnormal */
               case 8: /* The paper cutter has been recovered */
               case 9: /* No black mark has been detected */
                  return;
            }

            /* Executa montagem da imagem. */
            sunmiPrinterService.enterPrinterBuffer(true);
            sunmiPrinterService.printBitmap(image, null);
            sunmiPrinterService.lineWrap(2, null);
            // Executa impresão da imagem.
            sunmiPrinterService.exitPrinterBufferWithCallback(true, new InnerResultCallbcak() {
               @Override
               public void onRunResult(boolean isSuccess) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + isSuccess);
                  terminouImpri = true;
               }

               @Override
               public void onReturnString(String result) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + result);
                  terminouImpri = true;
               }

               @Override
               public void onRaiseException(int code, String msg) throws RemoteException {
                  Log.e(TAG, "exit Exception Callback: " + msg + " Código:" + code);
                  terminouImpri = true;
               }

               @Override
               public void onPrintResult(int code, String msg) throws RemoteException {
                  Log.d(TAG, "exit Resultado Callback: " + msg + " Código:" + code);
                  terminouImpri = true;
               }

            });

            while (!terminouImpri) {
               /* Aguarda SDK */
               SystemClock.sleep(100);
            }
         }
      } catch (Exception e) {
         throw new Exception(e);
      }
   }

   private void bindPrintService() {
      try {
         if (!mModuloInic) {
            this.sunmiPrinterService = null;
            InnerPrinterManager.getInstance().bindService(mContext, new InnerPrinterCallback() {
               @Override
               protected void onConnected(SunmiPrinterService service) {
                  mModuloInic = true;
                  sunmiPrinterService = service;
               }

               @Override
               protected void onDisconnected() {
                  mModuloInic = false;
                  sunmiPrinterService = null;
               }
            });
         }
      } catch (InnerPrinterException e) {
         Log.w(TAG, "[bindPrintService] InnerPrinterException: " + e);
         e.printStackTrace();
      }
   }

}

