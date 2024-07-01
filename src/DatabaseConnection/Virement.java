
package DatabaseConnection;

import java.util.Date;

public class Virement {
        //Déclaration
    private String mnumCompte;
    private String mnumCompteB;
    private int mMontant;
    private Date mdateTransf;
    
    //Constructeur par défaut
    public Virement(){
        
    }
    
    //Constructeur avec parametre pour l'ajout
    public Virement(String numCompte, String numCompteB, int montant, Date dateTransf){
        this.mnumCompte = numCompte;
        this.mnumCompteB = numCompteB;
        this.mMontant = montant;
        this.mdateTransf = dateTransf;
    }

    //acces et modification des valeurs des attributs: getters et setters
    public String getnumCompte() {
        return mnumCompte;
    }
    public void setnumCompte(String numCompte) {
        this.mnumCompte = numCompte;
    }
    
    public String getnumCompteB() {
        return mnumCompteB;
    }
    public void setnumCompteB(String numCompteB) {
        this.mnumCompteB = numCompteB;
    }
    
    public int getMontant() {
        return mMontant;
    }
    public void setnumCompteB(int montant) {
        this.mMontant = montant;
    }
    
    public Date getdateTransf() {
        return mdateTransf;
    }
    public void setdateTransf(Date dateTransf) {
        this.mdateTransf = dateTransf;
    }
}
