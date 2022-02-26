package com.nicolas.bahamut.olxclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nicolas.bahamut.olxclone.R;
import com.nicolas.bahamut.olxclone.adapter.AdapterAnuncios;
import com.nicolas.bahamut.olxclone.helper.ConfiguracaoFirebase;
import com.nicolas.bahamut.olxclone.helper.RecyclerItemClickListener;
import com.nicolas.bahamut.olxclone.model.Anuncio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private Button buttonRegiao, buttonCategoria;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private AlertDialog alertDialog;
    private DatabaseReference anunciosPublicosRef;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        inicializarComponentes();

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Configurar RecyclerView
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recyclerAnunciosPublicos.setAdapter( adapterAnuncios );

        //Aplicar evento de clique
        recyclerAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Anuncio anuncioSelecionado = listaAnuncios.get(position);
                                Intent i = new Intent(AnunciosActivity.this, DetalhesProdutoActivity.class);
                                i.putExtra("anuncioSelecionado", anuncioSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    public void filtrarPorEstado(View view) {
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");

        //Configurando spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        //Spinner estados
        Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estados);
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstados);

        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnunciosPorEstado();
                filtrandoPorEstado = true;
            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    public void filtrarPorCategoria(View view){

        if(filtrandoPorEstado){

            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejada");

            //Configurar spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

            //Configura spinner de categorias
            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] estados = getResources().getStringArray(R.array.categoria);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    estados
            );
            adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            spinnerCategoria.setAdapter( adapter );

            dialogEstado.setView( viewSpinner );

            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnunciosPorCategoria();
                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();

        }else {
            Toast.makeText(this, "Escolha primeiro um estado!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void recuperarAnunciosPorCategoria(){

        listaAnuncios.clear();

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Filtrando anúncios")
                .setCancelable(false)
                .build();

        alertDialog.show();

        //Configura nó por categoria
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child( filtroCategoria );

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();
                for(DataSnapshot anuncios: dataSnapshot.getChildren() ){

                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add( anuncio );

                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void recuperarAnunciosPorEstado(){

        listaAnuncios.clear();

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Filtrando anúncios")
                .setCancelable(false)
                .build();

        alertDialog.show();

        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAnuncios.clear();
                for (DataSnapshot categorias: snapshot.getChildren()){
                    for (DataSnapshot anuncios: categorias.getChildren()){
                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                    }
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarAnunciosPublicos(){

        listaAnuncios.clear();

        alertDialog = new SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Recuperando anúncios")
            .setCancelable(false)
            .build();

        alertDialog.show();

        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase().child("anuncios");

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot estados: snapshot.getChildren()) {
                    for (DataSnapshot categorias: estados.getChildren()){
                        for (DataSnapshot anuncios: categorias.getChildren()){
                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autenticacao.getCurrentUser() == null) { //Deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);
        } else { //Logado
            menu.setGroupVisible(R.id.group_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_cadastrar :
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case R.id.menu_sair :
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case R.id.menu_anuncios :
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        recuperarAnunciosPublicos();
        super.onResume();
    }

    public void inicializarComponentes() {
        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
        buttonCategoria = findViewById(R.id.buttonCategoria);
        buttonRegiao = findViewById(R.id.buttonRegiao);
    }
}