package com.example.notification.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Builds SELECT structure only. Predicate values belong in named JDBC parameters. */
public final class SqlSelectBuilder {
    private final List<String> columns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final List<String> conditions = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private String from;

    public SqlSelectBuilder select(String... values) { columns.addAll(Arrays.asList(values)); return this; }
    public SqlSelectBuilder from(SqlIdentifier table, String alias) {
        from = table + (alias == null || alias.isBlank() ? "" : " " + alias); return this;
    }
    public SqlSelectBuilder join(String type, SqlIdentifier table, String alias, String on) {
        joins.add(type + " JOIN " + table + " " + alias + " ON " + on); return this;
    }
    public SqlSelectBuilder where(String condition) { conditions.add(condition); return this; }
    public SqlSelectBuilder groupBy(String... values) { groupBy.addAll(Arrays.asList(values)); return this; }
    public SqlSelectBuilder orderBy(String... values) { orderBy.addAll(Arrays.asList(values)); return this; }
    public String build() {
        if (columns.isEmpty() || from == null) throw new IllegalStateException("SELECT and FROM are required");
        StringBuilder sql = new StringBuilder("SELECT ").append(String.join(", ", columns)).append(" FROM ").append(from);
        if (!joins.isEmpty()) sql.append(" ").append(String.join(" ", joins));
        if (!conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
        if (!groupBy.isEmpty()) sql.append(" GROUP BY ").append(String.join(", ", groupBy));
        if (!orderBy.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", orderBy));
        return sql.toString();
    }
}
