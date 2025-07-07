package imdb;

import java.util.List;

record Movie(Integer rank,
             String title,
             Integer year,
             String rating,
             List<String> genres,
             List<String> casts,
             List<String> writers) {
}
