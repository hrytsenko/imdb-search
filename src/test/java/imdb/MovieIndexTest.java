package imdb;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(TestMoviesProfile.class)
class MovieIndexTest {

  @Inject
  MovieIndex movieIndex;

  @ParameterizedTest
  @MethodSource("testSearchData")
  void testSearchMovies(String sourceSearchText, String sourceSearchScope, Movie expectedMovie) {
    List<Movie> actualMovies = movieIndex.searchMovies(sourceSearchText, sourceSearchScope, 1);
    Assertions.assertEquals(expectedMovie, actualMovies.getFirst());
  }

  static Stream<Arguments> testSearchData() {
    return Stream.of(
        Arguments.of("Shining", "title", TestMoviesProfile.shiningMovie()),
        Arguments.of("Thing", "title", TestMoviesProfile.thingMovie()),
        Arguments.of("Drama", "genre", TestMoviesProfile.shiningMovie()),
        Arguments.of("Mystery", "genre", TestMoviesProfile.thingMovie()),
        Arguments.of("Nicholson", "cast", TestMoviesProfile.shiningMovie()),
        Arguments.of("Russell", "cast", TestMoviesProfile.thingMovie()),
        Arguments.of("King", "writer", TestMoviesProfile.shiningMovie()),
        Arguments.of("Campbell", "writer", TestMoviesProfile.thingMovie())
    );
  }

}
