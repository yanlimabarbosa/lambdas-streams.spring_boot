package br.com.yan.screenmatch.principal;

import br.com.yan.screenmatch.model.DadosEpisodio;
import br.com.yan.screenmatch.model.DadosSerie;
import br.com.yan.screenmatch.service.ConsumoApi;
import br.com.yan.screenmatch.service.ConverteDados;
import br.com.yan.screenmatch.service.DadosTemporada;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private static final String BASE_URL = "https://www.omdbapi.com/?t=";
    private static final String API_KEY_QUERY = "&apikey=6585022c";

    private final ConsumoApi consumoApi;
    private final ConverteDados conversor;

    public Principal() {
        this.consumoApi = new ConsumoApi();
        this.conversor = new ConverteDados();
    }

    public void exibeMenu() {
        try (Scanner leitura = new Scanner(System.in)) {
            System.out.print("Digite o nome da série que deseja buscar: ");
            String nomeSerie = leitura.nextLine().trim();

            String encodedNomeSerie = encodeValue(nomeSerie);
            String seriesUrl = BASE_URL + encodedNomeSerie + API_KEY_QUERY;

            String jsonSerie = consumoApi.obterDados(seriesUrl);
            DadosSerie dadosSerie = conversor.obterDados(jsonSerie, DadosSerie.class);

            if (dadosSerie == null) {
                System.out.println("Não foi possível obter os dados da série.");
                return;
            }

            System.out.println(dadosSerie);

            List<DadosTemporada> temporadas = new ArrayList<>();
            int totalTemporadas = dadosSerie.totalTemporadas();
            for (int i = 1; i <= totalTemporadas; i++) {
                // Construct URL for each season.
                String seasonUrl = String.format(
                        BASE_URL + encodedNomeSerie + "&season=%d" + API_KEY_QUERY, i);
                String jsonTemporada = consumoApi.obterDados(seasonUrl);
                DadosTemporada temporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
                if (temporada != null) {
                    temporadas.add(temporada);
                }
            }

            temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
        }
    }

    // Helper method to URL-encode input strings.
    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            // This exception should not occur with UTF-8 encoding.
            throw new RuntimeException("Erro ao codificar o valor: " + value, ex);
        }
    }
}
