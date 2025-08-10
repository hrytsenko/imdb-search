package imdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import imdb.Profiles.CustomDataset;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(CustomDataset.class)
class MovieIndexTest {

  @Inject
  MovieIndex movieIndex;

  @ParameterizedTest
  @MethodSource("testSearchData")
  void testSearchMovies(String sourceSearchText, String sourceSearchScope, Movie expectedMovie) {
    List<Movie> actualMovies = movieIndex.searchMovies(sourceSearchText, sourceSearchScope, 1);
    assertEquals(expectedMovie, actualMovies.getFirst());
  }

  static Stream<Arguments> testSearchData() {
    return Stream.of(
        Arguments.of("Shining", "title", CustomDataset.SHINING),
        Arguments.of("Thing", "title", CustomDataset.THING),
        Arguments.of("Drama", "genre", CustomDataset.SHINING),
        Arguments.of("Mystery", "genre", CustomDataset.THING),
        Arguments.of("Nicholson", "cast", CustomDataset.SHINING),
        Arguments.of("Russell", "cast", CustomDataset.THING),
        Arguments.of("King", "writer", CustomDataset.SHINING),
        Arguments.of("Campbell", "writer", CustomDataset.THING)
    );
  }

}
