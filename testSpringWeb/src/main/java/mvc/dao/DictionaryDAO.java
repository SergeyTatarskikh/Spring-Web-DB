package mvc.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import mvc.models.Dictionary;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DictionaryDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/dictionaries_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "sergey_2811";
    private static Connection connection;
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получение списка всех словарей.
     */
    public static List<Dictionary> index() throws SQLException {
        List<Dictionary> dictionaries = new ArrayList<>();
        Statement statement = connection.createStatement();
        String SQL = "SELECT * FROM dictionaries";
        ResultSet resultSet = statement.executeQuery(SQL);

        while (resultSet.next()){
            Dictionary dictionary = new Dictionary();

            dictionary.setNumber(resultSet.getInt("number"));
            dictionary.setKey(resultSet.getString("key"));
            Array array = resultSet.getArray("values");
            String[] values = (String[]) array.getArray();
            dictionary.setValue(Arrays.asList(values));

            dictionaries.add(dictionary);
        }

        return dictionaries;
    }

    /**
     * Удаление словаря.
     */
    public void delete(Dictionary dictionary) throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM dictionaries WHERE key=?");
        preparedStatement.setString(1, dictionary.getKey());

        preparedStatement.executeUpdate();
    }

    /**
     * Сохранение словаря.
     */
    public void save(Dictionary dictionary) throws SQLException, JsonProcessingException {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("INSERT INTO dictionaries VALUES (?, ?, ?)");

            preparedStatement.setInt(1, dictionary.getNumber());
            preparedStatement.setString(2, dictionary.getKey());
            Array array = connection.createArrayOf("VARCHAR", dictionary.getValue().stream()
                    .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))
                    .toArray(String[]::new));
            preparedStatement.setArray(3, array);
            preparedStatement.executeUpdate();
        } catch (PSQLException e) {
            if (e.getMessage().contains("unique_number_key")) {
                System.out.println("Ошибка: словарь с таким номером и ключом уже существует");
            } else {
                System.out.println("Ошибка при сохранении словаря");
            }
        }
    }

    /**
     * Редактирование словаря.
     */
    public Dictionary edit(String key) throws SQLException {
        Dictionary dictionary = null;
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM dictionaries WHERE key = ?");

        preparedStatement.setString(1,key);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();

        dictionary = new Dictionary();
        dictionary.setKey(resultSet.getString("key"));
        Array array = resultSet.getArray("values");
        String[] values = (String[]) array.getArray();
        dictionary.setValue(Arrays.asList(values));

        return dictionary;
    }

    /**
     * Обновление словаря.
     */
    public void update(String key, Dictionary dictionary) throws SQLException {
        PreparedStatement preparedStatement;
        String selectQuery = "SELECT * FROM dictionaries WHERE key = ?";
        preparedStatement = connection.prepareStatement(selectQuery);
        preparedStatement.setString(1, dictionary.getKey());

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            preparedStatement = connection.prepareStatement("UPDATE dictionaries SET values=? WHERE key=? AND number=?");

            Array array = connection.createArrayOf("VARCHAR", dictionary.getValue().stream()
                    .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))
                    .toArray(String[]::new));
            preparedStatement.setArray(1, array);

            preparedStatement.setString(2, key);
            preparedStatement.setInt(3, dictionary.getNumber());
        } else {
            preparedStatement = connection.prepareStatement("INSERT INTO dictionaries VALUES (?, ?, ?)");

            preparedStatement.setInt(1, dictionary.getNumber());
            preparedStatement.setString(2, dictionary.getKey());
            Array array = connection.createArrayOf("VARCHAR", dictionary.getValue().stream()
                    .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))
                    .toArray(String[]::new));
            preparedStatement.setArray(3, array);
        }

        preparedStatement.executeUpdate();
    }

    /**
     * Поиск значения в словаре по ключу и номеру.
     */
    public List<String> searchValue(String key, int number) throws SQLException {
        PreparedStatement preparedStatement;

        if(number == 1) {
            String searchItem = "SELECT * FROM dictionaries WHERE key = ? AND number = 1";
            preparedStatement = connection.prepareStatement(searchItem);
        } else if(number == 2) {
            String searchItem = "SELECT * FROM dictionaries WHERE key = ? AND number = 2";
            preparedStatement = connection.prepareStatement(searchItem);
        } else {
            String searchItem = "SELECT * FROM dictionaries WHERE key = ?";
            preparedStatement = connection.prepareStatement(searchItem);
        }

        preparedStatement.setString(1, key);

        ResultSet resultSet = preparedStatement.executeQuery();

        List<String> resultList = new ArrayList<>();

        if (resultSet.next()) {
            String value = resultSet.getString("values");
            String filteredValue = value.replaceAll("[^a-zA-Z0-9,]", "");
            resultList.add(filteredValue);
        }

        return resultList;
    }

    /**
     * Получение ключа по значению и номеру.
     */
    public String getKeyByValueAndNumber(String value, int number) throws SQLException {
        PreparedStatement statement;
        if (number==3){
            String searchItem = "SELECT * FROM dictionaries WHERE ? = ANY (values)";
            statement = connection.prepareStatement(searchItem);
            statement.setString(1, value);
        } else {
            String searchItem = "SELECT * FROM dictionaries WHERE ? = ANY (values) AND number = ?";
            statement = connection.prepareStatement(searchItem);
            statement.setString(1, value);
            statement.setInt(2, number);
        }

        ResultSet resultSet = statement.executeQuery();
        String key = null;
        if (resultSet.next()) {
            key = resultSet.getString("key");
        }
        resultSet.close();
        statement.close();
        return key;
    }
}
