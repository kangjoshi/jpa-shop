package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> orders = findOrders();

        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return orders;
    }

    public List<OrderQueryDto> findOrderQueryDtosOptimization() {
        List<OrderQueryDto> orders = findOrders();

        List<Long> orderIds = toOrderId(orders);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return orders;
    }

    // 쿼리를 줄이기 위해 in 문을 이용, orderId를 이용하여 orderItemMap에 담김 주문의 주문아이템을 찾아온다.
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private List<Long> toOrderId(List<OrderQueryDto> orders) {
        List<Long> orderIds = orders.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderFlatDto> findOrderQueryDtosFlat() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d" +
                " join o.orderItems oi" +
                " join oi.item i", OrderFlatDto.class)
                .getResultList();

    }
}
