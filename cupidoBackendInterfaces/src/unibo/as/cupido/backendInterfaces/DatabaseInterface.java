package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.Rank;

/**
 * Schema del database:
 * 
 * [RF1701, 2, desiderabile] Il database degli utenti conterrà per ogni utente
 * le seguenti informazioni: Nome utente Password Punteggio Rank.
 * <p>
 * Le operazioni previste sul database sono:
 * <ul>
 * <li>[RF1702, 3, necessario] Inserisci nuovo utente: operazione effettuata
 * solo alla registrazione di un nuovo utente. Non è possibile per ora prevedere
 * la frequenza con cui questa operazione dovrà essere svolta.</li>
 * <li>[RF1703, 3, necessario] Verifica login: viene confrontata la password
 * salvata con quella fornita dall'utente al momento del login. Si stima che
 * tutti gli utenti effettuino un login mediamente 3 volte al giorno.</li>
 * <li>[RF1704, 3, necessario] Aggiorna punteggio: alla fine di ogni partita
 * deve essere aggiornato il punteggio dei giocatori. Questa operazione comporta
 * anche cambiare il rank dei giocatori. Si stima che questa operazione dovrà
 * avvenire un numero di volte al giorno pari al numero di giocatori registrati.
 * </li>
 * <li>[RF1705, 2, necessario] Guarda classifica: un utente o un amministratore
 * ha richiesto di vedere la classifica e questa viene restituita. Non è
 * possibile stimare la frequenza di questa operazione.</li>
 * <li>[RF1706, 3, necessario] Al database viene richiesto il punteggio ed il
 * rank di un singolo giocatore. Si stima che l'operazione verrà eseguita un
 * numero di volte al giorno pari al quadruplo del numero di giocatori
 * registrati.</li>
 * <li>[RF1707, 2, necessario] Un amministratore deve poter in qualunque momento
 * modificare i dati presenti nel database se necessario. Si stima che durante
 * il normale funzionamento del software questa operazione sarà effettuata 2
 * volte a settimana.</li>
 * <ul>
 * 
 * @author cane
 * 
 */
public interface DatabaseInterface extends Remote {
	public static final String database = "cupido";

	// Nome utente Password Punteggio Rank

	public void addNewUser(String userName, String password) throws RemoteException;

	public boolean login(String userName, String password) throws RemoteException;

	public void updateScore(String userName, int score) throws RemoteException;

	public Rank getGlobalRank() throws RemoteException;

	public int getPlayerRank(String player) throws RemoteException;

	public int getPlayerScore(String player) throws RemoteException;

}
