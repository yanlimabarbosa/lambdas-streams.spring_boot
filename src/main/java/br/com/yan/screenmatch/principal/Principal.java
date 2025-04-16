package br.com.yan.screenmatch.principal;

import br.com.yan.screenmatch.model.DadosEpisodio;
import br.com.yan.screenmatch.model.DadosSerie;
import br.com.yan.screenmatch.model.Episodio;
import br.com.yan.screenmatch.service.ConsumoApi;
import br.com.yan.screenmatch.service.ConverteDados;
import br.com.yan.screenmatch.service.DadosTemporada;
import br.com.yan.screenmatch.util.UrlEncoderUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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

            String encodedNomeSerie = UrlEncoderUtil.encode(nomeSerie);
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
                String seasonUrl = String.format(BASE_URL + encodedNomeSerie + "&season=%d" + API_KEY_QUERY, i);
                String jsonTemporada = consumoApi.obterDados(seasonUrl);
                DadosTemporada temporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
                if (temporada != null) {
                    temporadas.add(temporada);
                }
            }

            temporadas
                    .forEach(t -> t.episodios()
                            .forEach(e -> System.out.println(e.titulo())));

            List<DadosEpisodio> dadosEpisodios = temporadas
                    .stream().flatMap(t -> t.episodios().stream())
                    .collect(Collectors.toList());

            System.out.println("\nTop 5 episódios");
            dadosEpisodios.stream()
                    .filter(e -> !e.avaliacao().equals("N/A"))
                    .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                    .limit(5)
                    .forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(d -> new Episodio(t.numero(), d)))
                    .collect(Collectors.toList());

            episodios.forEach(System.out::println);

            System.out.println("A partir de que ano você deseja ver os episódios?");
            var ano = leitura.nextInt();
            leitura.nextLine();

            LocalDate dataBusca = LocalDate.of(ano, 1, 1);

            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            episodios.stream()
                    .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                    .forEach(e-> {
                        System.out.println(
                            "Temporada: " + e.getTemporada() +
                            " Episódio: " + e.getTitulo() +
                            " Data lançamento: " + e.getDataLancamento().format(formatador));
                    });
        }
    }
}
