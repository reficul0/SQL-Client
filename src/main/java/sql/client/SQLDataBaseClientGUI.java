package sql.client;

import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

public class SQLDataBaseClientGUI extends JFrame {
    private DataBaseConnection _connection;

    public JXTable dbDataTable;
    private JScrollPane _scrollPaneForDBDataTable;
    private JPanel _panel;
    private JButton _connectButton;

    private JLabel _databaseLabel;
    private JTextField _database;

    private JLabel _queryLabel;
    private JTextArea _query;
    private JLabel _tableLabel;
    private JTextField _table;

    private JButton _executeButton;
    private JTextPane _errorDescription;


    public SQLDataBaseClientGUI() {
        super("SQL Database Client");

        _panel = new JPanel();
        _panel.setLayout(new BoxLayout(_panel, BoxLayout.PAGE_AXIS));

        dbDataTable = new JXTable();
        dbDataTable.setAutoCreateColumnsFromModel(true);
        dbDataTable.setVisibleColumnCount(5);
        dbDataTable.setColumnControlVisible(true);
        dbDataTable.setHorizontalScrollEnabled(true);
        dbDataTable.setShowVerticalLines(true);
        dbDataTable.revalidate();

        dbDataTable.getModel().addTableModelListener(new TableModelListener() {
            private SQLDataBaseClientGUI _owner;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.UPDATE)
                    return;
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (row == -1 || column == -1)
                    return;
                TableModel model = (TableModel) e.getSource();
                String columnName = model.getColumnName(column);

                Vector<String> filters = new Vector<>();
                for (int i = 0; i < model.getColumnCount(); ++i) {
                    if (i == column)
                        continue;
                    Object value = model.getValueAt(row, i);

                    String valueAsString;
                    if (value instanceof String)
                        valueAsString = "\'" + String.valueOf(value) + "\'";
                    else if (value == null)
                        valueAsString = "NULL";
                    else
                        valueAsString = String.valueOf(value);

                    filters.add(model.getColumnName(i) + " = " + valueAsString);
                }

                Object data = model.getValueAt(row, column);
                String valueAsString = data.toString();
                if (data instanceof String)
                    valueAsString = "\'" + valueAsString + "\'";
                try {
                    _owner._executeUpdatingQuery(
                            String.format("update %s set %s = %s where %s", _owner._table.getText(), columnName, valueAsString, String.join(" AND ", filters))
                    );
                } catch (QueryException queryException) {
                    _owner._setError(queryException.getMessage());
                }
            }

            private TableModelListener init(SQLDataBaseClientGUI owner) {
                _owner = owner;
                return this;
            }
        }.init(this));

        _scrollPaneForDBDataTable = new JScrollPane(dbDataTable);
        // Force the scrollbars to always be displayed
        _scrollPaneForDBDataTable.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        _scrollPaneForDBDataTable.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        _scrollPaneForDBDataTable.setWheelScrollingEnabled(true);
        _scrollPaneForDBDataTable.setSize(new Dimension(100, 100));

        _scrollPaneForDBDataTable.revalidate();

        _databaseLabel = new JLabel("data base");
        _database = new JTextField("jdbc:sqlite:E:/projects/collections/src/main/resources/sqllite.db");
        _connectButton = new JButton("connect");

        _queryLabel = new JLabel("query");
        _query = new JTextArea("select * from user");
        _executeButton = new JButton("execute");
        _tableLabel = new JLabel("table");
        _table = new JTextField("user");

        _errorDescription = new JTextPane();
        _errorDescription.setEditable(false);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        _errorDescription.setCharacterAttributes(aset, false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        _connectButton.addActionListener(е -> this._onConnect());
        _executeButton.addActionListener(e -> this._onExecuteQuery());

        add(_panel);
        _panel.add(_scrollPaneForDBDataTable);

        _panel.add(_databaseLabel);
        _panel.add(_database);
        _panel.add(_connectButton);

        _panel.add(_queryLabel);
        _panel.add(_query);
        _panel.add(_tableLabel);
        _panel.add(_table);
        _panel.add(_executeButton);

        _panel.add(_errorDescription);

        _panel.setVisible(true);

        dbDataTable.setVisible(true);
        _scrollPaneForDBDataTable.setVisible(true);
        _databaseLabel.setVisible(true);
        _database.setVisible(true);

        _connectButton.setVisible(true);
        _errorDescription.setVisible(false);

        _tableLabel.setVisible(false);
        _table.setVisible(false);
        _queryLabel.setVisible(false);
        _query.setVisible(false);
        _executeButton.setVisible(false);

        setVisible(true);

        pack();
    }

    private void _onConnect() {
        _resetError();
        try {
            if (_database.getText().trim().toLowerCase().substring(0, 11).equals("jdbc:sqlite"))
                _connection = new SqlDBConnection();
            else
                throw new ConnectionException("Unsupported database type.\nSupported types: \"jdbc:sqlite\"");

            _connection.connect(_database.getText());
            _connectButton.setVisible(false);
            _databaseLabel.setVisible(false);
            _database.setVisible(false);

            _queryLabel.setVisible(true);
            _query.setVisible(true);
            _tableLabel.setVisible(true);
            _table.setVisible(true);
            _executeButton.setVisible(true);
        } catch (ConnectionException e) {
            _setError(e.toString());
        }
    }

    private void _onExecuteQuery() {
        _resetError();
        var queryText = _query.getText();
        queryText = queryText.trim();
        try {
            if (queryText.length() < 9)
                throw new QueryException("Query text contains less then 9 symbols.");

            if (queryText.toLowerCase().substring(0, 6).equals("select")) {
                _executeSelectionQuery(_query.getText());
                var parts = queryText.split(" ");
                _table.setText(parts[parts.length - 1]);
            } else
                _executeUpdatingQuery(_query.getText());
        } catch (QueryException | SQLException e) {
            _setError(e.toString());
        }
    }

    private void _executeSelectionQuery(String query) throws QueryException, SQLException {
        var result = _connection.executeQuery(query);
        var model = (DefaultTableModel) dbDataTable.getModel();

        _clearDBDataTable();

        ResultSetMetaData metaData = result.getMetaData();
        // add columns
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            model.addColumn(metaData.getColumnName(i));
        }
        // add rows
        while (result.next()) {
            Vector<Object> columnsValues = new Vector<>();
            for (int column = 1; column <= metaData.getColumnCount(); ++column) {
                columnsValues.add(result.getObject(column));
            }
            model.addRow(columnsValues);
        }

        model.fireTableStructureChanged();
    }

    private void _executeUpdatingQuery(String query) throws QueryException {
        _connection.executeUpdate(query);
    }

    private void _clearDBDataTable() {
        var model = (DefaultTableModel) dbDataTable.getModel();
        for (int i = 0; i < dbDataTable.getColumnCount(); ++i) {
            var columnExt = dbDataTable.getColumnExt(i);
            columnExt.setVisible(true);
            columnExt.setHideable(false);
        }
        model.fireTableStructureChanged();

        model.setColumnCount(0);
        model.fireTableStructureChanged();
        if (dbDataTable.getColumnCount() != 0) {
            var columnModel = dbDataTable.getColumnModel();
            int columns = dbDataTable.getColumnCount();
            for (int column = 0; column < columns; ++column)
                columnModel.removeColumn(columnModel.getColumn(0));
            columnModel.notify();
        }
        if (dbDataTable.getRowCount() != 0) {
            int rows = dbDataTable.getRowCount();
            for (int row = 0; row < rows; ++row)
                model.removeRow(0);
            model.fireTableStructureChanged();
        }
    }

    private void _setError(String errorDescription) {
        _errorDescription.setText(errorDescription);
        _errorDescription.setVisible(true);
        pack();
    }

    private void _resetError() {
        _errorDescription.setVisible(false);
        _errorDescription.setText("");
    }
}
