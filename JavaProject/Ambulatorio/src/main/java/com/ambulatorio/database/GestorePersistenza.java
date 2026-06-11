package com.ambulatorio.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;

public class GestorePersistenza {

    public boolean salva(Object oggetto) {
        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(oggetto);

            em.getTransaction().commit();

            return true;

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            // L'EntityManager è chiusa alla fine della transazione, la EntityManagerFactory rimane aperta
            em.close();
        }
    }

    public boolean salvaTutti(Object... oggetti) {
        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            for (Object oggetto : oggetti) {
                em.persist(oggetto);
            }

            em.getTransaction().commit();
            return true;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

    public <T> T trovaPerId(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            return em.find(classe, id);

        } finally {
            em.close();
        }
    }

    /*
     * Cerca tutti gli oggetti persistenti di una certa classe
     * per cui un campo ha un determinato valore.
     */
    public <T> List<T> cercaPerCampo(Class<T> classe,
                                     String nomeCampo,
                                     Object valore) {

        return cercaPerCampi(
                classe,
                Map.of(nomeCampo, valore)
        );
    }

    /*
     * Cerca tutti gli oggetti persistenti che soddisfano un insieme di condizioni.
     *
     * La query JPQL viene costruita nel livello database.
     */
    public <T> List<T> cercaPerCampi(Class<T> classe,
                                     Map<String, Object> campi) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder();

            jpql.append("SELECT e FROM ")
                    .append(classe.getSimpleName())
                    .append(" e");

            if (!campi.isEmpty()) {
                jpql.append(" WHERE ");

                int contatore = 0;

                for (String nomeCampo : campi.keySet()) {
                    if (contatore > 0) {
                        jpql.append(" AND ");
                    }

                    String nomeParametro = nomeCampo.replace(".", "_");

                    jpql.append("e.")
                            .append(nomeCampo)
                            .append(" = :")
                            .append(nomeParametro);

                    contatore++;
                }
            }

            TypedQuery<T> query = em.createQuery(
                    jpql.toString(),
                    classe
            );

            for (String nomeCampo : campi.keySet()) {
                String nomeParametro = nomeCampo.replace(".", "_");
                query.setParameter(nomeParametro, campi.get(nomeCampo));
            }

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    /*
     * Cerca il primo oggetto persistente che soddisfa un insieme di condizioni.
     *
     * Se non trova nessun risultato, restituisce null.
     */
    public <T> T cercaPrimoPerCampi(Class<T> classe,
                                    Map<String, Object> campi) {

        List<T> risultati = cercaPerCampi(classe, campi);

        if (risultati.isEmpty()) {
            return null;
        }

        return risultati.get(0);
    }


    public <T> T aggiorna(T oggetto) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            T oggettoAggiornato = em.merge(oggetto);

            em.getTransaction().commit();

            return oggettoAggiornato;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public <T> boolean elimina(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            T oggetto = em.find(classe, id);

            //se l'oggetto esiste, lo eliminiamo
            if (oggetto != null) {
                em.remove(oggetto);
                em.getTransaction().commit();
                return true;
            }

            em.getTransaction().commit();
            return false;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

}