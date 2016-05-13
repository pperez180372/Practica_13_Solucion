package com.example.ppere.practica_13_solucion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MainActivity myself;



 //   public unsubscribeContextTask ayy;
  //  public subscribeContextTask ayyy;


    String nameentity;
    int puerto_de_escucha=54545;
    ServerSocket sk;

    float [] buffer=new float[3600];
    int puntero_vector=0;
    int num_muestras=0;
    Canvas grafica_1;
    Canvas grafica_2;
    int canvas_visible;


    Paint paintred;
    Paint paintwhite;
    BitmapDrawable BitmapD_1;
    BitmapDrawable BitmapD_2;


    int mx,Mx,my,My;
    int bmpdx=200,bmpdy=100;



    Button Botonsubscribe;
    Button Botonactualize;
    Button Botonunsubscribe;
    Button LED1,LED2,LED3,LED4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myself=this;

        /* crear los listeners */

        Botonactualize = (Button) findViewById(R.id.buttonActualize);
        Botonactualize.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                nameentity=((TextView)(findViewById(R.id.textView_rr_name))).getText().toString();

                    QueryContextTask ay=new QueryContextTask();
                    ay.execute(nameentity);
                // cuando termine de ejecutarse la clase será destruida si ya no tiene referencias, por ejemplo la primera de dos ejecuciones.
            };
        });

        LED1= (Button) findViewById(R.id.buttonLED1);
        LED1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nameentity=((TextView)(findViewById(R.id.textView_rr_name))).getText().toString();

                updateContextTask ay=new updateContextTask();
                ay.execute(nameentity);
                // cuando termine de ejecutarse la clase será destruida si ya no tiene referencias, por ejemplo la primera de dos ejecuciones.
            };
        });


        /* crea el canvas para poder dibujar la gráfica */

        paintred = new Paint();
        paintred.setColor(Color.parseColor("#CD5C5C"));
        paintred.setStyle(Paint.Style.FILL_AND_STROKE);
        paintred.setAntiAlias(false);
        paintwhite = new Paint();
        paintwhite.setColor(Color.parseColor("#FFFFFF"));


        Bitmap bg_1 = Bitmap.createBitmap(bmpdx, bmpdy, Bitmap.Config.ARGB_8888);
        Bitmap bg_2 = Bitmap.createBitmap(bmpdx, bmpdy, Bitmap.Config.ARGB_8888);
        grafica_1 = new Canvas(bg_1);
        grafica_2 = new Canvas(bg_2);
        BitmapD_1=new BitmapDrawable(getResources(),bg_1);
        BitmapD_2=new BitmapDrawable(getResources(),bg_2);
        canvas_visible=1;
        FrameLayout ll = (FrameLayout) findViewById(R.id.Grafica);
        ll.setBackgroundDrawable(BitmapD_1);


        my=15; // temperatura minima;
        My=40; //temperatura máxima;
        mx=0;  // tiempo minimo; en segundos // dependerá del numero de muestras
        Mx=60;



        // crear los listener para los botones




        // hilo de subscripción se crea y se queda dormido esperando conexiones la IP debe ser pública o accesible

        new Thread(new Runnable() {
            public void run() {
                // cliente echo

                while (true)
                {
                    try {
                        while (true)
                        {
                      /*  java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                        String g=addr.getHostAddress();*/
                            InetAddress g1=getLocalAddress();
                            sk = new ServerSocket(puerto_de_escucha,0,g1);
                            StringBuilder     sb=null,sbj=null;

                            while (!sk.isClosed()) {
                                sbj=null;
                                sb=null;
                                Socket cliente = sk.accept();
                                BufferedReader entrada = new BufferedReader(
                                        new InputStreamReader(cliente.getInputStream()));
                                PrintWriter salida = new PrintWriter(
                                        new OutputStreamWriter(cliente.getOutputStream()), true);
                                String line = null;
                                sb= new StringBuilder();
                                try {
                                    do {
                                        line = entrada.readLine();
                                        sb.append(line + "\n");
                                        //hacking
                                        if ((sbj==null)&&(line.contains("{"))) // date
                                            sbj= new StringBuilder();
                                        if (sbj!=null)
                                        {
                                            sbj.append(line+"\n");
                                        }
                                        System.out.println(""+sb);
                                    }
                                    while (entrada.ready());
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                //String datos = entrada.readLine();

                                System.out.println("Fin de lectura");
                                JSONObject jObject = null;
                                try {
                                    jObject = new JSONObject(sbj.toString());

                                    JSONArray jArray = jObject.getJSONArray("contextResponses");
                                    JSONObject c = jArray.getJSONObject(0);
                                    JSONObject jo = c.getJSONObject("contextElement");
                                    String name = jo.getString("id");

                                    if (nameentity!=null)
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //((TextView)  findViewById(R.id.editResultado)).setText(nameentity);
                                            }
                                        });

                                        if (name.contains(nameentity))
                                        {

                                            JSONArray jArrayattr = jo.getJSONArray("attributes");
                                            for(int i = 0; i <  jArrayattr.length(); i++) {
                                                try {
                                                    JSONObject ca = jArrayattr.getJSONObject(i);

                                                    if (ca.getString("name").contains("temperature")) {

                                                        final double va = ca.getDouble("value");
                                                        nuevamuestra((float)va);

                                           /* runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    EditText t= (EditText)  findViewById(R.id.editTemperatura);

                                                    t.setText("" + va);

                                                }
                                            });*/


                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } // del for
                                        }
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                salida.println("POST HTTP/1.1 200");
                                cliente.close();
                            }
                            Thread.sleep(1000);

                        }
                    } catch (IOException e) {
                        System.out.println(e);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }




                }
            }

        }).start();
    }




    /* Funciones utiles */


    /**
     * + * Returns a valid InetAddress to use for RMI communication. + * If the
     * system property java.rmi.server.hostname is set it is used. + * Secondly
     * InetAddress.getLocalHost is used. + * If neither of these are
     * non-loopback all network interfaces + * are enumerated and the first
     * non-loopback ipv4 + * address found is returned. If that also fails null
     * is returned. +
     */
    private static InetAddress getLocalAddress() {
        InetAddress inetAddr = null;

        /**
         * 1) If the property java.rmi.server.hostname is set and valid, use it
         */
        try {
            System.out
                    .println("Attempting to resolve java.rmi.server.hostname");
            String hostname = System.getProperty("java.rmi.server.hostname");
            if (hostname != null) {
                inetAddr = InetAddress.getByName(hostname);
                if (!inetAddr.isLoopbackAddress()) {
                    return inetAddr;
                } else {
                    System.out
                            .println("java.rmi.server.hostname is a loopback interface.");
                }

            }
        } catch (SecurityException e) {
            System.out
                    .println("Caught SecurityException when trying to resolve java.rmi.server.hostname");
        } catch (UnknownHostException e) {
            System.out
                    .println("Caught UnknownHostException when trying to resolve java.rmi.server.hostname");
        }

        /** 2) Try to use InetAddress.getLocalHost */
        try {
            System.out
                    .println("Attempting to resolve InetADdress.getLocalHost");
            InetAddress localHost = null;
            localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost;
            } else {
                System.out
                        .println("InetAddress.getLocalHost() is a loopback interface.");
            }

        } catch (UnknownHostException e1) {
            System.out
                    .println("Caught UnknownHostException for InetAddress.getLocalHost()");
        }

        /** 3) Enumerate all interfaces looking for a candidate */
        Enumeration ifs = null;
        try {
            System.out
                    .println("Attempting to enumerate all network interfaces");
            ifs = NetworkInterface.getNetworkInterfaces();

            // Iterate all interfaces
            while (ifs.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) ifs.nextElement();

                // Fetch all IP addresses on this interface
                Enumeration ips = iface.getInetAddresses();

                // Iterate the IP addresses
                while (ips.hasMoreElements()) {
                    InetAddress ip = (InetAddress) ips.nextElement();
                    if ((ip instanceof Inet4Address) && !ip.isLoopbackAddress()) {
                        return (InetAddress) ip;
                    }
                }
            }
        } catch (SocketException se) {
            System.out.println("Could not enumerate network interfaces");
        }

        /** 4) Epic fail */
        System.out
                .println("Failed to resolve a non-loopback ip address for this host.");
        return null;
    }





    synchronized void imprimir(final String cad) {
        final String g = cad;

        runOnUiThread(new Runnable() {
            public void run() {
                TextView ll = (TextView) findViewById(R.id.TextViewEstado);
                if (ll != null) ll.setText(cad);


            }
        });
    }

    public void imprimirln(final String cad) {
        imprimir(cad + "\r\n");
    }


    // añade un dato a la gŕafica

    public void nuevamuestra(float dato)
    {

        final float datum=dato;
        final int num_muestrum=num_muestras++;

        Canvas grafica;
        canvas_visible=(canvas_visible+1)%2;
        if (canvas_visible==1)
            grafica=grafica_1;
        else
            grafica=grafica_2;
        // borrar esta escalado.
        grafica.drawRect(0, 0, bmpdx, bmpdy, paintwhite);
        float sy= (float) (bmpdy/(My-my)); // en grados
        float sx=(float) bmpdx/(Mx-mx);   // en segundos

        //añadir dato
        buffer[puntero_vector++]=dato;
        if ((puntero_vector>=Mx))
        {

            puntero_vector=0;
        }

        {
            // grafica de lineas no se escala la x
            int t;
            for (t=0;t<Mx-1;t++)
            {
                float va,va1;
                va=buffer[t];
                if (va>My) va=My;
                if (va<my) va=my;
                va1=buffer[t+1];
                if (va1>My) va1=My;
                if (va1<my) va1=my;
                float x1,y1,x2,y2;

                x1=t*sx;
                y1=bmpdy-(va-my)*sy;
                x2=(t+1)*sx;
                y2=bmpdy-(va1-my)*sy;

                grafica.drawLine(x1,y1,x2,y2, paintred);

            }



            if (canvas_visible == 1) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        /*TextView t= (TextView)  findViewById(R.id.editTemperatura);
                        t.setText("" + datum);
                        t= (TextView)  findViewById(R.id.editnmuestras);
                        t.setText("" + num_muestrum);*/
                        FrameLayout ll = (FrameLayout) findViewById(R.id.Grafica);
                        ll.setBackgroundDrawable(BitmapD_1);
                        // ll.postInvalidate();

                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                      /*  TextView t= (TextView)  findViewById(R.id.editTemperatura);
                        t.setText("" + datum);
                        t= (TextView)  findViewById(R.id.editnmuestras);
                        t.setText("" + num_muestrum);*/
                        FrameLayout ll = (FrameLayout) findViewById(R.id.Grafica);
                        ll.setBackgroundDrawable(BitmapD_2);

//                     ll.postInvalidate();

                    }
                });
            }
        }



    }



    class QueryContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);

           // HttpUriRequest req = (HttpUriRequest) urls[0];
            String nameresource=(String) urls[0];
            int pos=nameresource.indexOf(".");

            if (pos<0) {imprimirln("un recurso consiste en Entidad.Atributo");return "un recurso consiste en Entidad.Atributo";}

            String nameattribute = (String) urls[0].substring(pos+1);
            String nameentity= (String) urls[0].substring(0, pos);

            imprimirln(""+pos+" "+nameentity+"  "+nameattribute);




            EditText tEntidad;
            TextView tHumedad;
            TextView tTemperatura;
