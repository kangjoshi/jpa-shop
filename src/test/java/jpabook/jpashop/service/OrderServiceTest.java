package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    void 주문() {
        Member member = createMember();

        Item book = createBook();

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(),book.getId(), orderCount);

        Order ordered = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, ordered.getStatus());
        assertEquals(1, ordered.getOrderItems().size());
        assertEquals(10000 * orderCount, ordered.getTotalPrice());
        assertEquals(8, book.getStockQuantity());
    }

    private Item createBook() {
        Item book = new Book();
        book.setName("JPA");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "계남로", "123-456"));
        em.persist(member);
        return member;
    }

    @Test
    void 주문_재고수량초과() {
        Member member = createMember();
        Item book = createBook();

        int orderCount = 99;
        assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));
    }

    @Test
    void 취소() {
        Member member = createMember();
        Item book = createBook();

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(),book.getId(), orderCount);
        Order ordered = orderRepository.findOne(orderId);

        ordered.cancel();

        assertEquals(OrderStatus.CANCEL, ordered.getStatus());
        assertEquals(10, book.getStockQuantity());
    }
}