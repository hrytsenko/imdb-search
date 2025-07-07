package imdb;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@QuarkusTest
@TestProfile(MovieProviderTest.DisableStartup.class)
class MovieProviderTest {

  @InjectMock
  MovieProvider.Dataset movieDataset;
  @InjectMock
  MovieIndex movieIndex;

  @Inject
  MovieProvider movieProvider;

  @Test
  void testIndexMovies() {
    String sourceDataset = """
        rank,name,year,rating,genre,certificate,run_time,tagline,budget,box_office,casts,directors,writers
        61,The Shining,1980,8.4,"Drama,Horror",R,2h 26m,Iconic terror from the No 1 bestselling writer.,19000000,47335804,"Jack Nicholson,Shelley Duvall",Stanley Kubrick,"Stephen King,Stanley Kubrick"
        """;
    Mockito.doReturn(new StringReader(sourceDataset))
        .when(movieDataset).getReader();

    Movie expectedMovie = new Movie(61, "The Shining", 1980, "8.4",
        List.of("Drama", "Horror"),
        List.of("Jack Nicholson", "Shelley Duvall"),
        List.of("Stephen King", "Stanley Kubrick"));

    Mockito.doAnswer(invocation -> {
      Stream<Movie> actualMovies = invocation.getArgument(0);
      Assertions.assertEquals(List.of(expectedMovie), actualMovies.toList());
      return null;
    }).when(movieIndex).indexMovies(Mockito.any());

    movieProvider.indexMovies();
  }

  public static class DisableStartup implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.arc.test.disable-application-lifecycle-observers", "true");
    }

  }

}
