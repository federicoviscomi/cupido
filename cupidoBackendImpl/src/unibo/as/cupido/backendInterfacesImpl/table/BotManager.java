package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;

public class BotManager {

	public Bot chooseBotStrategy(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager) throws RemoteException {
		return (Bot) UnicastRemoteObject.exportObject(new BotFewTricks(
				initialTableStatus, singleTableManager));
	}
	/**
	 * If a player got all points (26) then he "shot the moon". The 26 points
	 * isn't added to his score, but to everyone else's. How to pass
	 * 
	 * When a new round starts, you have to pass 3 cards first to someone else.
	 * This is a very important moment. You'll want to get rid of your bad
	 * cards, especially queens, kings and ace's because with these cards you
	 * are likely to take tricks. You'll also want to pass away high hearts
	 * because when they are leaded you must be able to duck them. But the most
	 * important thing is the queen of spades. If you have like 2 spades and the
	 * queen of spades,then you are in trouble. Other players will lead spades
	 * to fish out the queen. If you don't have enough spades, you will be
	 * forced to give it to yourself. So pass her if you don't have enough
	 * spades. Also pass the king and ace of spades. A good pass contains always
	 * a bad shooting card. This is most of the time a low hearts. If you pass a
	 * low hearts, the receiver won't be able to shoot easily. Another good
	 * thing to do is sloughing your suit so you don't have any cards left of it
	 * and when it is lead, you can play what you want.
	 * 
	 * Ducking
	 * 
	 * Best way not to take tricks is ducking. It is a boring way of play,but
	 * can work fine. Ducking is always playing your highest card under the card
	 * that was played by others. So when a trick contains diamonds 10,4,7 you
	 * could play diamonds 9. This can work fine but it is a risk to get stuck
	 * with high cards near the end of the round. Eg. if you have the ace of
	 * clubs you can't get rid of it by ducking. So you'll keep it. When someone
	 * leads clubs a few times, you'll have to play it and get the risk to get
	 * the queen in that trick.
	 * 
	 * Avoiding the queen
	 * 
	 * The best way not to get the queen is to own it. If you have a lot of
	 * spades with the queen than you can avoid the queen fishing. When everyone
	 * is out of spades and you still have the queen, they will lead another
	 * suit.Try to get rid of all suits and then play the queen. If you don't
	 * have it, try to throw away ace and king of spades as quickly as possible.
	 * These cards win the queen easily. Also try to duck clubs and diamonds
	 * especially when a few rounds of them have been played. The person of the
	 * queen will try to get rid of clubs and diamonds and then throw it on
	 * those suits, so it is a risk. If you have high cards, take early tricks
	 * with them to get rid of them. Chances of someone dumping the queen early
	 * are small.
	 * 
	 * Getting rid of the queen
	 * 
	 * A bad thing happens when you were dealt few spades (0-3) and then someone
	 * passes the queen to you. You'll be the victim of fishing attempts. You
	 * can only hope to get rid of other suits so that you can dump the queen
	 * when someone leads such a suit. Best way to do this is not hoping other
	 * people will lead other suits than clubs. They simply won't. So take a
	 * trick and lead those suits yourself, always leading highest cards first.
	 * This way you keep taking tricks and keep leading non-spades until all are
	 * gone out of your hand. When that is done, hope for someone to lead a
	 * non-spade and dump the queen.
	 * 
	 * 
	 * Getting less hearts
	 * 
	 * If you have low hearts you are unlikely to take a trick that was lead by
	 * hearts. These leads are the next most dangerous to the queen. Because
	 * they are most of the time worth 4 points. Try to have a low heart (2, 3
	 * or 4) then a middle one (6, 7 or 8) and a high one. With these cards you
	 * can duck most heart leads. If you throw away hearts throw your highest
	 * ones. don't throw a high heart when they haven't been broken yet if you
	 * don't have low ones. Because hearts will be lead directly afterward and
	 * you'll most likely take it. If this is the situation, better do it during
	 * fishing.
	 * 
	 * Safe hands
	 * 
	 * A safe hand is one with very low cards, and no queen of spades. With this
	 * hand you can duck all the time getting no points. Be You can also have a
	 * safe hand even if you have a few aces or so, as long as you can dump
	 * those early. Beware though of having safe hands : It's likely that
	 * another player has all the high cards and will shoot. You can't stop him
	 * then.
	 * 
	 * Bad hands
	 * 
	 * You have the queen and few spades. Or you have very high cards (eg the
	 * joker,queen,king of diamonds will force you to take diamond tricks and
	 * the points in it). Or you have high hearts without low ones. Not much to
	 * explain why it is bad, you'll take many tricks and thus have higher risk
	 * to get the queen dumped on you.
	 * 
	 * Protection
	 * 
	 * Protection cards are low cards of a suit where you have high ones too.Eg
	 * if you have 5 or more hearts with 3 low ones you can duck all heart leads
	 * until no one has any anymore. So your high hearts aren't a risk for you
	 * because you are the only one with it. Best protection to have is spades
	 * when you have the queen.
	 * 
	 * Targeting hand
	 * 
	 * You have protection for spades and the queen. You also have little club
	 * sand diamonds. With this hand you can avoid fishing, slough your other
	 * suits and dump the queen easily. Consider waiting to dump it until your
	 * target (lowest player score) takes the trick.
	 * 
	 * Shooting hand
	 * 
	 * You have high hearts and other high ones. Cards that can make you take
	 * all tricks. Even with low cards you can have a shooting hand, you just
	 * need to get rid of them early. Other hands are one's with almost all
	 * cards of the same suit. You can keep leading it and taking the points in
	 * those.
	 * 
	 * Targeting
	 * 
	 * Try whenever possible, especially with safe and targeting hands, to give
	 * your hearts and queen to the player with the lowest points, also called
	 * the low-man. You'll be surprised when you a are winning how much points
	 * come to you and not to anybody else. Make sure not to give points to
	 * losing players, because when they are over the limit, you lose too
	 * (Winning is the only thing that counts). However when you are winning,
	 * you can:
	 * 
	 * Target the player closest to you Target the loser when he is near the
	 * limit.
	 * 
	 * Targeting is always a risk. You might end up with all high cards because
	 * you wanted to target but never were able to. Sometimes you end up with
	 * the queen, perhaps leading it in the last trick... When someone is losing
	 * and a trick might end the game you might consider taking it yourself to
	 * prevent an early end to the game.
	 * 
	 * Shooting
	 * 
	 * This is by far the coolest thing you can do. You need to get all
	 * points,but not all tricks. If you are dealt bad cards you might consider
	 * to shoot,and pass good cards and hope to get more bad ones. If you have a
	 * lot of hearts then beware to have the ace of hearts. Most shooting
	 * attempts fail there. Smart people hang on to their ace to prevent
	 * shooting. Try to get rid of your low cards early in the round, not taking
	 * any tricks. When a heart or the queen falls make sure you can take it.
	 * Best shooting is to stay low till you have all highest cards, and hoping
	 * people dump theirs early. You might lead a low card to get people to dump
	 * their high cards so that they can't stop you. Never come out your high
	 * cards too early, or your attempt will get quickly noticed. When they know
	 * they will hold their high cards and you will fail. Make it look like an
	 * accident when you get hearts. Also don't take a queen trick with a king
	 * or leading lower spades after that. It's likely that you get stuck with a
	 * mid-level hearts card. Make sure you get rid of it early when your
	 * shooting isn't noticed yet, by leading it. They will duck it.
	 * Professional Shooting requires certainly that you count all cards that
	 * are played. You need to know if someone else has still a higher card then
	 * you, so that you know it is safe to play it. There are two different
	 * shooting hands:
	 * 
	 * High cards of several suits Many (7+) cards of 1 suit The first one is
	 * easiest. Just get rid of the low ones until all higher cards of the
	 * others are out. Requires some luck though, you might want the ace of
	 * clubs to fall but another player might be too scared to do it (in order
	 * not to get the queen) and mess up your attempt. Having almost all cards
	 * of a suit is tricky. You need to get rid of other suits, and when that is
	 * done, immediately take the lead. If you can't take the lead you're in
	 * trouble. This is especially the case with 7+ hearts cards. You might have
	 * 10 hearts cards in your hand left, but when someone else leads diamonds
	 * you'll have to give him a heart. Stopping shoots
	 * 
	 * A good way to make sure people can't shoot is:
	 * 
	 * Pass a low heart When possible dump a heart on someone else instead of
	 * another bad card. Keep a high card (especially an ace) People are often
	 * shooting when you see them playing all low cards on high tricks. When you
	 * see them leading high cards when that suit has been played before. Also
	 * when they play high hearts (never smart). If you see that get rid of high
	 * and mid-level cards except one. Don't keep them all or you'll end up
	 * being the victim. Preferably, keep the ace of hearts when you have enough
	 * protection there.
	 */

