package DatabaseConnection;

public class Client {
    private String numCompte;
    private String nom;
    private String prenoms;
    private String tel;
    private String mail;
    private int solde;

    public Client() {
    }

    public Client(String numCompte, String nom, String prenoms, String tel, String mail, int solde) {
        this.numCompte = numCompte;
        this.nom = nom;
        this.prenoms = prenoms;
        this.tel = tel;
        this.mail = mail;
        this.solde = solde;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getSolde() {
        return solde;
    }

    public void setSolde(int solde) {
        this.solde = solde;
    }
}
