
package javaprojet;
import DatabaseConnection.DBConnect;


public class JavaProjet {

    public static void main(String[] args) {
    // Établir la connexion à la base de données
        DBConnect conn = new DBConnect();
        conn.dbConnection();

        // Créer une instance de ClientControl avec la connexion
       // ClientModel clientControl = new ClientModel(conn.connection);

       
       //Lancement de l'interface
       java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HomePage().setVisible(true);
            }
        });  
     
    }
}
