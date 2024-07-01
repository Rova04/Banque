
package DatabaseConnection;

import java.util.Date;

public class Preter {
     //Déclaration
    private String mnum_pret;
    private String mnumCompte;
    private int mMontant_prete;
    private Date mdatepret;
    
    //Constructeur par défaut
    public Preter(){
        
    }
    
    //Constructeur avec parametre pour l'ajout
    public Preter(String num_pret, String numCompte, int montant_prete, Date datepret){
        this.mnum_pret = num_pret;
        this.mnumCompte = numCompte;
        this.mMontant_prete = montant_prete;
        this.mdatepret = datepret;
    }
    
    //acces et modification des valeurs des attributs: getters et setters
    public String getnum_pret() {
        return mnum_pret;
    }
    public void setnum_pret(String num_pret) {
        this.mnum_pret = num_pret;
    }
    
    public String getNumCompte() {
        return mnumCompte;
    }
    public void setNumCompte(String numCompte) {
        this.mnumCompte = numCompte;
    }
    
    public int getmontant_prete() {
        return mMontant_prete;
    }
    public void setmontant_prete(int montant_prete) {
        this.mMontant_prete = montant_prete;
    }
    
    public Date getdatepret() {
        return mdatepret;
    }
    public void setdatepret(Date datepret) {
        this.mdatepret = datepret;
    }
}
