
package DatabaseConnection;

import java.util.Date;

public class Rendre {
    //Déclaration
    private String mnum_rendu;
    private String mnum_pret;
    private String msituation;
    private int mreste;
    private Date mdaterendu;
    
    //Constructeur par défaut
    public Rendre(){
        
    }
    
    //Constructeur avec parametre pour l'ajout
    public Rendre(String num_rendu, String num_pret, String situation,int reste, Date daterendu){
        this.mnum_rendu = num_rendu;
        this.mnum_pret = num_pret;
        this.msituation = situation;
        this.mreste = reste;
        this.mdaterendu = daterendu;
    }
    
    //acces et modification des valeurs des attributs: getters et setters
    public String getnum_rendu() {
        return mnum_rendu;
    }
    public void setnum_rendu(String num_rendu) {
        this.mnum_rendu = num_rendu;
    }
    
    public String getnum_pret() {
        return mnum_pret;
    }
    public void setnum_pret(String num_pret) {
        this.mnum_pret = num_pret;
    }
    
    public String getsituation() {
        return msituation;
    }
    public void setsituation(String situation) {
        this.msituation = situation;
    }
    
    public int getreste() {
        return mreste;
    }
    public void setreste(int reste) {
        this.mreste = reste;
    }
    
    public Date getdaterendu() {
        return mdaterendu;
    }
    public void setdaterendu(Date daterendu) {
        this.mdaterendu = daterendu;
    }
}
