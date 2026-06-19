package sn.iage.isi.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import sn.iage.isi.entities.Category;

import java.util.List;

public class CategoryRepository {
    EntityManager em = JpaUtil.getEntityManager();

    public Category create(Category category) {
        EntityTransaction tx = em.getTransaction();
        Category c = Category.builder()
                .name(category.getName())
                .state(Boolean.TRUE)
                .build();
        c.setUserCreated("admin");
        c.setUserUpdated("admin");
        try {
            tx.begin();      //Debut de transaction
            em.persist(c);
            tx.commit();     //Validation de transaction
        }catch(Exception e) {
            tx.rollback();   //Annulation de la transaction
        }
        return c;
    }

    public List<Category> getAll() {
//        return em
//                .createQuery("SELECT c FROM Category c ORDER BY c.name ASC", Category.class)
//                .getResultList();
        return em.createNamedQuery("Category.findAll", Category.class).getResultList();
    }
}
