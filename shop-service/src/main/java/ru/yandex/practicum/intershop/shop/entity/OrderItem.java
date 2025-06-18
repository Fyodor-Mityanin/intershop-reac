package ru.yandex.practicum.intershop.shop.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    @Id
    private Long id;
    @Column("order_id")
    private Long orderId;
    @Column("item_id")
    private Long itemId;
    @Column("quantity")
    private Integer quantity;

    public OrderItem(Item item, Order order) {
        this.itemId = item.getId();
        this.orderId = order.getId();
        this.quantity = 0;
    }
}