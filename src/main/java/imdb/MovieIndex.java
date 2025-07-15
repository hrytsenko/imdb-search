package imdb;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
class MovieIndex {

  IndexSearcher indexSearcher;

  @Produces
  @Readiness
  HealthCheck movieIndexHealth() {
    return () -> {
      int indexSize = indexSearcher != null ? indexSearcher.getIndexReader().numDocs() : 0;
      return HealthCheckResponse.builder()
          .name("Movie index")
          .status(indexSearcher != null)
          .withData("Size", indexSize)
          .build();
    };
  }

  @SneakyThrows
  void indexMovies(Stream<Movie> movies) {
    log.info("Indexing movies");
    var index = new ByteBuffersDirectory();
    try (IndexWriter indexWriter = new IndexWriter(index, new IndexWriterConfig())) {
      movies.map(MovieIndex::convertMovieToDocument)
          .forEach(document -> addDocumentToIndex(indexWriter, document));
    }
    indexSearcher = new IndexSearcher(DirectoryReader.open(index));
    log.info("Indexed {} movies", indexSearcher.getIndexReader().numDocs());
  }

  @SneakyThrows
  private static void addDocumentToIndex(IndexWriter indexWriter, Document document) {
    indexWriter.addDocument(document);
  }

  private static Document convertMovieToDocument(Movie movie) {
    Document document = new Document();
    document.add(new NumericDocValuesField("rank", movie.rank()));
    document.add(new StringField("rank", Integer.toString(movie.rank()), Field.Store.YES));
    document.add(new TextField("title", movie.title(), Field.Store.YES));
    document.add(new StringField("year", Integer.toString(movie.year()), Field.Store.YES));
    document.add(new StringField("rating", movie.rating(), Field.Store.YES));
    document.add(new TextField("genres", String.join(",", movie.genres()), Field.Store.YES));
    document.add(new TextField("casts", String.join(",", movie.casts()), Field.Store.YES));
    document.add(new TextField("writers", String.join(",", movie.writers()), Field.Store.YES));
    return document;
  }

  @WithSpan("lucene")
  @SneakyThrows
  List<Movie> searchMovies(String text, String scope, int limit) {
    log.info("Search '{}' with scope '{}'", text, scope);

    String[] words = text.toLowerCase().split("\\s+");
    var searchQuery = switch (scope) {
      case "genre" -> new PhraseQuery(2, "genres", words);
      case "cast" -> new PhraseQuery(2, "casts", words);
      case "writer" -> new PhraseQuery(2, "writers", words);
      default -> {
        var builder = new BooleanQuery.Builder();
        Arrays.stream(words)
            .map(word -> new Term("title", word))
            .map(term -> new FuzzyQuery(term, 1))
            .forEach(query -> builder.add(query, BooleanClause.Occur.SHOULD));
        yield builder.build();
      }
    };

    Sort sort = new Sort(new SortField("rank", SortField.Type.INT));
    var hits = indexSearcher.search(searchQuery, limit, sort);
    log.info("Found {} movies", hits.totalHits.value);
    return Stream.of(hits.scoreDocs)
        .map(hit -> getDocumentFromIndex(indexSearcher, hit))
        .map(MovieIndex::convertDocumentToMovie).toList();
  }

  @SneakyThrows
  private static Document getDocumentFromIndex(IndexSearcher indexSearcher, ScoreDoc hit) {
    return indexSearcher.storedFields().document(hit.doc);
  }

  @SneakyThrows
  private static Movie convertDocumentToMovie(Document document) {
    Function<String, String> getString =
        field -> document.getField(field).stringValue();
    Function<String, Integer> getInteger =
        field -> Integer.valueOf(document.getField(field).stringValue());
    Function<String, List<String>> getList =
        field -> Arrays.asList(document.getField(field).stringValue().split(","));
    return new Movie(
        getInteger.apply("rank"),
        getString.apply("title"),
        getInteger.apply("year"),
        getString.apply("rating"),
        getList.apply("genres"),
        getList.apply("casts"),
        getList.apply("writers"));
  }

}
