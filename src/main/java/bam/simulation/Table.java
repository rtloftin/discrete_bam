package bam.simulation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * A table that is generated row by row. This
 * data can be saved to a csv file.
 *
 * Created by Tyler on 10/11/2017.
 */
public class Table {

    private String name;
    private String[] columns;
    private List<Row> rows;

    private Table(String name, String[] columns, List<Row> rows) {
        this.name = name;
        this.columns = columns;
        this.rows = rows;
    }

    public static Table create(String name, List<String> columns) {
        return create(name, columns.toArray(new String[0]));
    }

    public static Table create(String name, String... columns) {
        return new Table(name, Arrays.copyOf(columns, columns.length), new LinkedList<>());
    }

    public static Table combine(String name, String grouping, List<Table> tables) {
        return combine(name, grouping, tables.toArray(new Table[tables.size()]));
    }

    public static Table combine(String name, String grouping, Table... tables) {

        // Create new header
        String[] new_columns = new String[tables[0].columns.length + 1];
        new_columns[0] = grouping;
        System.arraycopy(tables[0].columns, 0, new_columns, 1, tables[0].columns.length);

        // Create new table
        Table new_table = new Table(name, new_columns, new LinkedList<>());

        // Add rows
        for(Table table : tables)
            for(Row row : table.rows)
                new_table.newRow().add(table.name).entries.addAll(row.entries);

        return new_table;
    }

    public class Row {

        private ArrayList<String> entries;

        private Row() {
            entries = new ArrayList<>();
        }

        public Row add(String... values) {
            Collections.addAll(entries, values);

            return this;
        }

        public Row add(int... values) {
            for(int value : values)
                entries.add(Integer.toString(value));

            return this;
        }

        public Row add(double... values) {
            for(double value : values)
                entries.add(Double.toString(value));

            return this;
        }
    }

    public Row newRow() {
        Row row = this.new Row();
        rows.add(row);

        return row;
    }

    public void csv(File folder, String name) throws IOException {

        // Create file
        folder.mkdirs();
        PrintStream file = new PrintStream(new File(folder, name));

        // Print header
        file.println(String.join(",", columns));

        // Print rows
        for(Row row : rows)
            file.println(String.join(",", row.entries));

        // Save table
        file.close();
    }

    public void csv(File folder) throws IOException {
        csv(folder, name + ".csv");
    }

    public void table(File folder, String name) throws IOException {

        // Create file
        folder.mkdirs();
        PrintStream file = new PrintStream(new File(folder, name));

        // Print header
        file.println(String.join(" ", columns));

        // Print rows
        for(Row row : rows)
            file.println(String.join(" ", row.entries));

        // Save table
        file.close();
    }

    public void table(File folder) throws IOException {
        table(folder, name);
    }
}