/*
            tEntidad = (EditText) findViewById(R.id.editEntidad);
            //tHumedad=  (EditText)  rootView.findViewById(R.id.editHumedad);
            tTemperatura=  (TextView)  findViewById(R.id.editTemperatura);*/

            //String username = tuser.getText().toString(); //"Android_SEU_3n5_1";//
            //String passwd = tpassword.getText().toString(); // "sensor";//
            //String domain = tdomain.getText().toString(); // "Asignatura SEU";

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload = "{\"entities\": [{\"type\": \"Sensor\",\"isPattern\": \"false\",\"id\": \""+nameentity+"\"}]}";
            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            String resp=        "none";

            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/queryContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();

                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONArray jArray = jObject.getJSONArray("contextResponses");
                        JSONObject c = jArray.getJSONObject(0);
                        JSONObject jo = c.getJSONObject("contextElement");
                        JSONArray jArrayattr = jo.getJSONArray("attributes");

                        for(int i = 0; i <  jArrayattr.length(); i++) {
                            try {
                                JSONObject ca = jArrayattr.getJSONObject(i);

                                if (ca.getString("name").contains(nameattribute)) {

                                    final double va = ca.getDouble("value");
                                    nuevamuestra((float) va);
                                    imprimirln(nameentity+"."+nameattribute+"="+ (float) va);
                                    /*runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((TextView)  findViewById(R.id.editTemperatura)).setText("" + va);

                                        }
                                    });*/


                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } // del for



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    // cabeceras de recepcion
                    rr = conn.getHeaderFields();
                    System.out.println("headers: " + rr.toString());

                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resp;
        }
/*
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            res=result;
            TextView ttoken =   (TextView)  findViewById(R.id.editResultado);
            ttoken.setText(result);
        }*/
    }


    class updateContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);

            // HttpUriRequest req = (HttpUriRequest) urls[0];
            String nameresource=(String) urls[0];
            int pos=nameresource.indexOf(".");

            if (pos<0) {imprimirln("un recurso consiste en Entidad.Atributo");return "un recurso consiste en Entidad.Atributo";}

            String nameattribute = (String) urls[0].substring(pos+1);
            String nameentity= (String) urls[0].substring(0, pos);

            imprimirln(""+pos+" "+nameentity+"  "+nameattribute);




            EditText tEntidad;
            TextView tHumedad;
            TextView tTemperatura;
