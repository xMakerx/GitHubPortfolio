package online.coginvasion.spider.scene.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import online.coginvasion.spider.Globals;

public class Deck {
	
	final Random rand;
	private final ArrayList<Card> cards;
	
	public Deck(int numSuits) {
		this.cards = new ArrayList<Card>();
		this.rand = new Random();
		
		// This code will setup our deck.
		ArrayList<Suit> suits = new ArrayList<Suit>();
		
		for(Suit suit : Suit.values()) {
			suits.add(suit);
		}
		
		for(int i = 0; i < numSuits; i++) {
			int suitIndex = rand.nextInt(suits.size());
			Suit suit = suits.get(suitIndex);
			
			addCardsFromSuit(suit, ((Globals.DECK_SIZE / Globals.SUIT_SIZE) / numSuits));
			suits.remove(suitIndex);
		}
		
		Collections.shuffle(cards);
	}
	
	private void addCardsFromSuit(Suit suit, int amount) {
		for(int i = 0; i < amount; i++) {
			for(String cardName : Globals.CARD_NAMES) {
				Cards card = Cards.valueOf(cardName + "_OF_" + suit.name());
				cards.add(new Card(card));
			}
		}
	}
	
	/**
	 * Returns a random card from the deck.
	 * Removes the card from the deck.
	 * @return
	 */
	
	public Card takeCard() {
		Card card = null;

		if(cards.size() > 0) {
			card = cards.get(0);
			cards.remove(card);
		}

		return card;
	}
	
	public ArrayList<Card> getCards() {
		return this.cards;
	}

}