	/**
	 * Hearts Strategy - Passing strategy
	 * 
	 * With considerable latitude in play, Hearts allows players to create and
	 * improvise strategies that can make the game exciting and fun. Obviously,
	 * it is of prime importance to avoid taking tricks, but it is often a
	 * better strategy to plan on taking just a few at every hand. This keeps
	 * opponents from running and delivering 26 tricks to your scoresheet.
	 * 
	 * What to pass from your hand during the passing phase is the first place
	 * that strategy comes into play. First, it is nearly always a mistake to
	 * pass low spades (jack or under). A low spade is usually the safest lead
	 * for any player except the one who holds the queen, thus they are handy
	 * cards to have. Also, you might be passed the queen of spades and it is
	 * always good to have this card backed up by as many other cards as
	 * possible to prevent other players from playing low spades until you have
	 * to play the queen of spades on yourself.
	 * 
	 * If you are dealt the queen of spades the decision to pass it can be
	 * excruciating. It is almost always wise to hold it if you have at least
	 * three cards behind it. It should probably be passed with two or less
	 * spades behind it. The great thing about holding it is that you know where
	 * it is and when it can be played and you can sometimes orchestrate play so
	 * that it is played on your leading opponent.
	 * 
	 * If you absolutely must pass the queen of spades, don't forget where you
	 * have passed it. You may be passed the ace or king of spades and must know
	 * when it is safe to play (behind the player with the queen once he has
	 * played a different spade).
	 * 
	 * It should be an object in passing to void yourself in either diamonds or
	 * clubs. To be void in one of these suits helps to slough hearts onto your
	 * opponents and also relieve yourself of high cards in other suits. But
	 * this is merely a broad objective. Since the player passing to you will
	 * have the same objective in mind, you are quite likely to get cards
	 * similar or worse than to those you are passing. However, if you are
	 * lucky, you will acquire an unbalanced hand with many of one suit and few
	 * or none of another.
	 * 
	 * Should you pass hearts? No, and then again yes. Generally, it is best to
	 * keep your hearts for sloughing on your opponents. It is better to be void
	 * in another suit. Yet ... there is one good reason to pass a middling
	 * heart to your opponent. If he is considering running, this will nearly
	 * always provide a "poison pill". For during play, if you hold a high heart
	 * and your opponent must play the middling heart, you hold his stopper. If
	 * you take four tricks as a result, it is a small price to pay to prevent a
	 * run.
	 * 
	 * Now, if you are dealt a hand full of high cards, with no hearts or the
	 * ace, king, queen of hearts you can throw all of the above advice out the
	 * window. You may be in a position to "run". In this case, you want to hord
	 * your high cards and pass your low stuff. If you have any low hearts, pass
	 * them (unless you have so many high hearts as to make them safe) so they
	 * will not act as a "poison pill". Remember, to run, you do not need all
	 * the tricks, merely all the tricks.
	 * 
	 * A good pass is half the battle in hearts. You can also read passes from
	 * other players as unintended signals. A player passing ace, king or queen
	 * of spades is likely short of this suit. A player passing low hearts
	 * (especially two or more) is contemplating a run. A player passing a low
	 * spade is probably a novice. A pass of low cards in general means possible
	 * run.
	 * 
	 * Passing is somewhat of an art and somewhat of a science, but, ultimately,
	 * hearts is just a game and it is well not to take it too seriously.
	 * 
	 * Next Page
	 */

