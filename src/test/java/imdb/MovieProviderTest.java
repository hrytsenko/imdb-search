package imdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import imdb.Profiles.ManualDataset;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(ManualDataset.class)
class MovieProviderTest {

  @InjectMock
  MovieProvider.Dataset movieDataset;
  @InjectMock
  MovieIndex movieIndex;

  @Inject
  MovieProvider movieProvider;

  @Test
  void testIndexMovies() {
    doReturn(new StringReader(ManualDataset.DATASET))
        .when(movieDataset).getReader();

    doAnswer(invocation -> {
      Stream<Movie> actualMovies = invocation.getArgument(0);
      assertEquals(List.of(ManualDataset.ALIEN), actualMovies.toList());
      return null;
    }).when(movieIndex).indexMovies(Mockito.any());

    movieProvider.indexMovies();
  }

}
