package imdb;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        title = "IMDB Top Search",
        version = "1.0"
    )
)
@Path("movies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MovieResource {

  @ConfigProperty(name = "app.search.limit")
  int searchLimit;

  @Inject
  MetricProvider metricProvider;

  @Inject
  MovieIndex movieIndex;

  @Operation(
      summary = "Search movies",
      description = "Search movies by title, genre, cast or writer"
  )
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
      String searchText,

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
      String searchScope) {
    metricProvider.searchCounter().add(1);

    List<Movie> movies = movieIndex.searchMovies(searchText, searchScope, searchLimit);
    return MovieList.of(movies);
  }

  record MovieList(int total, List<Movie> movies) {
    static MovieList of(List<Movie> movies) {
      return new MovieList(movies.size(), movies);
    }
  }

  @Slf4j
  @ApplicationScoped
  static class MetricProvider {

    @Inject
    Meter meter;

    LongCounter searchCounter;

    @PostConstruct
    void init() {
      log.info("Registering metrics");
      searchCounter = meter.counterBuilder("search").build();
    }

    LongCounter searchCounter() {
      return searchCounter;
    }

  }

  @Slf4j
  @Provider
  static class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
      log.error("Unhandled exception", exception);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(new Problem("Internal error", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
          .build();
    }

    record Problem(String title, int status) {
    }

  }

}
