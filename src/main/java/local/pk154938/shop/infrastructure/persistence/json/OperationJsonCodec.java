package local.pk154938.shop.infrastructure.persistence.json;

import local.pk154938.shop.domain.trade.Delivery;
import local.pk154938.shop.domain.trade.DeliveryStatus;
import local.pk154938.shop.domain.trade.ItemOperation;
import local.pk154938.shop.domain.trade.OperationLine;
import local.pk154938.shop.domain.trade.Order;
import local.pk154938.shop.domain.trade.OrderStatus;
import local.pk154938.shop.domain.trade.Product;
import local.pk154938.shop.domain.trade.Return;
import local.pk154938.shop.domain.trade.Sale;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps trade operations to/from {@link JSONObject}. Stateless utility — all
 * methods are static.
 *
 * <p>JSON shape per operation (common envelope):
 * <pre>
 * {
 *   "id": "&lt;uuid&gt;",
 *   "timestamp": "&lt;ISO-8601 instant&gt;",
 *   "lines": [
 *     {
 *       "productName": "...",
 *       "priceNetPurchase": "10.50",
 *       "priceNetSale": "15.00",
 *       "vatRate": "0.23",
 *       "quantity": 5
 *     }
 *   ]
 * }
 * </pre>
 * Type-specific fields (supplierName, status, orderId) are added on top.
 */
public final class OperationJsonCodec {

    private OperationJsonCodec() {}

    // ---------- write ----------

    public static JSONObject toJson(ItemOperation op) {
        if (op instanceof Order) return toJson((Order) op);
        if (op instanceof Delivery) return toJson((Delivery) op);
        if (op instanceof Sale) return toJson((Sale) op);
        if (op instanceof Return) return toJson((Return) op);
        throw new IllegalArgumentException("Nieznany typ operacji: " + op.getClass().getSimpleName());
    }

    public static JSONObject toJson(Order order) {
        JSONObject json = baseEnvelope(order);
        json.put("supplierName", order.getSupplierName());
        json.put("status", order.getStatus().name());
        return json;
    }

    public static JSONObject toJson(Delivery delivery) {
        JSONObject json = baseEnvelope(delivery);
        json.put("orderId", delivery.getOrderId().toString());
        json.put("status", delivery.getStatus().name());
        return json;
    }

    public static JSONObject toJson(Sale sale) {
        return baseEnvelope(sale);
    }

    public static JSONObject toJson(Return ret) {
        JSONObject json = baseEnvelope(ret);
        json.put("orderId", ret.getOrderId().toString());
        return json;
    }

    private static JSONObject baseEnvelope(ItemOperation op) {
        JSONObject json = new JSONObject();
        json.put("id", op.getId().toString());
        json.put("timestamp", op.getTimestamp().toString());
        JSONArray lines = new JSONArray();
        for (OperationLine line : op.getLines()) {
            lines.put(lineToJson(line));
        }
        json.put("lines", lines);
        return json;
    }

    private static JSONObject lineToJson(OperationLine line) {
        Product p = line.getProduct();
        JSONObject json = new JSONObject();
        json.put("productName", p.getName());
        json.put("priceNetPurchase", p.getPriceNetPurchase().toPlainString());
        json.put("priceNetSale", p.getPriceNetSale().toPlainString());
        json.put("vatRate", p.getVatRate().toPlainString());
        json.put("quantity", line.getQuantity());
        return json;
    }

    // ---------- read ----------

    public static Order orderFromJson(JSONObject json) {
        return new Order(
                readId(json),
                readTimestamp(json),
                readLines(json),
                json.getString("supplierName"),
                OrderStatus.valueOf(json.getString("status"))
        );
    }

    public static Delivery deliveryFromJson(JSONObject json) {
        return new Delivery(
                readId(json),
                readTimestamp(json),
                readLines(json),
                UUID.fromString(json.getString("orderId")),
                DeliveryStatus.valueOf(json.getString("status"))
        );
    }

    public static Sale saleFromJson(JSONObject json) {
        return new Sale(readId(json), readTimestamp(json), readLines(json));
    }

    public static Return returnFromJson(JSONObject json) {
        return new Return(
                readId(json),
                readTimestamp(json),
                readLines(json),
                UUID.fromString(json.getString("orderId"))
        );
    }

    private static UUID readId(JSONObject json) {
        return UUID.fromString(json.getString("id"));
    }

    private static Instant readTimestamp(JSONObject json) {
        return Instant.parse(json.getString("timestamp"));
    }

    private static List<OperationLine> readLines(JSONObject json) {
        JSONArray arr = json.getJSONArray("lines");
        List<OperationLine> lines = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            lines.add(lineFromJson(arr.getJSONObject(i)));
        }
        return lines;
    }

    private static OperationLine lineFromJson(JSONObject json) {
        Product product = new Product(
                json.getString("productName"),
                new BigDecimal(json.getString("priceNetPurchase")),
                new BigDecimal(json.getString("priceNetSale")),
                new BigDecimal(json.getString("vatRate"))
        );
        return new OperationLine(product, json.getInt("quantity"));
    }
}
