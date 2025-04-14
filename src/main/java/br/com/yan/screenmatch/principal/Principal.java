package br.com.yan.screenmatch.principal;

import br.com.yan.screenmatch.model.DadosSerie;
import br.com.yan.screenmatch.service.ConsumoApi;
import br.com.yan.screenmatch.service.ConverteDados;
import br.com.yan.screenmatch.service.DadosTemporada;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    public void exibeMenu() {
        System.out.println("Digite o nome da série que desja buscar: ");
        var nomeSerie = leitura.nextLine();

        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            String url = String.format(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            json = consumoApi.obterDados(url);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);

            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println);
    }
}
