package Model;

import javaprojet.HomePage;
import DatabaseConnection.Client;
import DatabaseConnection.DBConnect;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static javaprojet.HomePage.jComboBoxNumCompteBeneficiaire;
import static javaprojet.HomePage.jComboBoxNumCompteEnvoyeur;
import static javaprojet.HomePage.jComboBoxNumComptePret;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class ClientModel {

    private final DBConnect dbConnect;


    /*public ClientModel(){
       
   }*/
    public ClientModel(DBConnect dbConnect) {
        this.dbConnect = dbConnect;
        ensureTableExists();
        virementTableExists();
        pretTableExists();
        rendreTableExists();
    }

    private void ensureTableExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS client ("
                + "numCompte VARCHAR(255) PRIMARY KEY,"
                + "Nom VARCHAR(255),"
                + "Prenoms VARCHAR(255),"
                + "Tel VARCHAR(50),"
                + "Mail VARCHAR(255),"
                + "Solde DOUBLE PRECISION)";
        try {
            PreparedStatement pstmt = dbConnect.connection.prepareStatement(createTableQuery);
            pstmt.execute();
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la création de la table client: " + ex);
        }
    }

    public void addClient(Client cli) {
        String query = "INSERT INTO client (numCompte, Nom, Prenoms, Tel, Mail, Solde) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, cli.getNumCompte());
            pstmt.setString(2, cli.getNom());
            pstmt.setString(3, cli.getPrenoms());
            pstmt.setString(4, cli.getTel());
            pstmt.setString(5, cli.getMail());
            pstmt.setDouble(6, cli.getSolde()); // Utiliser setDouble pour Solde
            pstmt.executeUpdate();
            table();
            System.out.println("Client ajouté avec succès");
            JOptionPane.showMessageDialog(null, "Client ajouté avec succes");

        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'ajout du client: " + ex);
            JOptionPane.showMessageDialog(null, "Le numCompte existe déjà, veuillez ajouter un nouveau" + ex);
        }
        loadNumCompteEnvoyeur();
    }

    public void table() {
        ResultSet rs;
        String[] client = {"numCompte", "Nom", "Prenoms", "Tel", "Mail", "Solde"};
        DefaultTableModel model = new DefaultTableModel(null, client);
        try {
            Statement st = dbConnect.connection.createStatement();
            rs = st.executeQuery("SELECT * FROM client");
            while (rs.next()) {
                // Conversion des valeurs en String
                String numCompte = rs.getString("numCompte");
                String nom = rs.getString("Nom");
                String prenoms = rs.getString("Prenoms");
                String tel = rs.getString("Tel");
                String mail = rs.getString("Mail");
                String solde = String.valueOf(rs.getDouble("Solde")); // Convertir Double en String

                // Débogage : Vérifiez les valeurs récupérées
                // System.out.println("Récupéré: " + numCompte + ", " + nom + ", " + prenoms + ", " + tel + ", " + mail + ", " + solde);
                model.addRow(new String[]{numCompte, nom, prenoms, tel, mail, solde});
            }

            HomePage.jTableCli.setModel(model);

            // Ajouter un écouteur de sélection
            HomePage.jTableCli.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) { // Pour éviter les appels multiples lors de la sélection
                    int selectedRow = HomePage.jTableCli.getSelectedRow();
                    if (selectedRow != -1) {
                        // Récupérer les données de la ligne sélectionnée
                        String numCompte = (String) model.getValueAt(selectedRow, 0);
                        String nom = (String) model.getValueAt(selectedRow, 1);
                        String prenoms = (String) model.getValueAt(selectedRow, 2);
                        String tel = (String) model.getValueAt(selectedRow, 3);
                        String mail = (String) model.getValueAt(selectedRow, 4);
                        String solde = (String) model.getValueAt(selectedRow, 5); // Déjà une String

                        // Mettre à jour les champs de texte
                        HomePage.jTextFieldNumCompte.setText(numCompte);
                        HomePage.jTextFieldNom.setText(nom);
                        HomePage.jTextFieldPrenoms.setText(prenoms);
                        HomePage.jTextFieldTel.setText(tel);
                        HomePage.jTextFieldMail.setText(mail);
                        HomePage.jTextFieldMontant.setText(solde);
                    }
                }
            });

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'exécution de la requête: " + e.getMessage());
        }
    }

    public void updateClient(String numCompte, String nom, String prenoms, String tel, String mail, String solde) {
        String query = "UPDATE client SET Nom = ?, Prenoms = ?, Tel = ?, Mail = ?, Solde = ? WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, prenoms);
            pstmt.setString(3, tel);
            pstmt.setString(4, mail);
            pstmt.setDouble(5, Double.parseDouble(solde)); // assuming solde is a double
            pstmt.setString(6, numCompte);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Client mis à jour avec succès");
                JOptionPane.showMessageDialog(null, "Client mis à jour avec succès");
                table(); // rafraîchir la table
            } else {
                System.out.println("Aucun client trouvé avec ce numéro de compte");
                JOptionPane.showMessageDialog(null, "Aucun client trouvé avec ce numéro de compte");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la mise à jour du client: " + ex);
        }
    }

    public void deleteClient(String numCompte) {
    String deleteClientQuery = "DELETE FROM client WHERE numCompte = ?";
    String deleteVirementQuery = "DELETE FROM virement WHERE numCompte = ? OR numCompteB = ?";
    String deletePretQuery = "DELETE FROM preter WHERE numCompte = ?";
    String deleteRendreQuery = "DELETE FROM rendre WHERE num_pret IN (SELECT num_pret FROM preter WHERE numCompte = ?)";

    try (PreparedStatement pstmtClient = dbConnect.connection.prepareStatement(deleteClientQuery);
         PreparedStatement pstmtVirement = dbConnect.connection.prepareStatement(deleteVirementQuery);
         PreparedStatement pstmtPret = dbConnect.connection.prepareStatement(deletePretQuery);
         PreparedStatement pstmtRendre = dbConnect.connection.prepareStatement(deleteRendreQuery)) {

        // Début de la transaction
        dbConnect.connection.setAutoCommit(false);

        // Supprimer les virements
        pstmtVirement.setString(1, numCompte);
        pstmtVirement.setString(2, numCompte);
        pstmtVirement.executeUpdate();

        // Supprimer les remboursements
        pstmtRendre.setString(1, numCompte);
        pstmtRendre.executeUpdate();

        // Supprimer les prêts
        pstmtPret.setString(1, numCompte);
        pstmtPret.executeUpdate();

        // Supprimer le client
        pstmtClient.setString(1, numCompte);
        int rowsAffected = pstmtClient.executeUpdate();

        if (rowsAffected > 0) {
            dbConnect.connection.commit();
            System.out.println("Client supprimé avec succès");
            JOptionPane.showMessageDialog(null, "Client supprimé avec succès");
            table(); // rafraîchir la table
            virementTable();
            tablePret();
            tableRendre(getSelectedSituation());
        } else {
            dbConnect.connection.rollback();
            System.out.println("Aucun client trouvé avec ce numéro de compte");
            JOptionPane.showMessageDialog(null, "Aucun client trouvé avec ce numéro de compte");
        }

    } catch (SQLException ex) {
        try {
            dbConnect.connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Erreur lors de la suppression du client: " + ex);
    } finally {
        try {
            dbConnect.connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    loadNumCompteEnvoyeur();
}


    //récupération des numCompte dans Client
    public static List<String> getAllNumCompte(DBConnect dbConnect) {
        List<String> numComptes = new ArrayList<>();
        String query = "SELECT numCompte FROM client";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }
        try (Statement stmt = dbConnect.connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                numComptes.add(rs.getString("numCompte"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des numCompte: " + e);
        }

        return numComptes;
    }

    //Chargement de la liste déroulante pour numCompte
    public void loadNumCompteEnvoyeur() {
        // Remove the ActionListener temporarily to avoid triggering the event for the envoyeur JComboBox
        ActionListener[] envoyeurListeners = jComboBoxNumCompteEnvoyeur.getActionListeners();
        for (ActionListener listener : envoyeurListeners) {
            jComboBoxNumCompteEnvoyeur.removeActionListener(listener);
        }

        // Remove the ActionListener temporarily to avoid triggering the event for the bénéficiaire JComboBox
        ActionListener[] beneficiaireListeners = jComboBoxNumCompteBeneficiaire.getActionListeners();
        for (ActionListener listener : beneficiaireListeners) {
            jComboBoxNumCompteBeneficiaire.removeActionListener(listener);
        }
        // Remove the ActionListener temporarily to avoid triggering the event for the numCompte in lender
        ActionListener[] numComptePret = jComboBoxNumComptePret.getActionListeners();
        for (ActionListener listener : numComptePret) {
            jComboBoxNumComptePret.removeActionListener(listener);
        }

        // Clear the JComboBox before adding items
        jComboBoxNumCompteEnvoyeur.removeAllItems();
        jComboBoxNumCompteBeneficiaire.removeAllItems();
        jComboBoxNumComptePret.removeAllItems();
        DBConnect conn = new DBConnect();
        conn.dbConnection();
        List<String> numComptes = ClientModel.getAllNumCompte(conn);
        for (String numCompte : numComptes) {
            HomePage.jComboBoxNumCompteEnvoyeur.addItem(numCompte);
            HomePage.jComboBoxNumCompteBeneficiaire.addItem(numCompte);
            HomePage.jComboBoxNumComptePret.addItem(numCompte);
        }

        // Add the ActionListeners back for the envoyeur JComboBox
        for (ActionListener listener : envoyeurListeners) {
            jComboBoxNumCompteEnvoyeur.addActionListener(listener);
        }

        // Add the ActionListeners back for the bénéficiaire JComboBox
        for (ActionListener listener : beneficiaireListeners) {
            jComboBoxNumCompteBeneficiaire.addActionListener(listener);
        }
        // Add the ActionListeners back for the bénéficiaire JComboBox
        for (ActionListener listener : beneficiaireListeners) {
            jComboBoxNumComptePret.addActionListener(listener);
        }
    }

    //Création de la table virement si elle n'existe pas encore
    private void virementTableExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS virement ("
                + "numCompte VARCHAR(255),"
                + "numCompteB VARCHAR(255),"
                + "montant DOUBLE PRECISION,"
                + "dateTransfert TIMESTAMP,"
                + "PRIMARY KEY (numCompte, numCompteB))";

        DBConnect conn = new DBConnect();
        conn.dbConnection();

        try (PreparedStatement pstmt = conn.connection.prepareStatement(createTableQuery)) {
            pstmt.execute();
            //  System.out.println("Table VIREMENT vérifiée ou créée avec succès.");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la création de la table VIREMENT: " + ex);
        }
    }

    public void addVirement(DBConnect dbConnect, String numCompteEnvoyeur, String numCompteBeneficiaire, double montant) throws DocumentException {

        //si l'envoyeur est égal au bénéficiaire
        if (numCompteEnvoyeur.equals(numCompteBeneficiaire)) {
            JOptionPane.showMessageDialog(null, "Vous ne pouvez pas faire un virement vers votre propre compte. Veuillez changer le Bénéficiaire ou l'envoyeur.", "Erreur de Virement", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si le virement existe déjà
        if (virementExisteDeja(dbConnect, numCompteEnvoyeur, numCompteBeneficiaire)) {
            JOptionPane.showMessageDialog(null, "Ce virement existe déjà.", "Erreur de Virement", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "INSERT INTO virement (numCompte, numCompteB, montant, dateTransfert) VALUES (?, ?, ?, ?)";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompteEnvoyeur);
            pstmt.setString(2, numCompteBeneficiaire);
            pstmt.setDouble(3, montant);
            pstmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();

            // Récupérer les informations sur l'envoyeur et le bénéficiaire
            String nomEnvoyeur = getClientName(dbConnect, numCompteEnvoyeur);
            String prenomEnvoyeur = getClientPrenoms(dbConnect, numCompteEnvoyeur);
            String nomBeneficiaire = getClientName(dbConnect, numCompteBeneficiaire);
            String prenomBeneficiaire = getClientPrenoms(dbConnect, numCompteBeneficiaire);
            Timestamp dateTransfert = getTransferDateFromDatabase(dbConnect, numCompteEnvoyeur, numCompteBeneficiaire);
            Double soldeEnvoyeur = getClientSolde(dbConnect, numCompteEnvoyeur);

            // Générer le PDF correspondant au virement
            genererVirement(dateTransfert, numCompteEnvoyeur, "005", nomEnvoyeur, prenomEnvoyeur, soldeEnvoyeur, numCompteBeneficiaire, nomBeneficiaire, prenomBeneficiaire, montant);

            JOptionPane.showMessageDialog(null, "Virement ajouté avec succès");
            
            virementTable();
            table();//pour actualiser en temps réel l'affichage de la table client
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du virement: " + e);
        }
    }

    // Méthode pour vérifier si le virement existe déjà dans la base de données
    private boolean virementExisteDeja(DBConnect dbConnect, String numCompteEnvoyeur, String numCompteBeneficiaire) {
        String query = "SELECT COUNT(*) AS count FROM virement WHERE numCompte = ? AND numCompteB = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompteEnvoyeur);
            pstmt.setString(2, numCompteBeneficiaire);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de l'existence du virement: " + e);
            JOptionPane.showMessageDialog(null, "Le virement entre ces deux clients existe déjà.\n Veuillez simplement modifier le montant du virement si nécessaire!");
        }
        return false;
    }

    // Méthode hypothétique pour récupérer la date de transfert depuis la base de données
    private Timestamp getTransferDateFromDatabase(DBConnect dbConnect, String numCompteEnvoyeur, String numCompteBeneficiaire) {
        String query = "SELECT dateTransfert FROM virement WHERE numCompte = ? AND numCompteB = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompteEnvoyeur);
            pstmt.setString(2, numCompteBeneficiaire);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("dateTransfert");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la date de transfert depuis la base de données : " + e);
        }
        return null;
    }

    // Méthode pour récupérer le solde du client à partir de son numéro de compte
    public Double getClientSolde(DBConnect dbConnect, String numCompte) {
        String query = "SELECT solde FROM client WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompte);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("solde");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du solde du client: " + e);
        }
        return 0.0; // Valeur par défaut si la récupération échoue
    }

    // Méthode pour générer le PDF de virement
    public void genererVirement(Date dateVir, String numcompteE, String numVir, String nomEnvoyeur, String prenomEnv, Double solde,
            String numCompteB, String nomBenef, String prenomBenef, Double montant) {

        // Nom du fichier PDF à générer
        String filename = "Virement de " + nomEnvoyeur + " " + prenomEnv + ".pdf";

        // Formater la date pour l'afficher sans l'heure
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateTransfert = sdf.format(dateVir);

        // Contenu du PDF
        String entete = "Banque Tosika \n Date:" + dateTransfert + " \n AVIS DE VIREMENT N°" + numVir;
        String suivant = "N° de compte :" + numcompteE + "\n" + nomEnvoyeur + " " + prenomEnv + "\nSolde actuel: " + solde + "Ar";
        String next = "N° de compte :" + numCompteB + "\n " + nomBenef + " " + prenomBenef;
        String ambany = "Montant:" + montant;

        // Générer le PDF
        Pdf.generatePDF(filename, entete, suivant, next, ambany);
    }

    // Méthode pour récupérer le nom du client à partir de son numéro de compte
    private String getClientName(DBConnect dbConnect, String numCompte) {
        String query = "SELECT nom FROM client WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompte);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du nom du client: " + e);
        }
        return "";
    }

    private String getClientPrenoms(DBConnect dbConnect, String numCompte) {
        String query = "SELECT prenoms FROM client WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompte);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prenoms");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des prénoms du client: " + e);
        }
        return "";
    }

    //Update solde dans client après un virement
    public boolean updateSolde(DBConnect dbConnect, String numCompteEnvoyeur, String numCompteBeneficiaire, double montant) {
        String querySelect = "SELECT solde, nom, prenoms FROM client WHERE numCompte = ?";
        String queryUpdateEnvoyeur = "UPDATE client SET solde = solde - ? WHERE numCompte = ?";
        String queryUpdateBeneficiaire = "UPDATE client SET solde = solde + ? WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try (PreparedStatement pstmtSelect = dbConnect.connection.prepareStatement(querySelect); PreparedStatement pstmtUpdateEnvoyeur = dbConnect.connection.prepareStatement(queryUpdateEnvoyeur); PreparedStatement pstmtUpdateBeneficiaire = dbConnect.connection.prepareStatement(queryUpdateBeneficiaire)) {

            // Vérifier le solde de l'envoyeur
            pstmtSelect.setString(1, numCompteEnvoyeur);
            ResultSet rs = pstmtSelect.executeQuery();
            if (rs.next()) {
                double soldeEnvoyeur = rs.getDouble("solde");
                String nomEnvoyeur = rs.getString("nom");
                String prenomEnvoyeur = rs.getString("prenoms");

                if (soldeEnvoyeur < montant) {
                    JOptionPane.showMessageDialog(null, "Le solde du compte " + numCompteEnvoyeur + " (" + nomEnvoyeur + " " + prenomEnvoyeur + ") est insuffisant.", "Solde Insuffisant", JOptionPane.ERROR_MESSAGE);
                    return false; // Solde insuffisant
                }

                // Mettre à jour le solde de l'envoyeur
                pstmtUpdateEnvoyeur.setDouble(1, montant);
                pstmtUpdateEnvoyeur.setString(2, numCompteEnvoyeur);
                pstmtUpdateEnvoyeur.executeUpdate();

                // Mettre à jour le solde du bénéficiaire
                pstmtUpdateBeneficiaire.setDouble(1, montant);
                pstmtUpdateBeneficiaire.setString(2, numCompteBeneficiaire);
                pstmtUpdateBeneficiaire.executeUpdate();
                virementTable(); // Appeler pour actualiser les données de la table après l'ajout d'un virement

                return true; 
            } else {
                JOptionPane.showMessageDialog(null, "Le compte de l'envoyeur n'a pas été trouvé.", "Erreur de Virement", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour des soldes: " + e);
            return false;
        }
    }

    public static void loadClientData(DefaultTableModel tableModel, DBConnect dbConnect) {
        String query = "SELECT * FROM client";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try (Statement stmt = dbConnect.connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            // Clear existing data
            tableModel.setRowCount(0);

            while (rs.next()) {
                String numCompte = rs.getString("numCompte");
                String nom = rs.getString("nom");
                String prenoms = rs.getString("prenoms");
                double solde = rs.getDouble("solde");

                tableModel.addRow(new Object[]{numCompte, nom, prenoms, solde});
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des données client: " + e);
        }
    }

    //affichage de la table virement
    public void virementTable() {
        ResultSet rs;
        String[] vir = {"numCompte Envoyeur", "Nom Envoyeur", "Prenoms Envoyeur", "numCompte Beneficiaire", "Nom Beneficiaire", "Prenoms Beneficiaire", "Montant", "DateTransfert"};
        String[] afficher = new String[8];
        DefaultTableModel model = new DefaultTableModel(null, vir);

        try {
            Statement st = dbConnect.connection.createStatement();
            String query = "SELECT v.numCompte, c1.nom AS nomEnvoyeur, c1.prenoms AS prenomsEnvoyeur, "
                    + "v.numCompteB, c2.nom AS nomBeneficiaire, c2.prenoms AS prenomsBeneficiaire, "
                    + "v.montant, v.dateTransfert "
                    + "FROM virement v "
                    + "JOIN client c1 ON v.numCompte = c1.numCompte "
                    + "JOIN client c2 ON v.numCompteB = c2.numCompte";
            rs = st.executeQuery(query);

            while (rs.next()) {
                afficher[0] = rs.getString("numCompte");
                afficher[1] = rs.getString("nomEnvoyeur");
                afficher[2] = rs.getString("prenomsEnvoyeur");
                afficher[3] = rs.getString("numCompteB");
                afficher[4] = rs.getString("nomBeneficiaire");
                afficher[5] = rs.getString("prenomsBeneficiaire");
                afficher[6] = rs.getString("montant");
                afficher[7] = rs.getString("dateTransfert");
                model.addRow(afficher);
            }

            HomePage.jTableVirement.setModel(model);

            // Ajouter un écouteur de sélection
            HomePage.jTableVirement.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) { // Pour éviter les appels multiples lors de la sélection
                    int selectedRow = HomePage.jTableVirement.getSelectedRow();
                    if (selectedRow != -1) {
                        // Récupérer les données de la ligne sélectionnée
                        String numCompte = (String) model.getValueAt(selectedRow, 0);
                        String numCompteB = (String) model.getValueAt(selectedRow, 3);
                        String montant = (String) model.getValueAt(selectedRow, 6);

                        // Mettre à jour les champs de texte
                        HomePage.jComboBoxNumCompteEnvoyeur.setSelectedItem(numCompte);
                        HomePage.jComboBoxNumCompteBeneficiaire.setSelectedItem(numCompteB);
                        HomePage.jTextFieldMontantVirement.setText(montant);

                        // Désactiver la modification de jComboBoxNumCompteEnvoyeur
                        HomePage.jComboBoxNumCompteEnvoyeur.setEnabled(false);
                    }
                }
            });

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des données de virement: " + e);
        }
    }

    //modification de le table virement
    public void updateVirement(DBConnect dbConnect, String newNumCompteBeneficiaire, double newMontant) {
        String querySelect = "SELECT montant, numCompte FROM virement WHERE numCompteB = ?";
        String queryUpdateVirement = "UPDATE virement SET montant = ? WHERE numCompte = ? AND numCompteB = ?";
        String queryUpdateSoldeEnvoyeur = "UPDATE client SET solde = solde - ? WHERE numCompte = ?";
        String queryUpdateSoldeBeneficiaire = "UPDATE client SET solde = solde + ? WHERE numCompte = ?";
        String queryUpdateSoldeEnvoyeurReverse = "UPDATE client SET solde = solde + ? WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try (PreparedStatement pstmtSelect = dbConnect.connection.prepareStatement(querySelect); PreparedStatement pstmtUpdateVirement = dbConnect.connection.prepareStatement(queryUpdateVirement); PreparedStatement pstmtUpdateSoldeEnvoyeur = dbConnect.connection.prepareStatement(queryUpdateSoldeEnvoyeur); PreparedStatement pstmtUpdateSoldeBeneficiaire = dbConnect.connection.prepareStatement(queryUpdateSoldeBeneficiaire); PreparedStatement pstmtUpdateSoldeEnvoyeurReverse = dbConnect.connection.prepareStatement(queryUpdateSoldeEnvoyeurReverse)) {

            // Récupérer le montant actuel du virement et le numéro de compte envoyeur
            pstmtSelect.setString(1, newNumCompteBeneficiaire);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                double currentMontant = rs.getDouble("montant");
                String numCompteEnvoyeur = rs.getString("numCompte");

                // Calculer la différence de montant
                double differenceMontant = newMontant - currentMontant;

                // Commencer une transaction
                dbConnect.connection.setAutoCommit(false);

                // Mettre à jour le virement avec le nouveau montant 
                pstmtUpdateVirement.setDouble(1, newMontant);
                pstmtUpdateVirement.setString(2, numCompteEnvoyeur); // Utiliser numCompteEnvoyeur
                pstmtUpdateVirement.setString(3, newNumCompteBeneficiaire); // Utiliser newNumCompteBeneficiaire
                pstmtUpdateVirement.executeUpdate();

                // Mettre à jour le solde de l'envoyeur
                if (differenceMontant != 0) {
                    if (differenceMontant > 0) {
                        // Augmentation du montant envoyé
                        pstmtUpdateSoldeEnvoyeur.setDouble(1, differenceMontant);
                        pstmtUpdateSoldeEnvoyeur.setString(2, numCompteEnvoyeur);
                        pstmtUpdateSoldeEnvoyeur.executeUpdate();
                    } else {
                        // Réduire le montant envoyé
                        pstmtUpdateSoldeEnvoyeurReverse.setDouble(1, -differenceMontant);
                        pstmtUpdateSoldeEnvoyeurReverse.setString(2, numCompteEnvoyeur);
                        pstmtUpdateSoldeEnvoyeurReverse.executeUpdate();
                    }
                }

                // Mettre à jour le solde du bénéficiaire uniquement si le montant a changé
                if (differenceMontant != 0) {
                    pstmtUpdateSoldeBeneficiaire.setDouble(1, differenceMontant);
                    pstmtUpdateSoldeBeneficiaire.setString(2, newNumCompteBeneficiaire); // Utiliser newNumCompteBeneficiaire
                    pstmtUpdateSoldeBeneficiaire.executeUpdate();
                }

                // Valider la transaction
                dbConnect.connection.commit();

                System.out.println("Virement mis à jour avec succès");
                JOptionPane.showMessageDialog(null, "Virement mis à jour avec succès");
                virementTable(); // Mettre à jour l'affichage de la table des virements
                table(); // Mettre à jour l'affichage de la table des clients
            } else {
                JOptionPane.showMessageDialog(null, "Virement non trouvé", "Erreur de Virement", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la mise à jour du virement: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour du virement", "Erreur", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                if (dbConnect.connection != null) {
                    dbConnect.connection.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    //suppression virement
    public void deleteVirement(DBConnect dbConnect, String numCompteEnvoyeur, String numCompteBeneficiaire) {
        String querySelect = "SELECT montant FROM virement WHERE numCompte = ? AND numCompteB = ?";
        String queryDeleteVirement = "DELETE FROM virement WHERE numCompte = ? AND numCompteB = ?";
        String queryUpdateSoldeEnvoyeur = "UPDATE client SET solde = solde + ? WHERE numCompte = ?";
        String queryUpdateSoldeBeneficiaire = "UPDATE client SET solde = solde - ? WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try (PreparedStatement pstmtSelect = dbConnect.connection.prepareStatement(querySelect); PreparedStatement pstmtDeleteVirement = dbConnect.connection.prepareStatement(queryDeleteVirement); PreparedStatement pstmtUpdateSoldeEnvoyeur = dbConnect.connection.prepareStatement(queryUpdateSoldeEnvoyeur); PreparedStatement pstmtUpdateSoldeBeneficiaire = dbConnect.connection.prepareStatement(queryUpdateSoldeBeneficiaire)) {

            // Récupérer le montant du virement
            pstmtSelect.setString(1, numCompteEnvoyeur);
            pstmtSelect.setString(2, numCompteBeneficiaire);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                double montant = rs.getDouble("montant");

                // Commencer une transaction
                dbConnect.connection.setAutoCommit(false);

                // Supprimer le virement
                pstmtDeleteVirement.setString(1, numCompteEnvoyeur);
                pstmtDeleteVirement.setString(2, numCompteBeneficiaire);
                int rowsAffected = pstmtDeleteVirement.executeUpdate();

                if (rowsAffected > 0) {
                    // Mettre à jour le solde de l'envoyeur
                    pstmtUpdateSoldeEnvoyeur.setDouble(1, montant);
                    pstmtUpdateSoldeEnvoyeur.setString(2, numCompteEnvoyeur);
                    pstmtUpdateSoldeEnvoyeur.executeUpdate();

                    // Mettre à jour le solde du bénéficiaire
                    pstmtUpdateSoldeBeneficiaire.setDouble(1, montant);
                    pstmtUpdateSoldeBeneficiaire.setString(2, numCompteBeneficiaire);
                    pstmtUpdateSoldeBeneficiaire.executeUpdate();

                    // Valider la transaction
                    dbConnect.connection.commit();

                    System.out.println("Virement supprimé avec succès");
                    JOptionPane.showMessageDialog(null, "Virement supprimé avec succès");
                    virementTable(); // rafraîchir la table des virements
                    table(); // rafraîchir la table des clients
                } else {
                    System.out.println("Aucun virement trouvé avec ce numéro de compte");
                    JOptionPane.showMessageDialog(null, "Aucun virement trouvé avec ce numéro de compte");
                }
            } else {
                System.out.println("Aucun virement trouvé avec ce numéro de compte");
                JOptionPane.showMessageDialog(null, "Aucun virement trouvé avec ce numéro de compte");
            }

        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la suppression du virement: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression du virement", "Erreur", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    // Création de la table preter si elle n'existe pas encore
    private void pretTableExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS preter ("
                + "num_pret VARCHAR(255),"
                + "numCompte VARCHAR(255),"
                + "montant_prete DOUBLE PRECISION,"
                + "datePret TIMESTAMP,"
                + "PRIMARY KEY (num_pret, numCompte))";

        DBConnect conn = new DBConnect();
        conn.dbConnection();

        try (PreparedStatement pstmt = conn.connection.prepareStatement(createTableQuery)) {
            pstmt.execute();
            // System.out.println("Table VIREMENT vérifiée ou créée avec succès.");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la création de la table VIREMENT: " + ex);
        }
    }

    public void tablePret() {
        ResultSet rs;

        String[] columns = {"num_pret", "numCompte", "Nom", "Prenoms", "Montant prete", "datePret"};
        DefaultTableModel model = new DefaultTableModel(null, columns);

        try {
            Statement st = dbConnect.connection.createStatement();
            String query = "SELECT p.num_pret, p.numcompte, c.nom, c.prenoms, p.montant_prete, p.datepret "
                    + "FROM preter p "
                    + "JOIN client c ON p.numcompte = c.numcompte";
            rs = st.executeQuery(query);

            while (rs.next()) {
                String[] row = new String[6];
                row[0] = rs.getString("num_pret");
                row[1] = rs.getString("numcompte");
                row[2] = rs.getString("nom");
                row[3] = rs.getString("prenoms");
                row[4] = String.valueOf(rs.getDouble("montant_prete")); // Conversion en chaîne de caractères
                row[5] = rs.getString("datepret");
                model.addRow(row);
            }

            HomePage.jTablePret.setModel(model);

            // Ajouter un écouteur de sélection
            HomePage.jTablePret.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) { // Pour éviter les appels multiples lors de la sélection
                    int selectedRow = HomePage.jTablePret.getSelectedRow();
                    if (selectedRow != -1) {
                        // Récupérer les données de la ligne sélectionnée
                        String numPret = (String) model.getValueAt(selectedRow, 0);
                        String numCompte = (String) model.getValueAt(selectedRow, 1);
                        String montantPrete = (String) model.getValueAt(selectedRow, 4);

                        // Mettre à jour les champs de texte
                        HomePage.jTextFieldnumPret.setText(numPret);
                        HomePage.jComboBoxNumComptePret.setSelectedItem(numCompte);
                        HomePage.jTextFieldMontantPret.setText(montantPrete);

                        // Désactiver la modification de jComboBoxNumCompteEnvoyeur
                        HomePage.jComboBoxNumComptePret.setEnabled(false);
                    }
                }
            });

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des données de prêt: " + e);
        }
    }

    public void addPret(DBConnect dbConnect, String numCompte, double montantPrete) throws MessagingException, UnsupportedEncodingException {
        String insertQuery = "INSERT INTO preter (num_pret, numCompte, montant_prete, datePret) VALUES (?, ?, ?, ?)";
        String selectSoldeQuery = "SELECT solde FROM client WHERE numCompte = ?";
        String updateSoldeQuery = "UPDATE client SET solde = ? WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try {
            // Commencer une transaction
            dbConnect.connection.setAutoCommit(false);

            // Générer le nouveau num_pret
            String newNumPret = generateNewNumPret(dbConnect);

            // Insérer le prêt
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(insertQuery)) {
                pstmt.setString(1, newNumPret);
                pstmt.setString(2, numCompte);
                pstmt.setDouble(3, montantPrete);
                pstmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                pstmt.executeUpdate();
            }

            // Sélectionner le solde actuel
            double currentSolde = 0.0;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectSoldeQuery)) {
                pstmt.setString(1, numCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentSolde = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Compte non trouvé : " + numCompte);
                    }
                }
            }

            // Calculer le nouveau solde
            double newSolde = currentSolde + montantPrete;

            // Mettre à jour le solde du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateSoldeQuery)) {
                pstmt.setDouble(1, newSolde);
                pstmt.setString(2, numCompte);
                pstmt.executeUpdate();
            }

            // Valider la transaction
            dbConnect.connection.commit();

            // Appeler les méthodes nécessaires après l'ajout et la mise à jour
            table();
            tablePret();
            System.out.println("Prêt ajouté avec succès");
            JOptionPane.showMessageDialog(null, "Prêt ajouté avec succès");
            
            // Récupérer les informations du client pour le mail
            String[] clientInfo = getClientInfo(dbConnect, numCompte);
            if (clientInfo == null) {
                System.out.println("Informations du client non trouvées pour le compte : " + numCompte);
                return;
            }
            String nom = clientInfo[0];
            String prenoms = clientInfo[1];
            String mail = clientInfo[2];
            
            // Récupérer les informations du prêt
            Object[] pretInfo = getPretInfo(dbConnect, numCompte);
            if (pretInfo == null) {
                System.out.println("Informations du prêt non trouvées pour le compte : " + numCompte);
                return;
            }
            double montant_Prete = (double) pretInfo[0];
            Timestamp datePret = (Timestamp) pretInfo[1];

            // Calculer la date limite de remboursement (200 jours après le prêt)
            Calendar cal = Calendar.getInstance();
            cal.setTime(datePret);
            cal.add(Calendar.DAY_OF_YEAR, 200);
            String dueDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            
            // Formatter le montant à rembourser
            DecimalFormat df = new DecimalFormat("#.##");
            String montantRembourse = df.format(montant_Prete * 1.10);
            
            // Envoyer la notification par e-mail
            Mail mailer = new Mail();
            mailer.sendNotification(mail, nom + prenoms , montantRembourse, dueDate);
            
            System.out.println("Notification envoyée avec succès.");
            
        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de l'ajout du prêt: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout du prêt: " + ex);
        
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }
    
    //Récupérer les info nécessaire du client pour le mail
    public String[] getClientInfo(DBConnect dbConnect, String numCompte) throws SQLException {
        String query = "SELECT Nom, Prenoms, Mail FROM client WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompte);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("Nom");
                    String prenoms = rs.getString("Prenoms");
                    String mail = rs.getString("Mail");
                    return new String[]{nom, prenoms, mail};
                } else {
                    throw new SQLException("Client non trouvé : " + numCompte);
                }
            }
        }
    }
    
    //Récupérer les info nécessaire dans preter pour le mail
    public Object[] getPretInfo(DBConnect dbConnect, String numCompte) throws SQLException {
        String query = "SELECT montant_prete, datePret FROM preter WHERE numCompte = ?";
        try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(query)) {
            pstmt.setString(1, numCompte);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double montantPrete = rs.getDouble("montant_prete");
                    Timestamp datePret = rs.getTimestamp("datePret");
                    return new Object[]{montantPrete, datePret};
                } else {
                    throw new SQLException("Prêt non trouvé pour le compte : " + numCompte);
                }
            }
        }
    }

    //Calcul de la date limite de remboursement
    public String calculateDueDate(Timestamp datePret) {
    LocalDate dueDate = datePret.toLocalDateTime().toLocalDate().plusDays(200);
    return dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
}

    // Méthode pour générer un nouveau num_pret
    private String generateNewNumPret(DBConnect dbConnect) {
        String query = "SELECT MAX(num_pret) AS max_pret FROM preter";
        try (Statement stmt = dbConnect.connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String maxPret = rs.getString("max_pret");
                if (maxPret != null) {
                    int num = Integer.parseInt(maxPret.substring(4)) + 1;
                    return "P123" + String.format("%03d", num);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la génération du nouveau numéro de prêt: " + ex);
        }
        return "P123001"; // Valeur par défaut si aucune entrée n'existe
    }

    public void updateMontantPret(DBConnect dbConnect, String numPret, double newMontantPret) {
        String selectOldMontantQuery = "SELECT montant_prete FROM preter WHERE num_pret = ?";
        String updateMontantQuery = "UPDATE preter SET montant_prete = ? WHERE num_pret = ?";
        String selectSoldeQuery = "SELECT solde FROM client WHERE numcompte = ?";
        String updateSoldeQuery = "UPDATE client SET solde = ? WHERE numcompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try {
            dbConnect.connection.setAutoCommit(false);

            double oldMontantPret;
            String numCompte;
            double oldSolde;

            // Récupérer l'ancien montant du prêt et le numéro de compte associé
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectOldMontantQuery)) {
                pstmt.setString(1, numPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        oldMontantPret = rs.getDouble("montant_prete");
                    } else {
                        throw new SQLException("Prêt non trouvé : " + numPret);
                    }
                }
            }

            // Mettre à jour le montant du prêt
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateMontantQuery)) {
                pstmt.setDouble(1, newMontantPret);
                pstmt.setString(2, numPret);
                pstmt.executeUpdate();
            }

            // Récupérer le numéro de compte associé au prêt
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement("SELECT numcompte FROM preter WHERE num_pret = ?")) {
                pstmt.setString(1, numPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        numCompte = rs.getString("numcompte");
                    } else {
                        throw new SQLException("Prêt non trouvé : " + numPret);
                    }
                }
            }

            // Récupérer le solde actuel du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectSoldeQuery)) {
                pstmt.setString(1, numCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        oldSolde = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Compte non trouvé : " + numCompte);
                    }
                }
            }

            // Calculer le nouveau solde
            double newSolde = oldSolde - oldMontantPret + newMontantPret;

            // Mettre à jour le solde du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateSoldeQuery)) {
                pstmt.setDouble(1, newSolde);
                pstmt.setString(2, numCompte);
                pstmt.executeUpdate();
            }

            dbConnect.connection.commit();

            // Mettre à jour l'affichage du tableau
            tablePret();
            table();
            JOptionPane.showMessageDialog(null, "Montant du prêt mis à jour avec succès");

        } catch (SQLException ex) {
            try {
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la mise à jour du montant du prêt: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour du montant du prêt: " + ex);
        } finally {
            try {
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

// Méthode pour supprimer un prêt
    public void deletePret(DBConnect dbConnect, String numPret) {
        String selectPretQuery = "SELECT montant_prete, numcompte FROM preter WHERE num_pret = ?";
        String updateSoldeQuery = "UPDATE client SET solde = ? WHERE numcompte = ?";
        String deletePretQuery = "DELETE FROM preter WHERE num_pret = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try {
            dbConnect.connection.setAutoCommit(false);

            // Récupérer les informations du prêt
            double montantPrete = 0.0;
            String numCompte;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectPretQuery)) {
                pstmt.setString(1, numPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        montantPrete = rs.getDouble("montant_prete");
                        numCompte = rs.getString("numcompte");
                    } else {
                        throw new SQLException("Prêt non trouvé : " + numPret);
                    }
                }
            }

            // Récupérer le solde actuel du client
            double currentSolde;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement("SELECT solde FROM client WHERE numcompte = ?")) {
                pstmt.setString(1, numCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentSolde = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Compte non trouvé : " + numCompte);
                    }
                }
            }

            // Calculer le nouveau solde
            double newSolde = currentSolde - montantPrete;

            // Mettre à jour le solde du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateSoldeQuery)) {
                pstmt.setDouble(1, newSolde);
                pstmt.setString(2, numCompte);
                pstmt.executeUpdate();
            }

            // Supprimer le prêt
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(deletePretQuery)) {
                pstmt.setString(1, numPret);
                pstmt.executeUpdate();
            }

            dbConnect.connection.commit();

            // Rafraîchir la table
            tablePret();
            table();
            JOptionPane.showMessageDialog(null, "Prêt supprimé avec succès");

        } catch (SQLException ex) {
            try {
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la suppression du prêt: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression du prêt: " + ex);
        } finally {
            try {
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    //create table "rendre" if not exist
    private void rendreTableExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS rendre ("
                + "num_rendu VARCHAR(255) PRIMARY KEY,"
                + "num_pret VARCHAR(255),"
                + "situation VARCHAR(255),"
                + "rest_payé DOUBLE PRECISION,"
                + "date_rendu DATE)";
        try {
            PreparedStatement pstmt = dbConnect.connection.prepareStatement(createTableQuery);
            pstmt.execute();
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la création de la table rendre: " + ex);
        }
    }

    //ajout dans la table rendre
    public void addRendre(DBConnect dbConnect, String numPret, double montantRembourse) {
        String insertRendreQuery = "INSERT INTO rendre (num_rendu, num_pret, situation, rest_payé, date_rendu, montant_rendu) VALUES (?, ?, ?, ?, ?, ?)";
        String selectPretQuery = "SELECT montant_prete, numcompte FROM preter WHERE num_pret = ?";
        String updatePretQuery = "UPDATE preter SET montant_prete = ? WHERE num_pret = ?";
        String selectClientSoldeQuery = "SELECT solde FROM client WHERE numCompte = ?";
        String updateClientSoldeQuery = "UPDATE client SET solde = ? WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        // Générer le nouveau num_rendu
        String newNumRendu = generateNewNumRendu(dbConnect);

        try {
            // Commencer une transaction
            dbConnect.connection.setAutoCommit(false);

            // Obtenir les informations du prêt
            double montantPrete = 0.0;
            String numCompte = null;
            double soldeClient = 0.0;

            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectPretQuery)) {
                pstmt.setString(1, numPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        montantPrete = rs.getDouble("montant_prete");
                        numCompte = rs.getString("numcompte");
                    } else {
                        throw new SQLException("Prêt non trouvé pour le numéro: " + numPret);
                    }
                }
            }

            // Obtenir le solde actuel du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectClientSoldeQuery)) {
                pstmt.setString(1, numCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        soldeClient = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Client non trouvé pour le numéro de compte: " + numCompte);
                    }
                }
            }

            // Calculer la nouvelle situation et rest_payé
            double restPaye = montantPrete + 0.1 * montantPrete - montantRembourse;
            if (restPaye < 0) {
                restPaye = 0;
            }
            double montantRendu = montantRembourse; // Montant effectivement rendu

            String situation = (restPaye == 0) ? "Tout payé" : "Payé d'une part";

            // Insérer le rendu
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(insertRendreQuery)) {
                pstmt.setString(1, newNumRendu);
                pstmt.setString(2, numPret);
                pstmt.setString(3, situation);
                pstmt.setDouble(4, restPaye);
                pstmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                pstmt.setDouble(6, montantRendu);
                pstmt.executeUpdate();
            }

            // Mettre à jour le montant prêté dans la table preter
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updatePretQuery)) {
                pstmt.setDouble(1, restPaye);
                pstmt.setString(2, numPret);
                pstmt.executeUpdate();
            }

            // Mettre à jour le solde du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateClientSoldeQuery)) {
                pstmt.setDouble(1, soldeClient - montantRembourse);
                pstmt.setString(2, numCompte);
                pstmt.executeUpdate();
            }

            // Valider la transaction
            dbConnect.connection.commit();

            System.out.println("Remboursement ajouté avec succès");
            JOptionPane.showMessageDialog(null, "Remboursement ajouté avec succès");
            tableRendre(getSelectedSituation());
            table();
            tablePret();
        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de l'ajout du rendu: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout du rendu: " + ex);
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    //Implémentation du numRendu
    private String generateNewNumRendu(DBConnect dbConnect) {
        String query = "SELECT MAX(num_rendu) AS max_rendu FROM rendre";
        try (Statement stmt = dbConnect.connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String maxRendu = rs.getString("max_rendu");
                if (maxRendu != null) {
                    int num = Integer.parseInt(maxRendu.substring(4)) + 1;
                    return "R123" + String.format("%03d", num);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la génération du nouveau numéro de prêt: " + ex);
        }
        return "R123001"; // Valeur par défaut si aucune entrée n'existe
    }

    public void tableRendre(String situation) {
        ResultSet rs;
        String[] columns = {"num_rendu", "num_pret", "situation", "rest_payé", "date_rendu", "montant_rendu"};
        DefaultTableModel model = new DefaultTableModel(null, columns);

        try {
            Statement st = dbConnect.connection.createStatement();
            String query = "SELECT r.num_rendu, r.num_pret, r.situation, r.rest_payé, r.date_rendu, r.montant_rendu, p.montant_prete - r.rest_payé AS montant_remboursé "
                    + "FROM rendre r "
                    + "JOIN preter p ON r.num_pret = p.num_pret";

            if (!situation.equals("Toute situation")) {
                query += " WHERE r.situation = ?";
            }

            PreparedStatement pstmt = dbConnect.connection.prepareStatement(query);
            if (!situation.equals("Toute situation")) {
                pstmt.setString(1, situation);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = new String[6];
                row[0] = rs.getString("num_rendu");
                row[1] = rs.getString("num_pret");
                row[2] = rs.getString("situation");
                row[3] = String.valueOf(rs.getDouble("rest_payé"));
                row[4] = rs.getString("date_rendu");
                row[5] = String.valueOf(rs.getDouble("montant_rendu"));
                model.addRow(row);
            }

            HomePage.jTableRendre.setModel(model);

            // Ajouter un écouteur de sélection
            HomePage.jTableRendre.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = HomePage.jTableRendre.getSelectedRow();
                    if (selectedRow != -1) {
                        String numRendu = (String) model.getValueAt(selectedRow, 0);
                        String numPret = (String) model.getValueAt(selectedRow, 1);
                        String situationRow = (String) model.getValueAt(selectedRow, 2);
                        String restePaye = (String) model.getValueAt(selectedRow, 3);
                        String dateRendu = (String) model.getValueAt(selectedRow, 4);

                        HomePage.jTextFieldNumRendu.setText(numRendu);

                        // Mettre à jour les champs de texte
                        HomePage.jTextFieldNumRendu.setText(numRendu);
                        HomePage.jTextFieldnumpretrendre.setText(numPret);
                        HomePage.jTextFieldsituation.setText(situationRow);
                        HomePage.jTextFieldRestePayer.setText(restePaye);
                        HomePage.jTextFieldDateRendu.setText(dateRendu);
                    }
                }
            });

        } catch (SQLException ex) {
            System.out.println("Erreur lors du chargement des données de rendre: " + ex);
        }
    }

    //modification de rendre
    public void modifierRendre(DBConnect dbConnect, String numRendu, String newNumPret, double newMontantRembourse) {
        String selectRendreQuery = "SELECT num_pret, rest_payé, montant_rendu FROM rendre WHERE num_rendu = ?";
        String selectPretQuery = "SELECT montant_prete, numcompte FROM preter WHERE num_pret = ?";
        String updateRendreQuery = "UPDATE rendre SET num_pret = ?, rest_payé = ?, situation = ?, montant_rendu = ? WHERE num_rendu = ?";
        String updatePretQuery = "UPDATE preter SET montant_prete = ? WHERE num_pret = ?";
        String updateClientSoldeQuery = "UPDATE client SET solde = solde + ? WHERE numCompte = ?";
        String selectClientSoldeQuery = "SELECT solde FROM client WHERE numCompte = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try {
            // Commencer une transaction
            dbConnect.connection.setAutoCommit(false);

            // Récupérer les informations actuelles du rendu
            String oldNumPret;
            double oldRestPaye;
            double oldMontantRendu;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectRendreQuery)) {
                pstmt.setString(1, numRendu);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        oldNumPret = rs.getString("num_pret");
                        oldRestPaye = rs.getDouble("rest_payé");
                        oldMontantRendu = rs.getDouble("montant_rendu");
                    } else {
                        throw new SQLException("Rendu non trouvé pour le numéro: " + numRendu);
                    }
                }
            }

            // Récupérer les informations actuelles du prêt associé à l'ancien numéro
            String oldNumCompte;
            double oldMontantPrete;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectPretQuery)) {
                pstmt.setString(1, oldNumPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        oldMontantPrete = rs.getDouble("montant_prete");
                        oldNumCompte = rs.getString("numcompte");
                    } else {
                        throw new SQLException("Prêt non trouvé pour le numéro: " + oldNumPret);
                    }
                }
            }

            // Récupérer le solde actuel de l'ancien compte client
            double oldSoldeClient;
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectClientSoldeQuery)) {
                pstmt.setString(1, oldNumCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        oldSoldeClient = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Compte client non trouvé pour le numéro: " + oldNumCompte);
                    }
                }
            }

            // Calculer l'ancien montant remboursé
            double oldMontantRembourse = oldMontantPrete - oldRestPaye; //lasa négatif

            // Calculer les nouvelles valeurs en fonction des modifications
            double newMontantPrete = oldMontantPrete;
            String newNumCompte = oldNumCompte;
            double newSoldeClient = oldSoldeClient;

            if (!newNumPret.equals(oldNumPret)) {
                // Si le numéro de prêt est modifié, ajuster les valeurs
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectPretQuery)) {
                    pstmt.setString(1, newNumPret);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            newMontantPrete = rs.getDouble("montant_prete");
                            newNumCompte = rs.getString("numcompte");
                        } else {
                            throw new SQLException("Prêt non trouvé pour le numéro: " + newNumPret);
                        }
                    }
                }

                // Mettre à jour le solde de l'ancien client
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateClientSoldeQuery)) {
                    pstmt.setDouble(1, oldSoldeClient + oldMontantRembourse);
                    pstmt.setString(2, oldNumCompte);
                    pstmt.executeUpdate();
                }

                // Soustraire le montant remboursé de l'ancien prêt
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updatePretQuery)) {
                    pstmt.setDouble(1, oldMontantPrete + oldMontantRembourse);
                    pstmt.setString(2, oldNumPret);
                    pstmt.executeUpdate();
                }

                // Mettre à jour le nouveau numéro de prêt et le nouveau montant prêté
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updatePretQuery)) {
                    pstmt.setDouble(1, newMontantPrete - newMontantRembourse);
                    pstmt.setString(2, newNumPret);
                    pstmt.executeUpdate();
                }

                // Ajouter le montant remboursé au nouveau solde du client
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateClientSoldeQuery)) {
                    pstmt.setDouble(1, newSoldeClient - newMontantRembourse);
                    pstmt.setString(2, newNumCompte);
                    pstmt.executeUpdate();
                }

            } else {
                // Si seulement le montant remboursé est modifié
                double newRestPaye = newMontantPrete - newMontantRembourse;
                String situation = (newRestPaye == 0) ? "Tout payé" : "Payé d'une part";

                // Ajouter le montant remboursé au solde de l'ancien client
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateClientSoldeQuery)) {
                    pstmt.setDouble(1, oldSoldeClient + (newMontantRembourse - oldMontantRendu));
                    pstmt.setString(2, oldNumCompte);
                    pstmt.executeUpdate();
                }

                // Soustraire le montant remboursé de l'ancien prêt
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updatePretQuery)) {
                    pstmt.setDouble(1, oldMontantPrete + (newMontantRembourse - oldMontantRendu));
                    pstmt.setString(2, oldNumPret);
                    pstmt.executeUpdate();
                }

                // Mettre à jour le rendu avec les nouvelles valeurs
                try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateRendreQuery)) {
                    pstmt.setString(1, newNumPret); // Nouveau numéro de prêt
                    pstmt.setDouble(2, newRestPaye);
                    pstmt.setString(3, situation);
                    pstmt.setDouble(4, newMontantRembourse);
                    pstmt.setString(5, numRendu);
                    pstmt.executeUpdate();
                }
            }

            // Valider la transaction
            dbConnect.connection.commit();

            System.out.println("Modification réussie");
            JOptionPane.showMessageDialog(null, "Modification réussie");

            tableRendre(getSelectedSituation());
            table();
            tablePret();
        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la modification: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la modification: " + ex);
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    //Supprimer un remboursement
    public void supprimerRendre(DBConnect dbConnect, String numRendu) {
        String selectRendreQuery = "SELECT num_pret, montant_rendu FROM rendre WHERE num_rendu = ?";
        String selectPretQuery = "SELECT montant_prete, numcompte FROM preter WHERE num_pret = ?";
        String selectClientSoldeQuery = "SELECT solde FROM client WHERE numCompte = ?";
        String updatePretQuery = "UPDATE preter SET montant_prete = ? WHERE num_pret = ?";
        String updateClientSoldeQuery = "UPDATE client SET solde = ? WHERE numCompte = ?";
        String deleteRendreQuery = "DELETE FROM rendre WHERE num_rendu = ?";

        if (dbConnect.connection == null) {
            dbConnect.dbConnection();
        }

        try {
            // Commencer une transaction
            dbConnect.connection.setAutoCommit(false);

            String numPret;
            double montantRendu;
            double montantPrete;
            String numCompte;
            double soldeClient;

            // Récupérer les informations du remboursement à supprimer
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectRendreQuery)) {
                pstmt.setString(1, numRendu);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        numPret = rs.getString("num_pret");
                        montantRendu = rs.getDouble("montant_rendu");
                    } else {
                        throw new SQLException("Rendu non trouvé pour le numéro: " + numRendu);
                    }
                }
            }

            // Récupérer les informations du prêt associé
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectPretQuery)) {
                pstmt.setString(1, numPret);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        montantPrete = rs.getDouble("montant_prete");
                        numCompte = rs.getString("numcompte");
                    } else {
                        throw new SQLException("Prêt non trouvé pour le numéro: " + numPret);
                    }
                }
            }

            // Récupérer le solde actuel du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(selectClientSoldeQuery)) {
                pstmt.setString(1, numCompte);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        soldeClient = rs.getDouble("solde");
                    } else {
                        throw new SQLException("Compte client non trouvé pour le numéro: " + numCompte);
                    }
                }
            }

            // Calculer les nouvelles valeurs
            double newSoldeClient = soldeClient + montantRendu;
            double initialMontantPrete = montantPrete + montantRendu;

            // Mettre à jour le solde du client
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updateClientSoldeQuery)) {
                pstmt.setDouble(1, newSoldeClient);
                pstmt.setString(2, numCompte);
                pstmt.executeUpdate();
            }

            // Mettre à jour le montant prêté dans la table preter
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(updatePretQuery)) {
                pstmt.setDouble(1, initialMontantPrete);
                pstmt.setString(2, numPret);
                pstmt.executeUpdate();
            }

            // Supprimer le remboursement de la base de données
            try (PreparedStatement pstmt = dbConnect.connection.prepareStatement(deleteRendreQuery)) {
                pstmt.setString(1, numRendu);
                pstmt.executeUpdate();
            }

            // Valider la transaction
            dbConnect.connection.commit();

            System.out.println("Remboursement supprimé avec succès");
            JOptionPane.showMessageDialog(null, "Remboursement supprimé avec succès");

            tableRendre(getSelectedSituation());
            table();
            tablePret();
        } catch (SQLException ex) {
            try {
                // En cas d'erreur, annuler la transaction
                dbConnect.connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur lors du rollback: " + rollbackEx);
            }
            System.out.println("Erreur lors de la suppression du remboursement: " + ex);
            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression du remboursement: " + ex);
        } finally {
            try {
                // Rétablir l'état auto-commit par défaut
                dbConnect.connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Erreur lors du rétablissement de l'auto-commit: " + ex);
            }
        }
    }

    private String getSelectedSituation() {
        return (String) HomePage.jComboBoxaffichagerembourse.getSelectedItem();
    }

    /*
    public class VirementPDF {
    public static void generateVirementPDF(String numCompteEnv, String nomEnv,String prenomsEnv, int soldeEnv, String numCompteBen, String nomBen,String prenomsBen, int montantVirement, Date dateTransfert, int avisNumero) {
    String pdfPath = "C:/Users/Juka/Documents/Javapdf/" + nomEnv + ".pdf";
    try {
    PdfWriter writer = new PdfWriter(pdfPath);
    PdfDocument pdfDoc = new PdfDocument(writer);
    try (Document document = new Document(pdfDoc)) {
    document.add(new Paragraph("Banque Tsika").setTextAlignment(TextAlignment.CENTER));
    document.add(new Paragraph(" "));
    // Centre le paragraphe contenant la date de transfert
    Paragraph dateTransfertParagraph = new Paragraph("Date : " + dateTransfert.toString());
    dateTransfertParagraph.setTextAlignment(TextAlignment.CENTER);
    document.add(dateTransfertParagraph);
    // Centre le paragraphe contenant l'avis de virement
    Paragraph avisNumeroParagraph = new Paragraph("AVIS DE VIREMENT N°: 0" + avisNumero);
    avisNumeroParagraph.setTextAlignment(TextAlignment.CENTER);
    document.add(avisNumeroParagraph);
    document.add(new Paragraph(" "));
    document.add(new Paragraph("N° de compte : " + numCompteEnv));
    document.add(new Paragraph(nomEnv + " " + prenomsEnv));
    document.add(new Paragraph("Solde Actuel : " + soldeEnv + " Ar"));
    document.add(new Paragraph(" "));
    Paragraph aParagraph = new Paragraph("À");
    aParagraph.setTextAlignment(TextAlignment.CENTER);
    document.add(aParagraph);
    Paragraph numCompteBenParagraph = new Paragraph("N° de compte : " + numCompteBen);
    numCompteBenParagraph.setTextAlignment(TextAlignment.RIGHT);
    document.add(numCompteBenParagraph);
    Paragraph nomBenParagraph = new Paragraph(nomBen +" "+prenomsBen);
    nomBenParagraph.setTextAlignment(TextAlignment.RIGHT);
    document.add(nomBenParagraph);
    document.add(new Paragraph(" "));
    document.add(new Paragraph("Montant : " + montantVirement + " Ar"));
    }
    System.out.println("PDF créé avec succès : " + pdfPath);
    } catch (FileNotFoundException e) {
    e.printStackTrace();
    }
    }
    }*/
}
