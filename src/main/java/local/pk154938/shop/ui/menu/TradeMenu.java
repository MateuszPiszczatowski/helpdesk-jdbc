package local.pk154938.shop.ui.menu;

import local.pk154938.shop.application.auth.AuthorizationService;
import local.pk154938.shop.application.auth.Operation;
import local.pk154938.shop.application.repository.ProductRepository;
import local.pk154938.shop.application.repository.StockRepository;
import local.pk154938.shop.application.repository.TradeRepository;
import local.pk154938.shop.application.service.TradeService;
import local.pk154938.shop.application.session.Session;

public class TradeMenu extends BaseMenu {
    protected final TradeService tradeService;
    protected final ProductRepository productRepository;
    protected final StockRepository stockRepository;
    protected final TradeRepository tradeRepository;

    public TradeMenu(TradeService tradeService,
                     ProductRepository productRepository,
                     StockRepository stockRepository,
                     TradeRepository tradeRepository,
                     Session session,
                     AuthorizationService authorizationService) {
        super("OPERACJE HANDLOWE", session, authorizationService);
        this.tradeService = tradeService;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.tradeRepository = tradeRepository;
    }

    @Override
    protected void addOptions() {
        addOption("Wyświetl stan magazynu", this::viewStock, Operation.VIEW_TRADE_INFO);
        addOption("Dokonaj sprzedaży", this::makeSale, Operation.MAKE_SALE);
        addOption("Dokonaj zwrotu", this::makeReturn, Operation.MAKE_RETURN);
        addOption("Zarejestruj dostawę", this::registerDelivery, Operation.REGISTER_DELIVERY);
        addOption("Złóż zamówienie u dostawcy", this::placeSupplierOrder, Operation.PLACE_SUPPLIER_ORDER);
    }

    private void viewStock() {
        System.out.println("[TODO] Wywołano: wyświetl stan magazynu.");
    }

    private void makeSale() {
        System.out.println("[TODO] Wywołano: dokonaj sprzedaży.");
    }

    private void makeReturn() {
        System.out.println("[TODO] Wywołano: dokonaj zwrotu.");
    }

    private void registerDelivery() {
        System.out.println("[TODO] Wywołano: zarejestruj dostawę.");
    }

    private void placeSupplierOrder() {
        System.out.println("[TODO] Wywołano: złóż zamówienie u dostawcy.");
    }
}
