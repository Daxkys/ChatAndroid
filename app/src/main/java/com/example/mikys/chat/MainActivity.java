package com.example.mikys.chat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private WebSocketClient cliente;
    private String nickname = "";
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage(); // al pulsar el boton flotante envia el mensaje al servidor
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(view, "Mensaje enviado", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        quest_nickname(); // al cargar el layout se crea automaticamente la alerta
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_nickname) {
            fab.setEnabled(true);
            Toast.makeText(MainActivity.this, "enviado nick", Toast.LENGTH_SHORT).show();
            try {
                cliente.send(String.valueOf(new JSONObject().put("id",nickname)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_chat) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void WebView() {
        WebView visor = null; //(WebView) findViewById(R.id.VisorWeb);
        visor.loadUrl("https://servidor-android-mikys.c9users.io/");
        visor.getSettings().setJavaScriptEnabled(true);
    }

    private void connectWebSocket() {
        URI uri;
        try {
            //https://servidor-android-mikys.c9users.io/
            uri = new URI("ws://servidor-android-mikys.c9users.io:8081");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        Map<String, String> headers = new HashMap<>();
        cliente = new WebSocketClient(uri, new Draft_17(), headers, 0) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {

            }

            @Override
            public void onMessage(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject recibe = new JSONObject(s);

                            String usuario = recibe.getString("id");
                            String msg = recibe.getString("msg");
                            String privado = recibe.getString("privado");
                            String dst = recibe.getString("dst");


                            String frase = usuario + " to " + dst + " [" + privado + "]" + ":\n" + msg;
                            TextView ListaMensajes = (TextView) findViewById(R.id.listado);
                            ListaMensajes.append(frase + "\n");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
            }

            @Override
            public void onError(Exception e) {
            }
        };
        cliente.connect();
    }

    /**
     * Metodo que recoge el texto del editText y se lo envia al servidor
     */
    public void sendMessage() throws JSONException {
        EditText mensaje = (EditText) findViewById(R.id.mensaje);
        CheckBox privado = (CheckBox) findViewById(R.id.privado);
        EditText destino = (EditText) findViewById(R.id.destino);

        String estado;
        if (privado.isChecked()) {
            estado = "true";
        } else {
            estado = "false";
        }
        JSONObject enviar = new JSONObject();
        enviar.put("id", nickname);
        enviar.put("msg", mensaje.getText().toString());
        enviar.put("privado", estado);
        enviar.put("dst", destino.getText().toString());

        if (mensaje.getText().toString().isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
        } else {
            cliente.send(enviar.toString());
            mensaje.setText("");
        }
    }

    /**
     * Metodo que crea una ventana de alerta para recoger el nombre de usuario
     */
    public void quest_nickname() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Nombre de usuario:");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nickname = input.getEditableText().toString();
                connectWebSocket();
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.create();
        alert.show();
    }
}