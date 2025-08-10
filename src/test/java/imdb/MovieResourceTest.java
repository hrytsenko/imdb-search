package imdb;

import static io.restassured.RestAssured.given;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

@QuarkusTest
@TestProfile(Profiles.CustomDataset.class)
class MovieResourceTest {

  @SneakyThrows
  @Test
  void testSearchMovies() {
    String actualMovies = given()
        .queryParam("query", "thing")
        .when().get("/movies")
        .then().statusCode(200)
        .extract().asString();

    String expectedMovies = """
        {
          "total": 1,
          "movies": [
            {
              "rank": 153,
              "title": "The Thing",
              "year": 1982,
              "rating": "8.2",
              "genres": [
                "Horror",
                "Mystery",
                "Sci-fi"
              ],
              "casts": [
                "Kurt Russell",
                "Wilford Brimley"
              ],
              "writers": [
                "Bill Lancaster",
                "John W. Campbell Jr."
              ]
            }
          ]
        }
        """;

    assertEquals(expectedMovies, actualMovies, JSONCompareMode.STRICT);
  }

}
