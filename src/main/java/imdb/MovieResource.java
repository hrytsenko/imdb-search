package imdb;

import io.micrometer.core.annotation.Counted;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.validator.constraints.Length;

@OpenAPIDefinition(
    info = @Info(
        title = "IMDB Top Search",
        version = "1.0"
    )
)
@Path("movies")
@Produces(MediaType.APPLICATION_JSON)
class MovieResource {

  @ConfigProperty(name = "app.search.limit")
  int searchLimit;

  @Inject
  MovieIndex movieIndex;

  @Operation(
      summary = "Search movies",
      description = "Search movies by title, genre, cast or writer"
  )
  @Counted(value = "movie_search")
  @GET
  public MovieList getMovies(
      @Parameter(
          description = "Search query",
          required = true,
          examples = {
              @ExampleObject(name = "title", value = "thing"),
              @ExampleObject(name = "genre", value = "Horror"),
              @ExampleObject(name = "cast", value = "Nicholson"),
              @ExampleObject(name = "writer", value = "King")}
      )
      @QueryParam("query")
      @NotEmpty(message = "Query is mandatory")
      @Length(min = 4,
          message = "Query must be at least 4 characters long")
      String query,

      @Parameter(
          description = "Search scope",
          examples = {
              @ExampleObject(name = "title", value = "title"),
              @ExampleObject(name = "genre", value = "genre"),
              @ExampleObject(name = "cast", value = "cast"),
              @ExampleObject(name = "writer", value = "writer")}
      )
      @QueryParam("scope")
      @DefaultValue("title")
      @Pattern(regexp = "^(title|genre|cast|writer)$",
          message = "Scope must be one of: title, genre, cast or writer")
      String scope) {
    List<Movie> movies = movieIndex.searchMovies(query, scope, searchLimit);
    return MovieList.of(movies);
  }

  record MovieList(int total, List<Movie> movies) {

    static MovieList of(List<Movie> movies) {
      return new MovieList(movies.size(), movies);
    }

  }

  @Slf4j
  @Provider
  static class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
      log.error("Unhandled exception", exception);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

  }

}