/*
            tEntidad = (EditText) findViewById(R.id.editEntidad);
            //tHumedad=  (EditText)  rootView.findViewById(R.id.editHumedad);
            tTemperatura=  (TextView)  findViewById(R.id.editTemperatura);*/

            //String username = tuser.getText().toString(); //"Android_SEU_3n5_1";//
            //String passwd = tpassword.getText().toString(); // "sensor";//
            //String domain = tdomain.getText().toString(); // "Asignatura SEU";

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload = "{\"entities\": [{\"type\": \"Sensor\",\"isPattern\": \"false\",\"id\": \""+nameentity+"\"}]}";
            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            String resp=        "none";

            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/queryContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();

                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONArray jArray = jObject.getJSONArray("contextResponses");
                        JSONObject c = jArray.getJSONObject(0);
                        JSONObject jo = c.getJSONObject("contextElement");
                        JSONArray jArrayattr = jo.getJSONArray("attributes");

                        for(int i = 0; i <  jArrayattr.length(); i++) {
                            try {
                                JSONObject ca = jArrayattr.getJSONObject(i);

                                if (ca.getString("name").contains(nameattribute)) {

                                    final double va = ca.getDouble("value");
                                    nuevamuestra((float) va);
                                    imprimirln(nameentity + "." + nameattribute + "=" + (float) va);
                                    /*runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((TextView)  findViewById(R.id.editTemperatura)).setText("" + va);

                                        }
                                    });*/


                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } // del for



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    // cabeceras de recepcion
                    rr = conn.getHeaderFields();
                    System.out.println("headers: " + rr.toString());

                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resp;
        }
/*
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            res=result;
            TextView ttoken =   (TextView)  findViewById(R.id.editResultado);
            ttoken.setText(result);
        }*/
    }



}
