import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.String.valueOf;

public class Plateau extends JPanel {

    private Case[][] monPlateau;

    public Plateau() {
        this.monPlateau = new Case[6][5];
        this.setLayout(new GridLayout(6, 5, 2, 2));
        this.setBackground(Color.DARK_GRAY);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                this.monPlateau[i][j] = new Case(Color.GRAY, i, j, 0);
                this.monPlateau[i][j].setBorder(Yote.empty);
                this.add(this.monPlateau[i][j]);
            }
        }
    }

    public boolean isIn(Case c) {
        if (c == null) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (this.monPlateau[i][j] == c) {
                    return true;
                }
            }
        }
        return false;
    }

    // Teste si le pion en dep peut se deplacer en arr :
    // renvoie 0 si interdit, 1 si deplacement valide, 2 si prise valide
    public int coupValide(Case dep, Case arr) {
        if (dep == null || arr == null || !dep.isOccupe()) {
            return 0;
        }
        // Pose d'un pion depuis le stock sur une case vide du plateau
        if (dep.getTypeCase() == 1 && isIn(arr) && !arr.isOccupe()) {
            return 1;
        }
        if (verifieDeplacementHor(dep, arr) || verifieDeplacementVert(dep, arr)) {
            return 1;
        }
        if (verifieDeplacementHorPrise(dep, arr) || verifieDeplacementVertPrise(dep, arr)) {
            return 2;
        }
        return 0;
    }

    public boolean verifieDeplacementVertPrise(Case dep, Case arr) {
        if (!isIn(dep) || !isIn(arr) || !dep.isOccupe() || arr.isOccupe()) {
            return false;
        }
        if (dep.getOrdonnee() == arr.getOrdonnee()
                && Yote.abs(dep.getAbscisse() - arr.getAbscisse()) == dep.getPion().getMvtPrise()) {
            int midAbs = (dep.getAbscisse() + arr.getAbscisse()) / 2;
            Case milieu = this.monPlateau[midAbs][dep.getOrdonnee()];
            return milieu.isOccupe() && milieu.getPion().getCouleur() != dep.getPion().getCouleur();
        }
        return false;
    }

    public boolean verifieDeplacementHorPrise(Case dep, Case arr) {
        if (!isIn(dep) || !isIn(arr) || !dep.isOccupe() || arr.isOccupe()) {
            return false;
        }
        if (dep.getAbscisse() == arr.getAbscisse()
                && Yote.abs(dep.getOrdonnee() - arr.getOrdonnee()) == dep.getPion().getMvtPrise()) {
            int midOrd = (dep.getOrdonnee() + arr.getOrdonnee()) / 2;
            Case milieu = this.monPlateau[dep.getAbscisse()][midOrd];
            return milieu.isOccupe() && milieu.getPion().getCouleur() != dep.getPion().getCouleur();
        }
        return false;
    }

    public boolean verifieDeplacementVert(Case dep, Case arr) {
        if (!isIn(dep) || !isIn(arr) || !dep.isOccupe() || arr.isOccupe()) {
            return false;
        }
        return dep.getOrdonnee() == arr.getOrdonnee()
                && Yote.abs(dep.getAbscisse() - arr.getAbscisse()) == dep.getPion().getMvt();
    }

    public boolean verifieDeplacementHor(Case dep, Case arr) {
        if (!isIn(dep) || !isIn(arr) || !dep.isOccupe() || arr.isOccupe()) {
            return false;
        }
        return dep.getAbscisse() == arr.getAbscisse()
                && Yote.abs(dep.getOrdonnee() - arr.getOrdonnee()) == dep.getPion().getMvt();
    }

    public Case casePrise(Case dep, Case arr) {
        if (verifieDeplacementHorPrise(dep, arr) || verifieDeplacementVertPrise(dep, arr)) {
            int midAbs = (dep.getAbscisse() + arr.getAbscisse()) / 2;
            int midOrd = (dep.getOrdonnee() + arr.getOrdonnee()) / 2;
            return this.monPlateau[midAbs][midOrd];
        }
        return null;
    }

    public void jouerCoupPrise(Case dep, Case arr) {
        if (coupValide(dep, arr) != 2) {
            return;
        }

        Case prise = casePrise(dep, arr);
        CouleurPion couleurAdverse = prise.getPion().getCouleur();
        Yote.casePrise = null;

        // Deplacement du pion sautant et suppression du pion saute
        arr.setPion(dep.getPion());
        dep.setPion(null);
        dep.setBorder(Yote.empty);
        prise.setPion(null);

        if (couleurAdverse == CouleurPion.blanc) {
            Yote.nbPionBlanc--;
        } else {
            Yote.nbPionNoir--;
        }

        Yote.scoreBlanc.setText("Score Joueur Blanc \n " + valueOf(Yote.nbPionBlanc));
        Yote.scoreNoir.setText("Score Joueur Noir \n" + valueOf(Yote.nbPionNoir));

        // Si l'adversaire n'a plus de pion des la 1ere prise, fin de partie immediate
        if (Yote.nbPionBlanc == 0 || Yote.nbPionNoir == 0) {
            String[] lesTextes = {"Recommencer", "Fermer le jeu"};
            int retour = JOptionPane.showOptionDialog(
                    this,
                    "Partie terminée. Le joueur " + couleurAdverse + " a perdu !",
                    "Fin de jeu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(),
                    lesTextes,
                    lesTextes[0]
            );
            if (retour == 0) {
                this.reinitialiser();
            } else {
                Yote.fenetrePrincipale.dispose();
            }
            return;
        }

        // Passage a l'etat 2 pour choisir le 2e pion adverse a prendre
        Fenetre.boutonAnnuler.setEnabled(true);
        Yote.etat = 2;
        Yote.fenetrePrincipale.repaint();
        Yote.fenetrePrincipale.validate();
    }

    public void jouerCoup(Case dep, Case arr) {
        if (coupValide(dep, arr) != 1) {
            return;
        }

        arr.setPion(dep.getPion());
        dep.setPion(null);
        dep.setBorder(Yote.empty);

        Fenetre.boutonAnnuler.setEnabled(true);
        Yote.joueur = (Yote.joueur == CouleurPion.blanc) ? CouleurPion.noir : CouleurPion.blanc;
        Yote.etat = 0;

        Yote.fenetrePrincipale.repaint();
        Yote.fenetrePrincipale.validate();
    }

    public void annulerCoup(Case dep, Case arr) {
        if (dep == null || arr == null || !arr.isOccupe()) {
            return;
        }

        Pion pionDeplace = arr.getPion();
        CouleurPion couleurAttaquant = pionDeplace.getCouleur();
        CouleurPion couleurAdverse = (couleurAttaquant == CouleurPion.blanc) ? CouleurPion.noir : CouleurPion.blanc;

        // Verifie si le coup precedent etait un saut de prise (distance de 2 sur le plateau)
        boolean etaitUnePrise = (dep.getTypeCase() == 0)
                && (Yote.abs(dep.getAbscisse() - arr.getAbscisse()) == 2
                || Yote.abs(dep.getOrdonnee() - arr.getOrdonnee()) == 2);

        // Replacer le pion sur sa case de depart (plateau ou stock)
        dep.setPion(pionDeplace);
        arr.setPion(null);
        dep.setBorder(Yote.empty);
        arr.setBorder(Yote.empty);

        if (etaitUnePrise) {
            // 1. Restituer le pion saute (au milieu entre dep et arr)
            int midAbs = (dep.getAbscisse() + arr.getAbscisse()) / 2;
            int midOrd = (dep.getOrdonnee() + arr.getOrdonnee()) / 2;
            this.monPlateau[midAbs][midOrd].setPion(new Pion(couleurAdverse));

            if (couleurAdverse == CouleurPion.blanc) {
                Yote.nbPionBlanc++;
            } else {
                Yote.nbPionNoir++;
            }

            // 2. Si le 2e pion avait deja ete pris (on etait repasse en etat 0), le restituer aussi
            if (Yote.etat == 0 && Yote.casePrise != null && !Yote.casePrise.isOccupe()) {
                Yote.casePrise.setPion(new Pion(couleurAdverse));
                if (couleurAdverse == CouleurPion.blanc) {
                    Yote.nbPionBlanc++;
                } else {
                    Yote.nbPionNoir++;
                }
                Yote.casePrise = null;
            }
            Yote.joueur = couleurAttaquant;
        } else {
            // Coup simple ou pose depuis le stock
            Yote.joueur = couleurAttaquant;
        }

        Yote.scoreBlanc.setText("Score Joueur Blanc \n " + valueOf(Yote.nbPionBlanc));
        Yote.scoreNoir.setText("Score Joueur Noir \n" + valueOf(Yote.nbPionNoir));

        Yote.etat = 0;
        Fenetre.boutonAnnuler.setEnabled(false);

        Yote.fenetrePrincipale.repaint();
        Yote.fenetrePrincipale.validate();
    }

    public void reinitialiser() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                this.monPlateau[i][j].setPion(null);
                this.monPlateau[i][j].setBorder(Yote.empty);
            }
        }

        for (int i = 0; i < 12; i++) {
            Case cBlanc = (Case) Yote.stockBlanc.getComponent(i);
            cBlanc.setPion(new Pion(CouleurPion.blanc));
            cBlanc.setBorder(Yote.empty);

            Case cNoir = (Case) Yote.stockNoir.getComponent(i);
            cNoir.setPion(new Pion(CouleurPion.noir));
            cNoir.setBorder(Yote.empty);
        }

        Yote.nbPionBlanc = 12;
        Yote.nbPionNoir = 12;
        Yote.joueur = CouleurPion.blanc;
        Yote.etat = 0;

        Yote.caseDep = null;
        Yote.caseArr = null;
        Yote.casePrise = null;

        Yote.scoreBlanc.setText("Score Joueur Blanc \n " + valueOf(Yote.nbPionBlanc));
        Yote.scoreNoir.setText("Score Joueur Noir \n" + valueOf(Yote.nbPionNoir));

        Fenetre.boutonAnnuler.setEnabled(false);

        Yote.fenetrePrincipale.repaint();
        Yote.fenetrePrincipale.validate();
    }
}