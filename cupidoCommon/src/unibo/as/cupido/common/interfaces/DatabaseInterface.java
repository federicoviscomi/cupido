/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.common.interfaces;

import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.NoSuchUserException;

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
 * <code>
CREATE TABLE `cupido`.`User` (
  `name` VARCHAR(16)  NOT NULL,
  `password` CHAR(8) UNICODE NOT NULL,
  `score` INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`),
  INDEX `scoreIndex`(`score`, `name`)
)
ENGINE = MyISAM;
</code>
 * 
 * 
 * 
 * 
 * 
 */
public interface DatabaseInterface {

	/** default database name */
	public static final String database = "cupido";

	/**
	 * Add a new user with name <code>userName</code>, password
	 * <code>password</code> and score zero.
	 * 
	 * @param userName
	 * @param password
	 * @throws DuplicateUserNameException
	 *             if a user named <code>userName</code> already exists in the
	 *             database
	 * @throws IllegalArgumentException
	 *             if any of the arguments is <code>null</code>
	 */
	public void addNewUser(String userName, String password)
			throws SQLException, DuplicateUserNameException,
			IllegalArgumentException;

	/**
	 * Check if the user can be logged in with the provided {@link password}.
	 * 
	 * @param userName
	 * @param password
	 * @return <code>true</code> if <code>userName</code> is in the database and
	 *         his password is <code>password</code>; otherwise return false.
	 * @throws SQLException
	 *             in case of communication problem with database
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public boolean login(String userName, String password) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Return true if userName is in the database
	 * @param userName
	 * @return
	 * @throws SQLException
	 */
	public boolean contains(String userName) throws SQLException;
	
	/**
	 * Update score of user <code>userName</code>
	 * 
	 * @param userName
	 * @param score
	 *            the new score of user <code>userName</code>
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public void updateScore(String userName, int score) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Retreive at most the first top <code>size</code> positions in the global
	 * rank.
	 * 
	 * @param
	 * @return
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if <code>size</code> is not positive
	 */
	public ArrayList<RankingEntry> getTopRank(int size)
			throws SQLException, IllegalArgumentException;

	/** Number of entries returned from {@link DatabseInterface#getLocalRank()}.*/
	public final int LOCAL_RANK_ENTRIES_NUM = 7;

	/**
	 * Returns {@link LOCAL_RANK_ENTRIES_NUM} from the global rank list
	 * containing in the middle <code>userName</code>.
	 * 
	 * @param userName
	 *            the user who wants the rank
	 * @return a list of size {@link LOCAL_RANK_ENTRIES_NUM} with user
	 *         {@link userName} in the middle.
	 * @throws SQLException
	 *             in case of database error
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public ArrayList<RankingEntry> getLocalRank(String userName)
			throws SQLException, IllegalArgumentException, NoSuchUserException;

	/**
	 * Get player position in the global rank.
	 * 
	 * @param userName
	 * @return rank of {@link userName}.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public RankingEntry getUserRank(String userName) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Get the player total score.
	 * 
	 * @param userName
	 * @return player score.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public int getPlayerScore(String userName) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Close the connection with database.
	 * 
	 * @throws SQLException
	 *             in case of errors.
	 */
	void close() throws SQLException;
}