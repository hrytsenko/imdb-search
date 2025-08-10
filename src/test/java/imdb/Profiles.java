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

class Profiles {

  public static class CustomDataset implements QuarkusTestProfile {

    static Movie SHINING = new Movie(61, "The Shining", 1980, "8.4",
        List.of("Drama", "Horror"),
        List.of("Jack Nicholson", "Shelley Duvall"),
        List.of("Stephen King", "Stanley Kubrick", "Diane Johnson"));
    static Movie THING = new Movie(153, "The Thing", 1982, "8.2",
        List.of("Horror", "Mystery", "Sci-fi"),
        List.of("Kurt Russell", "Wilford Brimley"),
        List.of("Bill Lancaster", "John W. Campbell Jr."));

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
        movieIndex.indexMovies(Stream.of(SHINING, THING));
      }

    }

  }

  public static class ManualDataset implements QuarkusTestProfile {

    static String DATASET = """
        rank,name,year,rating,genre,certificate,run_time,tagline,budget,box_office,casts,directors,writers
        51,Alien,1979,8.5,"Horror,Sci-Fi",R,1h 57m,In space no one can hear you scream.,11000000,106285522,"Sigourney Weaver,Tom Skerritt",Ridley Scott,"Dan O'Bannon,Ronald Shusett"
        """;

    static Movie ALIEN = new Movie(51, "Alien", 1979, "8.5",
        List.of("Horror", "Sci-Fi"),
        List.of("Sigourney Weaver", "Tom Skerritt"),
        List.of("Dan O'Bannon", "Ronald Shusett"));

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.arc.test.disable-application-lifecycle-observers", "true");
    }

  }

}
