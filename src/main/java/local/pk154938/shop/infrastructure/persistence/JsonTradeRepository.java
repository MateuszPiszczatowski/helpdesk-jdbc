package local.pk154938.shop.infrastructure.persistence;

import local.pk154938.shop.application.repository.TradeRepository;
import local.pk154938.shop.domain.trade.Delivery;
import local.pk154938.shop.domain.trade.ItemOperation;
import local.pk154938.shop.domain.trade.Order;
import local.pk154938.shop.domain.trade.Return;
import local.pk154938.shop.domain.trade.Sale;
import local.pk154938.shop.infrastructure.persistence.json.OperationJsonCodec;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores trade operations as one JSON file per operation, grouped into
 * subfolders by type ({@code orders}, {@code deliveries}, {@code sales},
 * {@code returns}) under a configurable root directory.
 *
 * <p>Filename pattern: {@code <epoch-millis>_<uuid-first-8>.json}. The epoch
 * prefix gives lexicographic = chronological ordering at the filesystem
 * level; the 8-char UUID suffix prevents collisions for operations created
 * at the same millisecond. Update operations re-verify the full UUID
 * against the file's content to be safe against rare prefix collisions.
 */
public class JsonTradeRepository implements TradeRepository {
    private final Path ordersDir;
    private final Path deliveriesDir;
    private final Path salesDir;
    private final Path returnsDir;

    public JsonTradeRepository(Path operationsRoot) {
        this.ordersDir = operationsRoot.resolve("orders");
        this.deliveriesDir = operationsRoot.resolve("deliveries");
        this.salesDir = operationsRoot.resolve("sales");
        this.returnsDir = operationsRoot.resolve("returns");
        ensureDirs();
    }

    private void ensureDirs() {
        try {
            Files.createDirectories(ordersDir);
            Files.createDirectories(deliveriesDir);
            Files.createDirectories(salesDir);
            Files.createDirectories(returnsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Nie udało się utworzyć folderów operacji: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(ItemOperation operation) {
        Path dir = dirFor(operation);
        writeJson(dir.resolve(filenameFor(operation)), OperationJsonCodec.toJson(operation));
    }

    @Override
    public void update(ItemOperation operation) {
        Path dir = dirFor(operation);
        Path target = locateFile(dir, operation.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Nie znaleziono pliku operacji o ID: " + operation.getId()));
        writeJson(target, OperationJsonCodec.toJson(operation));
    }

    @Override
    public List<Order> findOrders() {
        return loadAll(ordersDir, OperationJsonCodec::orderFromJson);
    }

    @Override
    public List<Delivery> findDeliveries() {
        return loadAll(deliveriesDir, OperationJsonCodec::deliveryFromJson);
    }

    @Override
    public List<Sale> findSales() {
        return loadAll(salesDir, OperationJsonCodec::saleFromJson);
    }

    @Override
    public List<Return> findReturns() {
        return loadAll(returnsDir, OperationJsonCodec::returnFromJson);
    }

    @Override
    public Optional<Order> findOrderById(UUID id) {
        return loadById(ordersDir, id, OperationJsonCodec::orderFromJson);
    }

    @Override
    public Optional<Delivery> findDeliveryById(UUID id) {
        return loadById(deliveriesDir, id, OperationJsonCodec::deliveryFromJson);
    }

    @Override
    public Optional<Sale> findSaleById(UUID id) {
        return loadById(salesDir, id, OperationJsonCodec::saleFromJson);
    }

    @Override
    public Optional<Return> findReturnById(UUID id) {
        return loadById(returnsDir, id, OperationJsonCodec::returnFromJson);
    }

    @Override
    public List<ItemOperation> findAll() {
        List<ItemOperation> all = new ArrayList<>();
        all.addAll(findOrders());
        all.addAll(findDeliveries());
        all.addAll(findSales());
        all.addAll(findReturns());
        all.sort(Comparator.comparing(ItemOperation::getTimestamp).reversed());
        return all;
    }

    // ---------- helpers ----------

    private Path dirFor(ItemOperation op) {
        if (op instanceof Order) return ordersDir;
        if (op instanceof Delivery) return deliveriesDir;
        if (op instanceof Sale) return salesDir;
        if (op instanceof Return) return returnsDir;
        throw new IllegalArgumentException("Nieznany typ operacji: " + op.getClass().getSimpleName());
    }

    private static String filenameFor(ItemOperation op) {
        long epoch = op.getTimestamp().toEpochMilli();
        String uuid8 = op.getId().toString().substring(0, 8);
        return epoch + "_" + uuid8 + ".json";
    }

    private static <T extends ItemOperation> List<T> loadAll(Path dir, Function<JSONObject, T> reader) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(p -> reader.apply(readJson(p)))
                    .sorted(Comparator.comparing(ItemOperation::getTimestamp).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Błąd odczytu folderu: " + dir + " — " + e.getMessage(), e);
        }
    }

    private static <T extends ItemOperation> Optional<T> loadById(
            Path dir, UUID id, Function<JSONObject, T> reader) {
        return locateFile(dir, id).map(p -> reader.apply(readJson(p)));
    }

    private static Optional<Path> locateFile(Path dir, UUID id) {
        String suffix = "_" + id.toString().substring(0, 8) + ".json";
        String fullId = id.toString();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    // Disambiguate rare prefix collisions by checking full UUID in file content
                    .filter(p -> readJson(p).getString("id").equals(fullId))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Błąd przeszukiwania folderu: " + dir + " — " + e.getMessage(), e);
        }
    }

    private static JSONObject readJson(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            return new JSONObject(content);
        } catch (IOException e) {
            throw new IllegalStateException("Błąd odczytu pliku JSON: " + file + " — " + e.getMessage(), e);
        }
    }

    private static void writeJson(Path file, JSONObject json) {
        try {
            Files.writeString(file, json.toString(2), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Błąd zapisu pliku JSON: " + file + " — " + e.getMessage(), e);
        }
    }
}
