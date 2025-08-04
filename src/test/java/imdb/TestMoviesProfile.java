package imdb;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class TestMoviesProfile implements QuarkusTestProfile {

  static Movie SHINING = new Movie(61, "The Shining", 1980, "8.4",
      List.of("Drama", "Horror"),
      List.of("Jack Nicholson", "Shelley Duvall"),
      List.of("Stephen King", "Stanley Kubrick", "Diane Johnson"));
  static Movie THING = new Movie(153, "The Thing", 1982, "8.2",
      List.of("Horror", "Mystery", "Sci-fi"),
      List.of("Kurt Russell", "Wilford Brimley"),
      List.of("Bill Lancaster", "John W. Campbell Jr."));

  static Stream<Movie> allMovies() {
    return Stream.of(SHINING, THING);
  }

  static Movie shiningMovie() {
    return SHINING;
  }

  static Movie thingMovie() {
    return THING;
  }

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("quarkus.arc.exclude-types", MovieProvider.class.getCanonicalName());
  }

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(TestMovieProvider.class);
  }

  @Alternative
  @ApplicationScoped
  static class TestMovieProvider {

    @Inject
    MovieIndex movieIndex;

    void onStart(@Observes StartupEvent event) {
      movieIndex.indexMovies(allMovies());
    }

  }

}
