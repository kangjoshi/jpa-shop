package jpabook.jpashop.repository;

import jpabook.jpashop.api.OrderSimpleQueryDto;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);

        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate predicate = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            predicates.add(predicate);
        }

        if (orderSearch.getMemberName() != null) {
            Predicate predicate = cb.equal(o.get("name"), orderSearch.getMemberName());
            predicates.add(predicate);
        }

        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDeilvery(OrderSearch orderSearch) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
                )
                .getResultList();
    }

    // new 키워드를 사용하여 가져옴
    // 원하는 것만 가져올 수 있지만, API 코드가 레포짓토리가 확장성이 적어짐
    // 정말 최적화가 필요한 레포짓토리가 필요하다면 API 전용 레포짓토리를 생성하는것도 방법 - 그냥 같은 레포짓토리에 만들고 네이밍만 잘 지어도 상관은 없을듯
    public List<OrderSimpleQueryDto> findAllDtos(OrderSearch orderSearch) {
        return em.createQuery(
                "select new jpabook.jpashop.api.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class
        )
                .getResultList();
    }
}
