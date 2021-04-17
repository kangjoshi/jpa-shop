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

    // distinct
    // 쿼리에 distinct 키워드가 추가되고
    // JPA가 동일한 객체 (PK가 동일하다면 == 동일한 객체라면) 중복을 제거한다.
    public List<Order> findAllWithItem(OrderSearch orderSearch) {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class
        )
                // 컬렉션 페치 조인을 사용하면 페이징이 불가능하다.
                // 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어와 메모리에서 페이징 처리를 한다. (outOfMemory 에러 발생이 가능하므로 위험)
                //.setFirstResult(1)
                //.setMaxResults(100)
                .getResultList();

    }

    public List<Order> findAllWithMemberDeilvery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
