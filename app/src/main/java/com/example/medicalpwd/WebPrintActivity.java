package com.example.medicalpwd;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class WebPrintActivity extends AppCompatActivity {

    private List<AtendimentoDados> atendimentosList;
    private ViewGroup mLinearLayout;

    private WebView mWebView;

    int[] icons = {
            R.string.fa_home_solid, R.string.fa_calendar_alt_solid, R.string.fa_user_solid,
            R.string.fa_heart_solid, R.string.fa_comment_solid, R.string.fa_dollar_sign_solid, R.string.fa_gift_solid
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLinearLayout = findViewById(R.id.butonLayout);
        getData();
    }

    private void getData() {

        final ProgressDialog pd = ProgressDialog.show(WebPrintActivity.this, "",
                "Buscando...", true);

        RequestQueue queue = Volley.newRequestQueue(WebPrintActivity.this);

        String url = "http://senhas.prototipo.abc.br/senha_functions/menu/menu.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Resposta", response);
                pd.dismiss();

                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray(json.optString("atendimentos"));

                    if (jsonArray.length() == 0) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Não foi possivel" +
                                "buscar seus Atendimentos. Por gentileza, verifique sua conexão" +
                                "com a internet e tente novamente.", Toast.LENGTH_LONG);
                        TextView v = toast.getView().findViewById(android.R.id.message);
                        if( v != null) v.setGravity(Gravity.CENTER);
                        toast.show();

                    } else {
                        atendimentosList = new ArrayList<>();
                        atendimentosList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String codigo = jsonObject.optString("codigo");
                            String descricao = jsonObject.optString("descricao");
                            String identificador = jsonObject.optString("identificador");

                            AtendimentoDados item = new AtendimentoDados(codigo, descricao, identificador);
                            atendimentosList.add(item);
                        }

                        Log.d("Atendimentos", atendimentosList.toString());
                        for (int i = 0; i < atendimentosList.size(); i++) {
                            addLayout(atendimentosList.get(i).getDescricao(), i);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    pd.dismiss();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                respostaWebServices(error);
                pd.dismiss();
            }
        });
        queue.add(stringRequest);
    }


    private void addLayout(String btnDescricao, final int index) {

        View addLayout = this.getLayoutInflater().inflate(R.layout.buttons_list_view, mLinearLayout, false);

        Button btn1 = addLayout.findViewById(R.id.btn1);
        btn1.setText(btnDescricao);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = atendimentosList.get(index).getCodigo();
                String type = "0";

                String url = "http://senhas.prototipo.abc.br/senha_functions/emissao_senha.php?step=1&codigo=" + code + "&tipo=" + type;
                getMedicalPwdPrint(url);
            }
        });


        Button btn2 = addLayout.findViewById(R.id.btn2);
        btn2.setText(btnDescricao);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = atendimentosList.get(index).getCodigo();
                String type = "1";

                String url = "http://senhas.prototipo.abc.br/senha_functions/emissao_senha.php?step=1&codigo=" + code + "&tipo=" + type;
                getMedicalPwdPrint(url);
            }
        });
        mLinearLayout.addView(addLayout);
    }



    private void getMedicalPwdPrint(String url) {

        final ProgressDialog pd = ProgressDialog.show(WebPrintActivity.this, "", "Buscando...", true);

        RequestQueue queue = Volley.newRequestQueue(WebPrintActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Resposta", response);
                doWebViewPrint(response);
                pd.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                respostaWebServices(error);
                pd.dismiss();
            }
        });
        queue.add(stringRequest);
    }



    private void doWebViewPrint(String htmlDocument) {

        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("Testes", "page finished loading" + url);
                createWebPrintJob(view);
                mWebView = null;
            }
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        mWebView = webView;
    }




    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        assert printManager != null;
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        List<PrintJob> printJobs = new ArrayList<>();
        printJobs.add(printJob);

        Log.d("Print Jobs List", printJobs.toString());
    }



    private Context getActivity() {
        return WebPrintActivity.this;
    }


    private void respostaWebServices(VolleyError error) {
        if (error instanceof TimeoutError) {
            Toast toast = Toast.makeText(getApplicationContext(), "Time out Error. " +
                    "Por gentileza, verifique sua conexão com a internet.", Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
        else if (error instanceof NoConnectionError) {
            Toast toast = Toast.makeText(getApplicationContext(), "Sem conexão. " +
                    "Por gentileza, ative sua conexão com a internet.", Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
        else if (error instanceof AuthFailureError) {
            Toast toast = Toast.makeText(getApplicationContext(), "Erro na autenticação dos dados. " +
                    "Por gentileza, tente novamente.", Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
        else if (error instanceof com.android.volley.NetworkError) {
            Toast toast = Toast.makeText(getApplicationContext(), "Erro de rede. " +
                    "Por gentileza, verifique sua conexão com a internet.", Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
        else if (error instanceof com.android.volley.ServerError) {
            Toast toast = Toast.makeText(getApplicationContext(), "Erro de servidor. " +
                    "Por gentileza, verifique o CPF digitado e tente novamente.",Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
        else if (error instanceof com.android.volley.ParseError) {
            Toast toast = Toast.makeText(getApplicationContext(), "JSON Parse Error. " +
                    "Por gentileza, tente novamente.", Toast.LENGTH_SHORT);
            TextView T = toast.getView().findViewById(android.R.id.message);
            if( T != null) T.setGravity(Gravity.CENTER);
            toast.show();
        }
    }
}
