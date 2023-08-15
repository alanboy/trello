import org.trello4j.model.Card;
import org.trello4j.model.List;
import org.trello4j.model.Board;

public class TrelloCardsWithInfo {
    private Board board;
    private List list;
    private java.util.List<Card> cards;

    public TrelloCardsWithInfo(Board board, List list , java.util.List<Card> cards) {
        this.list = list;
        this.board = board;
        this.cards = cards;
    }

    public Board getBoard() {
        return board;
    }

    public List getList() {
        return list;
    }

    public java.util.List<Card> getCards() {
        return cards;
    }
}
