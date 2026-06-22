package sn.iage.isi.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import sn.iage.isi.entities.Book;

import java.util.List;
import java.util.Random;

public class BookRepository {

    EntityManager em = JpaUtil.getEntityManager();

    // Ajouter un livre
    public Book createBook(Book book) {
        EntityTransaction tx = em.getTransaction();

        Book b = Book.builder()
                .isbn(generateIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(book.getPublicationYear())
                .countPages(book.getCountPages())
                .category(book.getCategory())
                .build();

        b.setUserCreated("admin");
        b.setUserUpdated("admin");

        try {
            tx.begin();
            em.persist(b);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        }

        return b;
    }

    // Lister tous les livres
    public List<Book> listAllBooks() {
        return em.createQuery(
                "SELECT b FROM Book b ORDER BY b.title ASC",
                Book.class
        ).getResultList();
    }

    // Rechercher par ID
    public Book findBookById(int id) {
        Book book = em.find(Book.class, id);

        if (book == null) {
            throw new EntityNotFoundException("Book not found");
        }

        return book;
    }

    // Rechercher par ISBN
    public Book findBookByIsbn(String isbn) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.isbn = :isbn",
                        Book.class)
                .setParameter("isbn", isbn)
                .getSingleResult();
    }

    // Modifier un livre
    public Book updateBook(int id, Book newBook) {

        EntityTransaction tx = em.getTransaction();

        Book book = findBookById(id);

        if (book != null) {

            book.setTitle(newBook.getTitle());
            book.setAuthor(newBook.getAuthor());
            book.setPublicationYear(newBook.getPublicationYear());
            book.setCountPages(newBook.getCountPages());
            book.setCategory(newBook.getCategory());

            book.setUserUpdated("admin");

            try {
                tx.begin();
                em.merge(book);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            }
        }

        return book;
    }

    // Supprimer un livre
    public void deleteBook(int id) {

        EntityTransaction tx = em.getTransaction();

        Book book = findBookById(id);

        try {
            tx.begin();
            em.remove(book);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        }
    }

    // Liste des livres par catégorie
    public List<Book> listeBooksByCategory(String categoryName) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.category.name) = :cat ORDER BY b.title",
                        Book.class)
                .setParameter("cat", categoryName.toLowerCase())
                .getResultList();
    }

    // Recherche par titre
    public List<Book> searchBooksByTitle(String keyword) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.title) LIKE :kw ORDER BY b.title",
                        Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    // Recherche par auteur
    public List<Book> searchBooksByAuthor(String keyword) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.author) LIKE :kw ORDER BY b.author",
                        Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    // Livres publiés après une année donnée
    public List<Book> searchBooksAfterYear(int year) {
        return em.createQuery(
                        "SELECT b FROM Book b WHERE b.publicationYear > :year ORDER BY b.publicationYear",
                        Book.class)
                .setParameter("year", year)
                .getResultList();
    }

    // Nombre de livres par catégorie
    public List<Object[]> countBooksByCategory() {
        return em.createQuery(
                        "SELECT b.category.name, COUNT(b) " +
                                "FROM Book b GROUP BY b.category.name",
                        Object[].class)
                .getResultList();
    }

    // Nombre total de livres
    public Long countAllBooks() {
        return em.createQuery(
                        "SELECT COUNT(b) FROM Book b",
                        Long.class)
                .getSingleResult();
    }

    // ==========================
    // Génération ISBN
    // ==========================

    private String generateIsbn() {

        String[] prefixes = {"978", "979"};
        Random random = new Random();

        String prefix = prefixes[random.nextInt(2)];
        String group = String.valueOf(random.nextInt(2));
        String publisher = String.format("%04d", random.nextInt(10000));
        String title = String.format("%04d", random.nextInt(10000));

        String base = prefix + group + publisher + title;

        int checkDigit = computeIsbn13CheckDigit(base);

        return String.format("%s-%s-%s-%s-%d",
                prefix,
                group,
                publisher,
                title,
                checkDigit);
    }

    private int computeIsbn13CheckDigit(String base12) {

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base12.charAt(i));

            sum += (i % 2 == 0)
                    ? digit
                    : digit * 3;
        }

        int remainder = sum % 10;

        return remainder == 0
                ? 0
                : 10 - remainder;
    }
}