
package DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientControl {
    private final Connection connection;

        //Constructeur pour ClientControl
        public ClientControl(Connection connection) {
            this.connection = connection;
        }

        //Methode d'ajout de client
        public void addClient(Client cli) {
            String query = "INSERT INTO client (numCompte, nom, prenoms, tel, mail, solde) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, cli.getNumCompte());
                pstmt.setString(2, cli.getNom());
                pstmt.setString(3, cli.getPrenoms());
                pstmt.setString(4, cli.getTel());
                pstmt.setString(5, cli.getMail());
                pstmt.setInt(6, cli.getSolde());
                pstmt.executeUpdate();
                System.out.println("Client ajouté avec succès");
            } catch (SQLException ex) {
                System.out.println("Erreur lors de l'ajout du client: " + ex);
            }
        }
 
}