	/**
	 * Hearts Play Strategies:
	 * 
	 * <ul>
	 * <li>Zero trick. The aim of this strategy is not to take any trick. It is
	 * a good strategy if the following conditions on initial cards and passed
	 * cards applies:</li>
	 * <li>Some trick. The aim of this strategy is to make just a few tricks in
	 * order to avoid a player from running</li>
	 * <li>Running. Ths aim of this strategy is to make all the tricks. It is a
	 * good strategy is initial cards all high</li>
	 * </ul>
	 * 
	 * Knowing what to play on the first trick is easy. If you have the two of
	 * clubs, you must lead it. As you cannot slough any tricks on the first
	 * trick, use this opportunity to get rid of a high club, if you are void in
	 * clubs, slough another high card, perhaps a king or ace of spades - two
	 * cards that could lead to trouble later if they are not sufficiently
	 * backed up.
	 * 
	 * Many players go right into leading spades. This is done in order to force
	 * the queen out, and it is considered the safest lead possible. The worst
	 * that can happen is the trick could draw a heart or two. This is something
	 * that the holder of a shallow queen of spades will dread. Her best
	 * strategy is then to seize the lead, if possible, and divert her opponents
	 * by leading back diamonds or clubs. An alternative strategy is to come
	 * back with high diamonds or clubs in order to draw a heart. Once the heart
	 * is broken she can dish out a low heart which can cause a heart war that
	 * might create a distraction from her precarious spade position, and for a
	 * shallow queen of spades, any distraction is a good distraction.
	 * 
	 * This heart war may occur because of a common tactic when playing the
	 * heart suit. When a player must take a trick, he should do so with as high
	 * a card as possible. This leaves him with the opportunity to dish out a
	 * low card in the same suit, getting rid of the lead and forcing another
	 * player to take several tricks. This can create a vicious cycle,
	 * especially where hearts are involved.
	 * 
	 * Of course, playing low and avoiding the queen are paramount, but this
	 * does not mean that a player should avoid tricks altogether. Taking a few
	 * tricks in every hand is generally a good idea because it prevents any
	 * other player from running. The best way to stop other players from
	 * running is to save a high heart and not slough it until at least two
	 * people have tricks. Holding back a stopper to prevent a run is only good
	 * sense, especially if you are sure your opponents have a middling heart
	 * losers (which you may well have passed at least one of them).
	 * 
	 * In games with sharp players, a successful run is infrequent. This is
	 * because a table of sharp players will generally pass a middling heart and
	 * orient their own hand in such a way as to prevent the run by their
	 * opponents while minimizing their own exposure to tricks, especially the
	 * queen of spades. But there will be times when the cards align to almost
	 * make a run seem inevitable. The best configuration for a run will be when
	 * no heart losers are held or only the very highest hearts in sequence.
	 * Ace, king and queen of hearts is nice, but trying to run with the king,
	 * queen, jack is a dangerous maneuver.
	 * 
	 * If such a run were to be attempted, the running player should try to draw
	 * the ace before making his intention to run perfectly obvious. Otherwise,
	 * the ace will be a sure stopper. If a player has a mere middling heart
	 * loser, another strategy is to lead high in clubs or diamonds until a
	 * heart has been drawn and then lead back the middling heart. If the
	 * opponents are not sure of his intentions they might let the trick slide
	 * and put the prospective runner in a good position to run.
	 * 
	 * Hearts is as much a game of personalities and bluffing as is poker. It
	 * requires skill and intuition to determine how opponents will react to
	 * certain styles of play. For example, a player who tries and fails to run
	 * frequently will be watched so closely in subsequent play that his
	 * prospects of any deceptive plays become impossible. Also, inexperienced
	 * players tend to shy away from running, so they usually need not be played
	 * closely. This means that when passing to such a player, you do not have
	 * to worry so much about making certain they have a middling heart or that
	 * by passing them three low hearts you are sending a clear signal of your
	 * intention to run.
	 * 
	 * There are so many possibilities of different hands that it is impossible
	 * to make provision or comment on every circumstance. The key to remember
	 * is that Hearts is only a game. Play it well, but play it to have fun.
	 * </ul>
	 * 
	 */

}
