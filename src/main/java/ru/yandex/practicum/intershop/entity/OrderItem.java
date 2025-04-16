package ru.yandex.practicum.intershop.entity;


import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @MapsId("itemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ColumnDefault("0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public OrderItem(Item item, Order order, int quantity) {
        this.item = item;
        this.order = order;
        this.quantity = quantity;
        this.id = new OrderItemId(item.getId(), order.getId());
    }
}