package service.endpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;
import operation.ClientOperation;
import types.Client;

@WebService
public class ClientService implements ClientOperation {

    private static Connection connection;

    public ClientService(Connection connection) {
        ClientService.connection = connection;
    }

    @WebMethod()
    @Override
    public List<Client> getListOfClients() {

        CompletableFuture completableFuture = CompletableFuture.supplyAsync(this::ListClientsAsync);

        try {
            List<Client> result = (List<Client>) completableFuture.get();
            return result;
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<Client> ListClientsAsync() {

        List listClients = new ArrayList<Client>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT "
                    + "id, "
                    + "login, "
                    + "hash_password, "
                    + "second_name, "
                    + "first_name, "
                    + "middle_name, "
                    + "date_birthday, "
                    + "phone_number, "
                    + "passport_data, "
                    + "drivers_license, "
                    + "email, "
                    + "is_blocked FROM client WHERE is_blocked = false");
            while (result.next()) {
                listClients.add(new Client(
                        result.getLong("id"),
                        result.getString("login"),
                        result.getString("hash_password"),
                        result.getString("second_name"),
                        result.getString("first_name"),
                        result.getString("middle_name"),
                        result.getDate("date_birthday"),
                        result.getString("phone_number"),
                        result.getString("passport_data"),
                        result.getString("drivers_license"),
                        result.getString("email"),
                        result.getBoolean("is_blocked")
                ));
            }
            System.out.println("Получен список клиентов");
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        return listClients;
    }

    @WebMethod()
    @Override
    public Long findClientByNumberPhone(String numberPhone) {
        Long idClient = 0l;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id FROM client "
                    + "WHERE is_blocked = false "
                    + "AND phone_number = '" + numberPhone + "'");
            result.next();
            idClient = result.getLong("id");
            System.out.println("Клиент найден");
            return idClient;

        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        return idClient;
    }
    
    @WebMethod()
    @Override
    public void updateClient(Client client) {
        CompletableFuture.runAsync(() -> {
            try {
                updateClientAsync(client);
            } catch (SQLException ex) {
                Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void updateClientAsync(Client client) throws SQLException {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE client SET "
                    + "second_name = ?, "
                    + "first_name = ?, "
                    + "middle_name = ?, "
                    + "passport_data = ?, "
                    + "drivers_license = ?, "
                    + "phone_number = ?, "
                    + "email = ? "
                    + "WHERE id = ?");
            statement.setString(1, client.getSecondName());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getMiddleName());
            statement.setString(4, client.getPassportData());
            statement.setString(5, client.getDriversLicense());
            statement.setString(6, client.getPhoneNumber());
            statement.setString(7, client.getEmail());
            statement.setLong(8, client.getId());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Клиент обновлен!");

            }
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
    }

    @WebMethod()
    @Override
    public void blockClient(Long id) {
        CompletableFuture.runAsync(() -> {
            try {
                blockClientAsync(id);
            } catch (SQLException ex) {
                Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void blockClientAsync(Long id) throws SQLException {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE client SET is_blocked = true WHERE id = ?");
            statement.setLong(1, id);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Клиент заблокирован!");
            }
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
    }
}
