package co.edu.eci.blueprints.persistence;

import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintPersistence;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveBlueprint(Blueprint bp) {

        // Insertar blueprint
        String insertBlueprint = "INSERT INTO blueprints(author, name) VALUES (?, ?)";

        jdbcTemplate.update(insertBlueprint,
                bp.getAuthor(),
                bp.getName());

        // Insertar puntos
        String insertPoint = "INSERT INTO points(author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";

        for (Point p : bp.getPoints()) {
            jdbcTemplate.update(insertPoint,
                    bp.getAuthor(),
                    bp.getName(),
                    p.x(),
                    p.y());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name)
            throws BlueprintNotFoundException {

        String checkBlueprint = "SELECT COUNT(*) FROM blueprints WHERE author=? AND name=?";
        Integer count = jdbcTemplate.queryForObject(
                checkBlueprint,
                Integer.class,
                author,
                name);

        if (count == null || count == 0) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        String queryPoints = "SELECT x, y FROM points WHERE author=? AND blueprint_name=?";

        List<Point> points = jdbcTemplate.query(
                queryPoints,
                new Object[] { author, name },
                (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")));

        return new Blueprint(author, name, points);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author)
            throws BlueprintNotFoundException {

        String queryNames = "SELECT name FROM blueprints WHERE author=?";

        List<String> names = jdbcTemplate.queryForList(
                queryNames,
                String.class,
                author);

        if (names.isEmpty()) {
            throw new BlueprintNotFoundException("Author not found");
        }

        Set<Blueprint> blueprints = new HashSet<>();

        for (String name : names) {
            blueprints.add(getBlueprint(author, name));
        }

        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {

        String query = "SELECT author, name FROM blueprints";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        Set<Blueprint> blueprints = new HashSet<>();

        for (Map<String, Object> row : rows) {
            String author = (String) row.get("author");
            String name = (String) row.get("name");
            try {
                blueprints.add(getBlueprint(author, name));
            } catch (BlueprintNotFoundException e) {
                // No debería ocurrir si la BD está consistente
            }
        }

        return blueprints;
    }

    @Override
    public void addPoint(String author, String name, int x, int y)
            throws BlueprintNotFoundException {

        String checkBlueprint = "SELECT COUNT(*) FROM blueprints WHERE author=? AND name=?";
        Integer count = jdbcTemplate.queryForObject(
                checkBlueprint,
                Integer.class,
                author,
                name);

        if (count == null || count == 0) {
            throw new BlueprintNotFoundException("Blueprint not found");
        }

        String insertPoint = "INSERT INTO points(author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(
                insertPoint,
                author,
                name,
                x,
                y);
    }
}
