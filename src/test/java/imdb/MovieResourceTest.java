package imdb;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
@TestProfile(TestMoviesProfile.class)
class MovieResourceTest {

  @Test
  void testSearchMovies() {
    MovieResource.MovieList actualMovies = RestAssured.given()
        .queryParam("query", "thing")
        .when().get("/movies")
        .then().statusCode(200)
        .extract().as(MovieResource.MovieList.class);

    MovieResource.MovieList expectedMovies = MovieResource.MovieList.of(List.of(TestMoviesProfile.thingMovie()));

    Assertions.assertEquals(expectedMovies, actualMovies);
  }

}
