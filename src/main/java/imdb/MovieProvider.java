package imdb;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
@ApplicationScoped
class MovieProvider {

  @Inject
  Dataset movieDataset;
  @Inject
  MovieIndex movieIndex;

  void onStart(@Observes StartupEvent event) {
    indexMovies();
  }

  void indexMovies() {
    log.info("Loading movies");
    CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter(',').setQuote('"')
        .setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).get();
    try (var reader = movieDataset.getReader();
         var parser = CSVParser.builder().setReader(reader).setFormat(format).get()) {
      movieIndex.indexMovies(parser.stream().map(MovieProvider::convertRecordToMovie));
      log.info("Loaded {} movies", parser.getRecordNumber());
    } catch (Exception exception) {
      log.error("Failed to load movies", exception);
    }
  }

  @SneakyThrows
  private static Movie convertRecordToMovie(CSVRecord record) {
    Function<Integer, String> getString = record::get;
    Function<Integer, Integer> getInteger =
        index -> Integer.valueOf(record.get(index));
    Function<Integer, List<String>> getList =
        index -> Arrays.asList(record.get(index).split(","));
    return new Movie(
        getInteger.apply(0),
        getString.apply(1),
        getInteger.apply(2),
        getString.apply(3),
        getList.apply(4),
        getList.apply(10),
        getList.apply(12));
  }

  @ApplicationScoped
  static class Dataset {

    @ConfigProperty(name = "app.dataset.location")
    String datasetLocation;

    @SneakyThrows
    Reader getReader() {
      return Files.newBufferedReader(Path.of(datasetLocation));
    }

  }

}
