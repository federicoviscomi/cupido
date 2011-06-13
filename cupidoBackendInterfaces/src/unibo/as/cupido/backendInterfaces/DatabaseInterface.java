package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;

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
 * 
 * 
 * 
 * 
 * TODO what is score? the total number of game won by this user minus the
 * number of lost game? in this case should not be unsigned. should be added a
 * field logged BOOLEAN which stores if the user is logged in or not?
 * 
 * <code>
CREATE TABLE `cupido`.`User` (
  `name` VARCHAR(16)  NOT NULL,
  `password` CHAR(8) UNICODE NOT NULL,
  `score` INTEGER UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`),
  INDEX `scoreIndex`(`score`, `name`)
)
ENGINE = MyISAM;
</code>
 * 
 * 
 * 
 * 
 * @author cane
 * 
 */
public interface DatabaseInterface extends Remote {

	/** default database name */
	public static final String database = "cupido";

	/**
	 * Add a new user with name <code>userName</code>, password
	 * <code>password</code> and score zero.
	 * 
	 * @param userName
	 * @param password
	 * @throws RemoteException
	 * @throws DuplicateUserNameException
	 *             if a user named <code>userName</code> already exists in the
	 *             database
	 * @throws IllegalArgumentException
	 *             if any of the arguments is <code>null</code>
	 */
	public void addNewUser(String userName, String password)
			throws RemoteException, SQLException, DuplicateUserNameException,
			IllegalArgumentException;

	/**
	 * TODO ?Log <code>userName</code> in the database.?
	 * 
	 * @param userName
	 * @param password
	 * @return <code>true</code> if <code>userName</code> is in the database and
	 *         his password is <code>password</code>; otherwise return false.
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public boolean login(String userName, String password)
			throws RemoteException, SQLException, IllegalArgumentException,
			NoSuchUserException;

	/**
	 * Update score of user <code>userName</code>
	 * 
	 * @param userName
	 * @param score
	 *            the new score of user <code>userName</code>
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public void updateScore(String userName, int score) throws RemoteException,
			SQLException, IllegalArgumentException, NoSuchUserException;

	/**
	 * Retreive at most the first top <code>size</code> positions in the global
	 * rank.
	 * 
	 * @param
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if <code>size</code> is not positive
	 */
	public ArrayList<Pair<String, Integer>> getTopRank(int size)
			throws RemoteException, SQLException, IllegalArgumentException;

	/**
	 * Returns one chunk the global rank that contains from four position before
	 * <code>userName</code> to five position after the <code>userName</code>.
	 * 
	 * 
	 * TODO this methods troubles me
	 * 
	 * @param userName
	 *            the user who wants the rank
	 * @return a list of size twenty, the first half contains the first chunk
	 *         and the second half contains the second chunk
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public ArrayList<Pair<String, Integer>> getLocalRank(String userName)
			throws RemoteException, SQLException, IllegalArgumentException,
			NoSuchUserException;

	/**
	 * Get player position in the global rank.
	 * 
	 * @param userName
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public int getUserRank(String userName) throws RemoteException,
			SQLException, IllegalArgumentException, NoSuchUserException;

	/**
	 * Get the player total score which is TODO ?the number of total wins minus
	 * the number of total losses?
	 * 
	 * @param userName
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public int getPlayerScore(String userName) throws RemoteException,
			SQLException, IllegalArgumentException, NoSuchUserException;

	/**
	 * Close the connection with database.
	 * 
	 * @throws RemoteException
	 */
	void close() throws RemoteException;

}
